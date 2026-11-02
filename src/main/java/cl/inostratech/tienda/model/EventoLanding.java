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
 * Evento simple de la landing (visita, click en WhatsApp, click en precios).
 */
@Entity
@Table(name = "eventos_landing", indexes = {
        @Index(name = "idx_eventos_landing_fecha", columnList = "fecha")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoLanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoLanding tipo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void alCrear() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
