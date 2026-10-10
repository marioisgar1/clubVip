# clubVip (portal web)

PrimeFaces 14 (`jakarta`) · Jakarta Faces 4 (Mojarra) · CDI · Jakarta EE 10 · Java 17 · Maven WAR.
Portal para negocios afiliados (administrador y personal) y para el back office de Club VIP.
Consume el backend REST `clubVipServices` (contrato en `../ENDPOINTS.md`).

## Requisitos
- JDK 17, Maven 3.9+.
- Tomcat 10.1+ (Servlet 6 / Jakarta EE 10). Tomcat no trae Faces ni CDI: copia a `$CATALINA_HOME/lib`
  (o empaqueta) `jakarta.faces-4.0.x.jar` (Mojarra) y `weld-servlet-shaded-5.x.jar` como en Leonisa.
- Backend `clubVipServices` corriendo en `http://localhost:8884`.

## Configuración
`src/main/java/com/prolog/clubvip/constant/Constants.java`:
- `BASE_URL` — URL del backend (por defecto `http://localhost:8884/api/v1/`).
- `GATEWAY_USER` / `GATEWAY_PASSWORD` — Basic de pasarela (`developer / Club.Vip.Dev`), deben coincidir con el backend.
- `CLUBVIP_WHATSAPP` — número de soporte que aparece en el login (`wa.me`).
- `TIMEZONE` — zona para cortar "hoy" y formatear fechas (`America/Mexico_City`).

## Compilar y desplegar
```bash
cd clubVip
mvn -q -o package                       # genera target/clubVip.war
cp target/clubVip.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh
```
Abre `http://localhost:8080/clubVip/login.xhtml`.

## Usuarios de prueba (seed de `database/schema.sql`)
| Usuario | Contraseña | Perfil | Aterriza en |
|---|---|---|---|
| `operador` | `Operador2026` | CLUBVIP_OPERATOR | `/pages/affiliate.xhtml` |
| `admin` | `ClubVip2026` | BUSINESS_ADMIN (Studio Lumière) | `/pages/dashboard.xhtml` |
| `ana` | `Empleado2026` | EMPLOYEE | `/pages/myAppointments.xhtml` |
| `leonisa` | `Leonisa2026` | BUSINESS_ADMIN (Embajadora Leonisa, negocio solo ventas) | `/pages/sale.xhtml` |
| `vendedora` | `Vendedora2026` | SELLER (Embajadora Leonisa) | `/pages/sale.xhtml` |

Los clientes (perfil 4) no pueden entrar al portal: usan la app móvil.
Un negocio nuevo se da de alta desde `register.xhtml` (público) y entra directo al dashboard.

## Módulo de punto de venta (portado de Leonisa Ambassador)
Se activa por negocio con `moduleSales` (switch en "Mi negocio"); si `moduleAppointments` está apagado el inicio
del administrador es el punto de venta y el dashboard muestra la variante de ventas (tickets del día, cartera).
El perfil `SELLER` (5) vende, devuelve, cambia y cobra; no ve ganancias ni catálogos de alta.

