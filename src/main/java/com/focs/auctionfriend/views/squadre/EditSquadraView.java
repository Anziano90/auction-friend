package com.focs.auctionfriend.views.squadre;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.services.GiocatoreService;
import com.focs.auctionfriend.data.services.SquadraService;
import com.focs.auctionfriend.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Optional;

@Route(value = "modifica-squadra", layout = MainLayout.class)
public class EditSquadraView extends VerticalLayout implements HasUrlParameter<String> {

    private final SquadraService squadraService;
    private final GiocatoreService giocatoreService;
    private Squadra squadra;

    private Grid<Giocatore> giocatoriGrid = new Grid<>(Giocatore.class);

    public EditSquadraView(SquadraService squadraService, GiocatoreService giocatoreService) {
        this.squadraService = squadraService;
        this.giocatoreService = giocatoreService;

        giocatoriGrid.setColumns("nome", "ruolo", "quotaIniziale", "prezzoAcquisto");

        Button acquistaGiocatoreButton = new Button("Acquista giocatore", event -> openAcquistaGiocatoreDialog());
        acquistaGiocatoreButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        acquistaGiocatoreButton.addClassName("fixed-button");

        add(acquistaGiocatoreButton);
        add(giocatoriGrid);
    }

    private void openAcquistaGiocatoreDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        // Contenuto del dialog simile a quello nella vista ListoneView
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        TextField nomeFilter = new TextField();
        nomeFilter.setPlaceholder("Nome");

        // Creazione dei checkbox
        Checkbox portiereFilter = new Checkbox("P");
        Checkbox difensoreFilter = new Checkbox("D");
        Checkbox centrocampistaFilter = new Checkbox("C");
        Checkbox attaccanteFilter = new Checkbox("A");
        Checkbox svincolatiFilter = new Checkbox("SV");

        portiereFilter.setTooltipText("Portiere");
        difensoreFilter.setTooltipText("Difensore");
        centrocampistaFilter.setTooltipText("Centrocampista");
        attaccanteFilter.setTooltipText("Attaccante");
        svincolatiFilter.setTooltipText("Svincolato");

        // Layout per i checkbox
        FlexLayout checkboxLayout = new FlexLayout(
                portiereFilter, difensoreFilter,
                centrocampistaFilter, attaccanteFilter,
                svincolatiFilter
        );
        checkboxLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        checkboxLayout.getStyle().set("margin-left", "1em");
        checkboxLayout.getStyle().set("margin-right", "1em");

        // Layout per il filtro e i checkbox
        FlexLayout filterLayout = new FlexLayout(nomeFilter, checkboxLayout);
        filterLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        filterLayout.getStyle().set("margin-bottom", "1em");

        // Griglia dei giocatori da filtrare (simile a quella nella vista ListoneView)
        Grid<Giocatore> giocatoriDialogGrid = new Grid<>(Giocatore.class, false);
        giocatoriDialogGrid.setColumns("nome", "ruolo", "quotaIniziale", "prezzoAcquisto");

        // Pulsante di ricerca
        Button cercaButton = new Button("Cerca", event -> {
            List<Giocatore> filteredGiocatori = giocatoreService.getGiocatoriFiltered(
                    nomeFilter.getValue(),
                    portiereFilter.getValue(),
                    difensoreFilter.getValue(),
                    centrocampistaFilter.getValue(),
                    attaccanteFilter.getValue(),
                    svincolatiFilter.getValue());

            ListDataProvider<Giocatore> dataProvider = DataProvider.ofCollection(filteredGiocatori);
            giocatoriDialogGrid.setDataProvider(dataProvider);
        });
        cercaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterLayout.add(cercaButton);

        content.add(filterLayout);
        content.add(giocatoriDialogGrid);

        // Pulsante di acquisto
        Button acquistaButton = new Button("Acquista", event -> {
            // Ottieni il giocatore selezionato dalla griglia del dialog
            Giocatore giocatoreSelezionato = giocatoriDialogGrid.asSingleSelect().getValue();

            if (giocatoreSelezionato != null) {
                // Aggiorna il giocatore
                Optional<Giocatore> giocatoreDaAggiornare = giocatoreService.getGiocatoreById(giocatoreSelezionato.getId());
                if (giocatoreDaAggiornare.isPresent()) {
                    giocatoreDaAggiornare.get().setSquadraProprietaria(squadra);
                    giocatoreService.saveGiocatore(giocatoreDaAggiornare.get());
                    // Aggiungi il giocatore alla lista dei giocatori acquistati della squadra
                    squadra.getListaGiocatoriAcquistati().add(giocatoreDaAggiornare.get());
                    squadraService.saveSquadra(squadra);
                    // Chiudi il dialog dopo l'acquisto
                    dialog.close();
                    // Aggiorna la griglia dei giocatori nella schermata principale
                    giocatoriGrid.setItems(squadra.getListaGiocatoriAcquistati());
                } else {
                    Notification.show("Qualcosa è andato storto durante l'acquisto.");
                }
            } else {
                // Mostra un messaggio di errore se nessun giocatore è selezionato
                Notification.show("Seleziona un giocatore prima di procedere all'acquisto.");
            }
        });

        acquistaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        content.add(acquistaButton);
        content.setAlignSelf(FlexComponent.Alignment.CENTER, acquistaButton);

        dialog.setHeaderTitle("Cerca un giocatore");

        // Pulsante per chiudere il dialog
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeButton);

        dialog.add(content);
        dialog.open();
    }



    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null) {
            Long squadraId = Long.parseLong(parameter);
            Optional<Squadra> optionalSquadra = squadraService.getSquadraById(squadraId);

            if (optionalSquadra.isPresent()) {
                this.squadra = optionalSquadra.get();
                giocatoriGrid.setItems(this.squadra.getListaGiocatoriAcquistati());

                // Aggiungi pulsanti di modifica e cancellazione per ciascun giocatore
                giocatoriGrid.addComponentColumn(this::createModificaPrezzoAcquistoButton).setHeader("");
                giocatoriGrid.addComponentColumn(this::createDeleteButton).setHeader("");
            }
        }
    }

    private Button createModificaPrezzoAcquistoButton(Giocatore giocatore) {
        Button editButton = new Button(new Icon("lumo", "edit"));
        editButton.addClickListener(e -> {
            // Implementa la logica di modifica del giocatore
        });
        return editButton;
    }

    private Button createDeleteButton(Giocatore giocatore) {
        Button deleteButton = new Button(new Icon("lumo", "cross"));
        deleteButton.addClickListener(e -> {
            // Implementa la logica di cancellazione del giocatore
        });
        return deleteButton;
    }
}

