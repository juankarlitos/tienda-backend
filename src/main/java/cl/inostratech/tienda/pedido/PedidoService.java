package cl.inostratech.tienda.pedido;

import cl.inostratech.tienda.exception.RecursoNoEncontradoException;
import cl.inostratech.tienda.exception.ReglaNegocioException;
import cl.inostratech.tienda.model.EstadoPedido;
import cl.inostratech.tienda.model.ItemPedido;
import cl.inostratech.tienda.model.Pedido;
import cl.inostratech.tienda.model.Producto;
import cl.inostratech.tienda.pedido.dto.CrearPedidoRequest;
import cl.inostratech.tienda.pedido.dto.PedidoResponse;
import cl.inostratech.tienda.repository.PedidoRepository;
import cl.inostratech.tienda.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logica de pedidos (checkout y administracion).
 *
 * Reglas clave (seccion 4 de la especificacion):
 * <ul>
 *   <li>El total se recalcula SIEMPRE en el backend a partir de los precios reales.</li>
 *   <li>Se descuenta el stock; si no alcanza, se rechaza con error claro.</li>
 *   <li>Se guarda snapshot de nombre y precio en cada item.</li>
 *   <li>Se notifica al vendedor por correo.</li>
 * </ul>
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final CorreoService correoService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ProductoRepository productoRepository,
                         CorreoService correoService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.correoService = correoService;
    }

    /** Crea un pedido (checkout). */
    @Transactional
    public PedidoResponse crear(CrearPedidoRequest request) {
        Pedido pedido = Pedido.builder()
                .nombreCliente(request.nombreCliente())
                .telefonoCliente(request.telefonoCliente())
                .emailCliente(request.emailCliente())
                .comentario(request.comentario())
                .metodoPago(request.metodoPago())
                .estado(EstadoPedido.PENDIENTE)
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CrearPedidoRequest.ItemPedidoRequest itemReq : request.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .orElseThrow(() -> new ReglaNegocioException(
                            "No existe el producto con id " + itemReq.productoId()));

            if (!producto.isActivo()) {
                throw new ReglaNegocioException(
                        "El producto '" + producto.getNombre() + "' ya no esta disponible");
            }
            if (producto.getStock() < itemReq.cantidad()) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. " +
                                "Disponible: " + producto.getStock() + ", solicitado: " + itemReq.cantidad());
            }

            // Snapshot de nombre y precio al momento de comprar.
            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(itemReq.cantidad()));

            ItemPedido item = ItemPedido.builder()
                    .producto(producto)
                    .nombreProducto(producto.getNombre())
                    .precioUnitario(producto.getPrecio())
                    .cantidad(itemReq.cantidad())
                    .subtotal(subtotal)
                    .build();
            pedido.agregarItem(item);

            // Descontar stock.
            producto.setStock(producto.getStock() - itemReq.cantidad());

            total = total.add(subtotal);
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);

        // Notificar al vendedor (correo real o consola segun configuracion).
        correoService.notificarPedidoAlVendedor(guardado);

        return PedidoResponse.desde(guardado);
    }

    /** Lista todos los pedidos (admin), del mas reciente al mas antiguo. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc()
                .stream().map(PedidoResponse::desde).toList();
    }

    /** Detalle de un pedido (admin). */
    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return PedidoResponse.desde(buscar(id));
    }

    /** Cambia el estado de un pedido (admin). */
    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = buscar(id);
        pedido.setEstado(nuevoEstado);
        return PedidoResponse.desde(pedidoRepository.save(pedido));
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pedido con id " + id));
    }
}