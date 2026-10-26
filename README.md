# FinTech PAGOFLEX - Prototipo Móvil Académico

## Descripción del Proyecto
PagoFlex es una aplicación móvil nativa para Android desarrollada como solución al caso de cobranza y gestión financiera personal (DSY1105). El prototipo permite a los usuarios centralizar sus compromisos de pago vigentes, simular transacciones mediante medios de pago electrónicos, adjuntar comprobantes fotográficos mediante la cámara del dispositivo, configurar recordatorios locales con notificaciones para evitar atrasos y personalizar su perfil de usuario con fotografía.

## Integrante
* **Desarrollador**: Maicol Rivas

## Planificación del Proyecto
* **Tablero de Gestión (Trello)**: (https://trello.com/invite/b/6adea087f66216983c999b98/ATTI6e2a80af6569258e2d260274315adabaDDE9D2B8/pagoflex)

## Arquitectura y Stack Tecnológico
* **Lenguaje**: Kotlin 2.2 con soporte para Coroutines y Serialization.
* **Arquitectura**: MVVM (Model-View-ViewModel) con flujo unidireccional de datos reactivos mediante StateFlow.
* **Interfaz de Usuario**: Jetpack Compose con componentes Material Design 3, paleta personalizada Fintech (Naranja y Carbón) y soporte Edge-to-Edge.
* **Navegación**: Jetpack Navigation 3 (`androidx.navigation3`) con soporte de destinos desacoplados y BackStack estructurado.
* **Persistencia Local**:
   * Room Database con SQLite para compromisos, pagos, recomendaciones y recordatorios (con soporte de `@Transaction` y `fallbackToDestructiveMigration`).
   * SharedPreferences para la persistencia del perfil de usuario y avatar.
* **Carga de Imágenes**: Coil Compose (`AsyncImage`) para el renderizado eficiente de comprobantes y foto de perfil.

## Funcionalidades Principales
1. **Inicio (Dashboard)**:
   * Resumen financiero en tiempo real: cálculo dinámico del monto total pendiente y fecha del próximo vencimiento en Pesos Chilenos (CLP).
   * Lista clasificada de compromisos financieros por estado (Pendiente, Vencido, Pagado) con contenedores semánticos M3.
   * Acceso directo al perfil de usuario mediante avatar interactivo en la barra superior.
2. **Perfil de Usuario**:
   * Pantalla accesible desde el avatar del Inicio.
   * Nombre de usuario editable con validación desacoplada (campo obligatorio, máximo 40 caracteres con AnimatedVisibility).
   * Captura y asignación de fotografía de perfil en tiempo real usando la cámara nativa.
   * Opción para remover la foto y restablecer el avatar por defecto.
   * Saludo personalizado en el Dashboard sincronizado con el nombre guardado.
3. **Detalle de Compromiso**:
   * Visualización detallada de montos, conceptos, empresa y estado.
   * Captura y visualización de fotografías de comprobantes físicos asociados a cada compromiso.
   * Acceso condicional: habilita el pago si está pendiente o muestra el botón de ver comprobante si ya fue pagado.
4. **Pago Simulado**:
   * Formulario con validación estricta: selección obligatoria del medio de pago y confirmación obligatoria de disclaimer.
   * Transición animada de procesamiento y prevención de pagos duplicados.
   * Generación de comprobante digital con la leyenda explícita "SIMULADO — SIN VALIDEZ".
5. **Recordatorios Locales**:
   * Programación de alertas mediante selectores nativos de fecha (`DatePickerDialog`) y hora (`TimePickerDialog`).
   * Listado de recordatorios activos con opción de cancelación y eliminación atómica.
6. **Historial de Pagos**:
   * Registro cronológico de todas las transacciones simuladas con acceso directo a sus recibos.
7. **Módulo de Recomendación**:
   * Formulario validado para registrar organizaciones sugeridas por el usuario.

## Recursos Nativos del Dispositivo
1. **Cámara del Dispositivo**:
   * Integrada a través del contrato `ActivityResultContracts.TakePicture()` junto con `FileProvider`.
   * Utilizada en dos flujos independientes: captura de comprobantes de pago y captura de fotografía para el perfil de usuario.
   * Almacenamiento seguro en el directorio privado de la aplicación (`filesDir/images` y `filesDir/profile`) sin requerir permisos invasivos a nivel de sistema.
2. **Notificaciones del Sistema**:
   * Implementadas mediante `AlarmManager` y `NotificationManager`.
   * Manejo dinámico del permiso `POST_NOTIFICATIONS` en tiempo de ejecución para Android 13+.
   * Soporte de navegación profunda: al pulsar la notificación, la app abre directamente el detalle del compromiso correspondiente.
   * *Aclaración*: Corresponde a un servicio de alarmas locales; no requiere conexión a servidores push externos.

## Instrucciones de Compilación y Ejecución
1. Clonar el repositorio en Android Studio (Ladybug o superior).
2. Sincronizar las dependencias con Gradle.
3. Seleccionar un emulador con API 33+ (o dispositivo físico con depuración USB activa).
4. Ejecutar la compilación:
   ```bash
   ./gradlew :app:assembleDebug