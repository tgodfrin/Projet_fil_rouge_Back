-- =============================================
-- JEU DE DONNÉES LOC-MNS
-- Idempotent : safe à rejouer sur ddl-auto=update
-- Tables avec contrainte unique  → ON CONFLICT (...) DO NOTHING
-- Tables sans contrainte unique  → INSERT ... SELECT ... WHERE NOT EXISTS
-- Note : les colonnes @CreationTimestamp (created_at, begin_date, added_date,
--        begin_status_date) sont NOT NULL → fournies explicitement ici
-- =============================================


-- =============================================
-- 1. PROFILS
-- =============================================
INSERT INTO profil (type) VALUES ('GESTIONNAIRE')  ON CONFLICT DO NOTHING;
INSERT INTO profil (type) VALUES ('COLLABORATEUR') ON CONFLICT DO NOTHING;
INSERT INTO profil (type) VALUES ('INTERVENANT')   ON CONFLICT DO NOTHING;
INSERT INTO profil (type) VALUES ('STAGIAIRE')     ON CONFLICT DO NOTHING;


-- =============================================
-- 2. FAMILLES D'ÉQUIPEMENT
-- =============================================
INSERT INTO equipment_family (name_equipment_family) VALUES ('Ordinateur portable')    ON CONFLICT (name_equipment_family) DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Tablette')               ON CONFLICT (name_equipment_family) DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Périphérique')           ON CONFLICT (name_equipment_family) DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Écran')                  ON CONFLICT (name_equipment_family) DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Matériel audiovisuel')   ON CONFLICT (name_equipment_family) DO NOTHING;


