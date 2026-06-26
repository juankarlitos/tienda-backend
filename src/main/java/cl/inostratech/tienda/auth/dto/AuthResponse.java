package cl.inostratech.tienda.auth.dto;

/**
 * Respuesta de autenticacion: token JWT y datos basicos del usuario.
 */
public record AuthResponse(
        String token,
        String tipo,
        Long id,
        String nombre,
        String email,
        String rol
) {
    public AuthResponse(String token, Long id, String nombre, String email, String rol) {
        this(token, "Bearer", id, nombre, email, rol);
    }
}