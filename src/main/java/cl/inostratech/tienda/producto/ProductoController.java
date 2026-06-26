package cl.inostratech.tienda.producto;

import cl.inostratech.tienda.producto.dto.ProductoRequest;
import cl.inostratech.tienda.producto.dto.ProductoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de productos. Las lecturas son publicas; la administracion (crear,
 * editar, eliminar, subir imagen) requiere rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final AlmacenamientoImagenService almacenamientoImagenService;

    public ProductoController(ProductoService productoService,
                              AlmacenamientoImagenService almacenamientoImagenService) {
        this.productoService = productoService;
        this.almacenamientoImagenService = almacenamientoImagenService;
    }

    /** Catalogo publico (filtro opcional por busqueda y/o categoria). */
    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Long categoriaId) {
        return productoService.listar(busqueda, categoriaId);
    }

    /** Detalle de un producto. */
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    /** Crea un producto (ADMIN). */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    /** Edita un producto (ADMIN). */
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    /** Elimina (desactiva) un producto (ADMIN). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** Sube una imagen y devuelve su URL (ADMIN). */
    @PostMapping("/imagen")
    public Map<String, String> subirImagen(@RequestParam("archivo") MultipartFile archivo) {
        String url = almacenamientoImagenService.subir(archivo);
        return Map.of("url", url);
    }

    /** Sube varias imagenes (hasta 10) y devuelve sus URLs (ADMIN). */
    @PostMapping("/imagenes")
    public Map<String, List<String>> subirImagenes(@RequestParam("archivos") MultipartFile[] archivos) {
        List<String> urls = almacenamientoImagenService.subirVarias(archivos);
        return Map.of("urls", urls);
    }
}