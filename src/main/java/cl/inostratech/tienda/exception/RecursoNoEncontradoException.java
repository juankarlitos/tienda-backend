package cl.inostratech.tienda.exception;

/**
 * Se lanza cuando no existe el recurso solicitado. Se traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}