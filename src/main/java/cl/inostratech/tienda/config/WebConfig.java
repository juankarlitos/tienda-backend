package cl.inostratech.tienda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Configuracion web:
 * <ul>
 *   <li>Expone la carpeta local "uploads/" como recursos estaticos en la ruta
 *       "/uploads/**", usada cuando las imagenes se guardan localmente (sin Cloudinary).</li>
 *   <li>CORS: CORS_ALLOWED_ORIGIN acepta una o varias URLs separadas por coma
 *       (ej: tienda y landing). Con una sola URL funciona igual que antes.
 *       Spring Security usa este bean (ver SecurityConfig).</li>
 * </ul>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigins;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(parsearOrigenes(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** "https://a.cl, https://b.cl/" -> ["https://a.cl", "https://b.cl"]. */
    static List<String> parsearOrigenes(String valor) {
        if (valor == null) {
            return List.of();
        }
        return Arrays.stream(valor.split(","))
                .map(String::trim)
                // El navegador envia el Origin sin "/" final.
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .filter(o -> !o.isEmpty())
                .distinct()
                .toList();
    }
}
