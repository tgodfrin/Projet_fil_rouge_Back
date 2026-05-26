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
-- Mots de passe haches BCrypt :
--   admin123 -> $2b$10$PsWybdIaWo/oud8qrXBJL.Q9ByKvP6E/ZMw0hPUzuf2ElNEHMdFJC
--   user123  -> $2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS
-- =============================================
INSERT INTO app_user (email, name, lastname, password, created_at, profil_id) VALUES
  -- Gestionnaires (mdp : admin123)
  ('jean.martin@mns.fr',    'Jean',    'Martin',   '$2b$10$PsWybdIaWo/oud8qrXBJL.Q9ByKvP6E/ZMw0hPUzuf2ElNEHMdFJC', '2024-09-01 08:00:00', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE')),
  ('sophie.leblanc@mns.fr', 'Sophie',  'Leblanc',  '$2b$10$PsWybdIaWo/oud8qrXBJL.Q9ByKvP6E/ZMw0hPUzuf2ElNEHMdFJC', '2024-09-01 08:00:00', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE')),
  -- Collaborateurs (mdp : user123)
  ('thomas.dupont@mns.fr',  'Thomas',  'Dupont',   '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('marie.leroy@mns.fr',    'Marie',   'Leroy',    '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('lucas.bernard@mns.fr',  'Lucas',   'Bernard',  '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('emma.petit@mns.fr',     'Emma',    'Petit',    '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('nathan.durand@mns.fr',  'Nathan',  'Durand',   '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-02 09:00:00', (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  -- Intervenants (mdp : user123)
  ('pierre.moreau@mns.fr',  'Pierre',  'Moreau',   '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-03 09:00:00', (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  ('laura.simon@mns.fr',    'Laura',   'Simon',    '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-03 09:00:00', (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  ('hugo.michel@mns.fr',    'Hugo',    'Michel',   '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-03 09:00:00', (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  -- Stagiaires (mdp : user123)
  ('camille.robert@mns.fr', 'Camille', 'Robert',   '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-04 09:00:00', (SELECT id FROM profil WHERE type = 'STAGIAIRE')),
  ('alexis.laurent@mns.fr', 'Alexis',  'Laurent',  '$2b$10$WpSR9zSHzR/o1oKfL9fWZuQzxIVfoBzXFG6L0m2m/mbkCvxmJS3aS', '2024-09-04 09:00:00', (SELECT id FROM profil WHERE type = 'STAGIAIRE'));


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

-- COLLABORATEUR : PC, Écran, Casque VR, Vidéoprojecteur, Périphérique
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'COLLABORATEUR'
    AND ef.name_equipment_family IN ('PC', 'Écran', 'Casque VR', 'Vidéoprojecteur', 'Périphérique');

-- INTERVENANT : PC, Périphérique, Casque VR
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'INTERVENANT'
    AND ef.name_equipment_family IN ('PC', 'Périphérique', 'Casque VR');

-- STAGIAIRE : Périphérique, Autre
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'STAGIAIRE'
    AND ef.name_equipment_family IN ('Périphérique', 'Autre');


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
-- =============================================

-- -----------------------------------------------
-- A. EMPRUNTS PASSÉS — TERMINE (pour tous les users)
-- -----------------------------------------------
-- Thomas Dupont — PC-001
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-09-02 08:00:00', '2025-09-09 18:00:00', '2025-09-09 17:30:00', 'TERMINE',
   '2025-09-09 17:30:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

-- Marie Leroy — ECR-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-10-06 09:00:00', '2025-10-10 18:00:00', '2025-10-09 16:00:00', 'TERMINE',
   '2025-10-09 16:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-002'));

-- Lucas Bernard — PC-002 (avec incident BREAKDOWN)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-10-20 08:30:00', '2025-10-24 18:00:00', '2025-10-24 18:00:00', 'TERMINE',
   '2025-10-24 18:00:00',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-002'));

-- Emma Petit — VR-001
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-11-03 09:00:00', '2025-11-07 18:00:00', '2025-11-07 17:00:00', 'TERMINE',
   '2025-11-07 17:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-001'));

-- Nathan Durand — PER-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-11-17 08:00:00', '2025-11-21 18:00:00', '2025-11-21 16:30:00', 'TERMINE',
   '2025-11-21 16:30:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-002'));

-- Pierre Moreau — VP-002 (avec EARLY_RETURN)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-12-01 09:00:00', '2025-12-05 18:00:00', '2025-12-03 14:00:00', 'TERMINE',
   '2025-12-03 14:00:00',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-002'));

-- Laura Simon — PER-001
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-12-08 08:00:00', '2025-12-12 18:00:00', '2025-12-12 18:00:00', 'TERMINE',
   '2025-12-12 18:00:00',
   (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));

-- Hugo Michel — PC-001 (avec BREAKDOWN)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-01-12 08:00:00', '2026-01-16 18:00:00', '2026-01-16 18:00:00', 'TERMINE',
   '2026-01-16 18:00:00',
   (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

-- Camille Robert — AUT-001
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-01-20 09:00:00', '2026-01-24 18:00:00', '2026-01-24 17:00:00', 'TERMINE',
   '2026-01-24 17:00:00',
   (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-001'));

-- Alexis Laurent — AUT-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-02-03 08:00:00', '2026-02-07 18:00:00', '2026-02-07 18:00:00', 'TERMINE',
   '2026-02-07 18:00:00',
   (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-002'));

-- Thomas Dupont — 2ème emprunt, ECR-001
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-02-16 09:00:00', '2026-02-20 18:00:00', '2026-02-20 17:30:00', 'TERMINE',
   '2026-02-20 17:30:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));

-- Marie Leroy — VR-001 (avec EXTENSION)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-03-02 08:00:00', '2026-03-06 18:00:00', '2026-03-07 10:00:00', 'TERMINE',
   '2026-03-07 10:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-001'));

-- Lucas Bernard — VP-003
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-03-16 09:00:00', '2026-03-20 18:00:00', '2026-03-20 18:00:00', 'TERMINE',
   '2026-03-20 18:00:00',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-003'));

-- -----------------------------------------------
-- B. EMPRUNTS EN RETARD — IN_PROGRESS avec end_date dépassée
-- -----------------------------------------------
-- Emma Petit — ECR-003 (end_date dépassée → retard)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-04-28 08:00:00', '2026-05-05 18:00:00', NULL, 'IN_PROGRESS',
   '2026-04-25 10:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-003'));

-- Hugo Michel — PER-003 (end_date dépassée → retard)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-01 09:00:00', '2026-05-08 18:00:00', NULL, 'IN_PROGRESS',
   '2026-04-29 14:00:00',
   (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-003'));

-- -----------------------------------------------
-- C. EMPRUNTS EN COURS — IN_PROGRESS (non en retard, end_date >= aujourd'hui)
-- -----------------------------------------------
-- Thomas Dupont — PC-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-12 08:00:00', '2026-05-23 18:00:00', NULL, 'IN_PROGRESS',
   '2026-05-11 15:00:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-002'));

-- Nathan Durand — VR-003
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-13 09:00:00', '2026-05-27 18:00:00', NULL, 'IN_PROGRESS',
   '2026-05-12 10:30:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VR-003'));

-- Pierre Moreau — PER-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-14 08:00:00', '2026-05-21 18:00:00', NULL, 'IN_PROGRESS',
   '2026-05-13 09:00:00',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-002'));

-- -----------------------------------------------
-- C bis. EMPRUNTS EN RETARD — VALID dont endDate est dépassée (pour les alertes retards)
-- -----------------------------------------------
-- Nathan Durand — PERI-001 (retard de 7 jours)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-06 08:00:00', '2026-05-13 18:00:00', NULL, 'VALID',
   '2026-05-05 10:00:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));

-- Emma Petit — ECR-003 (retard de 4 jours)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-10 09:00:00', '2026-05-16 18:00:00', NULL, 'VALID',
   '2026-05-09 11:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-003'));

