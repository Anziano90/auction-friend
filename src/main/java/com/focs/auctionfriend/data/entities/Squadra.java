package com.focs.auctionfriend.data.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "squadra")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Squadra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "crediti")
    private int crediti;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "squadraProprietaria", cascade = {CascadeType.ALL})
    @JsonManagedReference
    private List<Giocatore> listaGiocatoriAcquistati = new ArrayList<>();

    @CreatedDate
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @LastModifiedDate
    @Column(name = "last_update")
    private LocalDateTime lastUpdated;

    @Version
    @Column
    private int version;


}
