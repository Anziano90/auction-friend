package com.focs.auctionfriend.data.repositories;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.util.Ruolo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GiocatoreRepository extends JpaRepository<Giocatore, Long> {


    @Query("SELECT g FROM Giocatore g " +
            "WHERE (:nome is null or g.nome LIKE %:nome%) " +
            "AND (g.ruolo IN (:ruoli)) AND ((:svincolato = true AND g.squadraProprietaria is null) OR (:svincolato = false AND g.squadraProprietaria is not null))")
    List<Giocatore> findByFilter(String nome, Set<Ruolo> ruoli, boolean svincolato);

}

