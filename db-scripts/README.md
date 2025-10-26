# Base de Datos — Asistente Local de Code Review

Este repositorio contiene los **scripts SQL** para crear y operar la base de datos del **Local Code Review Assistant**.  
Los archivos principales son:

- `db-creation.sql` → **DDL** completo (creación de esquema/tablas, índices y FKs).
- `seed.sql` → **Datos de prueba** iniciales para desarrollo y testing.
- `Comandos_SQL_PR_DB.sql` → **plantillas** de **INSERT / SELECT / DELETE** para todas las tablas.

> Requisitos: **MySQL 8.x**, motor **InnoDB**, charset **utf8mb4**.

---

## 📦 Estructura del directorio

```
/db-scripts
  ├─ db-creation.sql          # DDL: esquema completo
  ├─ seed.sql                 # Datos de prueba iniciales
  ├─ Comandos_SQL_PR_DB.sql   # Plantillas de operaciones CRUD
  └─ README.md                # Esta documentación
```

> Base de datos: **`code_review_local`**

---

## 🚀 Uso rápido

### 1) Crear el esquema y las tablas

**Linux/MacOS:**
```bash
mysql -h <HOST> -u <USER> -p < db-creation.sql
```

**Windows (PowerShell):**
```powershell
Get-Content .\db-creation.sql | mysql -h <HOST> -u <USER> -p
```

**Docker (cliente MySQL dentro de contenedor):**
```bash
docker run --rm -i --network host -e MYSQL_PWD=<PASSWORD> mysql:8 mysql -h <HOST> -u <USER> < db-creation.sql
```

### 2) Cargar datos de prueba (seed)

**Después de crear el esquema, cargá los datos de ejemplo:**

```bash
mysql -h <HOST> -u <USER> -p code_review_local < seed.sql
```

Esto creará:
- 4 usuarios de prueba (Developer, Tech Lead, QA, Admin)
- 1 repositorio de ejemplo
- 2 políticas de severidad versionadas
- 1 endpoint mock para simular análisis
- 1 análisis exitoso con findings y diff files
- Métricas y estadísticas agregadas

### 3) Ejecutar plantillas de inserción/consulta/borrado

**Importar el archivo de plantillas (comandos de ejemplo con datos reales):**
```bash
mysql -h <HOST> -u <USER> -p code_review_local < Comandos_SQL_PR_DB.sql
```

> El archivo incluye `USE code_review_local;` y **consultas de ejemplo** con datos reales que coinciden con el seed.  
> Podés ejecutarlo completo o copiar comandos individuales para tus pruebas.

---

## 🧩 Datos de prueba incluidos

El archivo `seed.sql` proporciona un conjunto completo de datos de prueba que incluye:

### Usuarios (4 usuarios con diferentes roles)
- **Diego Soler** (`diego@crombie.dev`) - Developer
- **Lucía Romero** (`lucia@crombie.dev`) - Tech Lead
- **Mariano Funes** (`mariano@crombie.dev`) - QA
- **Admin System** (`admin@crombie.dev`) - Admin

### Análisis y datos relacionados
- **1 repositorio**: `/Users/diego/projects/code-review-assistant`
- **2 políticas de severidad** versionadas (v1 y v2)
- **1 endpoint mock** configurado para simular respuestas de análisis
- **1 análisis exitoso** (`run-001`) con:
  - 3 archivos modificados (UserService.java, User.java, README.md)
  - 4 findings de diferentes severidades:
    - **CRITICAL**: Contraseña hardcodeada
    - **HIGH**: Consulta SQL sin índice
    - **MEDIUM**: Clase sin Javadoc
    - **LOW**: Método con demasiadas líneas

### Métricas y estadísticas
- Estadísticas de usuario para los últimos 7 días
- Snapshot de métricas globales del sistema
- Vista de dashboard configurada para Tech Lead

---

## 🛠️ Notas técnicas

- El DDL crea la BD `code_review_local` y configura `utf8mb4_0900_ai_ci`.
- Todas las FKs están ordenadas para evitar errores de dependencia.
- Se usan tablas de catálogo (`user_role_type`, `run_status_type`, `file_change_type`, `severity_type`) para normalización.
- Campos de auditoría/fechas emplean `CURRENT_TIMESTAMP`.
- Las políticas de severidad se versionan y pueden tener fechas de vigencia diferentes.

---

## ✅ Comprobaciones rápidas

1. **Esquema creado**  
   ```sql
   SHOW DATABASES LIKE 'code_review_local';
   SHOW TABLES FROM code_review_local;
   ```
2. **Datos de seed cargados**  
   ```sql
   SELECT COUNT(*) FROM users;  -- Debería retornar 4
   SELECT COUNT(*) FROM findings; -- Debería retornar 4
   ```
3. **Consulta de verificación de análisis**  
   ```sql
   SELECT id, base_branch, target_branch, status_code 
   FROM analysis_runs 
   WHERE user_id='u-001';
   ```

---

## 🔐 Seguridad y buenas prácticas

- No comitees credenciales. Usá variables de entorno o un gestor de secretos.
- Asegurá **TLS** en la conexión a MySQL si es remoto.
- Asigná privilegios mínimos al usuario de base de datos.
- Rotá/limpiá datos en tablas voluminosas (`auditoria`, `metric_event`) según política.

---

## 📄 Licencia y contribuciones

- Abrí PRs con cambios a la estructura en `db-creation.sql`.
- Para nuevas consultas utilitarias, agregalas como comentarios o en un `queries-extra.sql`.

---

## 🆘 Soporte

Si algo falla al ejecutar los scripts:
- Indicá **versión de MySQL**, **comando usado** y **mensaje de error**.
- Adjuntá el bloque SQL involucrado para acelerar el debug.
