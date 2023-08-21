package com.focs.auctionfriend.data.repositories;

import com.focs.auctionfriend.data.entities.Squadra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SquadraRepository extends JpaRepository<Squadra, Long> {
}
