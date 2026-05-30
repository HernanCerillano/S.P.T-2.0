package com.SPT.Services.impl;

import com.SPT.Model.Cliente;
import com.SPT.Model.Factura;
import com.SPT.Repository.FacturaRepository;
import com.SPT.Services.PdfFacturaService;
import com.SPT.Services.exception.ResourceNotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PdfFacturaServiceImpl implements PdfFacturaService {

    // Paleta SPT — Suspensión Tito
    private static final Color BLUE_DARK = new Color(13, 27, 42);    // #0D1B2A azul primario
    private static final Color RED_ACCENT = new Color(164, 22, 26);  // #A4161A rojo accent
    private static final Color GRAY_DATA = new Color(42, 42, 42);    // #2A2A2A gris datos
    private static final Color BORDER_SOFT = new Color(197, 197, 197); // #C5C5C5 borde sutil
    private static final Color ZEBRA_BG = new Color(244, 244, 244);  // #F4F4F4 zebra
    private static final Color WARN_BG = new Color(255, 245, 245);   // #FFF5F5 fondo aviso

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, BLUE_DARK);
    private static final Font WARN_TITLE_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, RED_ACCENT);
    private static final Font WARN_BODY_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_DATA);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_DATA);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font TOTAL_LABEL_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, GRAY_DATA);
    private static final Font TOTAL_VALUE_FONT = new Font(Font.HELVETICA, 13, Font.BOLD, BLUE_DARK);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(120, 120, 120));

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat MONEY;

    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setDecimalSeparator(',');
        sym.setGroupingSeparator('.');
        MONEY = new DecimalFormat("$ #,##0.00", sym);
    }

    private final FacturaRepository facturaRepository;

    public PdfFacturaServiceImpl(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfFactura(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 24, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            agregarLogo(document);
            agregarTitulo(document, factura);
            agregarCabecera(document, factura);
            agregarTotales(document, factura);
            agregarFooter(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del comprobante.", e);
        }
    }

    private void agregarLogo(Document document) throws Exception {
        try (InputStream stream = new ClassPathResource("logo-suspension-tito.png").getInputStream()) {
            Image logo = Image.getInstance(stream.readAllBytes());
            logo.scaleToFit(280, 120);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception ignored) {
            // logo no disponible
        }
    }

    private void agregarTitulo(Document document, Factura factura) throws Exception {
        String titulo = "COMPROBANTE INTERNO N° " + factura.getNumeroFactura();
        Paragraph p = new Paragraph(titulo, TITLE_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(12f);
        p.setSpacingAfter(10f);
        document.add(p);
    }

    private void agregarCabecera(Document document, Factura factura) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35f, 65f});
        table.setSpacingBefore(6f);
        table.setSpacingAfter(12f);

        agregarEncabezadoTabla(table, "DATOS DEL COMPROBANTE");
        agregarFilaCabecera(table, "Número", factura.getNumeroFactura(), false);
        agregarFilaCabecera(table, "Estado", factura.getEstado().name(), true);
        agregarFilaCabecera(table, "Fecha emisión", formatFecha(factura.getFechaEmision()), false);

        Cliente cliente = factura.getCliente();
        if (cliente != null) {
            String nombreCompleto = (cliente.getNombre() != null ? cliente.getNombre() : "") + " "
                    + (cliente.getApellido() != null ? cliente.getApellido() : "");
            agregarFilaCabecera(table, "Cliente", nombreCompleto.trim(), true);
        }

        if (factura.getOrdenTrabajo() != null) {
            agregarFilaCabecera(table, "OT asociada", factura.getOrdenTrabajo().getNumeroOt(), false);
        }
        if (factura.getPresupuesto() != null) {
            agregarFilaCabecera(table, "Presupuesto asociado",
                    factura.getPresupuesto().getNumeroPresupuesto(),
                    factura.getOrdenTrabajo() == null);
        }
        if (factura.getObservaciones() != null && !factura.getObservaciones().isBlank()) {
            agregarFilaCabecera(table, "Observaciones", factura.getObservaciones(), false);
        }

        document.add(table);
    }

    private void agregarTotales(Document document, Factura factura) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{50f, 50f});
        table.setSpacingBefore(6f);

        agregarFilaTotalGris(table, "Subtotal", factura.getSubtotal());
        agregarFilaTotalGris(table, "Impuestos", factura.getImpuestos());

        // TOTAL destacado: fondo blanco, texto azul oscuro grande
        PdfPCell labelTotal = new PdfPCell(new Phrase("TOTAL", TOTAL_VALUE_FONT));
        labelTotal.setBackgroundColor(Color.WHITE);
        labelTotal.setPadding(10f);
        labelTotal.setBorderColor(BLUE_DARK);
        labelTotal.setBorderWidth(1.5f);

        PdfPCell valueTotal = new PdfPCell(new Phrase(formatMonto(factura.getTotal()), TOTAL_VALUE_FONT));
        valueTotal.setBackgroundColor(Color.WHITE);
        valueTotal.setPadding(10f);
        valueTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueTotal.setBorderColor(BLUE_DARK);
        valueTotal.setBorderWidth(1.5f);

        table.addCell(labelTotal);
        table.addCell(valueTotal);

        document.add(table);
    }

    private void agregarFooter(Document document) throws Exception {
        Paragraph footer = new Paragraph(
                "Generado por S.P.T. — Sistema de Gestión Suspensión Tito",
                FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_LEFT);
        footer.setSpacingBefore(36f);
        document.add(footer);
    }

    private void agregarEncabezadoTabla(PdfPTable table, String titulo) {
        PdfPCell header = new PdfPCell(new Phrase(titulo, HEADER_FONT));
        header.setColspan(2);
        header.setBackgroundColor(BLUE_DARK);
        header.setPadding(8f);
        header.setBorderColor(BLUE_DARK);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }

    private void agregarFilaCabecera(PdfPTable table, String label, String value, boolean zebra) {
        Color labelBg = BLUE_DARK;
        Color valueBg = zebra ? ZEBRA_BG : Color.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBackgroundColor(labelBg);
        labelCell.setBorderColor(BORDER_SOFT);
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", VALUE_FONT));
        valueCell.setBackgroundColor(valueBg);
        valueCell.setBorderColor(BORDER_SOFT);
        valueCell.setPadding(6f);
        table.addCell(valueCell);
    }

    private void agregarFilaTotalGris(PdfPTable table, String label, BigDecimal valor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, TOTAL_LABEL_FONT));
        labelCell.setBackgroundColor(ZEBRA_BG);
        labelCell.setBorderColor(BORDER_SOFT);
        labelCell.setPadding(6f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatMonto(valor),
                new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_DATA)));
        valueCell.setBorderColor(BORDER_SOFT);
        valueCell.setPadding(6f);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String formatFecha(LocalDateTime dt) {
        return dt != null ? dt.format(FORMATTER) : "";
    }

    private String formatMonto(BigDecimal valor) {
        return MONEY.format(valor != null ? valor : BigDecimal.ZERO);
    }
}
