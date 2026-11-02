package cl.inostratech.tienda.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorSolicitudesTest {

    @Test
    void permiteHastaElMaximoYLuegoBloquea_porClave() {
        LimitadorSolicitudes limitador = new LimitadorSolicitudes();
        for (int i = 0; i < 5; i++) {
            assertThat(limitador.permitir("cotizacion:1.1.1.1", 5, Duration.ofHours(1))).isTrue();
        }
        assertThat(limitador.permitir("cotizacion:1.1.1.1", 5, Duration.ofHours(1))).isFalse();
        // Otra IP y otro endpoint tienen su propio contador.
        assertThat(limitador.permitir("cotizacion:2.2.2.2", 5, Duration.ofHours(1))).isTrue();
        assertThat(limitador.permitir("metricas:1.1.1.1", 60, Duration.ofHours(1))).isTrue();
    }

    @Test
    void obtenerIp_usaLaUltimaDeXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "6.6.6.6, 200.1.2.3");
        assertThat(LimitadorSolicitudes.obtenerIp(request)).isEqualTo("200.1.2.3");
    }

    @Test
    void obtenerIp_sinEncabezadoUsaRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        assertThat(LimitadorSolicitudes.obtenerIp(request)).isEqualTo("10.0.0.1");
    }
}
