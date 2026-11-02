package cl.inostratech.tienda.cotizacion.dto;

import cl.inostratech.tienda.model.Cotizacion;
import cl.inostratech.tienda.model.EstadoCotizacion;
import cl.inostratech.tienda.model.TipoServicio;

import java.time.LocalDateTime;

/**
 * Cotizacion completa para el panel de administracion.
 */
public record CotizacionResponse(
        Long id,
        String nombre,
        String telefono,
        String email,
        TipoServicio tipoServicio,
        String mensaje,
        EstadoCotizacion estado,
        LocalDateTime fechaCreacion
) {
    public static CotizacionResponse desde(Cotizacion c) {
        return new CotizacionResponse(
                c.getId(),
                c.getNombre(),
                c.getTelefono(),
                c.getEmail(),
                c.getTipoServicio(),
                c.getMensaje(),
                c.getEstado(),
                c.getFechaCreacion());
    }
}
