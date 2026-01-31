package com.example.musicapi.repository;

import com.example.musicapi.model.Artist;
import com.example.musicapi.model.ArtistType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByType(ArtistType type, Sort sort);
    List<Artist> findByNameContainingIgnoreCase(String name, Sort sort);
}
