package com.focs.auctionfriend.views.squadre;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.services.GiocatoreService;
import com.focs.auctionfriend.data.services.SquadraService;
import com.focs.auctionfriend.data.util.Ruolo;
import com.focs.auctionfriend.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

        giocatoriGrid.setColumns("nome", "ruolo", "quotaIniziale", "prezzoAcquisto", "club");

        giocatoriGrid.setAllRowsVisible(true);

        giocatoriGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        Button acquistaGiocatoreButton = new Button("Acquista giocatore", event -> openAcquistaGiocatoreDialog());
        acquistaGiocatoreButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        acquistaGiocatoreButton.addClassName("fixed-button");

        add(acquistaGiocatoreButton);
        add(giocatoriGrid);
    }

    private void openAcquistaGiocatoreDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

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

        portiereFilter.setTooltipText("Portiere");
        difensoreFilter.setTooltipText("Difensore");
        centrocampistaFilter.setTooltipText("Centrocampista");
        attaccanteFilter.setTooltipText("Attaccante");

        // Layout per i checkbox
        FlexLayout checkboxLayout = new FlexLayout(
                portiereFilter, difensoreFilter,
                centrocampistaFilter, attaccanteFilter
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
        giocatoriDialogGrid.setColumns("nome", "ruolo", "quotaIniziale", "prezzoAcquisto", "club");

        // Pulsante di ricerca
        Button cercaButton = new Button("Cerca", event -> {
            List<Giocatore> filteredGiocatori = giocatoreService.getGiocatoriFiltered(
                    nomeFilter.getValue(),
                    portiereFilter.getValue(),
                    difensoreFilter.getValue(),
                    centrocampistaFilter.getValue(),
                    attaccanteFilter.getValue(), true);

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
                dialog.removeAll();
                dialog.add(confirmPurchaseContent(giocatoreSelezionato, dialog));
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

    private Component confirmPurchaseContent(Giocatore giocatoreSelezionato, Dialog dialog) {
        VerticalLayout confirmPurchaseDialog = new VerticalLayout();
        confirmPurchaseDialog.setPadding(true);
        confirmPurchaseDialog.setSpacing(true);

        TextField purchasePriceField = new TextField("Prezzo di acquisto");
        purchasePriceField.setValue("1");
        confirmPurchaseDialog.add(purchasePriceField);

        dialog.getHeader().removeAll();

        Button confirmPurchaseButton = new Button("Conferma acquisto", event -> {
            String purchasePrice = purchasePriceField.getValue();
            if(purchasePrice != null && !purchasePrice.isEmpty()){

                //Controllo se il giocatore è acquistabile

                //Controllo se ho abbastanza crediti

                if (squadra.getCrediti() < Integer.parseInt(purchasePrice)){
                    Notification.show("Crediti insufficienti").setPosition(Notification.Position.TOP_END);
                }

                //Controllo se ho abbastanza slot

                else if (giocatoreSelezionato.getRuolo().equals(Ruolo.P) && !squadraService.hoSlotPorta(squadra)) {
                    Notification.show("Hai già 3 portieri").setPosition(Notification.Position.TOP_END);
                }
                else if (giocatoreSelezionato.getRuolo().equals(Ruolo.D) && !squadraService.hoSlotDifesa(squadra)) {
                    Notification.show("Hai già 8 difensori").setPosition(Notification.Position.TOP_END);
                }
                else if (giocatoreSelezionato.getRuolo().equals(Ruolo.C) && !squadraService.hoSlotCentrocampo(squadra)) {
                    Notification.show("Hai già 8 centrocampisti").setPosition(Notification.Position.TOP_END);
                }
                else if (giocatoreSelezionato.getRuolo().equals(Ruolo.A) && !squadraService.hoSlotAttacco(squadra)) {
                    Notification.show("Hai già 6 attaccanti").setPosition(Notification.Position.TOP_END);
                }
                else {
                    // Aggiorna il giocatore
                    Optional<Giocatore> giocatoreDaAggiornare = giocatoreService.getGiocatoreById(giocatoreSelezionato.getId());
                    if (giocatoreDaAggiornare.isPresent()) {
                        squadraService.acquistaGiocatore(squadra, giocatoreDaAggiornare.get(), Integer.parseInt(purchasePrice));
                        giocatoriGrid.setItems(squadra.getListaGiocatoriAcquistati()); // Aggiorna la griglia
                        dialog.close();
                        Notification.show("Acquisto confermato!").setPosition(Notification.Position.TOP_END);
                    } else {
                        Notification.show("Qualcosa è andato storto durante l'acquisto.").setPosition(Notification.Position.TOP_END);
                    }
                }
            }
            else {
                Notification.show("Inserisci il prezzo di acquisto.").setPosition(Notification.Position.TOP_END);
            }
        });
        confirmPurchaseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", event -> {
            dialog.close();
        });

        // Aggiungi il componente per i pulsanti nella parte inferiore del layout
        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmPurchaseButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        confirmPurchaseDialog.add(purchasePriceField, buttonLayout);

        dialog.setHeaderTitle("Conferma acquisto");

        return confirmPurchaseDialog;
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
                giocatoriGrid.addComponentColumn(this::createSvincolaButton).setHeader("");
            }
        }
    }

    private Button createModificaPrezzoAcquistoButton(Giocatore giocatore) {
        Button editButton = new Button(new Icon("lumo", "edit"));
        editButton.addClickListener(e -> {
            // Crea un campo di testo per la modifica del prezzo
            TextField prezzoField = new TextField();
            prezzoField.setValue(String.valueOf(giocatore.getPrezzoAcquisto()));

            // Crea un pulsante di conferma
            Button confirmButton = new Button(new Icon("lumo", "checkmark"));
            confirmButton.addClickListener(confirmEvent -> {
                try {
                    int nuovoPrezzo = Integer.parseInt(prezzoField.getValue());
                    giocatore.setPrezzoAcquisto(nuovoPrezzo);
                    giocatoreService.updateGiocatore(giocatore); // Supponendo che ci sia un metodo per l'aggiornamento
                    giocatoriGrid.getDataProvider().refreshItem(giocatore);
                    giocatoriGrid.setItems(squadra.getListaGiocatoriAcquistati()); // Aggiorna la griglia
                    Notification.show("Prezzo di acquisto aggiornato con successo.", 3000, Notification.Position.BOTTOM_START);
                } catch (NumberFormatException ex) {
                    Notification.show("Inserisci un prezzo valido.", 3000, Notification.Position.BOTTOM_START);
                }
            });

            // Crea un pulsante di annullamento
            Button cancelButton = new Button(new Icon("lumo", "cross"));
            cancelButton.addClickListener(cancelEvent -> {
                giocatoriGrid.getDataProvider().refreshItem(giocatore); // Ripristina il valore originale
            });

            // Crea un layout per i pulsanti di conferma e annullamento
            HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);

            // Aggiungi i componenti al layout
            VerticalLayout editLayout = new VerticalLayout(prezzoField, buttonLayout);

            // Aggiorna il componente nella cella
            giocatoriGrid.getEditor().editItem(giocatore);
            giocatoriGrid.getColumnByKey("prezzoAcquisto").setEditorComponent(editLayout);
        });
        return editButton;
    }


    private Button createSvincolaButton(Giocatore giocatore) {
        Button deleteButton = new Button(new Icon("lumo", "cross"));
        deleteButton.addClickListener(e -> {
            boolean flag = squadraService.ridaiSoldiESvincola(squadra, giocatore);
            if (flag) {
                squadra.getListaGiocatoriAcquistati().remove(giocatore); // Rimuovi il giocatore dalla lista
                giocatoriGrid.setItems(squadra.getListaGiocatoriAcquistati()); // Aggiorna la griglia
                Notification notification = Notification.show("Giocatore svincolato correttamente.", 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification notification = Notification.show("Errore nello svincolo del giocatore.", 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return deleteButton;
    }

}

