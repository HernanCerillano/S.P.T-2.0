package com.SPT.Services.impl;

import com.SPT.Dtos.Response.ClienteResponse;
import com.SPT.Dtos.Response.OTDetalleResponse;
import com.SPT.Dtos.Response.OrdenTrabajoResponse;
import com.SPT.Dtos.Response.PresupuestoDetalleResponse;
import com.SPT.Dtos.Response.PresupuestoResponse;
import com.SPT.Dtos.Response.VehiculoResponse;
import com.SPT.Model.TipoItemDetalle;
import com.SPT.Repository.PagoRepository;
import com.SPT.Services.ClienteService;
import com.SPT.Services.DocumentoPdfService;
import com.SPT.Services.OrdenTrabajoService;
import com.SPT.Services.PresupuestoService;
import com.SPT.Services.VehiculoService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DocumentoPdfServiceImpl implements DocumentoPdfService {

    // Paleta SPT — Suspensión Tito
    private static final Color BLUE_DARK = new Color(13, 27, 42);    // #0D1B2A
    private static final Color RED_ACCENT = new Color(164, 22, 26);  // #A4161A
    private static final Color GRAY_DATA = new Color(42, 42, 42);    // #2A2A2A
    private static final Color BORDER_SOFT = new Color(197, 197, 197); // #C5C5C5
    private static final Color ZEBRA_BG = new Color(244, 244, 244);  // #F4F4F4

    // Aliases para minimizar el diff (mismos nombres viejos, paleta nueva)
    private static final Color PURPLE = BLUE_DARK;       // fondo de cabeceras/secciones
    private static final Color PURPLE_LIGHT = ZEBRA_BG;  // fondo de labels (gris claro)
    private static final Color TEXT_DARK = GRAY_DATA;    // texto de labels

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, BLUE_DARK);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, TEXT_DARK);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font TOTAL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, TEXT_DARK);
    // Total de Presupuesto: rojo accent y grande. Total de OT/saldo: azul oscuro grande.
    private static final Font TOTAL_VALUE_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, RED_ACCENT);
    private static final Font TOTAL_VALUE_BLUE_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, BLUE_DARK);
    private static final Font EMPTY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(120, 120, 120));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        MONEY_FORMAT = new DecimalFormat("$ #,##0.00", symbols);
    }

    private final PresupuestoService presupuestoService;
    private final OrdenTrabajoService ordenTrabajoService;
    private final ClienteService clienteService;
    private final VehiculoService vehiculoService;
    private final PagoRepository pagoRepository;

    public DocumentoPdfServiceImpl(PresupuestoService presupuestoService,
                                   OrdenTrabajoService ordenTrabajoService,
                                   ClienteService clienteService,
                                   VehiculoService vehiculoService,
                                   PagoRepository pagoRepository) {
        this.presupuestoService = presupuestoService;
        this.ordenTrabajoService = ordenTrabajoService;
        this.clienteService = clienteService;
        this.vehiculoService = vehiculoService;
        this.pagoRepository = pagoRepository;
    }

    @Override
    public byte[] generarPdfPresupuesto(Long idPresupuesto) {
        PresupuestoResponse presupuesto = presupuestoService.obtenerPorId(idPresupuesto);
        List<PresupuestoDetalleResponse> detalles = presupuestoService.listarDetalles(idPresupuesto);
        ClienteResponse cliente = clienteService.obtenerPorId(presupuesto.getIdCliente());
        VehiculoResponse vehiculo = vehiculoService.obtenerPorId(presupuesto.getIdVehiculo());

        return crearPdf(document -> {
            agregarLogo(document);
            agregarTitulo(document, "Presupuesto");
            agregarTablaCabecera(
                    document,
                    cliente,
                    vehiculo,
                    presupuesto.getNumeroPresupuesto(),
                    "Presupuesto",
                    presupuesto.getFechaCreacion(),
                    texto(presupuesto.getEstado())
            );
            agregarBloqueDescripcion(document, "Descripcion del trabajo", presupuesto.getResumen());
            boolean ocultarP = presupuesto.isOcultarPreciosItems();
            BigDecimal precioPerP = presupuesto.getPrecioPersonalizado();
            BigDecimal totalMostradoP = precioPerP != null ? precioPerP : nvl(presupuesto.getTotal());
            List<FilaDetalle> filasP = convertirDetallesPresupuesto(detalles);
            agregarTablaDetalles(document, filasP, ocultarP);
            agregarTotalesPresupuesto(document, filasP, totalMostradoP, ocultarP, precioPerP != null);
            agregarFooter(document);
        });
    }

    @Override
    public byte[] generarPdfOrdenTrabajo(Long idOt) {
        OrdenTrabajoResponse orden = ordenTrabajoService.obtenerPorId(idOt);
        List<OTDetalleResponse> detalles = ordenTrabajoService.listarDetalles(idOt);
        ClienteResponse cliente = clienteService.obtenerPorId(orden.getIdCliente());
        VehiculoResponse vehiculo = vehiculoService.obtenerPorId(orden.getIdVehiculo());
        BigDecimal totalPagado = nvl(pagoRepository.sumMontoByOrdenTrabajo(idOt));

        return crearPdf(document -> {
            agregarLogo(document);
            agregarTitulo(document, "Orden de Trabajo");
            agregarTablaCabecera(
                    document,
                    cliente,
                    vehiculo,
                    orden.getNumeroOt(),
                    "Orden de Trabajo",
                    orden.getFechaCreacion(),
                    texto(orden.getEstado())
            );
            agregarBloqueDescripcion(document, "Descripcion del trabajo", orden.getResumenTrabajo());
            boolean ocultarOt = orden.isOcultarPreciosItems();
            BigDecimal precioPerOt = orden.getPrecioPersonalizado();
            BigDecimal totalMostradoOt = precioPerOt != null ? precioPerOt : nvl(orden.getTotal());
            List<FilaDetalle> filasOt = convertirDetallesOt(detalles);
            agregarTablaDetalles(document, filasOt, ocultarOt);
            agregarTotalesOt(document, filasOt, totalMostradoOt, totalPagado, ocultarOt, precioPerOt != null);
            agregarFooter(document);
        });
    }

    private byte[] crearPdf(PdfBuilder builder) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 24, 30);
            PdfWriter.getInstance(document, baos);
            document.open();
            builder.build(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF.", e);
        }
    }

    private void agregarLogo(Document document) throws Exception {
        Image logo = cargarLogo();
        if (logo == null) return;
        logo.scaleToFit(280, 120);
        logo.setAlignment(Image.ALIGN_CENTER);
        document.add(logo);
    }

    private Image cargarLogo() {
        try (InputStream stream = new ClassPathResource("logo-suspension-tito.png").getInputStream()) {
            return Image.getInstance(stream.readAllBytes());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void agregarFooter(Document document) throws Exception {
        Paragraph footer = new Paragraph(
                "Generado por S.P.T. — Sistema de Gestión Suspensión Tito",
                FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_LEFT);
        footer.setSpacingBefore(36f);
        document.add(footer);
    }

    private void agregarTitulo(Document document, String titulo) throws Exception {
        Paragraph paragraph = new Paragraph(titulo, TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(8f);
        paragraph.setSpacingAfter(12f);
        document.add(paragraph);
    }

    private void agregarTablaCabecera(Document document,
                                      ClienteResponse cliente,
                                      VehiculoResponse vehiculo,
                                      String numeroDocumento,
                                      String tipoDocumento,
                                      LocalDateTime fecha,
                                      String estado) throws Exception {
        PdfPTable table = new PdfPTable(new float[]{1.15f, 2.15f, 1.0f, 1.7f, 1.0f, 1.7f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);

        addLabelValuePair(table, "Nombre", nombreCompleto(cliente), "Orden", texto(numeroDocumento), "Tipo", tipoDocumento);
        addLabelValuePair(table, "Direccion", texto(cliente.getDireccion()), "Fecha", formatDate(fecha), "Telefono", texto(cliente.getTelefono()));
        addLabelValuePair(table, "Marca", texto(vehiculo.getMarca()), "Modelo", texto(vehiculo.getModelo()), "Patente", texto(vehiculo.getPatente()));
        addLabelValuePair(table, "Estado", estado, "WhatsApp", texto(cliente.getWhatsapp()), "", "");

        table.addCell(labelCell("Kilometraje"));
        table.addCell(valueCell(vehiculo.getKilometraje() == null ? "-" : String.valueOf(vehiculo.getKilometraje())));
        table.addCell(labelCell("Observacion"));
        table.addCell(valueCell("-", 3));

        document.add(table);
    }

    private void addLabelValuePair(PdfPTable table,
                                   String label1, String value1,
                                   String label2, String value2,
                                   String label3, String value3) {
        table.addCell(labelCell(label1));
        table.addCell(valueCell(value1));
        table.addCell(labelCell(label2));
        table.addCell(valueCell(value2));
        table.addCell(labelCell(label3));
        table.addCell(valueCell(value3));
    }

    private void agregarBloqueDescripcion(Document document, String titulo, String descripcion) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12f);

        PdfPCell title = new PdfPCell(new Phrase(titulo, SECTION_FONT));
        title.setBackgroundColor(PURPLE);
        title.setBorderColor(PURPLE);
        title.setPadding(8f);
        title.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(title);

        PdfPCell content = new PdfPCell(new Phrase(valorOGuion(descripcion), VALUE_FONT));
        content.setBorderColor(PURPLE);
        content.setPadding(8f);
        content.setMinimumHeight(70f);
        content.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(content);

        document.add(table);
    }

    private void agregarTablaDetalles(Document document, List<FilaDetalle> filas, boolean ocultarPrecios) throws Exception {
        int cols = ocultarPrecios ? 2 : 5;
        float[] widths = ocultarPrecios ? new float[]{3.2f, 1.2f} : new float[]{3.2f, 1.2f, 0.9f, 1.6f, 1.6f};

        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(12f);

        PdfPCell section = new PdfPCell(new Phrase("Materiales / Repuestos", SECTION_FONT));
        section.setColspan(cols);
        section.setBackgroundColor(PURPLE);
        section.setBorderColor(PURPLE);
        section.setPadding(8f);
        table.addCell(section);

        table.addCell(headerCell("Descripcion"));
        table.addCell(headerCell(ocultarPrecios ? "Cant." : "Tipo"));
        if (!ocultarPrecios) {
            table.addCell(headerCell("Cant."));
            table.addCell(headerCell("P. Unitario"));
            table.addCell(headerCell("Subtotal"));
        }

        if (filas.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Sin items cargados.", EMPTY_FONT));
            empty.setColspan(cols);
            empty.setPadding(10f);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setBorderColor(PURPLE);
            table.addCell(empty);
        } else {
            for (FilaDetalle fila : filas) {
                table.addCell(detailCell(valorOGuion(fila.descripcion), Element.ALIGN_LEFT));
                table.addCell(detailCell(
                        ocultarPrecios ? texto(fila.cantidad) : texto(fila.tipo),
                        Element.ALIGN_CENTER));
                if (!ocultarPrecios) {
                    table.addCell(detailCell(texto(fila.cantidad), Element.ALIGN_CENTER));
                    table.addCell(detailCell(formatMoney(fila.precioUnitario), Element.ALIGN_RIGHT));
                    table.addCell(detailCell(formatMoney(fila.subtotal), Element.ALIGN_RIGHT));
                }
            }
        }

        document.add(table);
    }

    private void agregarTotalesPresupuesto(Document document, List<FilaDetalle> filas,
                                            BigDecimal totalDocumento, boolean ocultarPrecios,
                                            boolean ajustado) throws Exception {
        PdfPTable table = crearTablaTotales();
        if (!ocultarPrecios) {
            BigDecimal subtotalPiezas = subtotalPorTipo(filas, TipoItemDetalle.PIEZA);
            BigDecimal subtotalServicios = subtotalPorTipo(filas, TipoItemDetalle.SERVICIO);
            addTotalRow(table, "Piezas", subtotalPiezas, false);
            addTotalRow(table, "Servicios / mano de obra", subtotalServicios, false);
        }
        String labelTotal = "Total del presupuesto" + (ajustado ? " *" : "");
        addTotalRow(table, labelTotal, nvl(totalDocumento), true);
        document.add(table);
        if (ajustado) {
            Paragraph nota = new Paragraph("* Total ajustado manualmente.",
                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.DARK_GRAY));
            nota.setAlignment(Element.ALIGN_RIGHT);
            nota.setSpacingBefore(2f);
            document.add(nota);
        }
    }

    private void agregarTotalesOt(Document document, List<FilaDetalle> filas,
                                   BigDecimal totalDocumento, BigDecimal totalPagado,
                                   boolean ocultarPrecios, boolean ajustado) throws Exception {
        BigDecimal total = nvl(totalDocumento);
        BigDecimal pagado = nvl(totalPagado);
        BigDecimal saldo = total.subtract(pagado);
        if (saldo.signum() < 0) saldo = BigDecimal.ZERO;

        PdfPTable table = crearTablaTotales();
        if (!ocultarPrecios) {
            BigDecimal subtotalPiezas = subtotalPorTipo(filas, TipoItemDetalle.PIEZA);
            BigDecimal subtotalServicios = subtotalPorTipo(filas, TipoItemDetalle.SERVICIO);
            addTotalRow(table, "Piezas", subtotalPiezas, false);
            addTotalRow(table, "Servicios / mano de obra", subtotalServicios, false);
        }
        addTotalRow(table, "Total pagado", pagado, false);
        addTotalRow(table, "Saldo pendiente", saldo, false);
        String labelTotal = "Total del trabajo" + (ajustado ? " *" : "");
        addTotalRow(table, labelTotal, total, TOTAL_VALUE_BLUE_FONT);
        document.add(table);
        if (ajustado) {
            Paragraph nota = new Paragraph("* Total ajustado manualmente.",
                    new Font(Font.HELVETICA, 8, Font.ITALIC, Color.DARK_GRAY));
            nota.setAlignment(Element.ALIGN_RIGHT);
            nota.setSpacingBefore(2f);
            document.add(nota);
        }
    }

    private PdfPTable crearTablaTotales() {
        // Ancho generoso en la columna del valor para que "$ 230.000,00" entre en una sola línea
        // incluso con el font del total grande (13pt bold).
        PdfPTable table = new PdfPTable(new float[]{2.0f, 2.0f});
        table.setWidthPercentage(54);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return table;
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal value, boolean highlight) {
        addTotalRow(table, label, value, highlight ? TOTAL_VALUE_FONT : TOTAL_FONT);
    }

    private void addTotalRow(PdfPTable table, String label, BigDecimal value, Font font) {
        Font labelFont = font;
        Font valueFont = font;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPaddingTop(2f);
        labelCell.setPaddingBottom(2f);
        labelCell.setPaddingRight(8f);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatMoney(value), valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(2f);
        valueCell.setPaddingBottom(2f);
        // Forzar una sola línea — clave para el row del Total grande con "$ 230.000,00".
        valueCell.setNoWrap(true);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, LABEL_FONT));
        cell.setBackgroundColor(PURPLE_LIGHT);
        cell.setBorderColor(PURPLE);
        cell.setPadding(6f);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        return valueCell(text, 1);
    }

    private PdfPCell valueCell(String text, int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(valorOGuion(text), VALUE_FONT));
        cell.setBorderColor(PURPLE);
        cell.setPadding(6f);
        cell.setColspan(colspan);
        return cell;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(PURPLE);
        cell.setBorderColor(PURPLE);
        cell.setPadding(7f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell detailCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, VALUE_FONT));
        cell.setBorderColor(PURPLE);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private BigDecimal subtotalPorTipo(List<FilaDetalle> filas, TipoItemDetalle tipo) {
        return filas.stream()
                .filter(fila -> fila.tipo == tipo)
                .map(fila -> nvl(fila.subtotal))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<FilaDetalle> convertirDetallesPresupuesto(List<PresupuestoDetalleResponse> detalles) {
        return detalles.stream()
                .map(d -> new FilaDetalle(d.getTipoItem(), d.getDescripcionItem(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
    }

    private List<FilaDetalle> convertirDetallesOt(List<OTDetalleResponse> detalles) {
        return detalles.stream()
                .map(d -> new FilaDetalle(d.getTipoItem(), d.getDescripcionItem(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()))
                .toList();
    }

    private String nombreCompleto(ClienteResponse cliente) {
        return (texto(cliente.getNombre()) + " " + texto(cliente.getApellido())).trim();
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "-" : DATE_TIME_FORMATTER.format(date);
    }

    private String formatMoney(BigDecimal value) {
        return MONEY_FORMAT.format(nvl(value));
    }

    private String valorOGuion(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String texto(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @FunctionalInterface
    private interface PdfBuilder {
        void build(Document document) throws Exception;
    }

    private static class FilaDetalle {
        private final TipoItemDetalle tipo;
        private final String descripcion;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal subtotal;

        private FilaDetalle(TipoItemDetalle tipo, String descripcion, Integer cantidad,
                            BigDecimal precioUnitario, BigDecimal subtotal) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }
    }
}
