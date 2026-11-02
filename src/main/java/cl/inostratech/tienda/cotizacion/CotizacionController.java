package cl.inostratech.tienda.cotizacion;

import cl.inostratech.tienda.cotizacion.dto.CambiarEstadoCotizacionRequest;
import cl.inostratech.tienda.cotizacion.dto.CotizacionCreadaResponse;
import cl.inostratech.tienda.cotizacion.dto.CotizacionResponse;
import cl.inostratech.tienda.cotizacion.dto.CrearCotizacionRequest;
import cl.inostratech.tienda.cotizacion.dto.PaginaCotizacionesResponse;
import cl.inostratech.tienda.model.EstadoCotizacion;
import cl.inostratech.tienda.security.LimitadorSolicitudes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cotizaciones de la landing de servicios.
 * La creacion es publica; la gestion vive bajo /api/admin/** (rol ADMIN,
 * ver SecurityConfig).
 */
@RestController
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    /** Solicitar cotizacion (publico). */
    @PostMapping("/api/cotizaciones")
    public ResponseEntity<CotizacionCreadaResponse> crear(@Valid @RequestBody CrearCotizacionRequest request,
                                                          HttpServletRequest httpRequest) {
        cotizacionService.crear(request, LimitadorSolicitudes.obtenerIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(CotizacionCreadaResponse.ok());
    }

    /** Listar cotizaciones paginadas, opcionalmente filtradas por estado (ADMIN). */
    @GetMapping("/api/admin/cotizaciones")
    public PaginaCotizacionesResponse listar(@RequestParam(required = false) EstadoCotizacion estado,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return cotizacionService.listar(estado, page, size);
    }

    /** Cambiar estado de una cotizacion (ADMIN). */
    @PatchMapping("/api/admin/cotizaciones/{id}/estado")
    public CotizacionResponse cambiarEstado(@PathVariable Long id,
                                            @Valid @RequestBody CambiarEstadoCotizacionRequest request) {
        return cotizacionService.cambiarEstado(id, request.estado());
    }
}
