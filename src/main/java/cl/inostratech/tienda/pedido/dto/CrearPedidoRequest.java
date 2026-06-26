package cl.inostratech.tienda.pedido.dto;

import cl.inostratech.tienda.model.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Datos del checkout enviados por el comprador. El total NO se envia: se
 * recalcula siempre en el backend.
 */
public record CrearPedidoRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String nombreCliente,

        @NotBlank(message = "El telefono es obligatorio")
        String telefonoCliente,

        @Email(message = "El email no es valido")
        String emailCliente,

        String comentario,

        @NotNull(message = "El metodo de pago es obligatorio")
        MetodoPago metodoPago,

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Valid
        List<ItemPedidoRequest> items
) {

    /** Linea del pedido: producto y cantidad (el precio lo pone el backend). */
    public record ItemPedidoRequest(

            @NotNull(message = "El id del producto es obligatorio")
            Long productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad debe ser al menos 1")
            Integer cantidad
    ) {
    }
}