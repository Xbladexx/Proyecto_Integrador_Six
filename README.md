# SIX - Sistema Integrado de Gestión Empresarial

SIX es un sistema integral de gestión empresarial desarrollado con Spring Boot que permite administrar inventarios, ventas, clientes y productos de manera eficiente a través de una interfaz intuitiva y funcional.

## Características Principales

- **Gestión Completa de Inventario:** Control en tiempo real, movimientos de stock y alertas automáticas
- **Administración de Productos:** Catálogo completo con categorización y variantes de productos
- **Procesamiento de Ventas:** Registro de transacciones, historial y detalles de ventas
- **Gestión de Clientes:** Base de datos completa con información de contacto e historial de compras
- **Sistema de Usuarios:** Registro, autenticación y administración de permisos
- **Alertas Automáticas:** Notificaciones para niveles bajos de inventario

## Tecnologías Utilizadas

- **Backend:** Java 11+, Spring Boot
- **Base de Datos:** MySQL 8.0
- **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript
- **Seguridad:** Spring Security
- **ORM:** Hibernate, Spring Data JPA
- **Gestión de Dependencias:** Maven

## Requisitos Previos

- Java 11 o superior
- Maven 3.6 o superior
- MySQL 8.0
- MySQL Workbench (para gestión de base de datos)
- Git

## Instalación y Configuración

### 1. Clonar el repositorio
```
git clone https://github.com/tu-usuario/six.git
cd six
```

### 2. Configurar la base de datos
Crear una base de datos MySQL:
```sql
CREATE DATABASE sixdb;
USE sixdb;
```

### 3. Configurar application.properties
El archivo ya está configurado con los siguientes parámetros por defecto:

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/sixdb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Si necesitas personalizar estos valores, edita el archivo `src/main/resources/application.properties`.

### 4. Compilar y ejecutar el proyecto
```
mvn clean install
mvn spring-boot:run
```

El servidor estará disponible en http://localhost:8080

## Estructura del Proyecto

El proyecto sigue una arquitectura en capas basada en el patrón MVC:

- **Controllers:** Manejo de peticiones HTTP y flujo de la aplicación
- **Services:** Implementación de la lógica de negocio
- **Models:**
  - **Entities:** Modelos de datos (Usuario, Cliente, Producto, Venta, etc.)
  - **Repositories:** Interfaces para acceso a datos con Spring Data JPA
- **DTOs:** Objetos de transferencia de datos entre capas

## Módulos Principales

### Gestión de Usuarios
Permite crear y administrar usuarios del sistema con diferentes niveles de acceso.

### Gestión de Inventario
Control completo del stock disponible con seguimiento detallado de todos los movimientos.

### Gestión de Productos
Administración de productos incluyendo información detallada, categorización y variantes.

### Gestión de Ventas
Registro y seguimiento de todas las transacciones comerciales con sus respectivos detalles.

### Gestión de Clientes
Administración completa de la información de clientes y su historial de compras.

## Base de Datos

### Importante: Configuración de MySQL
El archivo de la base de datos debe abrirse utilizando **MySQL Workbench**. La configuración predeterminada para la conexión es:

- **URL:** jdbc:mysql://localhost:3306/sixdb
- **Usuario:** root
- **Contraseña:** root

El sistema está configurado para crear automáticamente la base de datos si no existe (`createDatabaseIfNotExist=true`).

## Autores

Desarrollado por Grupo 2
- Jeremy Alexander Martinez Muñoz 
- Claudio Stevenson Tafur Cortez