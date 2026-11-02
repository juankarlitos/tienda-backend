package cl.inostratech.tienda.cotizacion.dto;

import cl.inostratech.tienda.model.EstadoCotizacion;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para cambiar el estado de una cotizacion (admin).
 */
public record CambiarEstadoCotizacionRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoCotizacion estado
) {
}
