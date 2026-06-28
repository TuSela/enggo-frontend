package com.nhom12.enggo_backend.repository;

import com.nhom12.enggo_backend.entity.identity.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);

    List<Role> findByRoleNameIn(Collection<String> roleNames);

    void deleteByRoleName(String roleName);
}
