-- Serj Tankian
INSERT INTO artist (id, name, type) VALUES (1, 'Serj Tankian', 'SOLO');
INSERT INTO album (id, title, artist_id) VALUES (1, 'Harakiri', 1);
INSERT INTO album (id, title, artist_id) VALUES (2, 'Black Blooms', 1);
INSERT INTO album (id, title, artist_id) VALUES (3, 'The Rough Dog', 1);

-- Mike Shinoda
INSERT INTO artist (id, name, type) VALUES (2, 'Mike Shinoda', 'SOLO');
INSERT INTO album (id, title, artist_id) VALUES (4, 'The Rising Tied', 2);
INSERT INTO album (id, title, artist_id) VALUES (5, 'Post Traumatic', 2);
INSERT INTO album (id, title, artist_id) VALUES (6, 'Post Traumatic EP', 2);
INSERT INTO album (id, title, artist_id) VALUES (7, 'Where’d You Go', 2);

-- Michel Teló
INSERT INTO artist (id, name, type) VALUES (3, 'Michel Teló', 'SOLO');
INSERT INTO album (id, title, artist_id) VALUES (8, 'Bem Sertanejo', 3);
INSERT INTO album (id, title, artist_id) VALUES (9, 'Bem Sertanejo - O Show (Ao Vivo)', 3);
INSERT INTO album (id, title, artist_id) VALUES (10, 'Bem Sertanejo - (1ª Temporada) - EP', 3);

-- Guns N’ Roses
INSERT INTO artist (id, name, type) VALUES (4, 'Guns N’ Roses', 'BAND');
INSERT INTO album (id, title, artist_id) VALUES (11, 'Use Your Illusion I', 4);
INSERT INTO album (id, title, artist_id) VALUES (12, 'Use Your Illusion II', 4);
INSERT INTO album (id, title, artist_id) VALUES (13, 'Greatest Hits', 4);

-- Reset sequence for H2
ALTER TABLE artist ALTER COLUMN id RESTART WITH 5;
ALTER TABLE album ALTER COLUMN id RESTART WITH 14;
