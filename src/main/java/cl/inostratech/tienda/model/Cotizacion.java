package cl.inostratech.tienda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Solicitud de cotizacion enviada desde la landing de servicios.
 * La fecha queda en hora de Chile (la JVM corre en America/Santiago).
 */
@Entity
@Table(name = "cotizaciones", indexes = {
        @Index(name = "idx_cotizaciones_estado", columnList = "estado"),
        @Index(name = "idx_cotizaciones_fecha", columnList = "fechaCreacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    /** Opcional. */
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoServicio tipoServicio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCotizacion estado;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoCotizacion.NUEVA;
        }
    }
}
