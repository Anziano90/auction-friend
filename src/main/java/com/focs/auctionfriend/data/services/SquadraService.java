package com.focs.auctionfriend.data.services;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.repositories.SquadraRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SquadraService {

    private final SquadraRepository squadraRepository;

    @PersistenceContext
    private EntityManager em;

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
        return squadraRepository.saveAndFlush(squadra);
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

    public boolean hoSlotPorta(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(0))) < 3;
    }

    public boolean hoSlotDifesa(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(1))) < 8;
    }

    public boolean hoSlotCentrocampo(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(2))) < 8;
    }

    public boolean hoSlotAttacco(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(3))) < 6;
    }

    /**
     * il metodo ripristina i crediti della squadra che sta svincolando un giocatore
     * e rimuove il giocatore selezionato
     *
     * @param squadra
     * @param giocatoreDaSvincolare
     * @return true se esiste un giocatore da svincolare in quella squadra
     */
    @Transactional
    public boolean ridaiSoldiESvincola(Squadra squadra, Giocatore giocatoreDaSvincolare) {
        Squadra managedSquadra = em.find(Squadra.class, squadra.getId());
        Giocatore managedGiocatore = em.find(Giocatore.class, giocatoreDaSvincolare.getId());
        if (managedSquadra != null && managedSquadra.getListaGiocatoriAcquistati().contains(managedGiocatore)) {
            managedSquadra.getListaGiocatoriAcquistati().remove(managedGiocatore);
            managedSquadra.setCrediti(managedSquadra.getCrediti() + managedGiocatore.getPrezzoAcquisto());
            managedGiocatore.setPrezzoAcquisto(0);
            managedGiocatore.setSquadraProprietaria(null);
            return true;
        }
        return false;
    }


}

