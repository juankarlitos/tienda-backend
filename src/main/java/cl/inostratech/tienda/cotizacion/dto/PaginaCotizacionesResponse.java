package cl.inostratech.tienda.cotizacion.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pagina de cotizaciones con un formato JSON estable (no se serializa el
 * Page de Spring directamente).
 */
public record PaginaCotizacionesResponse(
        List<CotizacionResponse> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
    public static PaginaCotizacionesResponse desde(Page<CotizacionResponse> page) {
        return new PaginaCotizacionesResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
