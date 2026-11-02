package cl.inostratech.tienda.cotizacion.dto;

/**
 * Respuesta publica al crear una cotizacion: solo un mensaje de confirmacion,
 * sin datos internos (id, estado, etc.).
 */
public record CotizacionCreadaResponse(String mensaje) {

    public static final String MENSAJE_OK =
            "¡Gracias! Recibimos tu solicitud y te contactaremos pronto.";

    public static CotizacionCreadaResponse ok() {
        return new CotizacionCreadaResponse(MENSAJE_OK);
    }
}