-- =============================================
-- 3. UTILISATEURS (created_at obligatoire)
-- =============================================
INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'jean.martin@mns.fr', 'Jean', 'Martin', 'admin123', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'sophie.leblanc@mns.fr', 'Sophie', 'Leblanc', 'admin123', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'thomas.dupont@mns.fr', 'Thomas', 'Dupont', 'user123', (SELECT id FROM profil WHERE type = 'COLLABORATEUR'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'marie.leroy@mns.fr', 'Marie', 'Leroy', 'user123', (SELECT id FROM profil WHERE type = 'COLLABORATEUR'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'lucas.bernard@mns.fr', 'Lucas', 'Bernard', 'user123', (SELECT id FROM profil WHERE type = 'COLLABORATEUR'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'emma.petit@mns.fr', 'Emma', 'Petit', 'user123', (SELECT id FROM profil WHERE type = 'COLLABORATEUR'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'nathan.durand@mns.fr', 'Nathan', 'Durand', 'user123', (SELECT id FROM profil WHERE type = 'COLLABORATEUR'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'pierre.moreau@mns.fr', 'Pierre', 'Moreau', 'user123', (SELECT id FROM profil WHERE type = 'INTERVENANT'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'laura.simon@mns.fr', 'Laura', 'Simon', 'user123', (SELECT id FROM profil WHERE type = 'INTERVENANT'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'hugo.michel@mns.fr', 'Hugo', 'Michel', 'user123', (SELECT id FROM profil WHERE type = 'INTERVENANT'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'camille.robert@mns.fr', 'Camille', 'Robert', 'user123', (SELECT id FROM profil WHERE type = 'STAGIAIRE'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (created_at, email, name, lastname, password, profil_id) VALUES
  (CURRENT_TIMESTAMP, 'alexis.laurent@mns.fr', 'Alexis', 'Laurent', 'user123', (SELECT id FROM profil WHERE type = 'STAGIAIRE'))
ON CONFLICT (email) DO NOTHING;


-- =============================================
-- 4. ÉQUIPEMENTS
-- =============================================
INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PC-001', 'MacBook Pro M3', 'Salle B204', '2023-09-01', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PC-002', 'Dell XPS 15', 'Salle A101', '2023-06-15', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PC-003', 'Lenovo ThinkPad X1', 'Salle C302', '2022-11-20', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PC-004', 'HP EliteBook 840', 'Salle B204', '2022-03-10', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-TAB-001', 'iPad Pro 12.9', 'Accueil', '2023-01-15', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Tablette'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-TAB-002', 'Samsung Galaxy Tab S9', 'Salle A101', '2023-07-22', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Tablette'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-TAB-003', 'Microsoft Surface Pro', 'Salle C302', '2022-08-05', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Tablette'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PER-001', 'Magic Mouse Apple', 'Stock', '2023-02-28', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PER-002', 'Clavier Logitech MX', 'Stock', '2023-02-28', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-ECR-001', 'Dell UltraSharp 27"', 'Salle B204', '2021-12-01', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-ECR-002', 'LG 4K 32"', 'Salle A101', '2022-04-18', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran'))
ON CONFLICT (reference) DO NOTHING;

INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-AV-001', 'Projecteur Epson EB', 'Salle de réunion', '2021-06-10', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Matériel audiovisuel'))
ON CONFLICT (reference) DO NOTHING;


-- =============================================
-- 5. CAN_LOAN (pas de PK sur la table de jointure → WHERE NOT EXISTS)
-- =============================================
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'GESTIONNAIRE'
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);

INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'COLLABORATEUR'
    AND ef.name_equipment_family IN ('Ordinateur portable', 'Tablette', 'Périphérique', 'Écran')
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);

INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'INTERVENANT'
    AND ef.name_equipment_family IN ('Ordinateur portable', 'Périphérique')
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);

INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'STAGIAIRE'
    AND ef.name_equipment_family IN ('Tablette', 'Périphérique')
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);


-- =============================================
-- 6. CARACTÉRISTIQUES
-- =============================================
INSERT INTO characteristic (name) VALUES ('Processeur')              ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('RAM')                     ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('Stockage')                ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('Système d''exploitation') ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('Résolution')              ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('Connectivité')            ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('Luminosité')              ON CONFLICT (name) DO NOTHING;
INSERT INTO characteristic (name) VALUES ('Type de connexion')       ON CONFLICT (name) DO NOTHING;


-- =============================================
-- 7. EST_CONSTITUE (pas de PK → WHERE NOT EXISTS)
-- =============================================
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Ordinateur portable'
    AND c.name IN ('Processeur', 'RAM', 'Stockage', 'Système d''exploitation')
    AND NOT EXISTS (SELECT 1 FROM est_constitue ec WHERE ec.caracteristique_id = c.id AND ec.equipment_family_id = ef.id);

INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Tablette'
    AND c.name IN ('Processeur', 'RAM', 'Stockage', 'Système d''exploitation')
    AND NOT EXISTS (SELECT 1 FROM est_constitue ec WHERE ec.caracteristique_id = c.id AND ec.equipment_family_id = ef.id);

INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Écran' AND c.name = 'Résolution'
    AND NOT EXISTS (SELECT 1 FROM est_constitue ec WHERE ec.caracteristique_id = c.id AND ec.equipment_family_id = ef.id);

INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Périphérique' AND c.name = 'Connectivité'
    AND NOT EXISTS (SELECT 1 FROM est_constitue ec WHERE ec.caracteristique_id = c.id AND ec.equipment_family_id = ef.id);

INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Matériel audiovisuel'
    AND c.name IN ('Luminosité', 'Type de connexion')
    AND NOT EXISTS (SELECT 1 FROM est_constitue ec WHERE ec.caracteristique_id = c.id AND ec.equipment_family_id = ef.id);


-- =============================================
-- 8. VALEURS DE CARACTÉRISTIQUES (begin_date obligatoire)
-- =============================================
-- MacBook Pro M3
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'Apple M3 Pro', (SELECT id FROM characteristic WHERE name = 'Processeur')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'Apple M3 Pro' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Processeur'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '18 Go', (SELECT id FROM characteristic WHERE name = 'RAM')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '18 Go' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'RAM'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '512 Go SSD', (SELECT id FROM characteristic WHERE name = 'Stockage')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '512 Go SSD' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Stockage'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'macOS Sonoma', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'macOS Sonoma' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- Dell XPS 15
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'Intel Core i7-13700H', (SELECT id FROM characteristic WHERE name = 'Processeur')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'Intel Core i7-13700H' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Processeur'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '16 Go', (SELECT id FROM characteristic WHERE name = 'RAM')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '16 Go' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'RAM'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '1 To SSD', (SELECT id FROM characteristic WHERE name = 'Stockage')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '1 To SSD' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Stockage'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'Windows 11 Pro', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'Windows 11 Pro' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- iPad Pro
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'Apple M2', (SELECT id FROM characteristic WHERE name = 'Processeur')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'Apple M2' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Processeur'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '8 Go', (SELECT id FROM characteristic WHERE name = 'RAM')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '8 Go' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'RAM'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '256 Go', (SELECT id FROM characteristic WHERE name = 'Stockage')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '256 Go' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Stockage'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'iPadOS 17', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'iPadOS 17' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- Dell UltraSharp 27"
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '2560x1440 (QHD)', (SELECT id FROM characteristic WHERE name = 'Résolution')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '2560x1440 (QHD)' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Résolution'));

-- LG 4K 32"
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '3840x2160 (4K UHD)', (SELECT id FROM characteristic WHERE name = 'Résolution')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '3840x2160 (4K UHD)' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Résolution'));

-- Magic Mouse
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'Bluetooth 5.0', (SELECT id FROM characteristic WHERE name = 'Connectivité')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'Bluetooth 5.0' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Clavier Logitech MX
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'USB + Bluetooth', (SELECT id FROM characteristic WHERE name = 'Connectivité')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'USB + Bluetooth' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Projecteur Epson
INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, '3600 lumens', (SELECT id FROM characteristic WHERE name = 'Luminosité')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = '3600 lumens' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Luminosité'));

INSERT INTO characteristic_value (begin_date, value, characteristic_id)
  SELECT CURRENT_TIMESTAMP, 'HDMI / VGA / USB', (SELECT id FROM characteristic WHERE name = 'Type de connexion')
  WHERE NOT EXISTS (SELECT 1 FROM characteristic_value WHERE value = 'HDMI / VGA / USB' AND characteristic_id = (SELECT id FROM characteristic WHERE name = 'Type de connexion'));


-- =============================================
-- 9. POSSEDE (pas de PK → WHERE NOT EXISTS)
-- =============================================
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PC-001'
    AND cv.value IN ('Apple M3 Pro', '18 Go', '512 Go SSD', 'macOS Sonoma')
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PC-002'
    AND cv.value IN ('Intel Core i7-13700H', '16 Go', '1 To SSD', 'Windows 11 Pro')
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-TAB-001'
    AND cv.value IN ('Apple M2', '8 Go', '256 Go', 'iPadOS 17')
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-001' AND cv.value = '2560x1440 (QHD)'
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-002' AND cv.value = '3840x2160 (4K UHD)'
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-001' AND cv.value = 'Bluetooth 5.0'
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-002' AND cv.value = 'USB + Bluetooth'
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);

INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-AV-001' AND cv.value IN ('3600 lumens', 'HDMI / VGA / USB')
    AND NOT EXISTS (SELECT 1 FROM possede po WHERE po.characteristic_value_id = cv.id AND po.equipment_id = e.id);


-- =============================================
-- 10. EMPRUNTS (WHERE NOT EXISTS sur requester+equipment+begin_date)
-- =============================================
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2024-09-02 08:00:00', '2024-09-06 18:00:00', '2024-09-06 17:30:00', 'TERMINE', '2024-09-06 17:30:00',
    (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-PC-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PC-001') AND begin_date = '2024-09-02 08:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2024-10-14 09:00:00', '2024-10-18 18:00:00', '2024-10-17 16:00:00', 'TERMINE', '2024-10-17 16:00:00',
    (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-TAB-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-TAB-001') AND begin_date = '2024-10-14 09:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2024-11-04 08:30:00', '2024-11-08 18:00:00', '2024-11-08 18:00:00', 'TERMINE', '2024-11-08 18:00:00',
    (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-PC-003')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PC-003') AND begin_date = '2024-11-04 08:30:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2024-12-09 09:00:00', '2024-12-13 18:00:00', '2024-12-12 14:00:00', 'TERMINE', '2024-12-12 14:00:00',
    (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-PER-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PER-001') AND begin_date = '2024-12-09 09:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-05 08:00:00', '2025-05-16 18:00:00', NULL, 'IN_PROGRESS', '2025-05-04 15:00:00',
    (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-PC-002')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PC-002') AND begin_date = '2025-05-05 08:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-06 09:00:00', '2025-05-20 18:00:00', NULL, 'IN_PROGRESS', '2025-05-05 10:30:00',
    (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-TAB-002')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-TAB-002') AND begin_date = '2025-05-06 09:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-07 08:00:00', '2025-05-14 18:00:00', NULL, 'IN_PROGRESS', '2025-05-06 09:00:00',
    (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-PER-002')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PER-002') AND begin_date = '2025-05-07 08:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-08 10:00:00', '2025-05-22 18:00:00', NULL, 'IN_PROGRESS', '2025-05-07 14:00:00',
    (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-PC-004')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PC-004') AND begin_date = '2025-05-08 10:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-19 08:00:00', '2025-05-23 18:00:00', NULL, 'VALID', '2025-05-12 08:00:00',
    (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr'),
    NULL,
    (SELECT id FROM equipment WHERE reference = 'REF-TAB-003')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-TAB-003') AND begin_date = '2025-05-19 08:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-20 09:00:00', '2025-05-27 18:00:00', NULL, 'VALID', '2025-05-12 09:30:00',
    (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
    NULL,
    (SELECT id FROM equipment WHERE reference = 'REF-ECR-002')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-ECR-002') AND begin_date = '2025-05-20 09:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-04-21 08:00:00', '2025-04-25 18:00:00', NULL, 'INVALID', '2025-04-18 11:00:00',
    (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-AV-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-AV-001') AND begin_date = '2025-04-21 08:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-01 09:00:00', '2025-05-05 18:00:00', NULL, 'INVALID', '2025-04-29 10:00:00',
    (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-ECR-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-ECR-001') AND begin_date = '2025-05-01 09:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-02-03 08:00:00', '2025-02-07 18:00:00', '2025-02-07 17:00:00', 'TERMINE', '2025-02-07 17:00:00',
    (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-ECR-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-ECR-001') AND begin_date = '2025-02-03 08:00:00');

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id)
  SELECT '2025-05-09 08:00:00', '2025-05-16 18:00:00', NULL, 'IN_PROGRESS', '2025-05-08 11:00:00',
    (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
    (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
    (SELECT id FROM equipment WHERE reference = 'REF-AV-001')
  WHERE NOT EXISTS (SELECT 1 FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-AV-001') AND begin_date = '2025-05-09 08:00:00');


-- =============================================
-- 11. STATUTS ÉQUIPEMENT (begin_status_date obligatoire)
-- =============================================
INSERT INTO status_equipment (begin_status_date, description_status, status_equipment_type, equipment_id)
  SELECT CURRENT_TIMESTAMP, 'Écran fissuré suite à une chute signalée par l''utilisateur.', 'OUT_OF_SERVICE',
    (SELECT id FROM equipment WHERE reference = 'REF-TAB-003')
  WHERE NOT EXISTS (SELECT 1 FROM status_equipment WHERE description_status = 'Écran fissuré suite à une chute signalée par l''utilisateur.');

INSERT INTO status_equipment (begin_status_date, description_status, status_equipment_type, equipment_id)
  SELECT CURRENT_TIMESTAMP, 'Batterie défectueuse — envoyé en réparation chez le prestataire.', 'UNDER_REPAIR',
    (SELECT id FROM equipment WHERE reference = 'REF-PC-003')
  WHERE NOT EXISTS (SELECT 1 FROM status_equipment WHERE description_status = 'Batterie défectueuse — envoyé en réparation chez le prestataire.');

INSERT INTO status_equipment (begin_status_date, description_status, status_equipment_type, end_status_date, equipment_id)
  SELECT '2024-11-10 00:00:00', 'Panne clavier — touche Entrée bloquée. Réparé en interne.', 'UNDER_REPAIR', '2024-11-15 12:00:00',
    (SELECT id FROM equipment WHERE reference = 'REF-PC-001')
  WHERE NOT EXISTS (SELECT 1 FROM status_equipment WHERE description_status = 'Panne clavier — touche Entrée bloquée. Réparé en interne.');

INSERT INTO status_equipment (begin_status_date, description_status, status_equipment_type, end_status_date, equipment_id)
  SELECT '2025-02-15 00:00:00', 'Connecteur HDMI défaillant. Remplacement du câble interne.', 'UNDER_REPAIR', '2025-02-20 09:00:00',
    (SELECT id FROM equipment WHERE reference = 'REF-ECR-001')
  WHERE NOT EXISTS (SELECT 1 FROM status_equipment WHERE description_status = 'Connecteur HDMI défaillant. Remplacement du câble interne.');

INSERT INTO status_equipment (begin_status_date, description_status, status_equipment_type, end_status_date, equipment_id)
  SELECT '2025-01-05 00:00:00', 'Souris inopérante — capteur laser HS. Mise au rebut.', 'OUT_OF_SERVICE', '2025-01-10 16:00:00',
    (SELECT id FROM equipment WHERE reference = 'REF-PER-001')
  WHERE NOT EXISTS (SELECT 1 FROM status_equipment WHERE description_status = 'Souris inopérante — capteur laser HS. Mise au rebut.');

INSERT INTO status_equipment (begin_status_date, description_status, status_equipment_type, equipment_id)
  SELECT CURRENT_TIMESTAMP, 'Lampe du projecteur en fin de vie — remplacement commandé.', 'UNDER_REPAIR',
    (SELECT id FROM equipment WHERE reference = 'REF-AV-001')
  WHERE NOT EXISTS (SELECT 1 FROM status_equipment WHERE description_status = 'Lampe du projecteur en fin de vie — remplacement commandé.');


-- =============================================
-- 12. ÉVÉNEMENTS (created_at obligatoire)
-- =============================================
INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Panne signalée : le laptop ne s''allume plus après une mise à jour forcée.', 'BREAKDOWN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr') AND status_type = 'TERMINE' AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PC-003') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Panne signalée : le laptop ne s''allume plus après une mise à jour forcée.');

INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Écran fissuré lors du transport — signalement par l''emprunteur.', 'BREAKDOWN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Écran fissuré lors du transport — signalement par l''emprunteur.');

INSERT INTO event (created_at, description, reading_date, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Retour anticipé — mission terminée plus tôt que prévu.', '2024-10-18 09:00:00', 'EARLY_RETURN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr') AND status_type = 'TERMINE' LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Retour anticipé — mission terminée plus tôt que prévu.');

INSERT INTO event (created_at, description, reading_date, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Retour anticipé — équipement non utilisé finalement.', '2024-12-13 10:00:00', 'EARLY_RETURN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PER-001') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Retour anticipé — équipement non utilisé finalement.');

INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Demande de prolongation de 5 jours — projet en cours non terminé.', 'EXTENSION',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Demande de prolongation de 5 jours — projet en cours non terminé.');

INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Demande de prolongation d''une semaine — formation reportée.', 'EXTENSION',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Demande de prolongation d''une semaine — formation reportée.');

INSERT INTO event (created_at, description, reading_date, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Prolongation accordée — présentation client reportée au vendredi.', '2024-09-04 11:00:00', 'EXTENSION',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr') AND status_type = 'TERMINE' LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Prolongation accordée — présentation client reportée au vendredi.');

INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Demande de prolongation de 3 jours — livrable en attente de validation.', 'EXTENSION',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Demande de prolongation de 3 jours — livrable en attente de validation.');

INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Panne signalée : tablette ne répond plus au tactile.', 'BREAKDOWN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Panne signalée : tablette ne répond plus au tactile.');

INSERT INTO event (created_at, description, reading_date, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Retour anticipé — congé maladie.', '2024-11-07 08:30:00', 'EARLY_RETURN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr') AND status_type = 'TERMINE' AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-PC-003') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Retour anticipé — congé maladie.');

INSERT INTO event (created_at, description, reading_date, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Retour anticipé — télétravail annulé, écran restitué avant terme.', '2025-02-07 11:00:00', 'EARLY_RETURN',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-ECR-001') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Retour anticipé — télétravail annulé, écran restitué avant terme.');

INSERT INTO event (created_at, description, type, loan_id)
  SELECT CURRENT_TIMESTAMP, 'Demande de prolongation de 2 jours — présentation repoussée.', 'EXTENSION',
    (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr') AND equipment_id = (SELECT id FROM equipment WHERE reference = 'REF-AV-001') LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM event WHERE description = 'Demande de prolongation de 2 jours — présentation repoussée.');


-- =============================================
-- 13. DOCUMENTS (added_date obligatoire)
-- =============================================
INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Manuel utilisateur MacBook Pro M3', 'https://support.apple.com/macbook-pro'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Manuel utilisateur MacBook Pro M3');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Guide de démarrage iPad Pro', 'https://support.apple.com/ipad-pro'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Guide de démarrage iPad Pro');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Documentation Dell XPS 15', 'https://www.dell.com/support/xps15'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Documentation Dell XPS 15');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Charte d''utilisation du matériel MNS', 'https://intranet.mns.fr/charte-materiel'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Charte d''utilisation du matériel MNS');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Procédure de signalement d''incident', 'https://intranet.mns.fr/procedure-incident'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Procédure de signalement d''incident');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Guide Logitech MX Keys', 'https://www.logitech.com/support/mx-keys'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Guide Logitech MX Keys');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Fiche technique Dell UltraSharp 27"', 'https://www.dell.com/support/ultrasharp27'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Fiche technique Dell UltraSharp 27"');

INSERT INTO doc (added_date, title, url)
  SELECT CURRENT_TIMESTAMP, 'Manuel projecteur Epson EB', 'https://www.epson.fr/support/eb-series'
  WHERE NOT EXISTS (SELECT 1 FROM doc WHERE title = 'Manuel projecteur Epson EB');


-- =============================================
-- 14. FAIT_REFERENCE (pas de PK → WHERE NOT EXISTS)
-- =============================================
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Manuel utilisateur MacBook Pro M3' AND e.reference = 'REF-PC-001'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Guide de démarrage iPad Pro' AND e.reference = 'REF-TAB-001'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Documentation Dell XPS 15' AND e.reference = 'REF-PC-002'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Charte d''utilisation du matériel MNS'
    AND e.reference IN ('REF-PC-001', 'REF-PC-002', 'REF-PC-003', 'REF-PC-004')
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Procédure de signalement d''incident'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Guide Logitech MX Keys' AND e.reference = 'REF-PER-002'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Fiche technique Dell UltraSharp 27"' AND e.reference = 'REF-ECR-001'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Manuel projecteur Epson EB' AND e.reference = 'REF-AV-001'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);
