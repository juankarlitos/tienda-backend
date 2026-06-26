package cl.inostratech.tienda.pago;

import cl.inostratech.tienda.model.DatosTransferencia;
import cl.inostratech.tienda.pago.dto.DatosTransferenciaDto;
import cl.inostratech.tienda.repository.DatosTransferenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona la unica fila de datos de transferencia del vendedor (id fijo = 1).
 */
@Service
public class PagoService {

    /** Id fijo de la fila de configuracion. */
    private static final Long ID_CONFIG = 1L;

    private final DatosTransferenciaRepository repository;

    public PagoService(DatosTransferenciaRepository repository) {
        this.repository = repository;
    }

    /** Devuelve los datos de transferencia (vacios si aun no se han cargado). */
    @Transactional(readOnly = true)
    public DatosTransferenciaDto obtener() {
        return repository.findById(ID_CONFIG)
                .map(DatosTransferenciaDto::desde)
                .orElseGet(() -> new DatosTransferenciaDto(null, null, null, null, null, null));
    }

    /** Crea o actualiza los datos de transferencia (admin). */
    @Transactional
    public DatosTransferenciaDto actualizar(DatosTransferenciaDto dto) {
        DatosTransferencia datos = repository.findById(ID_CONFIG)
                .orElseGet(() -> DatosTransferencia.builder().id(ID_CONFIG).build());

        datos.setBancoNombre(dto.bancoNombre());
        datos.setTipoCuenta(dto.tipoCuenta());
        datos.setNumeroCuenta(dto.numeroCuenta());
        datos.setRutTitular(dto.rutTitular());
        datos.setNombreTitular(dto.nombreTitular());
        datos.setEmailTitular(dto.emailTitular());

        return DatosTransferenciaDto.desde(repository.save(datos));
    }
}