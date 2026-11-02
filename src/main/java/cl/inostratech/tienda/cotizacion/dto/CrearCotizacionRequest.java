package cl.inostratech.tienda.cotizacion.dto;

import cl.inostratech.tienda.model.TipoServicio;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia la landing para solicitar una cotizacion (publico).
 * El campo "website" es un honeypot: un humano no lo ve ni lo llena; si
 * viene con valor, la solicitud se trata como spam.
 */
public record CrearCotizacionRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$",
                message = "El nombre solo puede contener letras y espacios")
        String nombre,

        @NotBlank(message = "El telefono es obligatorio")
        @Pattern(
                regexp = "^(\\+569|9)\\d{8}$",
                message = "El telefono debe tener formato chileno: +569XXXXXXXX o 9XXXXXXXX")
        String telefono,

        @Email(message = "El email no es valido")
        @Size(max = 120, message = "El email no puede superar los 120 caracteres")
        String email,

        @NotNull(message = "El tipo de servicio es obligatorio")
        TipoServicio tipoServicio,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(min = 10, max = 1000, message = "El mensaje debe tener entre 10 y 1000 caracteres")
        String mensaje,

        /** Honeypot anti-spam: debe venir vacio. */
        String website
) {
}
