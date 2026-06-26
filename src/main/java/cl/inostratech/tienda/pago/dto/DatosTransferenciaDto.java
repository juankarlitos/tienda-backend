package cl.inostratech.tienda.pago.dto;

import cl.inostratech.tienda.model.DatosTransferencia;

/**
 * Datos bancarios del vendedor para mostrar al comprador / editar en el admin.
 */
public record DatosTransferenciaDto(
        String bancoNombre,
        String tipoCuenta,
        String numeroCuenta,
        String rutTitular,
        String nombreTitular,
        String emailTitular
) {
    public static DatosTransferenciaDto desde(DatosTransferencia d) {
        return new DatosTransferenciaDto(
                d.getBancoNombre(),
                d.getTipoCuenta(),
                d.getNumeroCuenta(),
                d.getRutTitular(),
                d.getNombreTitular(),
                d.getEmailTitular());
    }
}