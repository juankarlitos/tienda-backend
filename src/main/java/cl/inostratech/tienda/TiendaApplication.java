package cl.inostratech.tienda;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class TiendaApplication {

	/** Zona horaria de la aplicacion: Chile continental. */
	private static final String ZONA_HORARIA = "America/Santiago";

	/**
	 * Fija la zona horaria por defecto de la JVM a la de Chile, de modo que todas
	 * las fechas/horas (fecha del pedido, comprobante, correo) salgan en hora
	 * local y no en UTC. Funciona igual en local y en Render.
	 */
	@PostConstruct
	public void configurarZonaHoraria() {
		TimeZone.setDefault(TimeZone.getTimeZone(ZONA_HORARIA));
	}

	public static void main(String[] args) {
		// Tambien se fija aqui (antes de arrancar el contexto) por seguridad.
		TimeZone.setDefault(TimeZone.getTimeZone(ZONA_HORARIA));
		SpringApplication.run(TiendaApplication.class, args);
	}

}
