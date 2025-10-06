# Base de Datos — Analizador de PR

Este repositorio contiene los **scripts SQL** para crear y operar la base de datos del sistema de análisis de Pull Requests.  
Los archivos principales son:

- `db-creation.sql` → **DDL** completo (creación de esquema/tablas, índices y FKs).
- `Comandos_SQL_PR_DB.sql` → **plantillas** de **INSERT / SELECT / DELETE** para todas las tablas.

> Requisitos: **MySQL 8.x**, motor **InnoDB**, charset **utf8mb4**.

---

## 📦 Estructura sugerida

```
/sql
  ├─ db-creation.sql
  └─ Comandos_SQL_PR_DB.sql
```

> Ubicá ambos archivos donde te resulte cómodo. En los ejemplos se asume `/sql`.

---

## 🚀 Uso rápido

### 1) Crear el esquema y las tablas

**Linux/MacOS:**
```bash
mysql -h <HOST> -u <USER> -p < /sql/db-creation.sql
```

**Windows (PowerShell):**
```powershell
Get-Content .\sql\db-creation.sql | mysql -h <HOST> -u <USER> -p
```

**Docker (cliente MySQL dentro de contenedor):**
```bash
docker run --rm -i   --network host   -e MYSQL_PWD=<PASSWORD> mysql:8   mysql -h <HOST> -u <USER> < /sql/db-creation.sql
```

### 2) Ejecutar plantillas de inserción/consulta/borrado

**Importar el archivo de plantillas (no cambia el esquema):**
```bash
mysql -h <HOST> -u <USER> -p < /sql/Comandos_SQL_PR_DB.sql
```

> El archivo incluye `USE proyecto_db;` y **consultas de ejemplo** con _placeholders_ como `<id_repo>`, `<id_pr>`, etc.  
> ⚠️ **Reemplazá** esos placeholders por valores reales antes de ejecutar cada instrucción.

---

## 🧩 Variables y placeholders

En `Comandos_SQL_PR_DB.sql` vas a encontrar marcadores como:

- `<id_usuario>`, `<id_repo>`, `<id_pr>`, `<id_politica>`, etc.
- Fechas/horarios en formato `YYYY-MM-DD HH:MM:SS`.

Podés editarlos a mano o generar versiones parametrizadas para tu entorno CI/CD.

Ejemplo (Linux) para reemplazar **temporalmente** un PR:
```bash
sed "s/<id_pr>/42/g" /sql/Comandos_SQL_PR_DB.sql | mysql -h <HOST> -u <USER> -p
```

---

## 🛠️ Notas técnicas

- El DDL crea la BD `proyecto_db` y configura `utf8mb4_0900_ai_ci`.
- Todas las FKs están ordenadas para evitar errores de dependencia.
- Se usan `ENUM` para catálogos (p. ej., estados y niveles de riesgo).
- Campos de auditoría/fechas emplean `CURRENT_TIMESTAMP`.

---

## ✅ Comprobaciones rápidas

1. **Esquema creado**  
   ```sql
   SHOW DATABASES LIKE 'proyecto_db';
   SHOW TABLES FROM proyecto_db;
   ```
2. **Inserción básica**  
   ```sql
   INSERT INTO usuario (nombre, email, rol, activo)
   VALUES ('Admin', 'admin@example.com', 'ADMIN', TRUE);
   ```
3. **Consulta de verificación**  
   ```sql
   SELECT id, nombre, email, rol FROM usuario LIMIT 5;
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
