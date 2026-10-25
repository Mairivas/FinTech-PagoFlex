# FinTech PAGOFLEX - Prototipo Móvil Académico

## Descripción del Proyecto
PagoFlex es una aplicación móvil nativa para Android desarrollada como solución al caso de cobranza y gestión financiera personal. El prototipo permite a los usuarios centralizar sus compromisos de pago vigentes, simular transacciones mediante medios de pago electrónicos, adjuntar comprobantes fotográficos mediante la cámara del dispositivo y configurar recordatorios locales con notificaciones para evitar atrasos.

## Integrante
* **Desarrollador**: Maicol Rivas

## Arquitectura y Stack Tecnológico
* **Lenguaje**: Kotlin 2.2
* **Arquitectura**: MVVM (Model-View-ViewModel) con flujo unidireccional de datos mediante StateFlow.
* **Interfaz de Usuario**: Jetpack Compose con componentes Material Design 3 y tema personalizado.
* **Persistencia Local**: Room Database con SQLite, fallbackToDestructiveMigration y transacciones atómicas (@Transaction).
* **Navegación**: Jetpack Navigation 3 para gestión de destinos y paso de argumentos.
* **Carga de Imágenes**: Coil Compose para renderizado de fotografías locales.

## Funcionalidades Principales
1. **Inicio (Dashboard)**:
   * Métricas en tiempo real: cálculo dinámico del monto total pendiente y fecha del próximo vencimiento en Pesos Chilenos (CLP).
   * Lista clasificada de compromisos financieros por estado (Pendiente, Vencido, Pagado).
2. **Detalle de Compromiso**:
   * Visualización completa de montos, conceptos y estado del compromiso.
   * Integración de cámara nativa para fotografiar y adjuntar comprobantes físicos a cada registro.
   * Acceso condicional al flujo de pago o consulta de comprobante emitido.
3. **Pago Simulado**:
   * Formulario con validación desacoplada: selección obligatoria de método de pago y confirmación de disclaimer.
   * Transición animada de procesamiento y prevención de pagos duplicados.
   * Generación de comprobante digital con la leyenda explícita "SIMULADO — SIN VALIDEZ".
4. **Recordatorios Locales**:
   * Programación de alarmas mediante selectores independientes de fecha y hora.
   * Lista de recordatorios activos con opción de cancelación individual.
5. **Historial de Pagos**:
   * Registro cronológico de transacciones simuladas con acceso directo a sus recibos.
6. **Módulo de Recomendación**:
   * Formulario validado para registrar organizaciones sugeridas por el usuario.

## Recursos Nativos del Dispositivo
1. **Notificaciones del Sistema**:
   * Implementadas mediante `AlarmManager` y `NotificationManager`.
   * Manejo dinámico del permiso `POST_NOTIFICATIONS` en tiempo de ejecución para Android 13+.
   * Soporte de navegación profunda: al presionar la notificación, la app abre directamente el detalle del compromiso asociado.
   * *Aclaración*: Corresponde a un servicio de alarmas locales; no requiere conexión a servicios push externos.
2. **Cámara del Dispositivo**:
   * Integrada a través del contrato `ActivityResultContracts.TakePicture()` junto con `FileProvider`.
   * Permite la captura segura de fotografías almacenadas en el directorio privado de la aplicación sin exigir permisos invasivos a nivel de sistema.

## Instrucciones de Compilación y Ejecución
1. Clonar el repositorio en Android Studio (Ladybug o superior).
2. Sincronizar las dependencias con Gradle.
3. Seleccionar un emulador con API 33+ (o dispositivo físico con depuración USB activa).
4. Ejecutar la tarea de compilación:
   ```bash
   ./gradlew :app:assembleDebug