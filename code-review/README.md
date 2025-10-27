# Code Review Assistant

**Aplicación desktop local (JavaFX + MySQL)** para análisis de código mediante integración con Git y endpoint simulado.

## 🎯 Objetivo

Sistema de revisión de código que:
1. Calcula **diff** entre ramas Git (`baseBranch` → `targetBranch`)
2. Llama a **endpoint mock** y recibe hallazgos
3. **Clasifica** según políticas de severidad
4. **Persiste** en MySQL
5. Muestra **reportes** (DEV) y **dashboards** (TL) en JavaFX

## 🛠️ Stack Tecnológico

- **Java SE 17** (sin Maven/Gradle, compilación manual)
- **JavaFX 25.0.1** (UI desktop)
- **JDBC + MySQL Connector 8.0.33** (persistencia)
- **Git** (ProcessBuilder para diffs)
- **Arquitectura Hexagonal** (Domain → Application → Infrastructure → UI)

## 📦 Estructura del Proyecto

```
code-review/
├── src/
│   ├── app/
│   │   ├── domain/          # Entidades, enums, ports, exceptions
│   │   ├── application/     # DTOs y servicios (UC-01 a UC-07)
│   │   ├── infra/           # JDBC repos, Git, Endpoint, Policy engine
│   │   ├── ui/              # JavaFX views y controllers
│   │   └── config/          # AppConfig, AppFactory
│   ├── resources/           # app.properties, mock JSON, policy rules
│   └── App.java             # Main entry point
├── lib/                     # JavaFX JARs + MySQL Connector
├── bin/                     # Compilados (generado)
├── compile.bat              # Script de compilación
├── run.bat                  # Script de ejecución
└── clean.bat                # Limpieza

db-scripts/                  # Scripts SQL de creación y seed
```

## ✅ Estado de Implementación

### **Completado (100%)**
- ✅ **Domain Layer**: Enums, entidades, value objects, ports
- ✅ **Infrastructure**: JDBC repos, TxManager, GitDiffEngine, EndpointMock, PolicyEngine
- ✅ **Application Services**: AnalyzeBranchService (UC-01), HistoryQuery, Analytics, PolicyAdmin, Export
- ✅ **UI Layer**: 4 vistas completas (Analysis, History, Analytics, Policy)
- ✅ **Configuration**: AppConfig, AppFactory, wiring completo
- ✅ **Build Scripts**: compile.bat, run.bat funcionando

### **Pendiente**
- ⏳ **Database Schema**: Crear tablas MySQL
- ⏳ **Integration Testing**: Tests con BD real
- ⏳ **Ajustes Finales**: Validaciones y refinamientos

## 🚀 Cómo Ejecutar

### Prerequisitos
- Java 17 instalado
- MySQL Server corriendo en localhost:3306
- Database `code_review_db` creada
- JavaFX SDK 25.0.1 en `lib/`
- MySQL Connector en `lib/`

### 1. Configurar Base de Datos

#### Opción A: Usando archivo `.env` (Recomendado)
```bash
# 1. Copiar template
cp .env.example .env

# 2. Editar con tus credenciales
notepad .env
```

Contenido de `.env`:
```properties
DB_URL=jdbc:mysql://localhost:3306/code_review_db
DB_USERNAME=root
DB_PASSWORD=tu_password
```

#### Opción B: Editar `app.properties`
```properties
# src/resources/app.properties
db.url=jdbc:mysql://localhost:3306/code_review_db
db.username=root
db.password=tu_password
```

**📖 Guía completa**: Ver `DB-CONNECTION-GUIDE.md`

### 2. Probar Conexión (Opcional pero Recomendado)
```bash
.\test-db-connection.bat
```

Deberías ver:
```
✅ ALL TESTS PASSED!
Database connection is working correctly.
```

### 3. Compilar
```bash
.\compile.bat
```

### 4. Ejecutar
```bash
.\run.bat
```

### 5. Limpiar (si necesario)
```bash
.\clean.bat
```

## 📊 Casos de Uso Implementados

### **UC-01: Analizar Código (Developer)**
Flujo: Seleccionar repo → Elegir ramas → Analizar → Ver findings
- ✅ Formulario de entrada
- ✅ Ejecución background
- ✅ Tabla de resultados con filtros
- ✅ Integración completa Diff → Endpoint → Policy → Persist

### **UC-02: Ver Historial (Developer)**
- ✅ Lista de runs anteriores
- ✅ Detalle de findings por run
- ✅ Filtros por período y severidad

### **UC-03: Dashboard Analytics (Team Lead)**
- ✅ Métricas: Total runs, files, findings, promedios
- ✅ Charts por severidad
- ✅ Filtro temporal

### **UC-04: Gestión de Políticas (Team Lead)**
- ✅ CRUD de severity policies
- ✅ Versionado de políticas
- ✅ Activación/desactivación
- ✅ Editor JSON de rules

## 🏗️ Arquitectura

### Capas
```
┌──────────────────────────────────────┐
│           UI (JavaFX)                │ ← Views + Controllers
├──────────────────────────────────────┤
│      Application Services            │ ← UC-01 a UC-07
├──────────────────────────────────────┤
│           Domain                     │ ← Entities, Ports, Rules
├──────────────────────────────────────┤
│        Infrastructure                │ ← JDBC, Git, Mock, Policy
└──────────────────────────────────────┘
```

### Flujo UC-01 (Análisis)
```
User → AnalysisView → AnalysisController → AnalyzeBranchService
                                              ↓
                            GitDiffEngine → DiffFile[]
                                              ↓
                         EndpointMockClient → Finding[] (raw)
                                              ↓
                          PolicyEngineImpl → Finding[] (classified)
                                              ↓
                          JDBC Repositories → MySQL (persist)
                                              ↓
                          AnalysisResultDTO → UI
```

## 📋 Próximos Pasos

1. **Crear esquema BD** (`db-scripts/db-creation.sql`)
2. **Seed data inicial** (usuarios, policies)
3. **Testing de integración** (UC-01 end-to-end)
4. **Validaciones adicionales** en formularios
5. **Export CSV/PDF** funcional

## 📚 Documentación Adicional

- `UI-STATUS.md` - Estado detallado de la capa UI
- `.github/copilot-instructions.md` - Especificación completa del proyecto
- `db-scripts/README.md` - Documentación del esquema de BD

## 📄 Licencia

Proyecto académico - Seminario de Práctica Informática

---

**Última actualización**: 2025-10-26  
**Estado**: 🟢 Capa UI Completa - 🔵 Pendiente: DB Schema
