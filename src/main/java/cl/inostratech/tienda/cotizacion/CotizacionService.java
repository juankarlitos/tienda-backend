package cl.inostratech.tienda.cotizacion;

import cl.inostratech.tienda.cotizacion.dto.CotizacionResponse;
import cl.inostratech.tienda.cotizacion.dto.CrearCotizacionRequest;
import cl.inostratech.tienda.cotizacion.dto.PaginaCotizacionesResponse;
import cl.inostratech.tienda.exception.LimiteExcedidoException;
import cl.inostratech.tienda.exception.RecursoNoEncontradoException;
import cl.inostratech.tienda.model.Cotizacion;
import cl.inostratech.tienda.model.EstadoCotizacion;
import cl.inostratech.tienda.pedido.CorreoService;
import cl.inostratech.tienda.repository.CotizacionRepository;
import cl.inostratech.tienda.security.LimitadorSolicitudes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Logica de cotizaciones de la landing de servicios.
 * <ul>
 *   <li>Anti-spam: limite por IP y honeypot "website".</li>
 *   <li>Se notifica al vendedor por correo; si el correo falla, la cotizacion
 *       igual queda guardada.</li>
 * </ul>
 */
@Service
public class CotizacionService {

    private static final Logger log = LoggerFactory.getLogger(CotizacionService.class);
    private static final Duration VENTANA_LIMITE = Duration.ofHours(1);
    private static final int TAMANO_MAXIMO_PAGINA = 100;

    private final CotizacionRepository cotizacionRepository;
    private final CorreoService correoService;
    private final LimitadorSolicitudes limitador;
    private final int limitePorHora;

    public CotizacionService(CotizacionRepository cotizacionRepository,
                             CorreoService correoService,
                             LimitadorSolicitudes limitador,
                             @Value("${app.limite.cotizaciones-por-hora:5}") int limitePorHora) {
        this.cotizacionRepository = cotizacionRepository;
        this.correoService = correoService;
        this.limitador = limitador;
        this.limitePorHora = limitePorHora;
    }

    /**
     * Registra una cotizacion. Si el honeypot viene con valor no se guarda
     * nada, pero el controlador responde igual que en el caso exitoso.
     */
    @Transactional
    public void crear(CrearCotizacionRequest request, String ip) {
        if (!limitador.permitir("cotizacion:" + ip, limitePorHora, VENTANA_LIMITE)) {
            throw new LimiteExcedidoException(
                    "Has enviado demasiadas solicitudes. Intenta nuevamente en una hora.");
        }

        if (StringUtils.hasText(request.website())) {
            log.warn("Cotizacion descartada por honeypot (IP {})", ip);
            return;
        }

        Cotizacion cotizacion = Cotizacion.builder()
                .nombre(request.nombre().trim())
                .telefono(normalizarTelefono(request.telefono()))
                .email(StringUtils.hasText(request.email()) ? request.email().trim() : null)
                .tipoServicio(request.tipoServicio())
                .mensaje(request.mensaje().trim())
                .estado(EstadoCotizacion.NUEVA)
                .build();

        Cotizacion guardada = cotizacionRepository.save(cotizacion);

        try {
            correoService.notificarCotizacion(guardada);
        } catch (Exception ex) {
            // La cotizacion NO debe perderse por un fallo de correo.
            log.error("No se pudo notificar la cotizacion {} por correo: {}",
                    guardada.getId(), ex.getMessage());
        }
    }

    /** Lista paginada (ADMIN), ordenada por fecha descendente y filtrable por estado. */
    @Transactional(readOnly = true)
    public PaginaCotizacionesResponse listar(EstadoCotizacion estado, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamano, 1), TAMANO_MAXIMO_PAGINA),
                Sort.by(Sort.Direction.DESC, "fechaCreacion"));

        Page<Cotizacion> page = estado != null
                ? cotizacionRepository.findByEstado(estado, pageable)
                : cotizacionRepository.findAll(pageable);

        return PaginaCotizacionesResponse.desde(page.map(CotizacionResponse::desde));
    }

    /** Cambia el estado de una cotizacion (ADMIN). */
    @Transactional
    public CotizacionResponse cambiarEstado(Long id, EstadoCotizacion nuevoEstado) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la cotizacion con id " + id));
        cotizacion.setEstado(nuevoEstado);
        return CotizacionResponse.desde(cotizacionRepository.save(cotizacion));
    }

    /** Deja el telefono siempre como +569XXXXXXXX. */
    static String normalizarTelefono(String telefono) {
        String t = telefono.trim();
        return t.startsWith("+") ? t : "+56" + t;
    }
}
