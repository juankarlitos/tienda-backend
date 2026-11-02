package cl.inostratech.tienda.repository;

import cl.inostratech.tienda.model.EventoLanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoLandingRepository extends JpaRepository<EventoLanding, Long> {

    /** Devuelve filas [TipoEventoLanding, Long] con el total por tipo desde la fecha dada. */
    @Query("SELECT e.tipo, COUNT(e) FROM EventoLanding e WHERE e.fecha >= :desde GROUP BY e.tipo")
    List<Object[]> contarPorTipoDesde(@Param("desde") LocalDateTime desde);
}
