package com.focs.auctionfriend.data.services;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.repositories.SquadraRepository;
import com.focs.auctionfriend.data.util.Ruolo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SquadraService {

    //FIXME: parametri non hardcoded

    public static final int MAX_PORTIERI = 3;
    public static final int MAX_DIFENSORI = 8;
    public static final int MAX_CENTROCAMPISTI = 8;
    public static final int MAX_ATTACCANTI = 6;
    private final SquadraRepository squadraRepository;
    public final int MAX_GIOCATORI_ROSA = MAX_PORTIERI + MAX_CENTROCAMPISTI + MAX_DIFENSORI + MAX_ATTACCANTI;

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
        return Integer.parseInt(String.valueOf(conteggio.charAt(0))) < MAX_PORTIERI;
    }

    public boolean hoSlotDifesa(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(1))) < MAX_DIFENSORI;
    }

    public boolean hoSlotCentrocampo(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(2))) < MAX_CENTROCAMPISTI;
    }

    public boolean hoSlotAttacco(Squadra s) {
        String conteggio = getNumeroGiocatori(s);
        return Integer.parseInt(String.valueOf(conteggio.charAt(3))) < MAX_ATTACCANTI;
    }

    @Transactional
    public void rimuoviSquadraESvincolaCalciatori(Squadra squadra) {
        Squadra managedSquadra = em.find(Squadra.class, squadra.getId());
        for (Giocatore daSvincolare :
                managedSquadra.getListaGiocatoriAcquistati()) {
            daSvincolare.setSquadraProprietaria(null);
            daSvincolare.setPrezzoAcquisto(0);
        }
        this.deleteSquadra(managedSquadra.getId());
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

    @Transactional
    public boolean acquistaGiocatore(Squadra squadraAcquirente, Giocatore giocatoreAcquistato, int prezzoAcquisto) {
        Squadra managedSquadra = em.find(Squadra.class, squadraAcquirente.getId());
        Giocatore managedGiocatore = em.find(Giocatore.class, giocatoreAcquistato.getId());

        if (managedSquadra != null && managedGiocatore != null) {
            managedSquadra.getListaGiocatoriAcquistati().add(managedGiocatore);
            managedSquadra.setCrediti(managedSquadra.getCrediti() - prezzoAcquisto);
            managedGiocatore.setPrezzoAcquisto(prezzoAcquisto);
            managedGiocatore.setSquadraProprietaria(managedSquadra);
            return true;
        }
        return false;
    }

    //TODO: forse vale la pena completare le casistiche ed usare sempre questo
    public boolean giocatoreAcquistabile(Squadra squadraAcquirente, Giocatore giocatoreAcquistato, int prezzoAcquisto) {
        if (squadraAcquirente.getListaGiocatoriAcquistati().size() >= this.MAX_GIOCATORI_ROSA) return false;
        if ((giocatoreAcquistato.getRuolo().equals(Ruolo.A) && !hoSlotAttacco(squadraAcquirente))
                || (giocatoreAcquistato.getRuolo().equals(Ruolo.C) && !hoSlotCentrocampo(squadraAcquirente))
                || (giocatoreAcquistato.getRuolo().equals(Ruolo.D) && !hoSlotDifesa(squadraAcquirente))
                || (giocatoreAcquistato.getRuolo().equals(Ruolo.P) && !hoSlotPorta(squadraAcquirente))) return false;
        if (squadraAcquirente.getCrediti() < prezzoAcquisto) {
            return false;
        }
        if (checkFuturiAcquisti(this.MAX_GIOCATORI_ROSA - squadraAcquirente.getListaGiocatoriAcquistati().size(), squadraAcquirente.getCrediti(), prezzoAcquisto)) {
            return false;
        }
        return true;
    }

    /**
     * Controllo che prezzoAcquisto < crediti - slotRimanenti (escluso l'acquisto corrente)
     *
     * @param slotRimanenti
     * @param creditiRimanenti
     * @param prezzoAcquisto
     * @return
     */
    public boolean checkFuturiAcquisti(int slotRimanenti, int creditiRimanenti, int prezzoAcquisto) {
        if (prezzoAcquisto <= creditiRimanenti) {
            int creditiDopoAcquisto = creditiRimanenti - prezzoAcquisto;
            return creditiDopoAcquisto >= slotRimanenti - 1;
        }
        return false;
    }


    public int getPortieri(Squadra squadra) {
        return (int) squadra.getListaGiocatoriAcquistati().stream()
                .filter(giocatore -> Ruolo.P.equals(giocatore.getRuolo()))
                .count();
    }

    public int getDifensori(Squadra squadra) {
        return (int) squadra.getListaGiocatoriAcquistati().stream()
                .filter(giocatore -> Ruolo.D.equals(giocatore.getRuolo()))
                .count();
    }

    public int getCentrocampisti(Squadra squadra) {
        return (int) squadra.getListaGiocatoriAcquistati().stream()
                .filter(giocatore -> Ruolo.C.equals(giocatore.getRuolo()))
                .count();
    }

    public int getAttaccanti(Squadra squadra) {
        return (int) squadra.getListaGiocatoriAcquistati().stream()
                .filter(giocatore -> Ruolo.A.equals(giocatore.getRuolo()))
                .count();
    }
}

