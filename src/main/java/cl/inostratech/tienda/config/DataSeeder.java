package cl.inostratech.tienda.config;

import cl.inostratech.tienda.model.Categoria;
import cl.inostratech.tienda.model.DatosTransferencia;
import cl.inostratech.tienda.model.Producto;
import cl.inostratech.tienda.model.Rol;
import cl.inostratech.tienda.model.Usuario;
import cl.inostratech.tienda.repository.CategoriaRepository;
import cl.inostratech.tienda.repository.DatosTransferenciaRepository;
import cl.inostratech.tienda.repository.ProductoRepository;
import cl.inostratech.tienda.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Siembra datos iniciales al arrancar (solo si no existen):
 * <ul>
 *   <li>Usuario ADMIN, configurable por app.admin.* / variables de entorno.</li>
 *   <li>Categorias y productos de ejemplo para probar el catalogo de inmediato.</li>
 *   <li>Datos de transferencia por defecto.</li>
 * </ul>
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final DatosTransferenciaRepository datosTransferenciaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nombre}")
    private String adminNombre;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      CategoriaRepository categoriaRepository,
                      ProductoRepository productoRepository,
                      DatosTransferenciaRepository datosTransferenciaRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.datosTransferenciaRepository = datosTransferenciaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        sembrarAdmin();
        sembrarCatalogo();
        sembrarDatosTransferencia();
    }

    private void sembrarAdmin() {
        if (usuarioRepository.existsByEmail(adminEmail)) {
            return;
        }
        Usuario admin = Usuario.builder()
                .nombre(adminNombre)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
        usuarioRepository.save(admin);
        log.info("Usuario ADMIN semilla creado: {} (cambia la contrasena en produccion)", adminEmail);
    }

    private void sembrarCatalogo() {
        if (productoRepository.count() > 0) {
            return;
        }

        Categoria tecnologia = categoriaRepository.save(
                Categoria.builder().nombre("Tecnologia").build());
        Categoria hogar = categoriaRepository.save(
                Categoria.builder().nombre("Hogar").build());

        // Producto con varias fotos (galeria) para probar el visor de imagenes.
        crearProducto("Audifonos Bluetooth", "Audifonos inalambricos con cancelacion de ruido y estuche de carga.",
                new BigDecimal("19990"), 10, tecnologia,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=600",
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600",
                "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600");

        crearProducto("Mouse Gamer RGB", "Mouse ergonomico con 7 botones programables e iluminacion RGB.",
                new BigDecimal("12990"), 15, tecnologia,
                "https://images.unsplash.com/photo-1527814050087-3793815479db?w=600",
                "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600");

        crearProducto("Lampara LED de Escritorio", "Lampara con brazo flexible, 3 tonos de luz y puerto USB.",
                new BigDecimal("15990"), 8, hogar,
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600");

        crearProducto("Set de Tazas Ceramica", "Juego de 4 tazas de ceramica artesanal de 350ml.",
                new BigDecimal("9990"), 20, hogar,
                "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=600");

        log.info("Catalogo de ejemplo creado: {} productos", productoRepository.count());
    }

    private void crearProducto(String nombre, String descripcion, BigDecimal precio,
                               int stock, Categoria categoria, String... imagenes) {
        productoRepository.save(Producto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .stock(stock)
                .imagenes(new ArrayList<>(List.of(imagenes)))
                .categoria(categoria)
                .activo(true)
                .build());
    }

    private void sembrarDatosTransferencia() {
        if (datosTransferenciaRepository.existsById(1L)) {
            return;
        }
        datosTransferenciaRepository.save(DatosTransferencia.builder()
                .id(1L)
                .bancoNombre("Banco Estado")
                .tipoCuenta("Cuenta RUT")
                .numeroCuenta("12345678")
                .rutTitular("12.345.678-9")
                .nombreTitular("Juan Carlos Inostroza")
                .emailTitular("jc.inostrozach@gmail.com")
                .build());
        log.info("Datos de transferencia por defecto creados");
    }
}