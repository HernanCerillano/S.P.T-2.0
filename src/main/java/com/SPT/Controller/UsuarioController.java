package com.SPT.Controller;

import com.SPT.Dtos.Request.UsuarioRequest;
import com.SPT.Dtos.Response.UsuarioResponse;
import com.SPT.Services.UsuarioService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long idUsuario,
                                                      @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(idUsuario, request));
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(idUsuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PatchMapping("/{idUsuario}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long idUsuario, @RequestParam boolean activo) {
        usuarioService.cambiarEstado(idUsuario, activo);
        return ResponseEntity.noContent().build();
    }
}
