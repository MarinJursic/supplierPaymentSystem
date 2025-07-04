package hr.javafx.projekt.controller;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.SupplierRepository;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.DialogUtils;
import hr.javafx.projekt.utils.Navigation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler za ekran za prikaz i upravljanje dobavljačima.
 */
public class SupplierController {

    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    @FXML private TextField nameFilterField;
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, String> oibColumn;
    @FXML private Button deleteSupplierButton;
    @FXML private MenuController menuController;

    private final SupplierRepository supplierRepository = new SupplierRepository();
    private List<Supplier> allSuppliers;

    /**
     * Inicijalizira kontroler, postavlja stupce tablice, vidljivost gumba
     * ovisno o roli korisnika i učitava podatke o dobavljačima.
     */
    public void initialize() {
        if (menuController != null) menuController.initialize();
        if (!SessionManager.isAdmin()) deleteSupplierButton.setVisible(false);

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        addressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
        oibColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOib()));

        loadAllSuppliers();
    }

    /**
     * Učitava sve dobavljače iz repozitorija i prikazuje ih u tablici.
     * U slučaju greške, prikazuje poruku korisniku.
     */
    private void loadAllSuppliers() {
        try {
            allSuppliers = supplierRepository.findAll();
            supplierTableView.setItems(FXCollections.observableArrayList(allSuppliers));
        } catch (RepositoryAccessException e) {
            log.error("Greška prilikom dohvaćanja dobavljača.", e);
            DialogUtils.showError("Greška", "Nije moguće dohvatiti podatke o dobavljačima.");
        }
    }

    /**
     * Filtrira prikazane dobavljače na temelju unesenog naziva.
     */
    @FXML
    private void handleFilter() {
        String nameFilter = nameFilterField.getText().toLowerCase();
        List<Supplier> filteredList = allSuppliers.stream()
                .filter(supplier -> supplier.getName().toLowerCase().contains(nameFilter))
                .collect(Collectors.toList());
        supplierTableView.setItems(FXCollections.observableArrayList(filteredList));
    }

    /**
     * Otvara prozor za unos novog dobavljača.
     */
    @FXML
    private void handleNewSupplier() {
        showEditDialog(null);
    }

    /**
     * Otvara prozor za izmjenu odabranog dobavljača.
     * Ako nijedan dobavljač nije odabran, prikazuje upozorenje.
     */
    @FXML
    private void handleEditSupplier() {
        Supplier selected = supplierTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showEditDialog(selected);
        } else {
            DialogUtils.showWarning("Nije odabran dobavljač", "Molimo odaberite dobavljača za izmjenu.");
        }
    }

    /**
     * Briše odabranog dobavljača nakon provjere i potvrde.
     * Provjerava je li dobavljač povezan s fakturama prije brisanja.
     */
    @FXML
    private void handleDeleteSupplier() {
        Supplier selected = supplierTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Nije odabran dobavljač", "Molimo odaberite dobavljača za brisanje.");
            return;
        }

        try {
            if (supplierRepository.isSupplierLinkedToInvoices(selected.getId())) {
                DialogUtils.showError("Brisanje nije moguće", "Dobavljač se ne može obrisati jer postoje fakture povezane s njim.");
                return;
            }

            if (DialogUtils.showConfirmation("Potvrda brisanja", "Jeste li sigurni da želite obrisati ovog dobavljača?")) {
                supplierRepository.deleteById(selected.getId());
                loadAllSuppliers();
                DialogUtils.showInformation("Uspjeh", "Dobavljač je uspješno obrisan.");
            }
        } catch (RepositoryAccessException e) {
            log.error("Neuspjelo brisanje dobavljača.", e);
            DialogUtils.showError("Greška", "Došlo je do greške prilikom brisanja dobavljača.");
        }
    }

    /**
     * Prikazuje modalni prozor za dodavanje ili izmjenu dobavljača.
     * Nakon zatvaranja prozora, ponovno učitava sve dobavljače.
     * @param supplier Dobavljač za izmjenu, ili null ako se dodaje novi.
     */
    private void showEditDialog(Supplier supplier) {
        String title = (supplier == null) ? "Dodaj Novog Dobavljača" : "Izmijeni Dobavljača";
        Optional<Navigation.Popup<SupplierEditController>> popupOptional = Navigation.createPopup("supplier_edit.fxml", title);

        if (popupOptional.isPresent()) {
            Navigation.Popup<SupplierEditController> popup = popupOptional.get();
            if (supplier != null) {
                popup.controller().setSupplierToEdit(supplier);
            }
            popup.stage().showAndWait();
            loadAllSuppliers();
        }
    }
}