-- -----------------------------------------------
-- D. EMPRUNTS FUTURS — VALID (begin_date dans le futur, approuvés)
-- -----------------------------------------------
-- Camille Robert — AUT-003
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-20 08:00:00', '2026-05-22 18:00:00', NULL, 'VALID',
   '2026-05-14 08:00:00',
   (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AUT-003'));

-- Laura Simon — PC-001
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-26 09:00:00', '2026-05-30 18:00:00', NULL, 'VALID',
   '2026-05-14 09:30:00',
   (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

-- Alexis Laurent — VP-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-06-02 08:00:00', '2026-06-06 18:00:00', NULL, 'VALID',
   '2026-05-15 10:00:00',
   (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-002'));

-- -----------------------------------------------
-- E. DEMANDES EN ATTENTE — VALID sans validator
-- -----------------------------------------------
-- Marie Leroy — ECR-002
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-19 08:00:00', '2026-05-23 18:00:00', NULL, 'VALID',
   '2026-05-15 08:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   NULL,
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-002'));

-- -----------------------------------------------
-- F. EMPRUNTS REFUSÉS
-- -----------------------------------------------
-- Thomas Dupont — VP-001 (refusé, équipement en réparation)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2026-05-20 08:00:00', '2026-05-24 18:00:00', NULL, 'INVALID',
   '2026-05-14 11:00:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-VP-001'));


-- =============================================
-- 12. ÉVÉNEMENTS (liés aux emprunts)
-- =============================================
-- BREAKDOWN — Lucas Bernard sur PC-002
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Panne signalée : le laptop ne s''allume plus après une mise à jour forcée.',
   '2025-10-22 10:00:00', 'BREAKDOWN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-PC-002')
    LIMIT 1));

