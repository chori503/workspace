package com.workspace.controller;

import com.workspace.dto.space.request.SpaceRequest;
import com.workspace.dto.space.response.SpaceResponse;
import com.workspace.service.SpaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spaces")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping
    public List<SpaceResponse> findAll() {
        return spaceService.findAll();
    }

    @GetMapping("/{id}")
    public SpaceResponse findById(@PathVariable Long id) {
        return spaceService.findById(id);
    }

    @PostMapping
    public ResponseEntity<SpaceResponse> create(@Valid @RequestBody SpaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(spaceService.create(request));
    }

    @PutMapping("/{id}")
    public SpaceResponse update(@PathVariable Long id, @Valid @RequestBody SpaceRequest request) {
        return spaceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        spaceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}