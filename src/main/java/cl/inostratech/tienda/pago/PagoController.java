package cl.inostratech.tienda.pago;

import cl.inostratech.tienda.pago.dto.DatosTransferenciaDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de datos de pago/transferencia. La lectura es publica; la edicion
 * requiere rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/pago")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /** Datos de transferencia del vendedor (publico). */
    @GetMapping("/transferencia")
    public DatosTransferenciaDto obtener() {
        return pagoService.obtener();
    }

    /** Editar datos de transferencia (ADMIN). */
    @PutMapping("/transferencia")
    public DatosTransferenciaDto actualizar(@RequestBody DatosTransferenciaDto dto) {
        return pagoService.actualizar(dto);
    }
}