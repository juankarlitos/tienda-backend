# InostraTech — Backend (API REST)

API REST de la tienda/catálogo **InostraTech**: catálogo de productos, carrito,
pedidos **sin pago en línea** (efectivo o transferencia al juntarse con el
vendedor), aviso por correo al vendedor y panel de administración.

Forma parte de un proyecto de dos repos:
- **Backend** (este): Spring Boot + Java 21 + PostgreSQL.
- **Frontend**: Angular (repo `tienda-frontend`).

## Stack
- Java 21, Spring Boot 3.5
- Spring Web, Data JPA, Security (JWT), Validation, Mail
- PostgreSQL (Neon) en producción · H2 en memoria para desarrollo
- Cloudinary (opcional) para imágenes
- Maven (incluye Maven Wrapper `mvnw`)

## Funcionalidades
- Autenticación JWT con roles `ADMIN` / `USER`.
- CRUD de productos con **múltiples imágenes** (hasta 10) y subida a Cloudinary o local.
- Pedidos: el **total se recalcula en el backend**, se **descuenta stock** y se
  **notifica al vendedor por correo** (HTML) de forma asíncrona.
- Datos de transferencia configurables y endpoint de dashboard (ventas, pedidos).

## Ejecutar en local
Requisitos: **JDK 21**. No necesitas instalar Maven (usa el wrapper).

```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

Por defecto usa **H2 en memoria** (no requiere base de datos) y arranca en
`http://localhost:8080`. Para otro puerto: `--server.port=8081`.

> Nota: el proyecto crea un usuario admin semilla (`admin@inostratech.cl` /
> `admin123` por defecto) y productos de ejemplo al arrancar.

### Activar correo real (opcional, en local)
Define las variables y arranca en la misma terminal:
```powershell
$env:MAIL_USERNAME = "tucorreo@gmail.com"
$env:MAIL_PASSWORD = "tu_app_password_sin_espacios"
.\mvnw.cmd spring-boot:run
```
Si no defines SMTP, el comprobante del pedido se imprime en consola.

## Variables de entorno
Todas las claves/secretos se leen de **variables de entorno** (nunca del repo).
Ver [`.env.example`](.env.example). Resumen:

| Variable | Uso |
|---|---|
| `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL/Neon (si se omite, usa H2) |
| `JWT_SECRET` | Firma de los JWT (>= 32 caracteres) |
| `CORS_ALLOWED_ORIGIN` | URL del frontend (Vercel) |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Gmail + App Password para enviar correos |
| `VENDEDOR_EMAIL`, `VENDEDOR_WHATSAPP` | Datos del vendedor |
| `CLOUDINARY_URL` | Cloudinary (opcional) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Usuario admin semilla |
| `PORT` | Puerto (lo inyecta Render automáticamente) |

## Cambiar de H2 a PostgreSQL (Neon)
No requiere cambios de código. Neon entrega una cadena
`postgresql://USUARIO:CLAVE@HOST/BASE?sslmode=require`. Configúrala así:
```
DATABASE_URL=jdbc:postgresql://HOST/BASE?sslmode=require
DB_USERNAME=USUARIO
DB_PASSWORD=CLAVE
```
Las tablas se crean solas (`spring.jpa.hibernate.ddl-auto=update`).

## Despliegue en Render
El repo incluye un **`Dockerfile`** (recomendado, fija Java 21). En Render:
1. New → **Web Service** → conecta este repo.
2. Runtime: **Docker** (se detecta el `Dockerfile`).
3. Define las variables de entorno (ver tabla y `.env.example`).
4. Deploy. Render expone el puerto vía `PORT` (ya contemplado).

> Alternativa sin Docker: Render también puede construirlo como servicio Java con
> Build `./mvnw clean package -DskipTests` y Start
> `java -jar target/tienda-0.0.1-SNAPSHOT.jar`. El Dockerfile es más fiable.

## API (resumen)
- `POST /api/auth/register`, `POST /api/auth/login`
- `GET /api/productos`, `GET /api/productos/{id}` (público) · POST/PUT/DELETE (ADMIN)
- `POST /api/productos/imagen` y `/imagenes` (ADMIN)
- `POST /api/pedidos` (público) · `GET /api/pedidos`, `PUT /api/pedidos/{id}/estado` (ADMIN)
- `GET /api/pago/transferencia` (público) · `PUT` (ADMIN)
- `GET /api/admin/dashboard` (ADMIN) · `GET /api/config` (público)
