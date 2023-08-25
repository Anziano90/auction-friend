package com.focs.auctionfriend.views.squadre;

import com.focs.auctionfriend.data.entities.Squadra;
import com.focs.auctionfriend.data.services.SquadraService;
import com.focs.auctionfriend.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@PageTitle("Squadre")
@Route(value = "squadre", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@Uses(Icon.class)
public class SquadreView extends Div {

    private List<Div> listaDivRuoli;
    private Grid<Squadra> grid;

    private final SquadraService squadraService;

    public SquadreView(SquadraService squadraService) {
        this.squadraService = squadraService;
        setSizeFull();
        addClassNames("sleek-view-grid");

        this.grid = createGrid();

        VerticalLayout layout = new VerticalLayout(grid);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);

        // Aggiungi il pulsante "Aggiungi squadra +"
        Button addButton = new Button("Aggiungi squadra", event -> openAddSquadraDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName("fixed-button");
        add(addButton);
    }

    private Div updateStatisticheText(Div statisticheDiv, int portieri, int difensori, int centrocampisti, int attaccanti) {
        FlexLayout statisticheLayout = new FlexLayout();
        statisticheLayout.addClassName("statistiche-layout");

        Span portiereIcon = new Span(String.valueOf(portieri));
        portiereIcon.addClassName("ruolo-icon");
        portiereIcon.addClassName("ruolo-icon-yellow");
        portiereIcon.addClassName("icon-margin");

        Span difensoreIcon = new Span(String.valueOf(difensori));
        difensoreIcon.addClassName("ruolo-icon");
        difensoreIcon.addClassName("ruolo-icon-green");
        difensoreIcon.addClassName("icon-margin");

        Span centrocampistaIcon = new Span(String.valueOf(centrocampisti));
        centrocampistaIcon.addClassName("ruolo-icon");
        centrocampistaIcon.addClassName("ruolo-icon-blue");
        centrocampistaIcon.addClassName("icon-margin");

        Span attaccanteIcon = new Span(String.valueOf(attaccanti));
        attaccanteIcon.addClassName("ruolo-icon");
        attaccanteIcon.addClassName("ruolo-icon-red");
        attaccanteIcon.addClassName("icon-margin");

        statisticheLayout.add(portiereIcon, difensoreIcon, centrocampistaIcon, attaccanteIcon);

        statisticheDiv.removeAll();
        statisticheDiv.add(statisticheLayout);

        return statisticheDiv;
    }

    private void openAddSquadraDialog() {
        // Crea un dialog per l'aggiunta di una nuova squadra
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        // Contenuto del dialog
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        TextField squadraNameField = new TextField("Nome squadra");
        content.add(squadraNameField);

        // Pulsante di conferma
        //TODO: aggiustami quando rimpicciolisco la finestra in verticale
        Button confirmButton = new Button("Conferma", event -> {
            // Implementa qui la logica per creare la nuova squadra
            String nomeSquadra = squadraNameField.getValue();
            if (!nomeSquadra.isEmpty()) {
                Squadra nuovaSquadra = new Squadra();
                nuovaSquadra.setNome(nomeSquadra);
                nuovaSquadra.setCrediti(500);
                nuovaSquadra.setCreationDate(LocalDateTime.now());
                nuovaSquadra.setLastUpdated(LocalDateTime.now());
                nuovaSquadra.setListaGiocatoriAcquistati(new ArrayList<>());
                squadraService.saveSquadra(nuovaSquadra);
                Div statisticheSquadraDiv = new Div();
                statisticheSquadraDiv = updateStatisticheText(statisticheSquadraDiv, 0, 0, 0, 0);
                listaDivRuoli.add(statisticheSquadraDiv);
                dialog.close(); // Chiudi il dialog dopo la creazione
                // Aggiorna la griglia delle squadre
                this.grid.setItems(squadraService.getAllSquadre());
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        content.add(confirmButton);
        content.setAlignSelf(FlexComponent.Alignment.CENTER, confirmButton);

        dialog.setHeaderTitle("Aggiungi una squadra");

        // Pulsante per chiudere il dialog
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeButton);

        dialog.add(content);
        dialog.open();
    }

    private Grid<Squadra> createGrid() {
        Grid<Squadra> grid = new Grid<>(Squadra.class, false);
        List<Squadra> squadre = squadraService.getAllSquadre();
        listaDivRuoli = new ArrayList<>(squadre.size());

        grid.addColumn(Squadra::getNome).setHeader("Nome").setSortable(true);
        grid.addColumn(Squadra::getCrediti).setHeader("Crediti").setSortable(true);

        grid.addComponentColumn(squadra -> {
            Div statisticheSquadraDiv = new Div();
            statisticheSquadraDiv = updateStatisticheText(new Div(), squadraService.getPortieri(squadra),
                    squadraService.getDifensori(squadra),
                    squadraService.getCentrocampisti(squadra),
                    squadraService.getAttaccanti(squadra));
            statisticheSquadraDiv.addClassName("statistiche-counter-no-mg-left");
            listaDivRuoli.add(statisticheSquadraDiv);
            return statisticheSquadraDiv;
        }).setHeader("Giocatori in Rosa");

        //modifica
        grid.addComponentColumn(squadra -> {
            Button editButton = new Button("Dettaglio Squadra");
            editButton.addClickListener(e -> {
                // Reindirizza all'URL della vista di modifica con il parametro squadraId come stringa
                UI.getCurrent().navigate(EditSquadraView.class, squadra.getId() + "");
            });
            return editButton;
        }).setHeader("");


        //elimina
        grid.addComponentColumn(squadra -> {
            Button deleteButton = new Button("Elimina");
            deleteButton.addClickListener(e -> {
                // Crea un dialog di conferma per l'eliminazione della squadra
                Dialog confirmDialog = new Dialog();
                confirmDialog.setCloseOnEsc(false);
                confirmDialog.setCloseOnOutsideClick(false);

                VerticalLayout confirmContent = new VerticalLayout();
                confirmContent.setSpacing(true);

                confirmContent.add(new H2("Conferma Eliminazione"));
                confirmContent.add(new Text("Sei sicuro di voler eliminare questa squadra?"));

                Button cancelButton = new Button("Annulla", event -> confirmDialog.close());
                Button deleteConfirmButton = new Button("Elimina", event -> {
                    // Esegui l'eliminazione della squadra e aggiorna la griglia
                    squadraService.rimuoviSquadraESvincolaCalciatori(squadra);
                    squadre.remove(squadra);
                    grid.setItems(squadre);
                    confirmDialog.close();
                });
                deleteConfirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                confirmContent.add(new HorizontalLayout(cancelButton, deleteConfirmButton));
                confirmDialog.add(confirmContent);
                confirmDialog.open();
            });
            return deleteButton;
        }).setHeader("");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setItems(squadre);

        grid.setHeight("75vh");

        return grid;
    }


}
