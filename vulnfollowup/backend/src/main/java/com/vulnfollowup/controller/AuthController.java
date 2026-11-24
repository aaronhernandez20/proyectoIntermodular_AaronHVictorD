package com.vulnfollowup.controller;

import com.vulnfollowup.dto.AuthDTOs.LoginRequest;
import com.vulnfollowup.dto.AuthDTOs.LoginResponse;
import com.vulnfollowup.dto.AuthDTOs.MeResponse;
import com.vulnfollowup.repository.AppUserRepository;
import com.vulnfollowup.security.JwtService;
import com.vulnfollowup.security.JwtService.IssuedToken;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          AppUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // POST /api/auth/login -> 200 con el token, o 401 si las credenciales son incorrectas.
    // El log guarda el usuario pero NUNCA la contraseña ni el token completo.
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        List<String> roles = roles(auth);
        IssuedToken token = jwtService.issue(auth.getName(), roles);
        String fullName = fullName(auth.getName());
        log.info("Login correcto de '{}' con roles {}", auth.getName(), roles);
        return new LoginResponse(token.value(), "Bearer", token.expiresAt(), auth.getName(), fullName, roles);
    }

    // GET /api/auth/me -> quién soy según el token (útil para que la web/app pinte el menú)
    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        return new MeResponse(auth.getName(), fullName(auth.getName()), roles(auth));
    }

    // Las authorities llegan como ROLE_ANALYST; a los clientes les devolvemos "ANALYST"
    private static List<String> roles(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .sorted()
                .toList();
    }

    private String fullName(String username) {
        return userRepository.findByUsername(username).map(u -> u.getFullName()).orElse(username);
    }
}
