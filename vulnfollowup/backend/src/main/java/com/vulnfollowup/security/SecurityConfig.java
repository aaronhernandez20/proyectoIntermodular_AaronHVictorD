package com.vulnfollowup.security;

import com.vulnfollowup.entity.Role;
import com.vulnfollowup.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

// Configuración de seguridad (misma base que la práctica de Spring Security de PSP U7):
// BCrypt + usuarios en BD + roles + @PreAuthorize. La diferencia es que la credencial
// de cada petición es un JWT (Bearer) en vez de HTTP Basic.
//
// Defensa en profundidad: aunque la web oculte botones, la API comprueba SIEMPRE el rol.
// "No confiar en permisos declarados por los clientes" (apartado 10 del enunciado).
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Delegating -> BCrypt por defecto y guarda el hash con el prefijo {bcrypt}
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // Los usuarios se buscan en PostgreSQL (tabla app_user), no en memoria
    @Bean
    UserDetailsService userDetailsService(AppUserRepository repository) {
        return username -> repository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .disabled(!user.isEnabled())
                        .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // Lo usa AuthController para comprobar usuario + contraseña en POST /api/auth/login
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    JwtDecoder jwtDecoder(JwtService jwtService) {
        return jwtService.decoder();
    }

    // Convierte el claim "roles": ["ANALYST"] del token en la authority ROLE_ANALYST,
    // que es lo que entienden hasRole('ANALYST') y @PreAuthorize.
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(JwtService.ROLES_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    // Cadena de filtros: toda petición pasa por aquí antes de llegar al controlador
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                // Sin sesión ni cookies no hay ataque CSRF posible
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público: login, estado del servicio y documentación de la API
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Regla general: leer -> cualquier rol; escribir -> ADMIN o ANALYST.
                        // Las reglas finas (p. ej. proveedores solo ADMIN) van con @PreAuthorize.
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(Role.ADMIN, Role.ANALYST, Role.VIEWER)
                        .requestMatchers("/api/**").hasAnyRole(Role.ADMIN, Role.ANALYST)
                        .anyRequest().authenticated())
                // Sin token o token inválido/caducado -> 401
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    // CORS: el navegador solo deja llamar a la API desde los orígenes configurados
    // (Angular en 4200, Ionic en 8100, y el dominio de Vercel cuando se despliegue).
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${vulnfollowup.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
