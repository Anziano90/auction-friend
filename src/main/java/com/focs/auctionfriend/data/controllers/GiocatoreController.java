package com.focs.auctionfriend.data.controllers;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.services.GiocatoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/giocatori")
public class GiocatoreController {

    private final GiocatoreService giocatoreService;

    @Autowired
    public GiocatoreController(GiocatoreService giocatoreService) {
        this.giocatoreService = giocatoreService;
    }

    @GetMapping
    public ResponseEntity<List<Giocatore>> getAllGiocatori() {
        List<Giocatore> giocatori = giocatoreService.getAllGiocatori();
        return new ResponseEntity<>(giocatori, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Giocatore> getGiocatoreById(@PathVariable Long id) {
        Optional<Giocatore> giocatore = giocatoreService.getGiocatoreById(id);
        return giocatore.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Giocatore> saveGiocatore(@RequestBody Giocatore giocatore) {
        Giocatore savedGiocatore = giocatoreService.saveGiocatore(giocatore);
        return new ResponseEntity<>(savedGiocatore, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Giocatore> updateGiocatore(@PathVariable Long id, @RequestBody Giocatore giocatore) {
        Optional<Giocatore> existingGiocatore = giocatoreService.getGiocatoreById(id);
        if (existingGiocatore.isPresent()) {
            giocatore.setId(id);
            Giocatore updatedGiocatore = giocatoreService.saveGiocatore(giocatore);
            return new ResponseEntity<>(updatedGiocatore, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGiocatore(@PathVariable Long id) {
        giocatoreService.deleteGiocatore(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

