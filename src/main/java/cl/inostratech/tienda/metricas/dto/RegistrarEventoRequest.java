package cl.inostratech.tienda.metricas.dto;

import cl.inostratech.tienda.model.TipoEventoLanding;
import jakarta.validation.constraints.NotNull;

/**
 * Evento enviado por la landing (publico).
 */
public record RegistrarEventoRequest(

        @NotNull(message = "El tipo de evento es obligatorio")
        TipoEventoLanding tipo
) {
}
