package cl.inostratech.tienda.exception;

/**
 * Se lanza cuando una IP supera el limite de solicitudes permitido
 * (anti-spam de la landing). Se traduce a HTTP 429 Too Many Requests.
 */
public class LimiteExcedidoException extends RuntimeException {

    public LimiteExcedidoException(String mensaje) {
        super(mensaje);
    }
}
