package cl.inostratech.tienda.admin;

import cl.inostratech.tienda.admin.dto.DashboardResponse;
import cl.inostratech.tienda.model.EstadoPedido;
import cl.inostratech.tienda.model.ItemPedido;
import cl.inostratech.tienda.model.Pedido;
import cl.inostratech.tienda.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula las metricas del panel admin a partir de los pedidos.
 *
 * Las ventas/ingresos consideran todos los pedidos salvo los CANCELADOS.
 */
@Service
public class DashboardService {

    private static final int TOP_PRODUCTOS = 5;

    private final PedidoRepository pedidoRepository;

    public DashboardService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse obtener() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal ingresosDelMes = BigDecimal.ZERO;
        Map<String, Long> productosVendidos = new LinkedHashMap<>();

        // Inicializa el conteo por estado en 0 para que todos los estados aparezcan.
        Map<String, Long> pedidosPorEstado = new LinkedHashMap<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            pedidosPorEstado.put(estado.name(), 0L);
        }

        for (Pedido pedido : pedidos) {
            pedidosPorEstado.merge(pedido.getEstado().name(), 1L, Long::sum);

            if (pedido.getEstado() == EstadoPedido.CANCELADO) {
                continue;
            }

            totalVentas = totalVentas.add(pedido.getTotal());
            if (pedido.getFechaCreacion() != null && !pedido.getFechaCreacion().isBefore(inicioMes)) {
                ingresosDelMes = ingresosDelMes.add(pedido.getTotal());
            }

            for (ItemPedido item : pedido.getItems()) {
                productosVendidos.merge(item.getNombreProducto(), (long) item.getCantidad(), Long::sum);
            }
        }

        List<DashboardResponse.ProductoVendido> masVendidos = productosVendidos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_PRODUCTOS)
                .map(e -> new DashboardResponse.ProductoVendido(e.getKey(), e.getValue()))
                .toList();

        return new DashboardResponse(
                totalVentas,
                pedidos.size(),
                ingresosDelMes,
                pedidosPorEstado,
                masVendidos);
    }
}