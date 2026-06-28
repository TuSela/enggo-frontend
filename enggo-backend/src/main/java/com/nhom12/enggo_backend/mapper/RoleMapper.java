package com.nhom12.enggo_backend.mapper;

import com.nhom12.enggo_backend.dto.request.RoleRequest;
import com.nhom12.enggo_backend.dto.response.RoleResponse;
import com.nhom12.enggo_backend.entity.identity.auth.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PermissionMapper.class)
public interface RoleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleName", source = "name")
    @Mapping(target = "roleDescription", source = "description")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    @Mapping(target = "name", source = "roleName")
    @Mapping(target = "description", source = "roleDescription")
    RoleResponse toRoleResponse(Role role);
}
