package cl.inostratech.tienda.metricas;

import cl.inostratech.tienda.exception.LimiteExcedidoException;
import cl.inostratech.tienda.metricas.dto.ResumenMetricasResponse;
import cl.inostratech.tienda.model.EventoLanding;
import cl.inostratech.tienda.model.TipoEventoLanding;
import cl.inostratech.tienda.repository.EventoLandingRepository;
import cl.inostratech.tienda.security.LimitadorSolicitudes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metricas simples de la landing: registro de eventos y resumen para el admin.
 */
@Service
public class MetricasService {

    private static final Duration VENTANA_LIMITE = Duration.ofHours(1);

    private final EventoLandingRepository eventoRepository;
    private final LimitadorSolicitudes limitador;
    private final int limitePorHora;

    public MetricasService(EventoLandingRepository eventoRepository,
                           LimitadorSolicitudes limitador,
                           @Value("${app.limite.metricas-por-hora:60}") int limitePorHora) {
        this.eventoRepository = eventoRepository;
        this.limitador = limitador;
        this.limitePorHora = limitePorHora;
    }

    /** Registra un evento de la landing (publico, con limite por IP). */
    @Transactional
    public void registrar(TipoEventoLanding tipo, String ip) {
        if (!limitador.permitir("metricas:" + ip, limitePorHora, VENTANA_LIMITE)) {
            throw new LimiteExcedidoException(
                    "Demasiados eventos registrados. Intenta nuevamente mas tarde.");
        }
        eventoRepository.save(EventoLanding.builder().tipo(tipo).build());
    }

    /** Totales por tipo en los ultimos 7 y 30 dias (ADMIN). */
    @Transactional(readOnly = true)
    public ResumenMetricasResponse resumen() {
        LocalDateTime ahora = LocalDateTime.now();
        return new ResumenMetricasResponse(
                totalesDesde(ahora.minusDays(7)),
                totalesDesde(ahora.minusDays(30)));
    }

    private Map<String, Long> totalesDesde(LocalDateTime desde) {
        Map<String, Long> totales = new LinkedHashMap<>();
        for (TipoEventoLanding tipo : TipoEventoLanding.values()) {
            totales.put(tipo.name(), 0L);
        }
        for (Object[] fila : eventoRepository.contarPorTipoDesde(desde)) {
            totales.put(((TipoEventoLanding) fila[0]).name(), (Long) fila[1]);
        }
        return totales;
    }
}
