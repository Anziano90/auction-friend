package com.focs.auctionfriend.data.services;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.repositories.SquadraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class SquadraService {

    private final SquadraRepository squadraRepository;

    @Autowired
    public SquadraService(SquadraRepository squadraRepository) {
        this.squadraRepository = squadraRepository;
    }

    public List<Squadra> getAllSquadre() {
        return squadraRepository.findAll();
    }

    public Optional<Squadra> getSquadraById(Long id) {
        return squadraRepository.findById(id);
    }

    public Squadra saveSquadra(Squadra squadra) {
        return squadraRepository.save(squadra);
    }

    public void deleteSquadra(Long id) {
        squadraRepository.deleteById(id);
    }

    public String getNumeroGiocatori(Squadra squadra) {
        int p = 0;
        int d = 0;
        int c = 0;
        int a = 0;
        for (Giocatore g : squadra.getListaGiocatoriAcquistati()) {
            switch (g.getRuolo()) {
                case P -> p++;
                case D -> d++;
                case C -> c++;
                case A -> a++;
            }
        }
        String res = p + "" + d + "" + c + "" + a + "";
        return res;
    }
}

