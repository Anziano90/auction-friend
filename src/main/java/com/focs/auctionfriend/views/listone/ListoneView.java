package com.focs.auctionfriend.views.listone;

import com.focs.auctionfriend.data.entities.Giocatore;
import com.focs.auctionfriend.data.services.GiocatoreService;
import com.focs.auctionfriend.views.MainLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

    private Grid<Giocatore> grid;
    private TextField nomeFilter = new TextField();
    private Checkbox portiereFilter = new Checkbox("P");
    private Checkbox difensoreFilter = new Checkbox("D");
    private Checkbox centrocampistaFilter = new Checkbox("C");
    private Checkbox attaccanteFilter = new Checkbox("A");
    private Checkbox svincolatiFilter = new Checkbox("Svincolati");

    public ListoneView(GiocatoreService giocatoreService) {
        this.giocatoreService = giocatoreService;
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

                Notification.show("Caricamento completato con successo.");
            } catch (Exception e) {
                e.printStackTrace();
                Notification.show("Si è verificato un errore durante l'importazione dei giocatori.");
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
        //popola la grid
        List<Giocatore> giocatori = fetchPlayers();

        grid.addColumn(Giocatore::getNome).setHeader("Nome");
        grid.addColumn(Giocatore::getRuolo).setHeader("Ruolo");
        grid.addColumn(Giocatore::getQuotaIniziale).setHeader("Quota iniziale");
        grid.addColumn(Giocatore::getPrezzoAcquisto).setHeader("Prezzo Acquisto");
        grid.addColumn(Giocatore::getClub).setHeader("Club");
        grid.addColumn(Giocatore::getSquadraProprietaria).setHeader("Squadra Proprietaria");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setItems(giocatori);

        grid.setHeight("75vh");

        return grid;
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
