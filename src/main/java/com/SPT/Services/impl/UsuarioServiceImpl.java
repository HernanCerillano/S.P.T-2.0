package com.SPT.Services.impl;

import com.SPT.Dtos.Request.UsuarioRequest;
import com.SPT.Dtos.Response.UsuarioResponse;
import com.SPT.Mapper.UsuarioMapper;
import com.SPT.Model.Usuario;
import com.SPT.Repository.UsuarioRepository;
import com.SPT.Services.UsuarioService;
import com.SPT.Services.exception.BusinessException;
import com.SPT.Services.exception.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        usuarioRepository.findByUserName(request.getUsername())
                .ifPresent(u -> {
                    throw new BusinessException("El username ya existe: " + request.getUsername());
                });

        Usuario entity = usuarioMapper.toEntity(request);
        Usuario saved = usuarioRepository.save(entity);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long idUsuario, UsuarioRequest request) {
        Usuario entity = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));

        usuarioRepository.findByUserName(request.getUsername())
                .filter(u -> !u.getIdUsuario().equals(idUsuario))
                .ifPresent(u -> {
                    throw new BusinessException("El username ya existe: " + request.getUsername());
                });

        usuarioMapper.updateEntityFromRequest(request, entity);
        Usuario saved = usuarioRepository.save(entity);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long idUsuario) {
        Usuario entity = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));
        return usuarioMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarEstado(Long idUsuario, boolean activo) {
        Usuario entity = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idUsuario));
        entity.setActivo(activo);
        usuarioRepository.save(entity);
    }
}
