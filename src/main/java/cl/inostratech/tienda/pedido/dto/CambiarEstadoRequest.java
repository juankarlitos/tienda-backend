package cl.inostratech.tienda.pedido.dto;

import cl.inostratech.tienda.model.EstadoPedido;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para cambiar el estado de un pedido (admin).
 */
public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoPedido estado
) {
}