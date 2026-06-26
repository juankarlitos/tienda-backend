package cl.inostratech.tienda.model;

/**
 * Metodo de pago elegido por el comprador. El pago NO es en linea:
 * se concreta en efectivo o por transferencia al juntarse.
 */
public enum MetodoPago {
    EFECTIVO,
    TRANSFERENCIA
}