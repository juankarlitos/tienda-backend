package cl.inostratech.tienda.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos bancarios del vendedor para que el comprador realice la transferencia.
 * Tabla de una sola fila (id fijo = 1). NO se hardcodea en el frontend: se
 * expone por endpoint publico y se edita desde el panel admin.
 */
@Entity
@Table(name = "datos_transferencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatosTransferencia {

    /** Id fijo: la tabla guarda una unica fila de configuracion. */
    @Id
    private Long id;

    private String bancoNombre;
    private String tipoCuenta;
    private String numeroCuenta;
    private String rutTitular;
    private String nombreTitular;
    private String emailTitular;
}