package com.SPT.Controller;

import com.SPT.Dtos.Request.ClienteRequest;
import com.SPT.Dtos.Response.ClienteResponse;
import com.SPT.Services.ClienteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{idCliente}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long idCliente, @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(idCliente, request));
    }

    @GetMapping("/{idCliente}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable Long idCliente) {
        return ResponseEntity.ok(clienteService.obtenerPorId(idCliente));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(required = false) Boolean activo) {
        if (activo == null) {
            return ResponseEntity.ok(clienteService.listarTodos());
        }
        return ResponseEntity.ok(clienteService.listarActivos(activo));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscar(@RequestParam("q") String q) {
        return ResponseEntity.ok(clienteService.buscar(q));
    }

    @DeleteMapping("/{idCliente}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idCliente) {
        clienteService.eliminar(idCliente);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idCliente}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long idCliente, @RequestParam boolean activo) {
        clienteService.cambiarEstado(idCliente, activo);
        return ResponseEntity.noContent().build();
    }
}