-- BREAKDOWN — Hugo Michel sur PC-001
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Panne signalée : tablette ne répond plus après mise en veille prolongée.',
   '2026-01-14 11:00:00', 'BREAKDOWN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-PC-001')
    LIMIT 1));

-- EARLY_RETURN — Pierre Moreau sur VP-002
INSERT INTO event (description, created_at, reading_date, type, loan_id) VALUES
  ('Retour anticipé — conférence annulée.',
   '2025-12-03 14:00:00', '2025-12-03 14:00:00', 'EARLY_RETURN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-VP-002')
    LIMIT 1));

-- EARLY_RETURN — Marie Leroy sur ECR-002
INSERT INTO event (description, created_at, reading_date, type, loan_id) VALUES
  ('Retour anticipé — mission terminée plus tôt que prévu.',
   '2025-10-09 10:00:00', '2025-10-09 10:00:00', 'EARLY_RETURN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-ECR-002')
    LIMIT 1));

-- EXTENSION — Marie Leroy sur VR-001
INSERT INTO event (description, created_at, reading_date, type, loan_id) VALUES
  ('Demande de prolongation d''un jour — présentation client reportée.',
   '2026-03-06 09:00:00', '2026-03-06 09:00:00', 'EXTENSION',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-VR-001')
    LIMIT 1));

-- EXTENSION — Thomas Dupont sur PC-002 (en cours)
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Demande de prolongation de 3 jours — projet en cours non terminé.',
   '2026-05-14 09:00:00', 'EXTENSION',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-PC-002')
    LIMIT 1));

-- BREAKDOWN — Emma Petit sur ECR-003 (en retard)
INSERT INTO event (description, created_at, type, loan_id) VALUES
  ('Incident signalé : pixel mort détecté sur l''angle inférieur droit.',
   '2026-05-02 11:00:00', 'BREAKDOWN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-ECR-003')
    LIMIT 1));


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
  WHERE d.title = 'Guide de démarrage iPad Pro' AND e.reference = 'REF-TAB-001'
    AND NOT EXISTS (SELECT 1 FROM fait_reference fr WHERE fr.doc_id = d.id AND fr.equipment_id = e.id);

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
