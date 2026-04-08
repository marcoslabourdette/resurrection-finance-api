# 🏦 Resurrection Finance - Sistema Bancario Basado en Eventos

> 🇺🇸 [**Read in English**](./README.md)

Un **backend bancario** seguro y escalable desarrollado con Java 21 y Spring Boot, diseñado bajo una **arquitectura distribuida basada en eventos**.

Este sistema gestiona usuarios, cuentas y transacciones garantizando la **consistencia de datos, tolerancia a fallos y actualizaciones en tiempo real** mediante Apache Kafka y WebSockets.

## 💡 Propósito

Los sistemas bancarios tradicionales suelen acoplar el procesamiento de transacciones con las notificaciones. Este proyecto resuelve esa problemática mediante:

* ⚡ **Desacoplamiento** de la lógica central de la comunicación en tiempo real.
* 🔁 Garantía de **consistencia eventual** entre microservicios.
* 📦 Prevención de pérdida de datos mediante el **Patrón Transactional Outbox**.
* 📡 Entrega de actualizaciones instantáneas vía WebSockets.

## 🚀 Stack Tecnológico
*   **Java 21** & **Spring Boot**
*   **Spring Security** (JWT & BCrypt)
*   **PostgreSQL** (Gestionado con Docker & Docker Compose)
*   **Hibernate / JPA** (Persistencia de datos y relaciones)
*   **Apache Kafka** (Streaming de eventos distribuidos y Patrón Outbox)
*   **Spring WebSockets (STOMP)** (Telemetría en tiempo real y notificaciones instantáneas)
*   **Swagger / OpenAPI 3** (Documentación interactiva)
*   **JUnit 5 & Mockito** (Pruebas unitarias para lógica central)
*   **Lombok** (Reducción de código repetitivo)

## 🛡️ Características Principales

### 🔐 Seguridad
* Autenticación JWT.
* Control de Acceso Basado en Roles (ADMIN / USER).
* Validación de propiedad de recursos (Resource Ownership).

### 💸 Transacciones
* Transferencias atómicas utilizando `BigDecimal`.
* Validaciones (prohibición de autotransferencias, verificación de saldo).
* Historial transaccional con paginación escalable.

### 📡 Arquitectura Basada en Eventos
* Implementación del Patrón Transactional Outbox.
* Publicación confiable de eventos hacia Kafka.
* Mecanismo de reintentos automáticos para entregas fallidas.

### ⚡ Actualizaciones en Tiempo Real
* Actualización de saldos vía WebSockets.
* Notificaciones de transacciones en vivo.
* Sincronización de UI basada en eventos.

## 🧱 Arquitectura del Sistema

```text
       Acción del Usuario (Transferencia)


                 |
                 v
      +----------------------+
      |     Banking API      |
      |  (Transacción + DB)  |
      +----------+-----------+


                 |
                 v
         Tabla Outbox (DB)
                 |
                 v
          Procesador Outbox


                 |
                 v
            Topic de Kafka
                 |
                 v
      +----------------------+

      |  Messenger Service   |
      |    (Consumer + WS)   |
      +----------+-----------+
                 |
                 v
        Actualización de UI en Vivo
```

## 🛰️ Resiliencia de Eventos (Patrón Outbox)

Resurrection Finance utiliza una **Arquitectura Distribuida** para asegurar la escalabilidad e integridad de los datos.

### 🏛️ Patrón Transactional Outbox
Para evitar la pérdida de datos entre la **API Bancaria Central (8080)** y el **Servicio de Mensajería (8081)**, implementamos el Patrón Outbox:
1. **Persistencia Atómica**: Las transacciones y los eventos se almacenan en un único commit atómico de la base de datos en PostgreSQL.
2. **Despachador de Eventos**: Un worker interno monitorea la tabla `outbox` y publica los eventos en **Apache Kafka**.
3. **Entrega en Tiempo Real**: El Servicio de Mensajería consume los temas de Kafka y emite alertas al dashboard del socio mediante **WebSockets**.
4. **Eventos Fallidos**: Se reintentan automáticamente para garantizar la entrega.

Esto asegura:
* Cero pérdida de eventos.
* Consistencia fuerte entre servicios.
* Tolerancia a fallos cuando Kafka no está disponible.

