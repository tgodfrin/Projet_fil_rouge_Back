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
-- 2. FAMILLES D'ÉQUIPEMENT (catégories fixes)
-- =============================================
INSERT INTO equipment_family (name_equipment_family) VALUES ('PC')             ON CONFLICT DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Écran')          ON CONFLICT DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Casque VR')      ON CONFLICT DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Vidéoprojecteur') ON CONFLICT DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Périphérique')   ON CONFLICT DO NOTHING;
INSERT INTO equipment_family (name_equipment_family) VALUES ('Autre')          ON CONFLICT DO NOTHING;


-- =============================================
-- 3. UTILISATEURS (2 gestionnaires, 5 collaborateurs, 3 intervenants, 2 stagiaires)
-- Mots de passe haches BCrypt $2a$ (compatible Spring Security BCryptPasswordEncoder) :
--   admin123 -> $2a$10$d3Lc5jqTd8Y5CfbEhQx/EOSeTL0ABEDjZTJkTKfRdzGMDgnC9EgdO
--   user123  -> $2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2
-- =============================================
INSERT INTO app_user (email, name, lastname, password, created_at, profil_id) VALUES
  -- Gestionnaires (mdp : admin123)
  ('jean.martin@mns.fr',    'Jean',    'Martin',   '$2a$10$d3Lc5jqTd8Y5CfbEhQx/EOSeTL0ABEDjZTJkTKfRdzGMDgnC9EgdO', '2024-09-01 08:00:00', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE')),
  ('sophie.leblanc@mns.fr', 'Sophie',  'Leblanc',  '$2a$10$d3Lc5jqTd8Y5CfbEhQx/EOSeTL0ABEDjZTJkTKfRdzGMDgnC9EgdO', '2024-09-01 08:00:00', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE')),
  -- Collaborateurs (mdp : user123)
  ('thomas.dupont@mns.fr',  'Thomas',  'Dupont',   '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('marie.leroy@mns.fr',    'Marie',   'Leroy',    '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('lucas.bernard@mns.fr',  'Lucas',   'Bernard',  '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('emma.petit@mns.fr',     'Emma',    'Petit',    '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('nathan.durand@mns.fr',  'Nathan',  'Durand',   '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  -- Intervenants (mdp : user123)
  ('pierre.moreau@mns.fr',  'Pierre',  'Moreau',   '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-03 09:00:00', (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  ('laura.simon@mns.fr',    'Laura',   'Simon',    '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-03 09:00:00', (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  ('hugo.michel@mns.fr',    'Hugo',    'Michel',   '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-03 09:00:00', (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  -- Stagiaires (mdp : user123)
  ('camille.robert@mns.fr', 'Camille', 'Robert',   '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-04 09:00:00', (SELECT id FROM profil WHERE type = 'STAGIAIRE')),
  ('alexis.laurent@mns.fr', 'Alexis',  'Laurent',  '$2a$10$YhwDOU96SOvontBIj6swEuVwPawVTwgXFB.UeqnmAk65NcSmh47G2', '2024-09-04 09:00:00', (SELECT id FROM profil WHERE type = 'STAGIAIRE'));


-- =============================================
-- 4. ÉQUIPEMENTS (3 par catégorie = 18 équipements)
-- =============================================
INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES

  -- PC (3)
  ('REF-PC-001', 'MacBook Pro M3',          'Salle B204',       '2023-09-01',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'PC')),
  ('REF-PC-002', 'Dell XPS 15',             'Salle A101',       '2023-06-15',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'PC')),
  ('REF-PC-003', 'Lenovo ThinkPad X1',      'Salle C302',       '2022-11-20',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'PC')),

  -- Écran (3)
  ('REF-ECR-001', 'Dell UltraSharp 27"',    'Salle B204',       '2021-12-01',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran')),
  ('REF-ECR-002', 'LG 4K 32"',              'Salle A101',       '2022-04-18',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran')),
  ('REF-ECR-003', 'Samsung 24" FHD',        'Salle C302',       '2023-03-10',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran')),

  -- Casque VR (3)
  ('REF-VR-001', 'Meta Quest 3',            'Salle VR',         '2024-01-15',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Casque VR')),
  ('REF-VR-002', 'HTC Vive Pro 2',          'Salle VR',         '2023-10-05',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Casque VR')),
  ('REF-VR-003', 'PlayStation VR2',         'Salle VR',         '2024-03-20',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Casque VR')),

  -- Vidéoprojecteur (3)
  ('REF-VP-001', 'Epson EB-X51',            'Salle de réunion', '2021-06-10',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Vidéoprojecteur')),
  ('REF-VP-002', 'BenQ MH550',              'Amphithéâtre',     '2022-09-01',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Vidéoprojecteur')),
  ('REF-VP-003', 'Optoma HD28HDR',          'Salle B204',       '2023-05-15',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Vidéoprojecteur')),

  -- Périphérique (3)
  ('REF-PER-001', 'Magic Mouse Apple',      'Stock',            '2023-02-28',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique')),
  ('REF-PER-002', 'Clavier Logitech MX',    'Stock',            '2023-02-28',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique')),
  ('REF-PER-003', 'Webcam Logitech C920',   'Stock',            '2023-07-12',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique')),

  -- Autre (3)
  ('REF-AUT-001', 'Trépied caméra',         'Stock',            '2022-01-10',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Autre')),
  ('REF-AUT-002', 'Rallonge multiprise 5m', 'Stock',            '2021-05-20',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Autre')),
  ('REF-AUT-003', 'Valise de transport PC', 'Stock',            '2023-08-15',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Autre'));


-- =============================================
-- 5. CAN_LOAN (pas de PK sur la table de jointure → WHERE NOT EXISTS)
-- =============================================
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'GESTIONNAIRE'
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);

-- COLLABORATEUR : toutes les familles
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'COLLABORATEUR'
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);

-- INTERVENANT : PC, Écran, Vidéoprojecteur, Périphérique (pas Casque VR, pas Autre)
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'INTERVENANT'
    AND ef.name_equipment_family IN ('PC', 'Écran', 'Vidéoprojecteur', 'Périphérique')
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);

-- STAGIAIRE : PC, Périphérique, Autre
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'STAGIAIRE'
    AND ef.name_equipment_family IN ('PC', 'Périphérique', 'Autre')
    AND NOT EXISTS (SELECT 1 FROM can_loan cl WHERE cl.profil_id = p.id AND cl.equipment_family_id = ef.id);


-- =============================================
-- 6. CARACTÉRISTIQUES
-- =============================================
INSERT INTO characteristic (name) VALUES
  ('Processeur'),
  ('RAM'),
  ('Stockage'),
  ('Système d''exploitation'),
  ('Résolution'),
  ('Connectivité'),
  ('Luminosité'),
  ('Type de connexion');


-- =============================================
-- 7. EST_CONSTITUE (pas de PK → WHERE NOT EXISTS)
-- =============================================
-- PC : Processeur, RAM, Stockage, OS
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'PC'
    AND c.name IN ('Processeur', 'RAM', 'Stockage', 'Système d''exploitation');

INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Écran' AND c.name = 'Résolution';

-- Périphérique : Connectivité
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Périphérique' AND c.name = 'Connectivité';

-- Vidéoprojecteur : Luminosité, Type de connexion
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Vidéoprojecteur'
    AND c.name IN ('Luminosité', 'Type de connexion');


-- =============================================
-- 8. VALEURS DE CARACTÉRISTIQUES
-- =============================================
-- PC : MacBook Pro M3
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('Apple M3 Pro',      '2023-09-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'Processeur')),
  ('18 Go',             '2023-09-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'RAM')),
  ('512 Go SSD',        '2023-09-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'Stockage')),
  ('macOS Sonoma',      '2023-09-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- PC : Dell XPS 15
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('Intel Core i7-13700H', '2023-06-15 00:00:00', (SELECT id FROM characteristic WHERE name = 'Processeur')),
  ('16 Go',                '2023-06-15 00:00:00', (SELECT id FROM characteristic WHERE name = 'RAM')),
  ('1 To SSD',             '2023-06-15 00:00:00', (SELECT id FROM characteristic WHERE name = 'Stockage')),
  ('Windows 11 Pro',       '2023-06-15 00:00:00', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- PC : Lenovo ThinkPad X1
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('Intel Core i5-1235U', '2022-11-20 00:00:00', (SELECT id FROM characteristic WHERE name = 'Processeur')),
  ('8 Go',                '2022-11-20 00:00:00', (SELECT id FROM characteristic WHERE name = 'RAM')),
  ('256 Go SSD',          '2022-11-20 00:00:00', (SELECT id FROM characteristic WHERE name = 'Stockage')),
  ('Windows 10 Pro',      '2022-11-20 00:00:00', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- Écran : Dell UltraSharp 27"
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('2560x1440 (QHD)', '2021-12-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'Résolution'));

-- Écran : LG 4K 32"
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('3840x2160 (4K UHD)', '2022-04-18 00:00:00', (SELECT id FROM characteristic WHERE name = 'Résolution'));

-- Écran : Samsung 24"
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('1920x1080 (FHD)', '2023-03-10 00:00:00', (SELECT id FROM characteristic WHERE name = 'Résolution'));

-- Périphérique : Magic Mouse
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('Bluetooth 5.0', '2023-02-28 00:00:00', (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Périphérique : Clavier Logitech MX
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('USB + Bluetooth', '2023-02-28 00:00:00', (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Périphérique : Webcam Logitech
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('USB-A', '2023-07-12 00:00:00', (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Vidéoprojecteur : Epson EB-X51
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('3600 lumens',       '2021-06-10 00:00:00', (SELECT id FROM characteristic WHERE name = 'Luminosité')),
  ('HDMI / VGA / USB',  '2021-06-10 00:00:00', (SELECT id FROM characteristic WHERE name = 'Type de connexion'));

-- Vidéoprojecteur : BenQ MH550
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('3500 lumens',           '2022-09-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'Luminosité')),
  ('HDMI x2 / VGA / USB-A', '2022-09-01 00:00:00', (SELECT id FROM characteristic WHERE name = 'Type de connexion'));

-- Vidéoprojecteur : Optoma HD28HDR
INSERT INTO characteristic_value (value, begin_date, characteristic_id) VALUES
  ('3000 lumens',      '2023-05-15 00:00:00', (SELECT id FROM characteristic WHERE name = 'Luminosité')),
  ('HDMI / MHL / USB', '2023-05-15 00:00:00', (SELECT id FROM characteristic WHERE name = 'Type de connexion'));


-- =============================================
-- 9. POSSEDE — valeurs de caractéristiques par équipement
-- =============================================
-- MacBook Pro M3
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PC-001'
    AND cv.value IN ('Apple M3 Pro', '18 Go', '512 Go SSD', 'macOS Sonoma');

-- Dell XPS 15
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PC-002'
    AND cv.value IN ('Intel Core i7-13700H', '16 Go', '1 To SSD', 'Windows 11 Pro');

-- Lenovo ThinkPad X1
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PC-003'
    AND cv.value IN ('Intel Core i5-1235U', '8 Go', '256 Go SSD', 'Windows 10 Pro');

-- Dell UltraSharp
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-001' AND cv.value = '2560x1440 (QHD)';

-- LG 4K
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-002' AND cv.value = '3840x2160 (4K UHD)';

-- Samsung 24"
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-003' AND cv.value = '1920x1080 (FHD)';

-- Magic Mouse
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-001' AND cv.value = 'Bluetooth 5.0';

-- Clavier Logitech MX
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-002' AND cv.value = 'USB + Bluetooth';

-- Webcam Logitech
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-003' AND cv.value = 'USB-A';

-- Epson EB-X51
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-VP-001' AND cv.value IN ('3600 lumens', 'HDMI / VGA / USB');

-- BenQ MH550
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-VP-002' AND cv.value IN ('3500 lumens', 'HDMI x2 / VGA / USB-A');

-- Optoma HD28HDR
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-VP-003' AND cv.value IN ('3000 lumens', 'HDMI / MHL / USB');


-- =============================================
-- 10. STATUTS ÉQUIPEMENT (incidents / réparations actifs)
-- =============================================
-- PC-003 en réparation (batterie)
INSERT INTO status_equipment (description_status, status_equipment_type, begin_status_date, equipment_id) VALUES
  ('Batterie défectueuse — envoyé en réparation chez le prestataire.', 'UNDER_REPAIR',
   '2026-05-10 09:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-PC-003'));

-- VR-002 hors service (lentille fissurée)
INSERT INTO status_equipment (description_status, status_equipment_type, begin_status_date, equipment_id) VALUES
  ('Lentille gauche fissurée suite à une chute — équipement hors service.', 'OUT_OF_SERVICE',
   '2026-04-22 14:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-VR-002'));

-- VP-001 en réparation (lampe en fin de vie)
INSERT INTO status_equipment (description_status, status_equipment_type, begin_status_date, equipment_id) VALUES
  ('Lampe du projecteur en fin de vie — remplacement commandé.', 'UNDER_REPAIR',
   '2026-05-05 10:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-VP-001'));

-- Incidents résolus (end_status_date renseignée)
INSERT INTO status_equipment (description_status, status_equipment_type, begin_status_date, end_status_date, equipment_id) VALUES
  ('Connecteur HDMI défaillant. Remplacement du câble interne.', 'UNDER_REPAIR',
   '2025-09-10 08:00:00', '2025-09-20 09:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));

INSERT INTO status_equipment (description_status, status_equipment_type, begin_status_date, end_status_date, equipment_id) VALUES
  ('Panne clavier — touche Entrée bloquée. Réparé en interne.', 'UNDER_REPAIR',
   '2025-11-05 10:00:00', '2025-11-15 12:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

INSERT INTO status_equipment (description_status, status_equipment_type, begin_status_date, end_status_date, equipment_id) VALUES
  ('Souris inopérante — capteur laser HS. Remplacée.', 'OUT_OF_SERVICE',
   '2025-10-01 09:00:00', '2025-10-10 16:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));


-- =============================================
-- 11. EMPRUNTS
-- Toutes les dates sont relatives à CURRENT_DATE → se recalculent à chaque démarrage
-- =============================================

-- -----------------------------------------------
-- A. EMPRUNTS PASSÉS — TERMINE
-- -----------------------------------------------
-- Thomas Dupont — PC-001 (J-270 à J-263)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '270 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '263 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '263 days' + TIME '17:30:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '263 days' + TIME '17:30:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

-- Marie Leroy — ECR-002 (J-240 à J-236)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '240 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '236 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '237 days' + TIME '16:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '237 days' + TIME '16:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-002'));

-- Lucas Bernard — PC-002 (J-226 à J-222, avec incident BREAKDOWN)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '226 days' + TIME '08:30:00',
   CURRENT_DATE - INTERVAL '222 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '222 days' + TIME '18:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '222 days' + TIME '18:00:00',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-002'));

-- Emma Petit — VR-001 (J-212 à J-208)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '212 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '208 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '208 days' + TIME '17:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '208 days' + TIME '17:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-001'));

-- Nathan Durand — PER-002 (J-198 à J-194)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '198 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '194 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '194 days' + TIME '16:30:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '194 days' + TIME '16:30:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-002'));

-- Pierre Moreau — VP-002 (J-185 à J-181, avec EARLY_RETURN)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '185 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '181 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '183 days' + TIME '14:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '183 days' + TIME '14:00:00',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-002'));

-- Laura Simon — PER-001 (J-177 à J-173)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '177 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '173 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '173 days' + TIME '18:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '173 days' + TIME '18:00:00',
   (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));

-- Hugo Michel — PC-001 (J-143 à J-139, avec BREAKDOWN)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '143 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '139 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '139 days' + TIME '18:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '139 days' + TIME '18:00:00',
   (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

-- Camille Robert — AUT-001 (J-134 à J-130)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '134 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '130 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '130 days' + TIME '17:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '130 days' + TIME '17:00:00',
   (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-001'));

-- Alexis Laurent — AUT-002 (J-120 à J-116)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '120 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '116 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '116 days' + TIME '18:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '116 days' + TIME '18:00:00',
   (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-002'));

-- Thomas Dupont — 2ème emprunt, ECR-001 (J-106 à J-102)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '106 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '102 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '102 days' + TIME '17:30:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '102 days' + TIME '17:30:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));

-- Marie Leroy — VR-001 (J-93 à J-89, avec EXTENSION)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '93 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '89 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '88 days' + TIME '10:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '88 days' + TIME '10:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-001'));

-- Lucas Bernard — VP-003 (J-79 à J-75)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '79 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '75 days' + TIME '18:00:00',
   CURRENT_DATE - INTERVAL '75 days' + TIME '18:00:00',
   'TERMINE',
   CURRENT_DATE - INTERVAL '75 days' + TIME '18:00:00',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-003'));

-- -----------------------------------------------
-- B. EMPRUNTS EN RETARD — IN_PROGRESS avec end_date dépassée
-- -----------------------------------------------
-- Emma Petit — ECR-003 (commencé J-10, devait finir J-3 → en retard)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '10 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '3 days' + TIME '18:00:00',
   NULL, 'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '12 days' + TIME '10:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-003'));

-- Hugo Michel — PER-003 (commencé J-7, devait finir J-2 → en retard)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '7 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00',
   NULL, 'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '9 days' + TIME '14:00:00',
   (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-003'));

-- -----------------------------------------------
-- C. EMPRUNTS EN COURS — IN_PROGRESS (end_date >= aujourd'hui)
-- -----------------------------------------------
-- Thomas Dupont — PC-002 (commencé J-2, finit J+10)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '2 days' + TIME '08:00:00',
   CURRENT_DATE + INTERVAL '10 days' + TIME '18:00:00',
   NULL, 'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '3 days' + TIME '15:00:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-002'));

-- Nathan Durand — VR-003 (commencé J-1, finit J+14)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '1 day' + TIME '09:00:00',
   CURRENT_DATE + INTERVAL '14 days' + TIME '18:00:00',
   NULL, 'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '2 days' + TIME '10:30:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-003'));

-- Pierre Moreau — PER-002 (commencé J-1, finit J+8)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '1 day' + TIME '08:00:00',
   CURRENT_DATE + INTERVAL '8 days' + TIME '18:00:00',
   NULL, 'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-002'));

-- -----------------------------------------------
-- C bis. EMPRUNTS EN RETARD — VALID dont endDate est dépassée (pour les alertes retards)
-- -----------------------------------------------
-- Nathan Durand — PER-001 (devait finir J-5 → retard)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '12 days' + TIME '08:00:00',
   CURRENT_DATE - INTERVAL '5 days' + TIME '18:00:00',
   NULL, 'VALID',
   CURRENT_DATE - INTERVAL '14 days' + TIME '10:00:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));

-- Emma Petit — ECR-001 (devait finir J-3 → retard)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '10 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '3 days' + TIME '18:00:00',
   NULL, 'VALID',
   CURRENT_DATE - INTERVAL '12 days' + TIME '11:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));

-- -----------------------------------------------
-- D. EMPRUNTS FUTURS — VALID (begin_date dans le futur, approuvés)
-- -----------------------------------------------
-- Camille Robert — AUT-003 (J+4 à J+6)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '4 days' + TIME '08:00:00',
   CURRENT_DATE + INTERVAL '6 days' + TIME '18:00:00',
   NULL, 'VALID',
   CURRENT_DATE - INTERVAL '1 day' + TIME '08:00:00',
   (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-003'));

-- Laura Simon — PC-001 (J+7 à J+11)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '7 days' + TIME '09:00:00',
   CURRENT_DATE + INTERVAL '11 days' + TIME '18:00:00',
   NULL, 'VALID',
   CURRENT_DATE - INTERVAL '1 day' + TIME '09:30:00',
   (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

-- Alexis Laurent — VP-002 (J+14 à J+18)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '14 days' + TIME '08:00:00',
   CURRENT_DATE + INTERVAL '18 days' + TIME '18:00:00',
   NULL, 'VALID',
   CURRENT_DATE - INTERVAL '1 day' + TIME '10:00:00',
   (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-002'));

-- -----------------------------------------------
-- E. DEMANDES EN ATTENTE — IN_PROGRESS sans validator (en attente de validation)
-- -----------------------------------------------
-- Marie Leroy — ECR-002 (J+2 à J+6)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '2 days' + TIME '08:00:00',
   CURRENT_DATE + INTERVAL '6 days' + TIME '18:00:00',
   NULL, 'IN_PROGRESS',
   CURRENT_DATE + TIME '08:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   NULL,
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-002'));

-- -----------------------------------------------
-- F. EMPRUNTS REFUSÉS
-- -----------------------------------------------
-- Thomas Dupont — VP-001 (refusé, équipement en réparation)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '3 days' + TIME '08:00:00',
   CURRENT_DATE + INTERVAL '7 days' + TIME '18:00:00',
   NULL, 'INVALID',
   CURRENT_DATE - INTERVAL '1 day' + TIME '11:00:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-001'));


-- =============================================
-- 12. ÉVÉNEMENTS (liés aux emprunts)
-- =============================================
-- BREAKDOWN — Lucas Bernard sur PC-002 (emprunt passé J-226)
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Panne signalée : le laptop ne s''allume plus après une mise à jour forcée.',
   CURRENT_DATE - INTERVAL '224 days' + TIME '10:00:00', 'BREAKDOWN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-PC-002')
    ORDER BY begin_date ASC LIMIT 1));

-- BREAKDOWN — Hugo Michel sur PC-001 (emprunt passé J-143)
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Panne signalée : tablette ne répond plus après mise en veille prolongée.',
   CURRENT_DATE - INTERVAL '141 days' + TIME '11:00:00', 'BREAKDOWN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-PC-001')
    ORDER BY begin_date ASC LIMIT 1));

-- EARLY_RETURN — Pierre Moreau sur VP-002 (emprunt passé J-185)
INSERT INTO event (description, created_at, reading_date, type, loan_id) VALUES
  ((CURRENT_DATE - 183)::text || '|Conférence annulée.',
   CURRENT_DATE - INTERVAL '183 days' + TIME '14:00:00',
   CURRENT_DATE - INTERVAL '183 days' + TIME '14:00:00',
   'EARLY_RETURN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-VP-002')
    ORDER BY begin_date ASC LIMIT 1));

-- EARLY_RETURN — Marie Leroy sur ECR-002 (emprunt passé J-240)
INSERT INTO event (description, created_at, reading_date, type, loan_id) VALUES
  ((CURRENT_DATE - 237)::text || '|Mission terminée plus tôt que prévu.',
   CURRENT_DATE - INTERVAL '237 days' + TIME '10:00:00',
   CURRENT_DATE - INTERVAL '237 days' + TIME '10:00:00',
   'EARLY_RETURN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-ECR-002')
    ORDER BY begin_date ASC LIMIT 1));

-- EXTENSION — Marie Leroy sur VR-001 (emprunt passé J-93)
INSERT INTO event (description, created_at, reading_date, type, loan_id) VALUES
  ((CURRENT_DATE - 88)::text || '|Présentation client reportée (prolongation 1 jour).',
   CURRENT_DATE - INTERVAL '89 days' + TIME '09:00:00',
   CURRENT_DATE - INTERVAL '89 days' + TIME '09:00:00',
   'EXTENSION',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-VR-001')
    ORDER BY begin_date ASC LIMIT 1));

-- EXTENSION — Thomas Dupont sur PC-002 (emprunt en cours J-2)
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ((CURRENT_DATE + 13)::text || '|Projet en cours non terminé (prolongation 3 jours).',
   CURRENT_DATE - INTERVAL '1 day' + TIME '09:00:00', 'EXTENSION',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-PC-002')
    ORDER BY begin_date DESC LIMIT 1));

-- BREAKDOWN — Emma Petit sur ECR-003 (emprunt en retard J-10)
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Incident signalé : pixel mort détecté sur l''angle inférieur droit.',
   CURRENT_DATE - INTERVAL '8 days' + TIME '11:00:00', 'BREAKDOWN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-ECR-003')
    ORDER BY begin_date DESC LIMIT 1));


-- =============================================
-- 13. DOCUMENTS
-- =============================================
INSERT INTO doc (title, url, added_date) VALUES
  ('Manuel utilisateur MacBook Pro M3',         'https://support.apple.com/macbook-pro',          '2023-09-01 00:00:00'),
  ('Documentation Dell XPS 15',                 'https://www.dell.com/support/xps15',             '2023-06-15 00:00:00'),
  ('Guide Logitech MX Keys',                    'https://www.logitech.com/support/mx-keys',       '2023-02-28 00:00:00'),
  ('Fiche technique Dell UltraSharp 27"',       'https://www.dell.com/support/ultrasharp27',      '2021-12-01 00:00:00'),
  ('Manuel Meta Quest 3',                       'https://www.meta.com/support/quest3',            '2024-01-15 00:00:00'),
  ('Guide Epson EB-X51',                        'https://www.epson.fr/support/eb-series',         '2021-06-10 00:00:00'),
  ('Charte d''utilisation du matériel MNS',     'https://intranet.mns.fr/charte-materiel',        '2024-09-01 00:00:00'),
  ('Procédure de signalement d''incident',      'https://intranet.mns.fr/procedure-incident',     '2024-09-01 00:00:00');


-- =============================================
-- 14. FAIT_REFERENCE — docs liés aux équipements
-- =============================================
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Manuel utilisateur MacBook Pro M3' AND e.reference = 'REF-PC-001';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Documentation Dell XPS 15' AND e.reference = 'REF-PC-002';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Documentation Dell XPS 15' AND e.reference = 'REF-PC-002'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Manuel Meta Quest 3' AND e.reference = 'REF-VR-001';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Guide Epson EB-X51' AND e.reference = 'REF-VP-001';

-- La charte s'applique à tous les PC
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Charte d''utilisation du matériel MNS'
    AND e.reference IN ('REF-PC-001', 'REF-PC-002', 'REF-PC-003');

-- La procédure incident s'applique à tout le parc
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Procédure de signalement d''incident';


-- =============================================
-- 15. ÉQUIPEMENTS SUPPLÉMENTAIRES (1 par catégorie = 6 de plus)
-- =============================================
INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  ('REF-PC-004',  'HP EliteBook 840',         'Salle A101',       '2024-02-10',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'PC')),
  ('REF-ECR-004', 'AOC 27" QHD',              'Salle B204',       '2024-01-20',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran')),
  ('REF-VR-004',  'Meta Quest Pro',            'Salle VR',         '2024-06-01',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Casque VR')),
  ('REF-VP-004',  'Acer X1526HK',             'Salle de réunion', '2024-03-15',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Vidéoprojecteur')),
  ('REF-PER-004', 'Hub USB-C 7 ports',        'Stock',            '2024-04-05',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique')),
  ('REF-AUT-004', 'Micro-cravate Rode',       'Stock',            '2024-05-12',
   (SELECT id FROM equipment_family WHERE name_equipment_family = 'Autre'));


-- =============================================
-- 16. EMPRUNTS SUPPLÉMENTAIRES (passé / présent / futur)
-- =============================================

-- Thomas Dupont — PC-004 (J-60 à J-55, terminé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '60 days',
   CURRENT_DATE - INTERVAL '55 days',
   CURRENT_DATE - INTERVAL '55 days',
   'TERMINE',
   CURRENT_DATE - INTERVAL '55 days',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-004'));

-- Marie Leroy — VR-004 (J-45 à J-40, terminé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '45 days',
   CURRENT_DATE - INTERVAL '40 days',
   CURRENT_DATE - INTERVAL '40 days',
   'TERMINE',
   CURRENT_DATE - INTERVAL '40 days',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-004'));

-- Lucas Bernard — ECR-004 (J-30 à J-25, terminé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '30 days',
   CURRENT_DATE - INTERVAL '25 days',
   CURRENT_DATE - INTERVAL '25 days',
   'TERMINE',
   CURRENT_DATE - INTERVAL '25 days',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-004'));

-- Emma Petit — PER-004 (en cours, J-3 à J+5)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '3 days',
   CURRENT_DATE + INTERVAL '5 days',
   NULL,
   'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '4 days',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-004'));

-- Nathan Durand — AUT-004 (en cours, J-1 à J+3)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE - INTERVAL '1 day',
   CURRENT_DATE + INTERVAL '3 days',
   NULL,
   'IN_PROGRESS',
   CURRENT_DATE - INTERVAL '2 days',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-004'));

-- Pierre Moreau — VP-004 (futur, J+5 à J+9, validé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '5 days',
   CURRENT_DATE + INTERVAL '9 days',
   NULL,
   'VALID',
   CURRENT_DATE - INTERVAL '1 day',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-004'));

-- Laura Simon — PC-004 (futur, J+10 à J+15, validé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '10 days',
   CURRENT_DATE + INTERVAL '15 days',
   NULL,
   'VALID',
   CURRENT_DATE,
   (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-004'));

-- Camille Robert — ECR-004 (futur, J+20 à J+24, validé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '20 days',
   CURRENT_DATE + INTERVAL '24 days',
   NULL,
   'VALID',
   CURRENT_DATE,
   (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-004'));

-- Hugo Michel — VR-004 (en attente de validation, J+3 à J+7)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  (CURRENT_DATE + INTERVAL '3 days',
   CURRENT_DATE + INTERVAL '7 days',
   NULL,
   'IN_PROGRESS',
   CURRENT_DATE,
   (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
   NULL,
   (SELECT id FROM equipment WHERE reference = 'REF-VR-004'));
