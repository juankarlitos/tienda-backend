package cl.inostratech.tienda.admin;

import cl.inostratech.tienda.admin.dto.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Panel de administracion: metricas de ventas y pedidos. Requiere rol ADMIN
 * (ruta bajo /api/admin/**, ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return dashboardService.obtener();
    }
}