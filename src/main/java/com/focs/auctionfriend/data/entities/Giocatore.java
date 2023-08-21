package com.focs.auctionfriend.data.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.focs.auctionfriend.data.util.Ruolo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "giocatore")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Giocatore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nome")
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "ruolo")
    private Ruolo ruolo;

    @Column(name = "quota_iniziale")
    private int quotaIniziale;

    @Column(name = "prezzo_acquisto")
    private int prezzoAcquisto;

    @ManyToOne(optional = true, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "id_squadra")
    @JsonBackReference
    private Squadra squadraProprietaria;

    @Column(name = "club")
    private String club;

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
