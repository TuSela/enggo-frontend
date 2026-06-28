package com.nhom12.enggo_backend.mapper;

import com.nhom12.enggo_backend.dto.request.UserCreationRequest;
import com.nhom12.enggo_backend.dto.request.UserUpdateRequest;
import com.nhom12.enggo_backend.dto.response.UserMinimalResponse;
import com.nhom12.enggo_backend.dto.response.UserResponse;
import com.nhom12.enggo_backend.entity.identity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
//test comment
@Mapper(componentModel = "spring", uses = RoleMapper.class)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "exp", constant = "0")
    @Mapping(target = "level", constant = "1")
    @Mapping(target = "streakDays", constant = "0")
    @Mapping(target = "completedTasks", constant = "0")
    @Mapping(target = "pvpWins", constant = "0")
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "status", constant = "active")
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    UserMinimalResponse toUserMinimalResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "exp", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "streakDays", ignore = true)
    @Mapping(target = "completedTasks", ignore = true)
    @Mapping(target = "pvpWins", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target =  "fullName", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}