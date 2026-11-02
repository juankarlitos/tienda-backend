package cl.inostratech.tienda.cotizacion;

import cl.inostratech.tienda.cotizacion.dto.CrearCotizacionRequest;
import cl.inostratech.tienda.exception.LimiteExcedidoException;
import cl.inostratech.tienda.model.Cotizacion;
import cl.inostratech.tienda.model.EstadoCotizacion;
import cl.inostratech.tienda.model.TipoServicio;
import cl.inostratech.tienda.pedido.CorreoService;
import cl.inostratech.tienda.repository.CotizacionRepository;
import cl.inostratech.tienda.security.LimitadorSolicitudes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    private static final String IP = "200.1.2.3";
    private static final int LIMITE = 5;

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private CorreoService correoService;

    @Mock
    private LimitadorSolicitudes limitador;

    private CotizacionService service;

    @BeforeEach
    void setUp() {
        service = new CotizacionService(cotizacionRepository, correoService, limitador, LIMITE);
    }

    private CrearCotizacionRequest request(String website) {
        return new CrearCotizacionRequest(
                "  María Pérez ",
                "912345678",
                "maria@ejemplo.cl",
                TipoServicio.PAGINA_NEGOCIO,
                "Necesito una página para mi negocio.",
                website);
    }

    private void permitirLimite() {
        when(limitador.permitir(eq("cotizacion:" + IP), eq(LIMITE), any(Duration.class))).thenReturn(true);
    }

    private void guardarDevolviendoConId() {
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> {
            Cotizacion c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
    }

    @Test
    void crear_guardaLaCotizacionYNotificaPorCorreo() {
        permitirLimite();
        guardarDevolviendoConId();

        service.crear(request(null), IP);

        ArgumentCaptor<Cotizacion> captor = ArgumentCaptor.forClass(Cotizacion.class);
        verify(cotizacionRepository).save(captor.capture());
        Cotizacion guardada = captor.getValue();
        assertThat(guardada.getNombre()).isEqualTo("María Pérez");
        assertThat(guardada.getTelefono()).isEqualTo("+56912345678");
        assertThat(guardada.getEmail()).isEqualTo("maria@ejemplo.cl");
        assertThat(guardada.getTipoServicio()).isEqualTo(TipoServicio.PAGINA_NEGOCIO);
        assertThat(guardada.getEstado()).isEqualTo(EstadoCotizacion.NUEVA);

        verify(correoService).notificarCotizacion(guardada);
    }

    @Test
    void crear_conHoneypotNoGuardaNiEnviaCorreo() {
        permitirLimite();

        assertThatCode(() -> service.crear(request("http://spam.example"), IP))
                .doesNotThrowAnyException();

        verify(cotizacionRepository, never()).save(any());
        verifyNoInteractions(correoService);
    }

    @Test
    void crear_siFallaElCorreoLaCotizacionIgualSeGuarda() {
        permitirLimite();
        guardarDevolviendoConId();
        doThrow(new RuntimeException("Resend caido"))
                .when(correoService).notificarCotizacion(any(Cotizacion.class));

        assertThatCode(() -> service.crear(request(""), IP)).doesNotThrowAnyException();

        verify(cotizacionRepository).save(any(Cotizacion.class));
        verify(correoService).notificarCotizacion(any(Cotizacion.class));
    }

    @Test
    void crear_superadoElLimiteLanzaExcepcionYNoGuarda() {
        when(limitador.permitir(eq("cotizacion:" + IP), eq(LIMITE), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> service.crear(request(null), IP))
                .isInstanceOf(LimiteExcedidoException.class);

        verify(cotizacionRepository, never()).save(any());
        verifyNoInteractions(correoService);
    }

    @Test
    void normalizarTelefono_aceptaAmbosFormatos() {
        assertThat(CotizacionService.normalizarTelefono("912345678")).isEqualTo("+56912345678");
        assertThat(CotizacionService.normalizarTelefono("+56912345678")).isEqualTo("+56912345678");
    }
}
