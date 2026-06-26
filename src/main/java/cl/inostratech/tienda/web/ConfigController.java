package cl.inostratech.tienda.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Configuracion publica que el frontend necesita en tiempo de ejecucion.
 * El numero de WhatsApp del vendedor vive como variable de entorno en el
 * backend (no se hardcodea en el frontend): el cliente lo consume desde aqui.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final String whatsappVendedor;

    public ConfigController(@Value("${app.vendedor.whatsapp:}") String whatsappVendedor) {
        this.whatsappVendedor = whatsappVendedor;
    }

    @GetMapping
    public Map<String, String> obtener() {
        return Map.of("whatsappVendedor", whatsappVendedor);
    }
}
