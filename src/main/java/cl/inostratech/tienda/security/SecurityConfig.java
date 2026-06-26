package cl.inostratech.tienda.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion central de Spring Security:
 * <ul>
 *   <li>Sesiones sin estado (stateless) porque la autenticacion es por JWT.</li>
 *   <li>Encriptacion de contrasenas con BCrypt.</li>
 *   <li>Reglas de acceso por ruta y rol.</li>
 *   <li>CORS habilitado para el dominio del frontend.</li>
 *   <li>Filtro JWT antes del filtro de usuario/contrasena.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioDetailsService usuarioDetailsService;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          UsuarioDetailsService usuarioDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // Permite que la consola H2 se muestre en un frame (solo desarrollo).
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Autenticacion publica.
                        .requestMatchers("/api/auth/**").permitAll()
                        // Consola H2 (desarrollo).
                        .requestMatchers("/h2-console/**").permitAll()
                        // Imagenes servidas localmente (cuando no hay Cloudinary).
                        .requestMatchers("/uploads/**").permitAll()
                        // Configuracion publica (ej: numero de WhatsApp del vendedor).
                        .requestMatchers(HttpMethod.GET, "/api/config").permitAll()
                        // Catalogo publico (solo lectura).
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll()
                        // Datos de transferencia publicos (solo lectura).
                        .requestMatchers(HttpMethod.GET, "/api/pago/transferencia").permitAll()
                        // Crear pedido es publico (checkout sin login).
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").permitAll()
                        // Administracion de productos (crear/editar/eliminar/subir imagen).
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                        // Gestion de pedidos (listar, ver, cambiar estado): solo ADMIN.
                        .requestMatchers("/api/pedidos/**").hasRole("ADMIN")
                        // Editar datos de transferencia: solo ADMIN.
                        .requestMatchers(HttpMethod.PUT, "/api/pago/**").hasRole("ADMIN")
                        // Zona de administracion (dashboard).
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Resto requiere autenticacion.
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}