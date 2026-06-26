package cl.inostratech.tienda.admin.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Resumen de metricas para el panel de administracion.
 */
public record DashboardResponse(
        BigDecimal totalVentas,
        long totalPedidos,
        BigDecimal ingresosDelMes,
        Map<String, Long> pedidosPorEstado,
        List<ProductoVendido> productosMasVendidos
) {
    /** Producto y cantidad total vendida (para el ranking). */
    public record ProductoVendido(
            String nombre,
            long cantidadVendida
    ) {
    }
}