-- =========================================================
-- Actualizar password hash del usuario demo
-- =========================================================
USE code_review_local;

-- Actualizar con el hash correcto de "demo123"
UPDATE users 
SET password_hash = 'd3ad9315b7be5dd53b31a273b3b3aba5defe700808305aa16a3062b76658a791'
WHERE email = 'demo@example.com';

-- Verificar
SELECT id, name, email, password_hash 
FROM users 
WHERE email = 'demo@example.com';
