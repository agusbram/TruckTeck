# TruckTeck

TruckTeck es una solución integral diseñada para la gestión y optimización de flotas de transporte. Este proyecto utiliza tecnologías modernas para proporcionar funcionalidades como generación de reportes en PDF, configuración de correos electrónicos, y conexión a bases de datos MySQL.

---

## Tabla de Contenidos

- [Características](#características)
- [Requisitos](#requisitos)
- [Configuración del Proyecto](#configuración-del-proyecto)
  - [Base de Datos](#base-de-datos)
  - [Configuración de Correos](#configuración-de-correos)
  - [Colores Corporativos](#colores-corporativos)
- [Ejecución del Proyecto](#ejecución-del-proyecto)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Contribuciones](#contribuciones)
- [Licencia](#licencia)

---

## Características

- **Gestión de Flotas**: Herramientas para la administración de vehículos y conductores.
- **Generación de PDFs**: Generación de reportes personalizados con colores corporativos.
- **Configuración de Correos**: Envío de notificaciones y alertas a través de SMTP.
- **Conexión a MySQL**: Configuración flexible para entornos locales y de producción.
- **Logs y Debugging**: Configuración avanzada de logs para monitoreo y depuración.

---

## Requisitos

Antes de comenzar, asegúrate de tener lo siguiente instalado:

- **Java 17** o superior
- **Spring Boot 2.7+**
- **MySQL 8.0+**
- **Maven** para la gestión de dependencias
- **Git** para el control de versiones

---

## Configuración del Proyecto

### Base de Datos
En el archivo `application.properties`, configura la conexión a tu base de datos MySQL. Hay ejemplos para diferentes entornos locales:

# Ejemplo
```
spring.datasource.url=jdbc:mysql://localhost:3306/iw3_db?createDatabaseIfNotExist=true&verifyServerCertificate=false&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username='tu_usuario_bd'
spring.datasource.password='tu_clave_bd'```

**Nota**: Asegúrate de ajustar los valores de username y password según tu configuración.

## Configuración de Correos
El proyecto utiliza un servidor SMTP para el envío de correos. Configura las credenciales en el archivo ```application.propertie```:

```
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true```

**Nota**: Crea un archivo .env en la raíz del proyecto para definir las variables de entorno MAIL_USERNAME y MAIL_PASSWORD.

## Ejecución del Proyecto

- 1. Clona el repositorio:
```
git clone https://github.com/tu-usuario/truckteck.git
cd truckteck```

- 2. Configura las variables de entorno en un archivo .env (opcional).
- 3. Ejecuta el proyecto con Maven: 
```mvn spring-boot:run```

- 4. Accede a la aplicación en tu navegador:
```http://localhost:8080```

## Estructura del Proyecto
```
TruckTeck/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ar/
│   │   │       └── edu/               # Código fuente de la aplicación
│   │   ├── resources/
│   │   │   ├── application.properties # Configuración principal
│   │   │   ├── application-mysqlprod.properties
│   │   │   ├── mio.txt
│   │   │   └── static/
│   │   │       └── TruckTeck.png      # Logo de la empresa
│   ├── test/
│   │   └── java/
│   │       └── ar/
│   │           └── edu/               # Pruebas unitarias
├── target/
│   ├── classes/                       # Archivos compilados
│   ├── generated-sources/
│   ├── test-classes/
├── docs/                              # Documentación del proyecto
│   ├── TMS_ARCHITECTURE_DIAGRAMS.md
│   ├── TMS_INTEGRATION_GUIDE.md
│   ├── TMS_README.md
│   └── TMS_REFACTORING_SUMMARY.md
├── pom.xml                            # Archivo de configuración de Maven
└── README.md                          # Documentación del proyecto
```

## Contribuciones
¡Las contribuciones son bienvenidas! Si deseas colaborar:

1. Haz un fork del repositorio.
2. Crea una nueva rama (git checkout -b feature/nueva-funcionalidad).
3. Realiza tus cambios y haz un commit (git commit -m 'Agrega nueva funcionalidad').
4. Sube tus cambios (git push origin feature/nueva-funcionalidad).
5. Abre un Pull Request.

## Licencia
Este proyecto está licenciado bajo la MIT License. Puedes usarlo, modificarlo y distribuirlo libremente.

¡Gracias por usar TruckTeck! 🚛