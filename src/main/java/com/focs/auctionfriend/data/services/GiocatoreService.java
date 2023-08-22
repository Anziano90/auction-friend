package com.focs.auctionfriend.data.services;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.repositories.GiocatoreRepository;
import com.focs.auctionfriend.data.util.Ruolo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GiocatoreService {

    private final GiocatoreRepository giocatoreRepository;

    @Autowired
    public GiocatoreService(GiocatoreRepository giocatoreRepository) {
        this.giocatoreRepository = giocatoreRepository;
    }

    public List<Giocatore> getAllGiocatori() {
        return giocatoreRepository.findAll();
    }

    public Optional<Giocatore> getGiocatoreById(Long id) {
        return giocatoreRepository.findById(id);
    }

    public Giocatore saveGiocatore(Giocatore giocatore) {
        return giocatoreRepository.saveAndFlush(giocatore);
    }

    public void deleteGiocatore(Long id) {
        giocatoreRepository.deleteById(id);
    }


    public List<Giocatore> getGiocatoriFiltered(
            String nome,
            boolean portiere,
            boolean difensore,
            boolean centrocampista,
            boolean attaccante,
            boolean svincolato) {

        Set<Ruolo> ruoliSelezionati = new HashSet<>();

        if (portiere) {
            ruoliSelezionati.add(Ruolo.P);
        }
        if (difensore) {
            ruoliSelezionati.add(Ruolo.D);
        }
        if (centrocampista) {
            ruoliSelezionati.add(Ruolo.C);
        }
        if (attaccante) {
            ruoliSelezionati.add(Ruolo.A);
        }

        return giocatoreRepository.findByFilter(nome, ruoliSelezionati, svincolato);
    }


    public void importGiocatoriFromExcel(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Salta le prime 2 righe dell'header
            for (int i = 0; i < 2; i++) {
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                } else {
                    // Il file Excel ha meno di 2 righe di dati
                    return;
                }
            }

            // Carica tutti i giocatori dal database
            List<Giocatore> existingGiocatori = giocatoreRepository.findAll();

            // Rimuovi i giocatori con id_squadra non null dall'elenco
            List<Giocatore> giocatoriToRemove = existingGiocatori.stream()
                    .filter(giocatore -> giocatore.getSquadraProprietaria() != null)
                    .collect(Collectors.toList());
            existingGiocatori.removeAll(giocatoriToRemove);

            // Elabora i dati dal file Excel per ottenere la lista dei nuovi giocatori
            List<Giocatore> newGiocatori = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Giocatore giocatore = extractGiocatoreFromRow(row);
                if (giocatore != null) {
                    // Verifica se il giocatore esiste già nel database basato su nome e club
                    boolean isDuplicate = existingGiocatori.stream()
                            .anyMatch(existing -> existing.getNome().equals(giocatore.getNome())
                                    && existing.getClub().equals(giocatore.getClub()));
                    // Aggiungi il giocatore alla lista dei nuovi giocatori solo se non è un duplicato
                    if (!isDuplicate) {
                        newGiocatori.add(giocatore);
                    }
                }
            }

            // Elimina tutti i giocatori presenti nel database (tranne quelli con id_squadra non null)
            giocatoreRepository.deleteAll(existingGiocatori);

            // Salva i nuovi giocatori nel database
            giocatoreRepository.saveAll(newGiocatori);
        }
    }

    private Giocatore extractGiocatoreFromRow(Row row) {
        String ruolo = row.getCell(1).getStringCellValue();
        String nome = row.getCell(3).getStringCellValue();
        String squadra = row.getCell(4).getStringCellValue();
        int quotaIniziale = (int) row.getCell(6).getNumericCellValue();

        Giocatore giocatore = new Giocatore();
        giocatore.setRuolo(Ruolo.valueOf(ruolo));
        giocatore.setNome(nome);
        giocatore.setClub(squadra);
        giocatore.setQuotaIniziale(quotaIniziale);
        giocatore.setCreationDate(LocalDateTime.now());
        giocatore.setLastUpdated(LocalDateTime.now());
        return giocatore;
    }

}

