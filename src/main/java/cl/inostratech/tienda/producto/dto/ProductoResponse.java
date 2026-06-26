package cl.inostratech.tienda.producto.dto;

import cl.inostratech.tienda.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representacion de un producto que se devuelve al cliente.
 * {@code imagenes} es la lista completa (la primera es la principal) y
 * {@code imagenUrl} es la principal, por conveniencia para el catalogo.
 */
public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String imagenUrl,
        List<String> imagenes,
        Long categoriaId,
        String categoriaNombre,
        boolean activo,
        LocalDateTime fechaCreacion
) {
    /** Construye el DTO a partir de la entidad. */
    public static ProductoResponse desde(Producto p) {
        List<String> imagenes = p.getImagenes() != null ? p.getImagenes() : List.of();
        String principal = imagenes.isEmpty() ? null : imagenes.get(0);
        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getStock(),
                principal,
                imagenes,
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                p.isActivo(),
                p.getFechaCreacion());
    }
}