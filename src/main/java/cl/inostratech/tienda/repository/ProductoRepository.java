package cl.inostratech.tienda.repository;

import cl.inostratech.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /** Productos visibles en el catalogo publico. */
    List<Producto> findByActivoTrue();

    /** Busqueda por nombre dentro de los productos activos. */
    List<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre);

    /** Productos activos de una categoria. */
    List<Producto> findByActivoTrueAndCategoriaId(Long categoriaId);
}