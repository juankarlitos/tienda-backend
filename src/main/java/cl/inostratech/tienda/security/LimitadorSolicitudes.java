package cl.inostratech.tienda.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limite simple de solicitudes por clave (ej: "cotizacion:IP") en memoria,
 * con ventana fija. Pensado para una sola instancia (Render); si la app se
 * reinicia, los contadores vuelven a cero.
 */
@Component
public class LimitadorSolicitudes {

    /** Sobre este tamano se limpian las ventanas vencidas para no crecer sin limite. */
    private static final int MAX_CLAVES_ANTES_DE_LIMPIAR = 10_000;

    private final Map<String, Ventana> ventanas = new ConcurrentHashMap<>();

    /**
     * Registra una solicitud para la clave y devuelve true si sigue dentro del limite.
     */
    public boolean permitir(String clave, int maximo, Duration duracion) {
        long ahora = System.currentTimeMillis();
        if (ventanas.size() > MAX_CLAVES_ANTES_DE_LIMPIAR) {
            ventanas.values().removeIf(v -> v.vencida(ahora));
        }
        Ventana ventana = ventanas.compute(clave, (k, actual) ->
                actual == null || actual.vencida(ahora)
                        ? new Ventana(ahora + duracion.toMillis(), 1)
                        : new Ventana(actual.fin(), actual.contador() + 1));
        return ventana.contador() <= maximo;
    }

    /**
     * IP del cliente. Detras del proxy de Render se usa la ULTIMA IP de
     * X-Forwarded-For (la agrega el proxy; las anteriores las puede falsificar
     * el cliente). Si no viene el encabezado, se usa getRemoteAddr().
     */
    public static String obtenerIp(HttpServletRequest request) {
        String reenviado = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(reenviado)) {
            String[] partes = reenviado.split(",");
            String ultima = partes[partes.length - 1].trim();
            if (StringUtils.hasText(ultima)) {
                return ultima;
            }
        }
        return request.getRemoteAddr();
    }

    private record Ventana(long fin, int contador) {
        boolean vencida(long ahora) {
            return ahora >= fin;
        }
    }
}
