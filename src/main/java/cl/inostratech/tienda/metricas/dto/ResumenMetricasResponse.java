package cl.inostratech.tienda.metricas.dto;

import java.util.Map;

/**
 * Totales por tipo de evento de la landing en los ultimos 7 y 30 dias.
 * Todos los tipos aparecen siempre (con 0 si no hubo eventos).
 */
public record ResumenMetricasResponse(
        Map<String, Long> ultimos7Dias,
        Map<String, Long> ultimos30Dias
) {
}
