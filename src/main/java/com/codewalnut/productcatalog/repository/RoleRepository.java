package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.security.ApplicationRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(ApplicationRole name);
}
