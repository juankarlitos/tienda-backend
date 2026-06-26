package cl.inostratech.tienda.model;

/**
 * Roles de usuario del sistema.
 * <ul>
 *   <li>{@link #ADMIN}: gestiona productos, pedidos y configuracion.</li>
 *   <li>{@link #USER}: usuario comprador del catalogo.</li>
 * </ul>
 */
public enum Rol {
    ADMIN,
    USER
}