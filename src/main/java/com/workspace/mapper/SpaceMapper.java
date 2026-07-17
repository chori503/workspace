package com.workspace.mapper;

import com.workspace.dto.space.request.SpaceRequest;
import com.workspace.dto.space.response.SpaceResponse;
import com.workspace.entity.Space;
import org.springframework.stereotype.Component;

@Component
public class SpaceMapper {

    public SpaceResponse toResponse(Space space) {
        return new SpaceResponse(
                space.getId(),
                space.getName(),
                space.getType(),
                space.getCapacity(),
                space.getLocation(),
                space.getFloor(),
                space.getHourlyRate(),
                space.getStatus()
        );
    }

    public void requestToEntity(Space space, SpaceRequest request) {
        space.setName(request.name());
        space.setType(request.type());
        space.setCapacity(request.capacity());
        space.setLocation(request.location());
        space.setFloor(request.floor());
        space.setHourlyRate(request.hourlyRate());
    }
}
