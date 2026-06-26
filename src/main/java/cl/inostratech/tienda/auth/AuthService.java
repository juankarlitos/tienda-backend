package cl.inostratech.tienda.auth;

import cl.inostratech.tienda.auth.dto.AuthResponse;
import cl.inostratech.tienda.auth.dto.LoginRequest;
import cl.inostratech.tienda.auth.dto.RegisterRequest;
import cl.inostratech.tienda.exception.ReglaNegocioException;
import cl.inostratech.tienda.model.Rol;
import cl.inostratech.tienda.model.Usuario;
import cl.inostratech.tienda.repository.UsuarioRepository;
import cl.inostratech.tienda.security.JwtService;
import cl.inostratech.tienda.security.UsuarioDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Logica de registro e inicio de sesion. Encripta contrasenas con BCrypt y
 * entrega un JWT al autenticar.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Registra un usuario nuevo con rol USER y devuelve su token. */
    public AuthResponse registrar(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ReglaNegocioException("Ya existe un usuario registrado con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.USER)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        return construirRespuesta(usuario);
    }

    /** Valida credenciales y devuelve el token JWT. */
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new ReglaNegocioException("Email o contrasena incorrectos");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ReglaNegocioException("Email o contrasena incorrectos"));

        return construirRespuesta(usuario);
    }

    private AuthResponse construirRespuesta(Usuario usuario) {
        String token = jwtService.generarToken(new UsuarioDetails(usuario), usuario.getRol().name());
        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name());
    }
}