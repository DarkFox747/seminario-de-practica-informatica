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
- 4 usuarios de prueba con autenticación (Developer, Tech Lead, QA, Admin)
- Contraseñas hasheadas con SHA-256 para todos los usuarios
- 1 repositorio de ejemplo
- 1 política de severidad activa
- 1 endpoint mock para simular análisis
- 1 análisis exitoso con findings y diff files
- Métricas y estadísticas agregadas

### 🔑 Credenciales de acceso (usuarios de prueba)

Todos los usuarios tienen contraseñas hasheadas con SHA-256:

| Email | Contraseña | Rol | Hash SHA-256 |
|-------|-----------|-----|--------------|
| `demo@example.com` | `demo123` | DEVELOPER | `d3ad9315...` |
| `lead@example.com` | `lead123` | TECH_LEAD | `8c6976e5...` |
| `qa@example.com` | `qa123` | QA | `9f735e0d...` |
| `admin@example.com` | `admin123` | ADMIN | `240be518...` |

> **Nota**: Para producción, cambiar estas contraseñas y usar un algoritmo más robusto como bcrypt o Argon2.

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

### Usuarios (4 usuarios con diferentes roles y autenticación)
- **Demo Developer** (`demo@example.com` / `demo123`) - Developer
- **Team Lead** (`lead@example.com` / `lead123`) - Tech Lead
- **QA Tester** (`qa@example.com` / `qa123`) - QA
- **Admin User** (`admin@example.com` / `admin123`) - Admin

> Todos los passwords están hasheados con SHA-256 y almacenados en la columna `password_hash`.

### Análisis y datos relacionados
- **1 repositorio**: `C:\projects\sample-repo`
- **1 política de severidad** activa (Default Policy)
- **1 endpoint mock** configurado para simular respuestas de análisis
- **1 análisis exitoso** con:
  - 5 archivos modificados (UserService.java, UserController.java, User.java, README.md, pom.xml)
  - 8 findings de diferentes severidades:
    - **CRITICAL**: Contraseña hardcodeada, SQL Injection, Vulnerable Dependency
    - **HIGH**: N+1 Query detectada
    - **MEDIUM**: Missing Javadoc (2 hallazgos)
    - **LOW**: Método largo, Magic Number

### Métricas y estadísticas
- Estadísticas de usuario para los últimos 7 días
- Snapshot de métricas globales del sistema
- Vista de dashboard configurada para Tech Lead

---

## 🛠️ Notas técnicas

- El DDL crea la BD `code_review_local` y configura `utf8mb4_0900_ai_ci`.
- La tabla `users` incluye la columna `password_hash` (VARCHAR(255)) para autenticación.
- Los passwords se hashean con **SHA-256** en la aplicación antes de almacenarse.
- Todas las FKs están ordenadas para evitar errores de dependencia.
- Se usan tablas de catálogo (`user_role_type`, `run_status_type`, `file_change_type`, `severity_type`) para normalización.
- Campos de auditoría/fechas emplean `CURRENT_TIMESTAMP`.
- Las políticas de severidad se versionan y pueden tener fechas de vigencia diferentes.

---

## 📋 Migración de password_hash

Si ya tenés una BD existente **sin** la columna `password_hash`, ejecutá:

```bash
mysql -u root -p code_review_local < add-user-password.sql
```

Este script agrega la columna y actualiza el usuario demo con el hash correcto.

Para actualizar solo el hash del usuario demo (si ya existe la columna):

```bash
mysql -u root -p code_review_local < fix-password-hash.sql
```

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
   SELECT COUNT(*) FROM findings; -- Debería retornar 8
   ```
3. **Verificar autenticación de usuarios**  
   ```sql
   SELECT id, name, email, role_code, 
          LEFT(password_hash, 8) AS hash_preview
   FROM users;
   ```
4. **Consulta de verificación de análisis**  
   ```sql
   SELECT id, base_branch, target_branch, status_code, total_findings
   FROM analysis_runs 
   WHERE user_id = 1;
   ```

---

## 🔐 Seguridad y buenas prácticas

- **Passwords**: La aplicación usa **SHA-256** para hashear contraseñas. Para producción, considera usar **bcrypt**, **Argon2** o **PBKDF2** que son más seguros contra ataques de fuerza bruta.
- No comitees credenciales reales. Usá variables de entorno o un gestor de secretos.
- Asegurá **TLS** en la conexión a MySQL si es remoto.
- Asigná privilegios mínimos al usuario de base de datos.
- Cambia las contraseñas de prueba antes de usar en cualquier entorno no local.
- Rotá/limpiá datos en tablas voluminosas (`findings`, `analysis_runs`) según política de retención.

---

## 📄 Licencia y contribuciones

- Abrí PRs con cambios a la estructura en `db-creation.sql`.
- Para nuevas consultas utilitarias, agregalas como comentarios o en un `queries-extra.sql`.

---

## 🆘 Soporte

Si algo falla al ejecutar los scripts:
- Indicá **versión de MySQL**, **comando usado** y **mensaje de error**.
- Adjuntá el bloque SQL involucrado para acelerar el debug.
