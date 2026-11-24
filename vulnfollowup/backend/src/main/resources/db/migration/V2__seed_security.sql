-- =====================================================================
-- VulnFollowUp - V2: roles y usuarios de prueba (CU1)
-- Contraseñas de PRUEBA guardadas con BCrypt (cuentas sintéticas, documentadas en el README):
--   admin / admin123   ·   analyst / analyst123   ·   viewer / viewer123
-- =====================================================================

INSERT INTO app_role (name) VALUES ('ADMIN'), ('ANALYST'), ('VIEWER');

INSERT INTO app_user (username, full_name, password) VALUES
  ('admin',   'Admin VulnFollowUp', '{bcrypt}$2a$10$IeMoCo1Nvrgw9Va.hYcs/.E4vmxkwmYIkMFH2eN5UjH.HkWTqgBAG'),
  ('analyst', 'Ana Analista',       '{bcrypt}$2a$10$v7qwm47QU7R9//dsgFHYp./5w69Fmvm67wJGxt4T4t9h/L1RRBPpm'),
  ('viewer',  'Victor Viewer',      '{bcrypt}$2a$10$.GaCy4MwlXsma8YqYbUhPeJk3CijdHghlaBaUs9SBAuanNXoMMeCy');

INSERT INTO app_user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN app_role r ON r.name = upper(u.username);
