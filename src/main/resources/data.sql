-- =============================================
-- JEU DE DONNÉES LOC-MNS
-- Exécuté automatiquement au démarrage
-- (spring.sql.init.mode=always + ddl-auto=create)
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
INSERT INTO equipment_family (name_equipment_family) VALUES
  ('Ordinateur portable'),
  ('Tablette'),
  ('Périphérique'),
  ('Écran'),
  ('Matériel audiovisuel');


-- =============================================
-- 3. UTILISATEURS (2 gestionnaires, 5 collaborateurs, 3 intervenants, 2 stagiaires)
-- Mot de passe : admin123 / user123 (mock — pas de BCrypt pour l'instant)
-- =============================================
INSERT INTO app_user (email, name, lastname, password, profil_id) VALUES
  -- Gestionnaires
  ('jean.martin@mns.fr',    'Jean',    'Martin',   'admin123', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE')),
  ('sophie.leblanc@mns.fr', 'Sophie',  'Leblanc',  'admin123', (SELECT id FROM profil WHERE type = 'GESTIONNAIRE')),
  -- Collaborateurs
  ('thomas.dupont@mns.fr',  'Thomas',  'Dupont',   'user123',  (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('marie.leroy@mns.fr',    'Marie',   'Leroy',    'user123',  (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('lucas.bernard@mns.fr',  'Lucas',   'Bernard',  'user123',  (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('emma.petit@mns.fr',     'Emma',    'Petit',    'user123',  (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  ('nathan.durand@mns.fr',  'Nathan',  'Durand',   'user123',  (SELECT id FROM profil WHERE type = 'COLLABORATEUR')),
  -- Intervenants
  ('pierre.moreau@mns.fr',  'Pierre',  'Moreau',   'user123',  (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  ('laura.simon@mns.fr',    'Laura',   'Simon',    'user123',  (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  ('hugo.michel@mns.fr',    'Hugo',    'Michel',   'user123',  (SELECT id FROM profil WHERE type = 'INTERVENANT')),
  -- Stagiaires
  ('camille.robert@mns.fr', 'Camille', 'Robert',   'user123',  (SELECT id FROM profil WHERE type = 'STAGIAIRE')),
  ('alexis.laurent@mns.fr', 'Alexis',  'Laurent',  'user123',  (SELECT id FROM profil WHERE type = 'STAGIAIRE'));


-- =============================================
-- 4. ÉQUIPEMENTS (12 équipements)
-- =============================================
INSERT INTO equipment (reference, equipment_name, location, acquisition_date, equipment_family_id) VALUES
  -- Ordinateurs portables
  ('REF-PC-001', 'MacBook Pro M3',        'Salle B204',       '2023-09-01', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable')),
  ('REF-PC-002', 'Dell XPS 15',           'Salle A101',       '2023-06-15', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable')),
  ('REF-PC-003', 'Lenovo ThinkPad X1',    'Salle C302',       '2022-11-20', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable')),
  ('REF-PC-004', 'HP EliteBook 840',      'Salle B204',       '2022-03-10', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Ordinateur portable')),
  -- Tablettes
  ('REF-TAB-001', 'iPad Pro 12.9',        'Accueil',          '2023-01-15', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Tablette')),
  ('REF-TAB-002', 'Samsung Galaxy Tab S9','Salle A101',       '2023-07-22', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Tablette')),
  ('REF-TAB-003', 'Microsoft Surface Pro','Salle C302',       '2022-08-05', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Tablette')),
  -- Périphériques
  ('REF-PER-001', 'Magic Mouse Apple',    'Stock',            '2023-02-28', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique')),
  ('REF-PER-002', 'Clavier Logitech MX',  'Stock',            '2023-02-28', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Périphérique')),
  -- Écrans
  ('REF-ECR-001', 'Dell UltraSharp 27"',  'Salle B204',       '2021-12-01', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran')),
  ('REF-ECR-002', 'LG 4K 32"',            'Salle A101',       '2022-04-18', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Écran')),
  -- Audiovisuel
  ('REF-AV-001',  'Projecteur Epson EB',  'Salle de réunion', '2021-06-10', (SELECT id FROM equipment_family WHERE name_equipment_family = 'Matériel audiovisuel'));


-- =============================================
-- 5. CAN_LOAN — quels profils peuvent emprunter quelles familles
-- =============================================
-- GESTIONNAIRE : tout
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'GESTIONNAIRE';

-- COLLABORATEUR : portables, tablettes, périphériques, écrans
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'COLLABORATEUR'
    AND ef.name_equipment_family IN ('Ordinateur portable', 'Tablette', 'Périphérique', 'Écran');

-- INTERVENANT : portables, périphériques
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'INTERVENANT'
    AND ef.name_equipment_family IN ('Ordinateur portable', 'Périphérique');

-- STAGIAIRE : tablettes, périphériques
INSERT INTO can_loan (profil_id, equipment_family_id)
  SELECT p.id, ef.id FROM profil p, equipment_family ef
  WHERE p.type = 'STAGIAIRE'
    AND ef.name_equipment_family IN ('Tablette', 'Périphérique');


-- =============================================
-- 6. CARACTÉRISTIQUES
-- =============================================
INSERT INTO characteristic (name) VALUES
  ('Processeur'),
  ('RAM'),
  ('Stockage'),
  ('Système d''exploitation'),
  ('Résolution');


-- =============================================
-- 7. EST_CONSTITUE — caractéristiques par famille
-- =============================================
-- Ordinateur portable : Processeur, RAM, Stockage, OS
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Ordinateur portable'
    AND c.name IN ('Processeur', 'RAM', 'Stockage', 'Système d''exploitation');

-- Tablette : Processeur, RAM, Stockage, OS
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Tablette'
    AND c.name IN ('Processeur', 'RAM', 'Stockage', 'Système d''exploitation');

-- Écran : Résolution
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Écran'
    AND c.name = 'Résolution';


-- =============================================
-- 8. VALEURS DE CARACTÉRISTIQUES
-- =============================================
-- MacBook Pro M3
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('Apple M3 Pro', (SELECT id FROM characteristic WHERE name = 'Processeur'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('18 Go', (SELECT id FROM characteristic WHERE name = 'RAM'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('512 Go SSD', (SELECT id FROM characteristic WHERE name = 'Stockage'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('macOS Sonoma', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- Dell XPS 15
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('Intel Core i7-13700H', (SELECT id FROM characteristic WHERE name = 'Processeur'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('16 Go', (SELECT id FROM characteristic WHERE name = 'RAM'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('1 To SSD', (SELECT id FROM characteristic WHERE name = 'Stockage'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('Windows 11 Pro', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- iPad Pro
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('Apple M2', (SELECT id FROM characteristic WHERE name = 'Processeur'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('8 Go', (SELECT id FROM characteristic WHERE name = 'RAM'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('256 Go', (SELECT id FROM characteristic WHERE name = 'Stockage'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('iPadOS 17', (SELECT id FROM characteristic WHERE name = 'Système d''exploitation'));

-- Dell UltraSharp 27"
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('2560x1440 (QHD)', (SELECT id FROM characteristic WHERE name = 'Résolution'));

-- LG 4K 32"
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('3840x2160 (4K UHD)', (SELECT id FROM characteristic WHERE name = 'Résolution'));


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

-- iPad Pro
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-TAB-001'
    AND cv.value IN ('Apple M2', '8 Go', '256 Go', 'iPadOS 17');

-- Dell UltraSharp
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-001' AND cv.value = '2560x1440 (QHD)';

-- LG 4K
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-ECR-002' AND cv.value = '3840x2160 (4K UHD)';


-- =============================================
-- 10. EMPRUNTS (12 emprunts, tous les statuts représentés)
-- =============================================
-- TERMINE (emprunt passé, retourné)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2024-09-02 08:00:00', '2024-09-06 18:00:00', '2024-09-06 17:30:00', 'TERMINE',
   '2024-09-06 17:30:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2024-10-14 09:00:00', '2024-10-18 18:00:00', '2024-10-17 16:00:00', 'TERMINE',
   '2024-10-17 16:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-TAB-001'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2024-11-04 08:30:00', '2024-11-08 18:00:00', '2024-11-08 18:00:00', 'TERMINE',
   '2024-11-08 18:00:00',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-003'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2024-12-09 09:00:00', '2024-12-13 18:00:00', '2024-12-12 14:00:00', 'TERMINE',
   '2024-12-12 14:00:00',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));

-- IN_PROGRESS (emprunt en cours, validé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-05 08:00:00', '2025-05-16 18:00:00', NULL, 'IN_PROGRESS',
   '2025-05-04 15:00:00',
   (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-002'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-06 09:00:00', '2025-05-20 18:00:00', NULL, 'IN_PROGRESS',
   '2025-05-05 10:30:00',
   (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-TAB-002'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-07 08:00:00', '2025-05-14 18:00:00', NULL, 'IN_PROGRESS',
   '2025-05-06 09:00:00',
   (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PER-002'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-08 10:00:00', '2025-05-22 18:00:00', NULL, 'IN_PROGRESS',
   '2025-05-07 14:00:00',
   (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-PC-004'));

-- VALID (demande en attente de validation)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-19 08:00:00', '2025-05-23 18:00:00', NULL, 'VALID',
   '2025-05-12 08:00:00',
   (SELECT id FROM app_user WHERE email = 'alexis.laurent@mns.fr'),
   NULL,
   (SELECT id FROM equipment WHERE reference = 'REF-TAB-003'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-20 09:00:00', '2025-05-27 18:00:00', NULL, 'VALID',
   '2025-05-12 09:30:00',
   (SELECT id FROM app_user WHERE email = 'laura.simon@mns.fr'),
   NULL,
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-002'));

-- INVALID (refusé)
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-04-21 08:00:00', '2025-04-25 18:00:00', NULL, 'INVALID',
   '2025-04-18 11:00:00',
   (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AV-001'));

INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-01 09:00:00', '2025-05-05 18:00:00', NULL, 'INVALID',
   '2025-04-29 10:00:00',
   (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));


-- =============================================
-- 11. STATUTS ÉQUIPEMENT (incidents / réparations)
-- =============================================
-- Équipement actuellement hors service
INSERT INTO status_equipment (description_status, status_equipment_type, equipment_id) VALUES
  ('Écran fissuré suite à une chute signalée par l''utilisateur.', 'OUT_OF_SERVICE',
   (SELECT id FROM equipment WHERE reference = 'REF-TAB-003'));

-- Équipement en cours de réparation
INSERT INTO status_equipment (description_status, status_equipment_type, equipment_id) VALUES
  ('Batterie défectueuse — envoyé en réparation chez le prestataire.', 'UNDER_REPAIR',
   (SELECT id FROM equipment WHERE reference = 'REF-PC-003'));

-- Incident résolu (endStatusDate renseignée)
INSERT INTO status_equipment (description_status, status_equipment_type, end_status_date, equipment_id) VALUES
  ('Panne clavier — touche Entrée bloquée. Réparé en interne.', 'UNDER_REPAIR',
   '2024-11-15 12:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-PC-001'));

INSERT INTO status_equipment (description_status, status_equipment_type, end_status_date, equipment_id) VALUES
  ('Connecteur HDMI défaillant. Remplacement du câble interne.', 'UNDER_REPAIR',
   '2025-02-20 09:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));

INSERT INTO status_equipment (description_status, status_equipment_type, end_status_date, equipment_id) VALUES
  ('Souris inopérante — capteur laser HS. Mise au rebut.', 'OUT_OF_SERVICE',
   '2025-01-10 16:00:00',
   (SELECT id FROM equipment WHERE reference = 'REF-PER-001'));

INSERT INTO status_equipment (description_status, status_equipment_type, equipment_id) VALUES
  ('Lampe du projecteur en fin de vie — remplacement commandé.', 'UNDER_REPAIR',
   (SELECT id FROM equipment WHERE reference = 'REF-AV-001'));


-- =============================================
-- 12. ÉVÉNEMENTS (liés aux emprunts)
-- =============================================
-- BREAKDOWN — signalement de panne pendant un emprunt
INSERT INTO event (description, type, loan_id) VALUES
  ('Panne signalée : le laptop ne s''allume plus après une mise à jour forcée.',
   'BREAKDOWN',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr')
    AND status_type = 'TERMINE' LIMIT 1));

INSERT INTO event (description, type, loan_id) VALUES
  ('Écran fissuré lors du transport — signalement par l''emprunteur.',
   'BREAKDOWN',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'camille.robert@mns.fr') LIMIT 1));

-- EARLY_RETURN — retour anticipé
INSERT INTO event (description, reading_date, type, loan_id) VALUES
  ('Retour anticipé — mission terminée plus tôt que prévu.',
   '2024-10-18 09:00:00',
   'EARLY_RETURN',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'marie.leroy@mns.fr')
    AND status_type = 'TERMINE' LIMIT 1));

INSERT INTO event (description, reading_date, type, loan_id) VALUES
  ('Retour anticipé — équipement non utilisé finalement.',
   '2024-12-13 10:00:00',
   'EARLY_RETURN',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr') LIMIT 1));

-- EXTENSION — demande de prolongation
INSERT INTO event (description, type, loan_id) VALUES
  ('Demande de prolongation de 5 jours — projet en cours non terminé.',
   'EXTENSION',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'emma.petit@mns.fr') LIMIT 1));

INSERT INTO event (description, type, loan_id) VALUES
  ('Demande de prolongation d''une semaine — formation reportée.',
   'EXTENSION',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'nathan.durand@mns.fr') LIMIT 1));

INSERT INTO event (description, reading_date, type, loan_id) VALUES
  ('Prolongation accordée — présentation client reportée au vendredi.',
   '2024-09-04 11:00:00',
   'EXTENSION',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'thomas.dupont@mns.fr')
    AND status_type = 'TERMINE' LIMIT 1));

INSERT INTO event (description, type, loan_id) VALUES
  ('Demande de prolongation de 3 jours — livrable en attente de validation.',
   'EXTENSION',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr') LIMIT 1));

INSERT INTO event (description, type, loan_id) VALUES
  ('Panne signalée : tablette ne répond plus au tactile.',
   'BREAKDOWN',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'hugo.michel@mns.fr') LIMIT 1));

INSERT INTO event (description, reading_date, type, loan_id) VALUES
  ('Retour anticipé — congé maladie.',
   '2024-11-07 08:30:00',
   'EARLY_RETURN',
   (SELECT id FROM loan WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr')
    AND status_type = 'TERMINE' LIMIT 1));


-- =============================================
-- 13. DOCUMENTS
-- =============================================
INSERT INTO doc (title, url) VALUES
  ('Manuel utilisateur MacBook Pro M3',          'https://support.apple.com/macbook-pro'),
  ('Guide de démarrage iPad Pro',                'https://support.apple.com/ipad-pro'),
  ('Documentation Dell XPS 15',                  'https://www.dell.com/support/xps15'),
  ('Charte d''utilisation du matériel MNS',      'https://intranet.mns.fr/charte-materiel'),
  ('Procédure de signalement d''incident',       'https://intranet.mns.fr/procedure-incident');


-- =============================================
-- 14. FAIT_REFERENCE — docs liés aux équipements
-- =============================================
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Manuel utilisateur MacBook Pro M3' AND e.reference = 'REF-PC-001';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Guide de démarrage iPad Pro' AND e.reference = 'REF-TAB-001';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Documentation Dell XPS 15' AND e.reference = 'REF-PC-002';

-- La charte s'applique à tous les ordinateurs portables
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Charte d''utilisation du matériel MNS'
    AND e.reference IN ('REF-PC-001', 'REF-PC-002', 'REF-PC-003', 'REF-PC-004');

-- La procédure incident s'applique à tout le parc
INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Procédure de signalement d''incident';


-- =============================================
-- COMPLÉMENTS — 1 entrée de chaque par famille manquante
-- =============================================

-- -----------------------------------------------
-- A. CARACTÉRISTIQUES manquantes
-- -----------------------------------------------
INSERT INTO characteristic (name) VALUES
  ('Connectivité'),
  ('Luminosité'),
  ('Type de connexion');

-- Périphérique ← Connectivité
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Périphérique' AND c.name = 'Connectivité';

-- Matériel audiovisuel ← Luminosité + Type de connexion
INSERT INTO est_constitue (caracteristique_id, equipment_family_id)
  SELECT c.id, ef.id FROM characteristic c, equipment_family ef
  WHERE ef.name_equipment_family = 'Matériel audiovisuel'
    AND c.name IN ('Luminosité', 'Type de connexion');

-- -----------------------------------------------
-- B. VALEURS DE CARACTÉRISTIQUES manquantes
-- -----------------------------------------------
-- Magic Mouse
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('Bluetooth 5.0', (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Clavier Logitech MX
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('USB + Bluetooth', (SELECT id FROM characteristic WHERE name = 'Connectivité'));

-- Projecteur Epson
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('3600 lumens', (SELECT id FROM characteristic WHERE name = 'Luminosité'));
INSERT INTO characteristic_value (value, characteristic_id) VALUES
  ('HDMI / VGA / USB', (SELECT id FROM characteristic WHERE name = 'Type de connexion'));

-- Possède — Magic Mouse
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-001' AND cv.value = 'Bluetooth 5.0';

-- Possède — Clavier Logitech
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-PER-002' AND cv.value = 'USB + Bluetooth';

-- Possède — Projecteur
INSERT INTO possede (characteristic_value_id, equipment_id)
  SELECT cv.id, e.id FROM characteristic_value cv, equipment e
  WHERE e.reference = 'REF-AV-001' AND cv.value IN ('3600 lumens', 'HDMI / VGA / USB');


-- -----------------------------------------------
-- C. EMPRUNTS manquants (Écran TERMINE + Audiovisuel IN_PROGRESS)
-- -----------------------------------------------
-- Écran — TERMINE
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-02-03 08:00:00', '2025-02-07 18:00:00', '2025-02-07 17:00:00', 'TERMINE',
   '2025-02-07 17:00:00',
   (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'jean.martin@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-ECR-001'));

-- Audiovisuel — IN_PROGRESS
INSERT INTO loan (begin_date, end_date, real_end_date, status_type, status_date, requester_id, validator_id, equipment_id) VALUES
  ('2025-05-09 08:00:00', '2025-05-16 18:00:00', NULL, 'IN_PROGRESS',
   '2025-05-08 11:00:00',
   (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr'),
   (SELECT id FROM app_user WHERE email = 'sophie.leblanc@mns.fr'),
   (SELECT id FROM equipment WHERE reference = 'REF-AV-001'));


-- -----------------------------------------------
-- D. ÉVÉNEMENTS manquants (Écran + Audiovisuel)
-- -----------------------------------------------
-- Événement sur l'emprunt Écran TERMINE
INSERT INTO event (description, reading_date, type, loan_id) VALUES
  ('Retour anticipé — télétravail annulé, écran restitué avant terme.',
   '2025-02-07 11:00:00',
   'EARLY_RETURN',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'lucas.bernard@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-ECR-001')
    LIMIT 1));

-- Événement sur l'emprunt Audiovisuel IN_PROGRESS
INSERT INTO event (description, type, loan_id) VALUES
  ('Demande de prolongation de 2 jours — présentation repoussée.',
   'EXTENSION',
   (SELECT id FROM loan
    WHERE requester_id = (SELECT id FROM app_user WHERE email = 'pierre.moreau@mns.fr')
      AND equipment_id  = (SELECT id FROM equipment WHERE reference = 'REF-AV-001')
    LIMIT 1));


-- -----------------------------------------------
-- E. DOCUMENTS manquants (Périphérique, Écran, Audiovisuel)
-- -----------------------------------------------
INSERT INTO doc (title, url) VALUES
  ('Guide Logitech MX Keys',              'https://www.logitech.com/support/mx-keys'),
  ('Fiche technique Dell UltraSharp 27"', 'https://www.dell.com/support/ultrasharp27'),
  ('Manuel projecteur Epson EB',          'https://www.epson.fr/support/eb-series');

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Guide Logitech MX Keys' AND e.reference = 'REF-PER-002';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Fiche technique Dell UltraSharp 27"' AND e.reference = 'REF-ECR-001';

INSERT INTO fait_reference (doc_id, equipment_id)
  SELECT d.id, e.id FROM doc d, equipment e
  WHERE d.title = 'Manuel projecteur Epson EB' AND e.reference = 'REF-AV-001';
