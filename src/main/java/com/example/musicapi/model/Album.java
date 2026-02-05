package com.example.musicapi.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToMany(mappedBy = "albums")
    @JsonIgnore
    private List<Artist> artists;

    @ElementCollection
    @CollectionTable(name = "album_covers", joinColumns = @JoinColumn(name = "album_id"))
    @Column(name = "cover_url")
    private List<String> coverUrls;
}