## 🛠️ Instalación y Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/marcoslabourdette/resurrection-finance-api
    cd resurrection-finance
    ```
2.  **Variables de Entorno:**
    Copia el archivo de ejemplo y configura tus llaves:
    ```bash
    cp .env.example .env
    ```
3.  **Ejecutar con Docker:**
    ```bash
    docker-compose up -d
    ```
4.  **Acceso a la Infraestructura:**
    * **Swagger UI (API Central)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) para probar la API.
    * **Kafka UI (Telemetría de Eventos)**: [http://localhost:8082](http://localhost:8082) - Para monitorear temas, mensajes y grupos de consumidores en tiempo real.

## 🧪 Pruebas (Testing)
Ejecuta las pruebas unitarias para verificar la lógica central de las transacciones:

```bash
mvn test
```

## 📸 Capturas de Pantalla

### 🔑 Flujo Seguro de Registro y Autenticación
Resurrection Finance cuenta con un proceso de registro robusto y validado en múltiples capas, diseñado para escalabilidad global. El sistema asegura la integridad de los datos mediante una sincronización inmediata de servicios.

#### 🏛️ Paso 1: Registro de Socios
La interfaz de registro proporciona validación de campos en tiempo real y soporte bilingüe (ES/EN). Al enviar el formulario, el sistema ejecuta una transacción atómica para persistir las credenciales del usuario e inicializar su perfil financiero.
![Flujo de Registro de Usuario](assets/onboarding-registration.png)

#### 🏛️ Paso 2: Sincronización de Servicios y Concesión Instantánea
Tras la persistencia exitosa, el **Patrón Transactional Outbox** asegura el despacho de un evento de 'Usuario Creado' a través de **Apache Kafka**. El Servicio de Mensajería consume este evento, activando un bono de bienvenida automatizado y la entrega de notificaciones en tiempo real mediante **WebSockets**.
![Autenticación y Notificación Instantánea](assets/onboarding-login.png)

### 🏛️ Dashboard del Socio (Interfaz en Tiempo Real)
El centro de mando principal para los socios, con actualizaciones de saldo en tiempo real, soporte bilingüe (ES/EN) e historial de transacciones sincronizado.

![Vista General del Dashboard](assets/dashboard-main.png)

### 👑 Centro de Control Administrativo (RBAC e Integridad de Datos)
Panel de control administrativo completo que demuestra el **Control de Acceso Basado en Roles**. 
Incluye monitoreo de socios en tiempo real, anonimización por borrado lógico para cumplimiento de **GDPR** y protocolos de reactivación instantánea. 

Nota sobre la implementación de **Borrado Lógico (Soft-Delete)**: los datos históricos permanecen intactos mientras que la identidad personal se protege según los estándares internacionales de seguridad.

![Vista General del Dashboard Administrativo](assets/admin-dashboard.png)

### 🛰️ Telemetría en Tiempo Real e Integración con Messenger
La **Arquitectura Basada en Eventos** en acción: Notificaciones instantáneas recibidas vía WebSockets después del procesamiento en Kafka.

Este servicio es gestionado por el microservicio independiente: 
👉 [**Resurrection Messenger (Consumidor de Kafka)**](https://github.com/marcoslabourdette/resurrection-messenger-kafka)

![Notificaciones en Tiempo Real](assets/messenger-notification.png)

### 🛠️ Documentación de API (Swagger UI)
Documentación interactiva con endpoints seguros y autorización mediante JWT.

![Vista General de Swagger UI](assets/swagger-ui.png)

### 🔑 Autenticación (Token JWT)
Proceso de inicio de sesión generando un Token Bearer seguro para el socio Liam Gallagher.

![Éxito de Inicio de Sesión](assets/auth-login-success.png)

### 🛡️ Validación de Propiedad de Recursos
Demostración del sistema bloqueando a un usuario autenticado que intenta acceder al historial privado de transacciones de otro socio (403 Forbidden).

![Validación de Propiedad de Recursos](assets/security-resource-ownership.png)

### 👮‍♂️ Seguridad (Control de Acceso)
Prueba de RBAC (Control de Acceso Basado en Roles) bloqueando a usuarios no autorizados.

![Acceso Prohibido por Seguridad](assets/security-forbidden-access.png)

### 👑 Acceso Admin (Lista Completa de Usuarios)
Prueba de privilegios administrativos que permiten el acceso a la base de datos completa de socios.

![Vista de Lista de Usuarios Admin](assets/admin-user-list-view.png)

### 📊 Historial Escalable (Paginación)
Historial de transacciones con metadatos completos de paginación (Página, Tamaño, Elementos Totales).

![Metadatos de Paginación de Transacciones](assets/transaction-pagination-metadata.png)

### 🧪 Pruebas Unitarias (Unit Testing)
Validación de la lógica de negocio central utilizando JUnit 5 y Mockito.

![Pruebas Unitarias Exitosas](assets/unit-tests-passed.png)