| Ruta (`outcome_web`) | Bean | Qué hace |
|---|---|---|
| `/pages/sale.xhtml` | `SaleMb` | Venta: lector USB (código + Enter agrega al carrito), búsqueda por descripción, precio regateado por línea con descuento, forma de pago y lugar; la forma de pago de crédito abre el armado del plan (clienta por celular con alta en mostrador, enganche, pagos, frecuencia, primera fecha, calendario de `credit/previewSchedule` editable). Resultado con folio, total, avisos de sobreventa y liga de WhatsApp con el calendario. |
| `/pages/saleReturn.xhtml` | `SaleReturnMb` | Devoluciones: ventas por rango/folio, detalle y cancelación con motivo. |
| `/pages/saleExchange.xhtml` | `SaleExchangeMb` | Cambio de mercancía en tres pasos (ticket, lo que regresa, lo que se lleva); solo se cobra la diferencia. |
| `/pages/collection.xhtml` | `CollectionMb` | Cobranza: créditos con saldo, detalle con pagos e historial, cobro total/parcial/abono libre, deshacer cobro, recordatorio por WhatsApp. |
| `/pages/salesHistory.xhtml` | `SalesHistoryMb` | Historial de ventas por rango con detalle (el vendedor solo ve las suyas; lo impone el backend). |
| `/pages/family.xhtml` `brand.xhtml` `productModel.xhtml` `paymentMethod.xhtml` `saleLocation.xhtml` `locationExpense.xhtml` | `FamilyMb` `BrandMb` `ProductModelMb` `PaymentMethodMb` `SaleLocationMb` `LocationExpenseMb` | Catálogos del módulo (forma de pago: comisión % y switch de crédito; gasto de lugar: rango de fechas y costo). |
| `/pages/reportConsulted.xhtml` `reportBestSelling.xhtml` `reportProfit.xhtml` `reportPortfolio.xhtml` `reportBehavior.xhtml` `reportStockDiff.xhtml` | `Report*Mb` | Más consultados, más vendidos, ventas y ganancia real (con desglose por lugar y gráfica), cartera vencida, cumplimiento de pago por clienta, diferencias de inventario. |

`product.xhtml` incorpora código de barras, familia, marca, modelo, talla y color. Textos del módulo con prefijo `pos.*`
en ambos bundles; estilos `pos-*` al final de `clubvip.css`. Las fechas "día" (`yyyy-MM-dd`) se pintan con
`fmtDay(Date)` de `BaseMb` y las marcas de tiempo con `fmtStamp(Date)` (zona del negocio).

## Arquitectura
- `constant/Constants` — configuración.
- `rest/RestClient` — HttpURLConnection + Gson. Agrega Basic, `X-Audit-User`, `Accept-Language`, `X-Timezone`.
  `get(path, type, userId, locale)` · `post(path, payload, userId, locale)` → `RestResponse` ·
  `post(path, payload, type, userId, locale)` → objeto o `RestException` · `delete(...)` · `extractErrors(body)` · `encode(value)`.
- `security/AuthenticationFilter` — protege `/pages/*`; `dashboard`, `home` y `accessDenied` siempre; el resto por `outcomeWeb` del menú.
- `view/SessionMb` (`@SessionScoped`) — usuario, idioma, tema (saga/vela/arya), menú (`menu.group.<grupo>` + `name` del permiso son claves i18n)
  y helpers `isOperator()/isAdmin()/isEmployee()/isSeller()`, `isAppointmentsEnabled()/isSalesEnabled()/isSalesHome()`,
  `getBusinessName()`, `fmtDay()`, `fmtMoney()`, `today()`, `text(key)`.
- `view/*Mb` (`@ViewScoped`) — un bean por página. Patrón en `DashboardMb` / `RegisterMb`.
- `vo/*` — espejo campo a campo de los VOs del backend (sin anotaciones de validación).
- `WEB-INF/template.xhtml` — layout maestro (topbar tinta + sidebar 240px + contenido). `resources/css/clubvip.css` — temas `--vip-*`.
- `i18n/messages_es.properties` / `messages_en.properties` — todos los textos.

## Identidad
Oro `#F0C756` (primario) · oro profundo `#B8902F` · oro claro `#E4D681` · tinta `#0B0B0C`.
Títulos "Playfair Display", texto "Inter" (Google Fonts con fallback local). Logo blanco `resources/img/logo.svg` sobre barra de tinta.

## Página de inicio (landing)

`index.xhtml` es ahora la página de bienvenida (`welcome-file`): landing comercial bilingüe basada en el requerimiento, con acceso a `login.xhtml` y `register.xhtml`. Estilos en `resources/css/landing.css`; textos en las claves `landing.*` de los bundles. Capturas en `../docs/screenshots/`.
