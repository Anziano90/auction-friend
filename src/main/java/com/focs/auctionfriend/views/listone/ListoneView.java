package com.focs.auctionfriend.views.listone;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.services.GiocatoreService;
import com.focs.auctionfriend.data.services.SquadraService;
import com.focs.auctionfriend.data.util.Ruolo;
import com.focs.auctionfriend.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.InputStream;
import java.util.List;

@PageTitle("Listone")
@Route(value = "giocatori", layout = MainLayout.class)
public class ListoneView extends Div {

    private final GiocatoreService giocatoreService;
    private final SquadraService squadraService;

    private Grid<Giocatore> grid;
    private TextField nomeFilter = new TextField();
    private Checkbox portiereFilter = new Checkbox("P");
    private Checkbox difensoreFilter = new Checkbox("D");
    private Checkbox centrocampistaFilter = new Checkbox("C");
    private Checkbox attaccanteFilter = new Checkbox("A");
    private Checkbox svincolatiFilter = new Checkbox("Svincolati");

    public ListoneView(GiocatoreService giocatoreService, SquadraService squadraService) {
        this.giocatoreService = giocatoreService;
        this.squadraService = squadraService;
        addClassNames("sleek-view-grid");

        nomeFilter.setPlaceholder("Nome");

        // Allinea i checkbox utilizzando un layout flessibile
        HorizontalLayout checkboxLayout = new HorizontalLayout(
                portiereFilter, difensoreFilter,
                centrocampistaFilter, attaccanteFilter
        );
        checkboxLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        checkboxLayout.setSpacing(true);
        checkboxLayout.setPadding(true);

        portiereFilter.setTooltipText("Portiere");
        difensoreFilter.setTooltipText("Difensore");
        centrocampistaFilter.setTooltipText("Centrocampista");
        attaccanteFilter.setTooltipText("Attaccante");

        grid = createGrid();

        svincolatiFilter.setValue(true);
        portiereFilter.setValue(true);
        difensoreFilter.setValue(true);
        centrocampistaFilter.setValue(true);
        attaccanteFilter.setValue(true);

        // Configura i filtri e il bottone di ricerca
        nomeFilter.addValueChangeListener(e -> updateFilters());
        portiereFilter.addValueChangeListener(e -> updateFilters());
        difensoreFilter.addValueChangeListener(e -> updateFilters());
        centrocampistaFilter.addValueChangeListener(e -> updateFilters());
        attaccanteFilter.addValueChangeListener(e -> updateFilters());
        svincolatiFilter.addValueChangeListener(e -> updateFilters());

        // Crea un buffer di memoria per l'upload dei file
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();

        // Crea il componente Upload
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".xlsx"); // Accetta solo file Excel (.xlsx)
        upload.setMaxFiles(1); // Imposta il numero massimo di file che possono essere caricati (in questo caso uno)

