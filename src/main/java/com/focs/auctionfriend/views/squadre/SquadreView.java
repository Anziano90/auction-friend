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

        grid.addColumn(Squadra::getNome).setHeader("Nome");
        grid.addColumn(Squadra::getCrediti).setHeader("Crediti");
        ValueProvider<Squadra, String> numeroGiocatoriProvider = squadra -> squadraService.getNumeroGiocatori(squadra);
        grid.addColumn(numeroGiocatoriProvider).setHeader("Rosa");

        //modifica
        grid.addComponentColumn(squadra -> {
            Button editButton = new Button("Modifica");
            editButton.addClickListener(e -> {
                // Reindirizza all'URL della vista di modifica con il parametro squadraId come stringa
                UI.getCurrent().navigate(EditSquadraView.class, squadra.getId()+"");
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
                    squadraService.deleteSquadra(squadra.getId());
                    squadre.remove(squadra);
                    grid.setItems(squadre);
                    confirmDialog.close();
                });

                confirmContent.add(new HorizontalLayout(cancelButton, deleteConfirmButton));
                confirmDialog.add(confirmContent);
                confirmDialog.open();
            });
            return deleteButton;
        }).setHeader("");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setItems(squadre);

        // Imposta l'altezza massima della griglia per farla scrollable
        grid.setMaxHeight("calc(100vh - 150px)"); // 150px è l'altezza del pulsante "Aggiungi squadra +"

        return grid;
    }



}
