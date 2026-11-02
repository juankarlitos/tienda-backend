package cl.inostratech.tienda.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que los endpoints nuevos sean publicos/ADMIN segun corresponde y
 * que los permisos de la tienda sigan iguales.
 */
@SpringBootTest(properties = {
        "resend.api-key=",
        "app.cors.allowed-origin=https://tienda.example.cl, https://landing.example.cl/"
})
@AutoConfigureMockMvc
class PermisosEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------------- Landing (nuevos) ----------------

    @Test
    void crearCotizacion_esPublico() throws Exception {
        mockMvc.perform(post("/api/cotizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.10")
                        .content("""
                                {"nombre":"Juan Pérez","telefono":"+56912345678",
                                 "tipoServicio":"TIENDA_ONLINE","mensaje":"Quiero una tienda online"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void crearCotizacion_validaCampos() throws Exception {
        mockMvc.perform(post("/api/cotizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.11")
                        .content("""
                                {"nombre":"J4","telefono":"12345","email":"no-es-email",
                                 "tipoServicio":"OTRO","mensaje":"corto"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.telefono").exists())
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.mensaje").exists());
    }

    @Test
    void crearCotizacion_superaLimiteResponde429() throws Exception {
        String body = """
                {"nombre":"Ana","telefono":"987654321","tipoServicio":"OTRO","mensaje":"Mensaje de prueba"}
                """;
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/cotizaciones").contentType(MediaType.APPLICATION_JSON)
                            .header("X-Forwarded-For", "203.0.113.12").content(body))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/cotizaciones").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.12").content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void registrarEvento_esPublico() throws Exception {
        mockMvc.perform(post("/api/metricas/evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", "203.0.113.20")
                        .content("{\"tipo\":\"VISITA\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void adminLanding_sinTokenEsRechazado() throws Exception {
        mockMvc.perform(get("/api/admin/cotizaciones")).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/admin/cotizaciones/1/estado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CERRADA\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/metricas/resumen")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminLanding_conRolUserEsRechazado() throws Exception {
        mockMvc.perform(get("/api/admin/cotizaciones")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/metricas/resumen")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminLanding_conRolAdminPermitido() throws Exception {
        mockMvc.perform(get("/api/admin/cotizaciones").param("estado", "NUEVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());
        mockMvc.perform(get("/api/admin/metricas/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ultimos7Dias.VISITA").exists())
                .andExpect(jsonPath("$.ultimos30Dias.CLICK_PRECIOS").exists());
        mockMvc.perform(patch("/api/admin/cotizaciones/999999/estado")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"CERRADA\"}"))
                .andExpect(status().isNotFound());
    }

    // ---------------- Tienda (sin cambios) ----------------

    @Test
    void tienda_endpointsPublicosSiguenPublicos() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/config")).andExpect(status().isOk());
    }

    @Test
    void tienda_endpointsAdminSiguenProtegidos() throws Exception {
        mockMvc.perform(get("/api/pedidos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void tienda_endpointsAdminRechazanRolUser() throws Exception {
        mockMvc.perform(get("/api/pedidos")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isForbidden());
    }

    // ---------------- CORS (varias URLs) ----------------

    @Test
    void cors_aceptaCadaOrigenConfigurado() throws Exception {
        for (String origen : new String[]{"https://tienda.example.cl", "https://landing.example.cl"}) {
            mockMvc.perform(options("/api/cotizaciones")
                            .header("Origin", origen)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origen));
        }
    }

    @Test
    void cors_rechazaOrigenNoConfigurado() throws Exception {
        mockMvc.perform(options("/api/cotizaciones")
                        .header("Origin", "https://malicioso.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