        // Aggiungi un listener per gestire l'evento di completamento dell'upload
        upload.addSucceededListener(event -> {
            try {
                // Estrai i dati dal file Excel caricato
                InputStream fileBytes = buffer.getInputStream(event.getFileName());
                giocatoreService.importGiocatoriFromExcel(fileBytes);

                // Aggiorna la griglia dei giocatori
                updateFilters();
                upload.clearFileList();

                Notification notification = Notification.show("Caricamento completato con successo.", 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                Notification notification = Notification.show("Si è verificato un errore durante l'importazione dei giocatori.", 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Crea il layout dei filtri
        HorizontalLayout filterLayout = new HorizontalLayout(
                nomeFilter, checkboxLayout,
                svincolatiFilter, upload
        );
        filterLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        filterLayout.setSpacing(true);

        // Popola la griglia con i dati iniziali
        updateFilters();

        // Aggiungi tutto al layout principale
        VerticalLayout layout = new VerticalLayout(filterLayout, grid);
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private Grid<Giocatore> createGrid() {
        Grid<Giocatore> grid = new Grid<>(Giocatore.class, false);

        List<Giocatore> giocatori = fetchPlayers();

        grid.addColumn(Giocatore::getNome).setHeader("Nome");
        grid.addColumn(Giocatore::getRuolo).setHeader("Ruolo");
        grid.addColumn(Giocatore::getQuotaIniziale).setHeader("Quota iniziale");
        grid.addColumn(Giocatore::getPrezzoAcquisto).setHeader("Prezzo Acquisto");
        grid.addColumn(Giocatore::getClub).setHeader("Club").setSortable(true);

        grid.addColumn(giocatore -> {
            Squadra squadraProprietaria = giocatore.getSquadraProprietaria();
            return squadraProprietaria != null ? squadraProprietaria.getNome() : "";
        }).setHeader("Squadra Proprietaria");

        grid.addComponentColumn(giocatore -> {
            Button euroButton = new Button(new Icon(VaadinIcon.EURO));
            euroButton.addClickListener(event -> openEuroDialog(giocatore));
            euroButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            return euroButton;
        }).setHeader("Acquista");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setItems(giocatori);

        grid.setHeight("75vh");

        return grid;
    }

    private void openEuroDialog(Giocatore giocatore) {
        Dialog acquistaDialog = new Dialog();
        acquistaDialog.setCloseOnOutsideClick(false);

        acquistaDialog.setHeaderTitle(giocatore.getNome());

        Select<Squadra> select = new Select<>();
        select.setItems(squadraService.getAllSquadre());
        select.setItemLabelGenerator(Squadra::getNome);
        select.setLabel("Seleziona una squadra");
        select.setClassName("mg-point5");

        TextField importoAcquisto = new TextField("Importo Acquisto");
        importoAcquisto.setValue("1");
        importoAcquisto.setClassName("mg-point5");

        Button confirmButton = new Button("Conferma", e -> {
            Squadra squadraSelezionata = select.getValue();
            if (squadraSelezionata != null) {

                //Controllo se il giocatore è acquistabile:

                //1)Controllo se ho abbastanza crediti
                if (squadraSelezionata.getCrediti() < Integer.parseInt(importoAcquisto.getValue())) {
                    Notification notification = Notification.show("Crediti insufficienti", 5000, Notification.Position.TOP_END);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                //2)Controllo se ho abbastanza crediti per i futuri acquisti
                else if (!squadraService.checkFuturiAcquisti(squadraService.MAX_GIOCATORI_ROSA - squadraSelezionata.getListaGiocatoriAcquistati().size(), squadraSelezionata.getCrediti(), Integer.parseInt(importoAcquisto.getValue()))) {
                    Notification notification = Notification.show("Crediti insufficienti per completare altri acquisti", 5000, Notification.Position.TOP_END);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }

                //3)Controllo se ho abbastanza slot
                else if (giocatore.getRuolo().equals(Ruolo.P) && !squadraService.hoSlotPorta(squadraSelezionata)) {
                    Notification notification = Notification.show("Hai già 3 portieri", 5000, Notification.Position.TOP_END);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                } else if (giocatore.getRuolo().equals(Ruolo.D) && !squadraService.hoSlotDifesa(squadraSelezionata)) {
                    Notification notification = Notification.show("Hai già 8 difensori", 5000, Notification.Position.TOP_END);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                } else if (giocatore.getRuolo().equals(Ruolo.C) && !squadraService.hoSlotCentrocampo(squadraSelezionata)) {
                    Notification notification = Notification.show("Hai già 8 centrocampisti", 5000, Notification.Position.TOP_END);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                } else if (giocatore.getRuolo().equals(Ruolo.A) && !squadraService.hoSlotAttacco(squadraSelezionata)) {
                    Notification notification = Notification.show("Hai già 6 attaccanti", 5000, Notification.Position.TOP_END);
                    notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                } else { //posso acquistare

                    boolean flag = squadraService.acquistaGiocatore(squadraSelezionata, giocatore, Integer.parseInt(importoAcquisto.getValue()));
                    if (flag) {
                        List<Giocatore> listaGiocatoriAggiornata = fetchPlayers();
                        this.grid.setItems(listaGiocatoriAggiornata);
                        acquistaDialog.close();
                        Notification notification = Notification.show("Acquisto completato con successo.", 5000, Notification.Position.TOP_END);
                        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } else {
                        acquistaDialog.close();
                        Notification notification = Notification.show("Errore nel completamento dell'acquisto.", 5000, Notification.Position.TOP_END);
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
            } else {
                Notification notification = Notification.show("Errore selezione della squadra", 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                acquistaDialog.close();
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> acquistaDialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(cancelButton, confirmButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        acquistaDialog.add(select, importoAcquisto, buttonsLayout);
        acquistaDialog.open();
    }

    private void updateFilters() {
        ListDataProvider<Giocatore> dataProvider = DataProvider.ofCollection(giocatoreService.getGiocatoriFiltered(
                nomeFilter.getValue(),
                portiereFilter.getValue(),
                difensoreFilter.getValue(),
                centrocampistaFilter.getValue(),
                attaccanteFilter.getValue(),
                svincolatiFilter.getValue()));
        grid.setDataProvider(dataProvider);
    }

    private List<Giocatore> fetchPlayers() {
        ListDataProvider<Giocatore> dataProvider = DataProvider.ofCollection(giocatoreService.getGiocatoriFiltered(
                nomeFilter.getValue(),
                portiereFilter.getValue(),
                difensoreFilter.getValue(),
                centrocampistaFilter.getValue(),
                attaccanteFilter.getValue(),
                svincolatiFilter.getValue()));
        return (List<Giocatore>) dataProvider.getItems();
    }
}
