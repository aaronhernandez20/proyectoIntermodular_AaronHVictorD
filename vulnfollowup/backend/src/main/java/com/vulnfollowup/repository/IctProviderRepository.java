package com.vulnfollowup.repository;

import com.vulnfollowup.entity.IctProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IctProviderRepository extends JpaRepository<IctProvider, Long> {

    Optional<IctProvider> findByNameIgnoreCase(String name);
}
