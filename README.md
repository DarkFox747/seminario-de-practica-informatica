# 🔍 Local Code Review Assistant

> **Seminario de Práctica Informática** - Aplicación de escritorio para revisión de código local con análisis automatizado y métricas en tiempo real.

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25.0.1-blue?logo=java)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-9.5-blue?logo=mysql)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📋 Descripción

**Local Code Review Assistant** es una aplicación de escritorio que permite analizar cambios en repositorios Git locales, detectar hallazgos mediante políticas configurables, clasificarlos por severidad y visualizar métricas detalladas para desarrolladores y líderes técnicos.

### ✨ Características principales

- 🔄 **Análisis de diferencias Git**: Compara branches (base vs target) y detecta cambios en archivos
- 🎯 **Detección de hallazgos**: Integración con endpoint simulado para análisis de código
- 📊 **Clasificación por severidad**: Sistema de políticas configurable (CRITICAL, HIGH, MEDIUM, LOW)
- 📈 **Panel de métricas**: Dashboard para Team Leaders con estadísticas agregadas
- 💾 **Persistencia en MySQL**: Almacenamiento de análisis, hallazgos y métricas
- 📤 **Exportación**: Generación de reportes en CSV
- 🖥️ **Interfaz JavaFX**: UI moderna con 4 pestañas principales

---

## 🏗️ Arquitectura

Implementa **arquitectura hexagonal** (puertos y adaptadores) con separación clara de responsabilidades:

```
📁 src/app/
├── 🎨 ui/              # Presentación (JavaFX)
│   ├── analysis/       # Vista de análisis
│   ├── history/        # Historial de ejecuciones
│   ├── analytics/      # Dashboard de métricas (TL)
│   └── policy/         # Administración de políticas
├── 🔧 application/     # Casos de uso
│   ├── dto/            # Data Transfer Objects
│   └── service/        # Servicios de aplicación
├── 💎 domain/          # Lógica de negocio
│   ├── entity/         # Entidades del dominio
│   ├── value/          # Value Objects (enums)
│   └── port/           # Interfaces (puertos)
└── 🔌 infra/           # Infraestructura
    ├── integration/    # Git, Endpoint, Policy Engine
    ├── persistence/    # Repositorios JDBC
    └── tx/             # Gestión de transacciones
```

---

## 🚀 Instalación y Configuración

### 📦 Prerrequisitos

