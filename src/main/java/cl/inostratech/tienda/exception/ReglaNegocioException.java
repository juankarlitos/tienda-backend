package cl.inostratech.tienda.exception;

/**
 * Excepcion de negocio que se traduce a una respuesta HTTP 400 con un mensaje
 * claro para el cliente (ej: email ya registrado, stock insuficiente, etc.).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}