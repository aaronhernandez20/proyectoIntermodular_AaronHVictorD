package com.vulnfollowup.controller;

import com.vulnfollowup.entity.AppUser;
import com.vulnfollowup.entity.Role;
import com.vulnfollowup.repository.AppUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// GET /api/users/assignable: usuarios que pueden ser responsables de una remediación
// (activos y con rol ADMIN o ANALYST). Lo usan la web y el Android para el desplegable
// "Asignar responsable" del CU4. Nunca devuelve contraseñas ni hashes.
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository userRepository;

    public UserController(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record AssignableUser(Long id, String username, String fullName, List<String> roles) {
    }

    @GetMapping("/assignable")
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @Transactional(readOnly = true)
    public List<AssignableUser> assignable() {
        return userRepository.findAll().stream()
                .filter(AppUser::isEnabled)
                .map(u -> new AssignableUser(u.getId(), u.getUsername(), u.getFullName(),
                        u.getRoles().stream().map(Role::getName).sorted().toList()))
                .filter(u -> u.roles().contains(Role.ADMIN) || u.roles().contains(Role.ANALYST))
                .sorted((a, b) -> a.fullName().compareToIgnoreCase(b.fullName()))
                .toList();
    }
}
