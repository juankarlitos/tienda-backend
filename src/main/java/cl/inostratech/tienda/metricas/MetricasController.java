package cl.inostratech.tienda.metricas;

import cl.inostratech.tienda.metricas.dto.RegistrarEventoRequest;
import cl.inostratech.tienda.metricas.dto.ResumenMetricasResponse;
import cl.inostratech.tienda.security.LimitadorSolicitudes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Metricas de la landing. El registro de eventos es publico; el resumen vive
 * bajo /api/admin/** (rol ADMIN, ver SecurityConfig).
 */
@RestController
public class MetricasController {

    private final MetricasService metricasService;

    public MetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    /** Registrar un evento de la landing (publico). */
    @PostMapping("/api/metricas/evento")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistrarEventoRequest request,
                                          HttpServletRequest httpRequest) {
        metricasService.registrar(request.tipo(), LimitadorSolicitudes.obtenerIp(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Totales por tipo de evento en los ultimos 7 y 30 dias (ADMIN). */
    @GetMapping("/api/admin/metricas/resumen")
    public ResumenMetricasResponse resumen() {
        return metricasService.resumen();
    }
}
