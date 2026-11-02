package cl.inostratech.tienda.repository;

import cl.inostratech.tienda.model.Cotizacion;
import cl.inostratech.tienda.model.EstadoCotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    Page<Cotizacion> findByEstado(EstadoCotizacion estado, Pageable pageable);
}
