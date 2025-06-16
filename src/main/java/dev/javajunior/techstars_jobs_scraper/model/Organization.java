package dev.javajunior.techstars_jobs_scraper.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "organizations")
@Builder
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String name;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String url;

    @Column
    private String logoUrl;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<Job> jobs = new HashSet<>();
} 