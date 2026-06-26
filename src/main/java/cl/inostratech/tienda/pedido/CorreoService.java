package cl.inostratech.tienda.pedido;

import cl.inostratech.tienda.model.DatosTransferencia;
import cl.inostratech.tienda.model.ItemPedido;
import cl.inostratech.tienda.model.Pedido;
import cl.inostratech.tienda.pedido.dto.PedidoResponse;
import cl.inostratech.tienda.repository.DatosTransferenciaRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Notifica al vendedor el detalle de un pedido con un comprobante HTML
 * (titulo InostraTech, detalle, total destacado y datos de transferencia).
 * Si hay credenciales SMTP (MAIL_USERNAME y VENDEDOR_EMAIL) envia un correo
 * real; si no, imprime un comprobante legible en consola.
 */
@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final DatosTransferenciaRepository transferenciaRepository;
    private final String remitente;
    private final String vendedorEmail;

    public CorreoService(JavaMailSender mailSender,
                         DatosTransferenciaRepository transferenciaRepository,
                         @Value("${spring.mail.username:}") String remitente,
                         @Value("${app.vendedor.email:}") String vendedorEmail) {
        this.mailSender = mailSender;
        this.transferenciaRepository = transferenciaRepository;
        this.remitente = remitente;
        this.vendedorEmail = vendedorEmail;
    }

    private boolean estaCorreoConfigurado() {
        return StringUtils.hasText(remitente) && StringUtils.hasText(vendedorEmail);
    }

    /**
     * Envia (o imprime) el comprobante del pedido al vendedor.
     * Se ejecuta de forma asincrona para no demorar la respuesta del checkout.
     */
    @Async
    public void notificarPedidoAlVendedor(Pedido pedido) {
        String numero = PedidoResponse.formatearNumero(pedido.getId());
        String asunto = "Nuevo pedido " + numero + " - " + pedido.getNombreCliente();
        DatosTransferencia transferencia = transferenciaRepository.findById(1L).orElse(null);

        if (!estaCorreoConfigurado()) {
            log.info("\n{}", comprobanteTexto(pedido, numero, transferencia));
            log.info("Correo NO enviado: falta configurar SMTP (MAIL_USERNAME/MAIL_PASSWORD) " +
                    "y/o VENDEDOR_EMAIL. El comprobante quedo listo arriba; conecta un App " +
                    "Password de Gmail para enviarlo de verdad.");
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(vendedorEmail);
            helper.setSubject(asunto);
            helper.setText(comprobanteHtml(pedido, numero, transferencia), true);
            mailSender.send(mensaje);
            log.info("Correo del pedido {} enviado a {}", numero, vendedorEmail);
        } catch (Exception ex) {
            // El pedido NO debe perderse por un fallo de correo: se registra y sigue.
            log.error("No se pudo enviar el correo del pedido {}: {}", numero, ex.getMessage());
            log.info("\n{}", comprobanteTexto(pedido, numero, transferencia));
        }
    }

    // ----------------------------------------------------------------------
    // Comprobante en HTML (email)
    // ----------------------------------------------------------------------
    private String comprobanteHtml(Pedido p, String numero, DatosTransferencia t) {
        StringBuilder filas = new StringBuilder();
        for (ItemPedido item : p.getItems()) {
            filas.append("""
                    <tr>
                      <td style="padding:10px 8px;border-bottom:1px solid #eee;">%s</td>
                      <td style="padding:10px 8px;border-bottom:1px solid #eee;text-align:center;">%d</td>
                      <td style="padding:10px 8px;border-bottom:1px solid #eee;text-align:right;">%s</td>
                      <td style="padding:10px 8px;border-bottom:1px solid #eee;text-align:right;font-weight:600;">%s</td>
                    </tr>
                    """.formatted(
                    escapar(item.getNombreProducto()),
                    item.getCantidad(),
                    clp(item.getPrecioUnitario()),
                    clp(item.getSubtotal())));
        }

        String bloqueTransferencia = "";
        if (t != null && p.getMetodoPago() != null
                && "TRANSFERENCIA".equals(p.getMetodoPago().name())) {
            bloqueTransferencia = """
                    <div style="margin-top:20px;padding:16px;background:#f5f3ff;border:1px solid #ddd6fe;border-radius:10px;">
                      <h3 style="margin:0 0 10px;color:#4338ca;font-size:15px;">Datos de transferencia</h3>
                      <table style="width:100%%;font-size:14px;color:#333;border-collapse:collapse;">
                        %s%s%s%s%s%s
                      </table>
                    </div>
                    """.formatted(
                    filaDato("Banco", t.getBancoNombre()),
                    filaDato("Tipo de cuenta", t.getTipoCuenta()),
                    filaDato("N° de cuenta", t.getNumeroCuenta()),
                    filaDato("RUT", t.getRutTitular()),
                    filaDato("Titular", t.getNombreTitular()),
                    filaDato("Email", t.getEmailTitular()));
        }

        String comentario = StringUtils.hasText(p.getComentario())
                ? "<p style=\"margin:6px 0 0;color:#555;\"><strong>Comentario:</strong> " + escapar(p.getComentario()) + "</p>"
                : "";

        return """
                <div style="font-family:Arial,Helvetica,sans-serif;background:#f3f4f6;padding:24px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;box-shadow:0 4px 14px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#7c3aed,#4f46e5);padding:24px;color:#fff;">
                      <h1 style="margin:0;font-size:22px;">InostraTech</h1>
                      <p style="margin:4px 0 0;opacity:.9;">Comprobante de pedido</p>
                    </div>
                    <div style="padding:24px;">
                      <p style="margin:0 0 4px;color:#111;font-size:16px;">Has recibido un nuevo pedido <strong>%s</strong></p>
                      <p style="margin:0 0 16px;color:#777;font-size:13px;">Fecha: %s</p>

                      <table style="width:100%%;border-collapse:collapse;font-size:14px;color:#333;">
                        <thead>
                          <tr style="background:#fafafa;text-align:left;">
                            <th style="padding:10px 8px;border-bottom:2px solid #eee;">Producto</th>
                            <th style="padding:10px 8px;border-bottom:2px solid #eee;text-align:center;">Cant.</th>
                            <th style="padding:10px 8px;border-bottom:2px solid #eee;text-align:right;">Precio</th>
                            <th style="padding:10px 8px;border-bottom:2px solid #eee;text-align:right;">Subtotal</th>
                          </tr>
                        </thead>
                        <tbody>%s</tbody>
                      </table>

                      <div style="margin-top:16px;padding:14px 16px;background:#4f46e5;border-radius:10px;color:#fff;display:flex;">
                        <table style="width:100%%;color:#fff;font-size:18px;">
                          <tr>
                            <td style="font-weight:600;">TOTAL</td>
                            <td style="text-align:right;font-weight:800;">%s</td>
                          </tr>
                        </table>
                      </div>

                      <div style="margin-top:20px;padding:16px;background:#f9fafb;border:1px solid #eee;border-radius:10px;">
                        <h3 style="margin:0 0 10px;font-size:15px;color:#111;">Datos del cliente</h3>
                        <p style="margin:2px 0;color:#333;font-size:14px;"><strong>Nombre:</strong> %s</p>
                        <p style="margin:2px 0;color:#333;font-size:14px;"><strong>Teléfono:</strong> %s</p>
                        <p style="margin:2px 0;color:#333;font-size:14px;"><strong>Email:</strong> %s</p>
                        <p style="margin:2px 0;color:#333;font-size:14px;"><strong>Método de pago:</strong> %s</p>
                        %s
                      </div>

                      %s

                      <p style="margin:22px 0 0;color:#999;font-size:12px;text-align:center;">
                        Este correo fue generado automáticamente por la tienda InostraTech.
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(
                numero,
                p.getFechaCreacion() != null ? p.getFechaCreacion().format(FECHA) : "-",
                filas.toString(),
                clp(p.getTotal()),
                escapar(p.getNombreCliente()),
                escapar(p.getTelefonoCliente()),
                StringUtils.hasText(p.getEmailCliente()) ? escapar(p.getEmailCliente()) : "-",
                p.getMetodoPago(),
                comentario,
                bloqueTransferencia);
    }

    private String filaDato(String etiqueta, String valor) {
        return "<tr><td style=\"padding:5px 0;color:#777;\">" + etiqueta + "</td>"
                + "<td style=\"padding:5px 0;text-align:right;font-weight:600;\">"
                + (StringUtils.hasText(valor) ? escapar(valor) : "—") + "</td></tr>";
    }

    // ----------------------------------------------------------------------
    // Comprobante en texto (consola, cuando no hay SMTP)
    // ----------------------------------------------------------------------
    private String comprobanteTexto(Pedido p, String numero, DatosTransferencia t) {
        StringBuilder sb = new StringBuilder();
        sb.append("===================== COMPROBANTE DE PEDIDO =====================\n");
        sb.append("InostraTech\n");
        sb.append("Pedido: ").append(numero).append("\n");
        sb.append("Fecha:  ").append(p.getFechaCreacion() != null ? p.getFechaCreacion().format(FECHA) : "-").append("\n");
        sb.append("----------------------------------------------------------------\n");
        for (ItemPedido item : p.getItems()) {
            sb.append(String.format("  %2dx %-28s %10s = %12s%n",
                    item.getCantidad(),
                    recortar(item.getNombreProducto()),
                    clp(item.getPrecioUnitario()),
                    clp(item.getSubtotal())));
        }
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("  TOTAL: %s%n", clp(p.getTotal())));
        sb.append("----------------------------------------------------------------\n");
        sb.append("Cliente:  ").append(p.getNombreCliente()).append("\n");
        sb.append("Telefono: ").append(p.getTelefonoCliente()).append("\n");
        if (StringUtils.hasText(p.getEmailCliente())) sb.append("Email:    ").append(p.getEmailCliente()).append("\n");
        sb.append("Pago:     ").append(p.getMetodoPago()).append("\n");
        if (StringUtils.hasText(p.getComentario())) sb.append("Comentario: ").append(p.getComentario()).append("\n");
        if (t != null && p.getMetodoPago() != null && "TRANSFERENCIA".equals(p.getMetodoPago().name())) {
            sb.append("----------------------------------------------------------------\n");
            sb.append("Datos de transferencia:\n");
            sb.append("  Banco:   ").append(valor(t.getBancoNombre())).append("\n");
            sb.append("  Cuenta:  ").append(valor(t.getTipoCuenta())).append(" ").append(valor(t.getNumeroCuenta())).append("\n");
            sb.append("  RUT:     ").append(valor(t.getRutTitular())).append("\n");
            sb.append("  Titular: ").append(valor(t.getNombreTitular())).append("\n");
        }
        sb.append("================================================================");
        return sb.toString();
    }

    // ----------------------------------------------------------------------
    // Utilidades
    // ----------------------------------------------------------------------
    private String clp(BigDecimal monto) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "CL"));
        simbolos.setGroupingSeparator('.');
        DecimalFormat formato = new DecimalFormat("#,##0", simbolos);
        return "$ " + formato.format(monto != null ? monto : BigDecimal.ZERO);
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String recortar(String s) {
        if (s == null) return "";
        return s.length() > 28 ? s.substring(0, 25) + "..." : s;
    }

    private String valor(String s) {
        return StringUtils.hasText(s) ? s : "-";
    }
}
