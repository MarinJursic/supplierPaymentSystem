package hr.javafx.projekt.controller;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.SupplierRepository;
import hr.javafx.projekt.session.SessionManager;
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

    @FXML private TextField nameFilterField;
    @FXML private TableView<Supplier> supplierTableView;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, String> oibColumn;
    @FXML private Button deleteSupplierButton;
    @FXML private MenuController menuController;

    private final SupplierRepository supplierRepository = new SupplierRepository();
    private List<Supplier> allSuppliers;
    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    /**
     * Inicijalizira kontroler, postavlja stupce tablice i učitava podatke.
     */
    public void initialize() {
        if (menuController != null) menuController.initialize();
        if (!SessionManager.isAdmin()) deleteSupplierButton.setVisible(false);

        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        addressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
        oibColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOib()));

        loadAllSuppliers();
    }

    private void loadAllSuppliers() {
        allSuppliers = supplierRepository.findAll();
        supplierTableView.setItems(FXCollections.observableArrayList(allSuppliers));
    }

    /**
     * Filtrira prikazane dobavljače na temelju unesenog naziva.
     */
    @FXML
    private void handleFilter() {
        String nameFilter = nameFilterField.getText().toLowerCase();
        List<Supplier> filteredList = allSuppliers.stream()
                .filter(supplier -> supplier.getName().toLowerCase().contains(nameFilter))
                .toList();
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
     */
    @FXML
    private void handleEditSupplier() {
        Supplier selected = supplierTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showEditDialog(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "Nije odabran dobavljač", "Molimo odaberite dobavljača za izmjenu.");
        }
    }

    /**
     * Briše odabranog dobavljača nakon provjere i potvrde.
     */
    @FXML
    private void handleDeleteSupplier() {
        Supplier selected = supplierTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Nije odabran dobavljač", "Molimo odaberite dobavljača za brisanje.");
            return;
        }

        if (supplierRepository.isSupplierLinkedToInvoices(selected.getId())) {
            showAlert(Alert.AlertType.ERROR, "Brisanje nije moguće", "Dobavljač se ne može obrisati jer postoje fakture povezane s njim.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Jeste li sigurni da želite obrisati ovog dobavljača?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                supplierRepository.deleteById(selected.getId());
                loadAllSuppliers();
                showAlert(Alert.AlertType.INFORMATION, "Uspjeh", "Dobavljač je uspješno obrisan.");
            } catch (RepositoryAccessException e) {
                log.error("Neuspjelo brisanje dobavljača.", e);
                showAlert(Alert.AlertType.ERROR, "Greška", "Došlo je do greške prilikom brisanja dobavljača.");
            }
        }
    }

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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}