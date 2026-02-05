-- Remover a restrição de chave estrangeira antiga
ALTER TABLE album DROP CONSTRAINT fk_album_artist;
ALTER TABLE album DROP COLUMN artist_id;

-- Criar a tabela de junção para N:N
CREATE TABLE artist_album (
    artist_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    PRIMARY KEY (artist_id, album_id),
    CONSTRAINT fk_artist_album_artist FOREIGN KEY (artist_id) REFERENCES artist(id),
    CONSTRAINT fk_artist_album_album FOREIGN KEY (album_id) REFERENCES album(id)
);

-- Migrar dados existentes (opcional se for base de teste, mas bom para integridade)
INSERT INTO artist_album (artist_id, album_id)
SELECT 1, 1 UNION ALL
SELECT 1, 2 UNION ALL
SELECT 1, 3 UNION ALL
SELECT 2, 4 UNION ALL
SELECT 2, 5 UNION ALL
SELECT 2, 6 UNION ALL
SELECT 2, 7 UNION ALL
SELECT 3, 8 UNION ALL
SELECT 3, 9 UNION ALL
SELECT 3, 10 UNION ALL
SELECT 4, 11 UNION ALL
SELECT 4, 12 UNION ALL
SELECT 4, 13;
