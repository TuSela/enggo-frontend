package com.nhom12.enggo_backend.mapper;

import com.nhom12.enggo_backend.dto.request.PermissionRequest;
import com.nhom12.enggo_backend.dto.response.PermissionResponse;
import com.nhom12.enggo_backend.entity.identity.auth.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissionName", source = "name")
    @Mapping(target = "permissionDescription", source = "description")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Permission toPermission(PermissionRequest request);

    @Mapping(target = "name", source = "permissionName")
    @Mapping(target = "description", source = "permissionDescription")
    PermissionResponse toPermissionResponse(Permission permission);
}
