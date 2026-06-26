package cl.inostratech.tienda.producto;

import cl.inostratech.tienda.exception.ReglaNegocioException;
import cl.inostratech.tienda.exception.RecursoNoEncontradoException;
import cl.inostratech.tienda.model.Categoria;
import cl.inostratech.tienda.model.Producto;
import cl.inostratech.tienda.producto.dto.ProductoRequest;
import cl.inostratech.tienda.producto.dto.ProductoResponse;
import cl.inostratech.tienda.repository.CategoriaRepository;
import cl.inostratech.tienda.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Logica de catalogo y administracion de productos.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /** Lista productos activos del catalogo, con filtro opcional por busqueda y/o categoria. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String busqueda, Long categoriaId) {
        List<Producto> productos;
        if (StringUtils.hasText(busqueda)) {
            productos = productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(busqueda.trim());
        } else if (categoriaId != null) {
            productos = productoRepository.findByActivoTrueAndCategoriaId(categoriaId);
        } else {
            productos = productoRepository.findByActivoTrue();
        }
        return productos.stream().map(ProductoResponse::desde).toList();
    }

    /** Detalle de un producto por id. */
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    /** Crea un producto. */
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio(request.precio())
                .stock(request.stock() != null ? request.stock() : 1)
                .imagenes(normalizarImagenes(request.imagenes()))
                .categoria(resolverCategoria(request.categoriaId()))
                .activo(request.activo() == null || request.activo())
                .build();
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    /** Edita un producto existente. */
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscar(id);
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setStock(request.stock() != null ? request.stock() : producto.getStock());
        producto.setImagenes(normalizarImagenes(request.imagenes()));
        producto.setCategoria(resolverCategoria(request.categoriaId()));
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    /**
     * "Elimina" un producto desactivandolo (soft delete). Se desactiva en lugar
     * de borrar fisicamente para no romper los pedidos historicos que lo referencian.
     */
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    /** Limpia la lista de imagenes: quita nulos/vacios y limita a 10. */
    private List<String> normalizarImagenes(List<String> imagenes) {
        if (imagenes == null) {
            return new ArrayList<>();
        }
        return imagenes.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .limit(10)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ReglaNegocioException("No existe la categoria con id " + categoriaId));
    }
}