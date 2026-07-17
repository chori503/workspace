package com.workspace.service;

import com.workspace.dto.space.request.SpaceRequest;
import com.workspace.dto.space.response.SpaceResponse;
import com.workspace.entity.Space;
import com.workspace.entity.SpaceStatus;
import com.workspace.exception.BusinessRuleException;
import com.workspace.exception.ResourceNotFoundException;
import com.workspace.mapper.SpaceMapper;
import com.workspace.repository.SpaceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    public SpaceService(SpaceRepository spaceRepository, SpaceMapper spaceMapper) {
        this.spaceRepository = spaceRepository;
        this.spaceMapper = spaceMapper;
    }

    public List<SpaceResponse> findAll() {
        return spaceRepository.findAll().stream()
                .map(spaceMapper::toResponse)
                .toList();
    }

    public SpaceResponse findById(Long id) {
        return spaceMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public SpaceResponse create(SpaceRequest request) {
        Space space = new Space();
        spaceMapper.requestToEntity(space, request);
        space.setStatus(SpaceStatus.ACTIVE);
        return spaceMapper.toResponse(spaceRepository.save(space));
    }

    @Transactional
    public SpaceResponse update(Long id, SpaceRequest request) {
        Space space = getOrThrow(id);
        spaceMapper.requestToEntity(space, request);
        return spaceMapper.toResponse(spaceRepository.save(space));
    }

    @Transactional
    public void delete(Long id) {
        Space space = getOrThrow(id);
        try {
            spaceRepository.delete(space);
            spaceRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("El espacio con id " + id + " no se puede eliminar porque tiene reservas asociadas");
        }
    }

    private Space getOrThrow(Long id) {
        return spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio con id " + id + " no encontrado"));
    }
}