- ☕ **Java SE 17** o superior ([Descargar aquí](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- 🎨 **JavaFX SDK 25.0.1** ([Descargar aquí](https://gluonhq.com/products/javafx/))
- 🐬 **MySQL 8.x o 9.x** ([Descargar aquí](https://dev.mysql.com/downloads/installer/))
- 🔧 **Git** instalado y disponible en PATH

### ⚙️ Configuración

#### 1️⃣ **Configurar JavaFX**

Descarga JavaFX SDK y extrae en `code-review/lib/javafx-sdk-25.0.1/`:

```
code-review/
└── lib/
    └── javafx-sdk-25.0.1/
        ├── bin/
        ├── legal/
        └── lib/
```

#### 2️⃣ **Crear base de datos MySQL**

Abre PowerShell y ejecuta (reemplaza la ruta de MySQL con tu instalación):

```powershell
cd db-scripts

# Opción 1: Si MySQL está en PATH
mysql -u root -p < db-creation.sql
mysql -u root -p code_review_local < seed.sql

# Opción 2: Ruta completa (ejemplo Windows)
& "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" -u root -p < db-creation.sql
& "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" -u root -p code_review_local < seed.sql
```

#### 3️⃣ **Configurar conexión a BD**

Edita `code-review/src/resources/app.properties`:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/code_review_local
db.username=root
db.password=TU_PASSWORD_AQUI

# Git Configuration
git.executable=git

# Endpoint Configuration (mock)
endpoint.mock.enabled=true
endpoint.mock.file=mock-findings.json
```

#### 4️⃣ **Compilar y ejecutar**

Desde la carpeta `code-review/`:

```powershell
# Compilar
.\compile.bat

# Ejecutar
.\run.bat

# Limpiar archivos compilados
.\clean.bat
```

---

## 📖 Uso

### 🔬 Análisis de código

1. **Seleccionar repositorio**: Navega a un repositorio Git local
2. **Elegir branches**: Selecciona base branch y target branch
3. **Ejecutar análisis**: Click en "Analyze" para iniciar
4. **Ver resultados**: Revisa hallazgos clasificados por severidad

### 📚 Historial

- Consulta análisis previos
- Filtra por repositorio, usuario o fecha
- Ve detalles de cada ejecución

### 📊 Analytics (Team Leader)

- **Métricas globales**: Total de análisis, hallazgos, promedios
- **Distribución de severidad**: Gráfico de barras CRITICAL/HIGH/MEDIUM/LOW
- **Exportar reportes**: Genera CSV con métricas seleccionadas

### ⚙️ Políticas

- Crea políticas de severidad personalizadas (formato JSON)
- Activa/desactiva políticas
- Versiona cambios en reglas

---

## 🗂️ Estructura del Proyecto

```
📂 seminario-de-practica-informatica/
├── 📂 code-review/              # Aplicación principal
│   ├── 📂 src/                  # Código fuente
│   │   ├── App.java             # Entry point alternativo
│   │   ├── TestConnection.java  # Test de conexión BD
│   │   ├── 📂 app/
│   │   │   ├── Main.java        # Entry point principal
│   │   │   ├── 📂 application/  # Servicios y DTOs
│   │   │   ├── 📂 config/       # Configuración y factory
│   │   │   ├── 📂 domain/       # Entidades, ports, values
│   │   │   ├── 📂 infra/        # Implementaciones
│   │   │   └── 📂 ui/           # Vistas JavaFX
│   │   └── 📂 resources/        # Configuración y datos mock
│   ├── 📂 bin/                  # Clases compiladas
│   ├── 📂 lib/                  # Librerías (JavaFX, MySQL connector)
│   ├── compile.bat              # Script de compilación
│   ├── run.bat                  # Script de ejecución
│   ├── clean.bat                # Limpieza de binarios
│   └── test-db-connection.bat   # Test de conexión
├── 📂 db-scripts/               # Scripts SQL
│   ├── db-creation.sql          # Esquema completo
│   ├── seed.sql                 # Datos de prueba
│   └── Comandos_SQL_PR_DB.sql   # Comandos útiles
└── 📄 README.md                 # Este archivo

```

---

## 🗄️ Base de Datos

### Esquema Principal

- **`users`**: Usuarios (DEV, TEAM_LEADER, ADMIN)
- **`repositories`**: Repositorios Git locales
- **`severity_policies`**: Políticas de clasificación
- **`endpoint_mocks`**: Configuración de endpoints simulados
- **`analysis_runs`**: Ejecuciones de análisis
- **`diff_files`**: Archivos modificados por análisis
- **`findings`**: Hallazgos detectados
- **`user_stats`**: Estadísticas por usuario
- **`metrics_snapshots`**: Métricas agregadas por período
- **`dashboard_views`**: Vistas guardadas del dashboard

### 🌱 Datos de Seed

El script `seed.sql` inserta:

- 4 usuarios de ejemplo (1 admin, 2 devs, 1 TL)
- 1 repositorio de prueba
- 1 política de severidad activa
- 1 endpoint mock configurado
- 1 análisis de ejemplo con 5 archivos y 8 hallazgos

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| ☕ **Java SE** | 17 | Lenguaje principal |
| 🎨 **JavaFX** | 25.0.1 | Interfaz gráfica (UI) |
| 🐬 **MySQL** | 8.x/9.x | Base de datos relacional |
| 📊 **JDBC** | - | Acceso a datos (sin ORM) |
| 🔧 **Git** | - | Integración con repositorios |
| 📝 **JSON** | - | Configuración de políticas |

---

## 🧪 Testing

### Probar conexión a BD

```powershell
cd code-review
.\test-db-connection.bat
```

Debe mostrar:
```
✅ Database connection successful!
✅ Found X users in database
✅ ALL TESTS PASSED!
```

### Tests unitarios

```powershell
# TODO: Agregar framework de testing (JUnit 5)
# Cobertura objetivo: ≥ 70% en capa de dominio y aplicación
```

---

## 📝 Casos de Uso Principales

### UC-01: Ejecutar Análisis de Branch
**Actor**: Developer  
**Flujo**:
1. Seleccionar repositorio local
2. Elegir base branch y target branch
3. Ejecutar análisis (Diff → Endpoint → Policy → Persist)
4. Visualizar hallazgos clasificados

### UC-02: Consultar Historial
**Actor**: Developer  
**Flujo**:
1. Acceder a pestaña "History"
2. Ver lista de análisis recientes
3. Filtrar por criterios

### UC-03: Ver Dashboard de Métricas
**Actor**: Team Leader  
**Flujo**:
1. Acceder a pestaña "Analytics"
2. Consultar métricas agregadas
3. Exportar reporte CSV

### UC-04: Administrar Políticas
**Actor**: Admin  
**Flujo**:
1. Acceder a pestaña "Policies"
2. Crear/editar política (JSON)
3. Activar política para uso en análisis

---

## 🤝 Contribución

Este es un proyecto académico para **Seminario de Práctica Informática**. 

### Pasos para contribuir:

1. Fork del repositorio
2. Crea una rama: `git checkout -b feature/nueva-funcionalidad`
3. Commit de cambios: `git commit -m 'Add: nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Ver archivo `LICENSE` para más detalles.

---

## 👥 Autores

- **Desarrollador Principal**: [Tu Nombre]
- **Universidad**: [Nombre de la Universidad]
- **Materia**: Seminario de Práctica Informática
- **Año**: 2025

---

## 📞 Soporte

Si encuentras algún problema o tienes sugerencias:

1. 🐛 Reporta bugs en [Issues](../../issues)
2. 💡 Propón mejoras en [Discussions](../../discussions)
3. 📧 Contacto: tu-email@example.com

---

## 🔮 Roadmap

### ✅ Completado
- [x] Arquitectura hexagonal implementada
- [x] Integración con Git (diff entre branches)
- [x] Motor de políticas configurable
- [x] Persistencia en MySQL con JDBC
- [x] UI con 4 vistas principales
- [x] Exportación a CSV

### 🚧 En Progreso
- [ ] Validación de formularios
- [ ] Manejo de errores mejorado
- [ ] Tests unitarios completos

### 📅 Futuro
- [ ] Integración con SonarQube real
- [ ] Soporte para múltiples endpoints
- [ ] Gráficos avanzados (líneas, tendencias)
- [ ] Exportación a PDF
- [ ] Mode oscuro en UI
- [ ] Internacionalización (i18n)

---

<div align="center">

**⭐ Si te gusta este proyecto, dale una estrella en GitHub ⭐**

Made with ☕ and 💻 by **Seminario de Práctica Informática Team**

</div>