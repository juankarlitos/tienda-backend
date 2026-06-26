package cl.inostratech.tienda.producto;

import cl.inostratech.tienda.exception.ReglaNegocioException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sube imagenes de productos. Si hay credenciales de Cloudinary (variable de
 * entorno CLOUDINARY_URL) las usa; si no, guarda el archivo localmente en la
 * carpeta "uploads/" y devuelve una URL servida por la propia aplicacion, para
 * no bloquear las pruebas sin cuenta de Cloudinary.
 */
@Service
public class AlmacenamientoImagenService {

    private static final Logger log = LoggerFactory.getLogger(AlmacenamientoImagenService.class);
    private static final Path CARPETA_LOCAL = Paths.get("uploads");

    private final String cloudinaryUrl;
    private Cloudinary cloudinary;

    public AlmacenamientoImagenService(@Value("${cloudinary.url:}") String cloudinaryUrl) {
        this.cloudinaryUrl = cloudinaryUrl;
        if (estaCloudinaryConfigurado()) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
            log.info("Almacenamiento de imagenes: Cloudinary");
        } else {
            log.info("Almacenamiento de imagenes: LOCAL (carpeta uploads/). " +
                    "Defina CLOUDINARY_URL para usar Cloudinary.");
        }
    }

    private boolean estaCloudinaryConfigurado() {
        return cloudinaryUrl != null && cloudinaryUrl.startsWith("cloudinary://");
    }

    /**
     * Sube la imagen y devuelve su URL publica.
     */
    public String subir(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Debe adjuntar un archivo de imagen");
        }
        try {
            return estaCloudinaryConfigurado() ? subirACloudinary(archivo) : guardarLocal(archivo);
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo procesar la imagen: " + e.getMessage());
        }
    }

    /**
     * Sube varias imagenes (hasta 10) y devuelve sus URLs en el mismo orden.
     */
    public List<String> subirVarias(MultipartFile[] archivos) {
        if (archivos == null || archivos.length == 0) {
            throw new ReglaNegocioException("Debe adjuntar al menos una imagen");
        }
        if (archivos.length > 10) {
            throw new ReglaNegocioException("Maximo 10 imagenes por producto");
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            if (archivo != null && !archivo.isEmpty()) {
                urls.add(subir(archivo));
            }
        }
        return urls;
    }

    private String subirACloudinary(MultipartFile archivo) throws IOException {
        Map<?, ?> resultado = cloudinary.uploader()
                .upload(archivo.getBytes(), ObjectUtils.asMap("folder", "tienda"));
        return String.valueOf(resultado.get("secure_url"));
    }

    private String guardarLocal(MultipartFile archivo) throws IOException {
        Files.createDirectories(CARPETA_LOCAL);
        String nombreArchivo = UUID.randomUUID() + extension(archivo.getOriginalFilename());
        Path destino = CARPETA_LOCAL.resolve(nombreArchivo);
        Files.write(destino, archivo.getBytes());

        // URL absoluta basada en el host/puerto de la peticion actual.
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(nombreArchivo)
                .toUriString();
    }

    private String extension(String nombreOriginal) {
        if (nombreOriginal == null) {
            return "";
        }
        int punto = nombreOriginal.lastIndexOf('.');
        return punto >= 0 ? nombreOriginal.substring(punto) : "";
    }
}