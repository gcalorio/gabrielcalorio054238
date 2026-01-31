CREATE TABLE artist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50)
);

CREATE TABLE album (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist_id BIGINT,
    CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artist(id)
);

CREATE TABLE album_covers (
    album_id BIGINT NOT NULL,
    cover_url VARCHAR(255),
    CONSTRAINT fk_covers_album FOREIGN KEY (album_id) REFERENCES album(id)
);
