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

        refreshTable();
    }

    /**
     * Centralna metoda koja dohvaća sve dobavljače, primjenjuje filter
     * i ažurira TableView.
     */
    private void refreshTable() {
        try {
            List<Supplier> allSuppliers = supplierRepository.findAll();
            String nameFilter = nameFilterField.getText().toLowerCase();

            List<Supplier> filteredList = allSuppliers.stream()
                    .filter(supplier -> supplier.getName().toLowerCase().contains(nameFilter))
                    .toList();

            supplierTableView.setItems(FXCollections.observableArrayList(filteredList));
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Nije moguće dohvatiti podatke o dobavljačima.", e);
            supplierTableView.setItems(FXCollections.emptyObservableList());
        }
    }

    /**
     * Filtrira prikazane dobavljače pozivom centralne metode za osvježavanje.
     */
    @FXML
    private void handleFilter() {
        refreshTable();
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
                refreshTable();
                DialogUtils.showInformation("Uspjeh", "Dobavljač je uspješno obrisan.");
            }
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Došlo je do greške prilikom brisanja dobavljača.", e);
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
            refreshTable();
        }
    }

    /**
     * Centralizirano obrađuje i logira greške iz repozitorija.
     * @param message Poruka koja se prikazuje korisniku.
     * @param e Iznimka koja se logira.
     */
    private void handleRepositoryError(String message, RepositoryAccessException e) {
        log.error(message, e);
        DialogUtils.showError("Greška baze podataka", message);
    }
}