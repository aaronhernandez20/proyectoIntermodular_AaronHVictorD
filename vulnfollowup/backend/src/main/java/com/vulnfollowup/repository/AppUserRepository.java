package com.vulnfollowup.repository;

import com.vulnfollowup.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Lo usa el UserDetailsService de SecurityConfig en el login
    Optional<AppUser> findByUsername(String username);
}
