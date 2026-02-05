package com.example.musicapi.repository;

import com.example.musicapi.model.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long> {
    java.util.List<Regional> findByAtivoTrue();
    java.util.Optional<Regional> findByIdAndAtivoTrue(Integer id);
}
