package com.nhom12.enggo_backend.repository;

import com.nhom12.enggo_backend.entity.identity.auth.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByPermissionName(String permissionName);

    List<Permission> findByPermissionNameIn(Collection<String> permissionNames);

    void deleteByPermissionName(String permissionName);
}
