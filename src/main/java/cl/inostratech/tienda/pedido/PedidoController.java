package cl.inostratech.tienda.pedido;

import cl.inostratech.tienda.pedido.dto.CambiarEstadoRequest;
import cl.inostratech.tienda.pedido.dto.CrearPedidoRequest;
import cl.inostratech.tienda.pedido.dto.PedidoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de pedidos. La creacion (checkout) es publica; el resto es ADMIN.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** Crear pedido (checkout, publico). */
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    /** Listar todos los pedidos (ADMIN). */
    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidoService.listar();
    }

    /** Detalle de un pedido (ADMIN). */
    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id) {
        return pedidoService.obtener(id);
    }

    /** Cambiar estado de un pedido (ADMIN). */
    @PutMapping("/{id}/estado")
    public PedidoResponse cambiarEstado(@PathVariable Long id,
                                        @Valid @RequestBody CambiarEstadoRequest request) {
        return pedidoService.cambiarEstado(id, request.estado());
    }
}