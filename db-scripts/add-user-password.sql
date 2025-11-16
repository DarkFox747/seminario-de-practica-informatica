-- =========================================================
-- Agregar columna password_hash a tabla users
-- =========================================================
USE code_review_local;

-- Agregar columna para contraseña hasheada
ALTER TABLE users 
ADD COLUMN password_hash VARCHAR(255) NULL AFTER email;

-- Actualizar usuario demo con contraseña "demo123" (hash SHA-256)
-- Hash calculado: d3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791
UPDATE users 
SET password_hash = 'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791'
WHERE email = 'demo@example.com';

-- Opcional: Hacer la columna NOT NULL después de migrar todos los usuarios
-- ALTER TABLE users MODIFY COLUMN password_hash VARCHAR(255) NOT NULL;
