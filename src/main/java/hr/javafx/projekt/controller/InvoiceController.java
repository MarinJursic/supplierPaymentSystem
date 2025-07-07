package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.main.MainApplication;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.InvoiceRepository;
import hr.javafx.projekt.repository.SupplierRepository;
import hr.javafx.projekt.service.StatusBarState;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.DialogUtils;
import hr.javafx.projekt.utils.Navigation;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Kontroler za ekran za prikaz i upravljanje fakturama.
 */
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    @FXML private TextField invoiceNumberFilterField;
    @FXML private ComboBox<Supplier> supplierFilterComboBox;
    @FXML private TableView<Invoice> invoiceTableView;
    @FXML private TableColumn<Invoice, String> invoiceNumberColumn;
    @FXML private TableColumn<Invoice, String> supplierColumn;
    @FXML private TableColumn<Invoice, String> amountColumn;
    @FXML private TableColumn<Invoice, String> issueDateColumn;
    @FXML private TableColumn<Invoice, String> dueDateColumn;
    @FXML private TableColumn<Invoice, String> statusColumn;
    @FXML private TableColumn<Invoice, String> remainingDaysColumn;
    @FXML private Button deleteInvoiceButton;
    @FXML private MenuController menuController;

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final SupplierRepository supplierRepository = new SupplierRepository();

    /**
     * Inicijalizira kontroler, postavlja stupce tablice, filtere i učitava podatke.
     */
    public void initialize() {
        if (menuController != null) menuController.initialize();
        if (!SessionManager.isAdmin()) deleteInvoiceButton.setVisible(false);

        setupTableColumns();
        setupRowColoring();
        setupSupplierFilterComboBox();
        loadAndDisplayInvoices();

        StatusBarState state = MainApplication.getStatusBarState();
        state.refreshSignalProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayInvoices());
    }

    /**
     * Konfigurira stupce tablice za prikaz podataka o fakturama.
     */
    public void setupTableColumns() {
        invoiceNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        supplierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplier().getName()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAmount().toString()));
        issueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssueDate().toString()));
        dueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        remainingDaysColumn.setCellValueFactory(data -> {
            Invoice invoice = data.getValue();
            return new SimpleStringProperty(invoice.getStatus() == InvoiceStatus.UNPAID ?
                    String.valueOf(invoice.calculateRemainingDays()) : "-");
        });
    }

    /**
     * Postavlja bojanje redaka u tablici ovisno o statusu fakture.
     */
    public void setupRowColoring() {
        invoiceTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.getStatus() == InvoiceStatus.OVERDUE) setStyle("-fx-background-color: #ffcccc;");
                else setStyle("");
            }
        });
    }

    /**
     * Postavlja ComboBox za filtriranje dobavljača, koristeći Set za jedinstvene vrijednosti.
     */
    private void setupSupplierFilterComboBox() {
        try {
            Set<Supplier> uniqueSuppliers = new HashSet<>(supplierRepository.findAll());
            List<Supplier> supplierList = new java.util.ArrayList<>();
            supplierList.add(null);
            supplierList.addAll(uniqueSuppliers);

            supplierFilterComboBox.setItems(FXCollections.observableArrayList(supplierList));
            supplierFilterComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Supplier supplier) {
                    return supplier == null ? "Svi dobavljači" : supplier.getName();
                }
                @Override
                public Supplier fromString(String string) { return null; }
            });
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Nije moguće učitati dobavljače za filter.", e);
        }
    }

    /**
     * Učitava i prikazuje fakture, pozivajući filter ako je aktivan.
     */
    private void loadAndDisplayInvoices() {
        List<Invoice> invoices;
        try {
            invoices = invoiceRepository.findAll();
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Nije moguće učitati fakture iz baze podataka.", e);
            invoices = Collections.emptyList();
        }

        List<Invoice> finalInvoices = invoices;
        Platform.runLater(() -> {
            if (!invoiceNumberFilterField.getText().isBlank() || supplierFilterComboBox.getValue() != null) {
                handleFilter();
            } else {
                invoiceTableView.setItems(FXCollections.observableArrayList(finalInvoices));
            }
        });
    }

    /**
     * Filtrira fakture na temelju unesenih kriterija.
     */
    @FXML
    public void handleFilter() {
        try {
            List<Invoice> allInvoices = invoiceRepository.findAll();
            String invoiceFilter = invoiceNumberFilterField.getText().toLowerCase();
            Supplier selectedSupplier = supplierFilterComboBox.getValue();

            List<Invoice> filteredList = allInvoices.stream()
                    .filter(inv -> inv.getInvoiceNumber().toLowerCase().contains(invoiceFilter))
                    .filter(inv -> {

                        if (selectedSupplier == null) {
                            return true;
                        }

                        return Objects.equals(inv.getSupplier().getId(), selectedSupplier.getId());
                    })
                    .toList();

            invoiceTableView.setItems(FXCollections.observableArrayList(filteredList));
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Nije moguće filtrirati podatke.", e);
        }
    }

    /**
     * Otvara prozor za unos nove fakture.
     */
    @FXML
    public void handleNewInvoice() {
        showEditDialog(null);
    }

    /**
     * Otvara prozor za izmjenu odabrane fakture.
     */
    @FXML
    public void handleEditInvoice() {
        Invoice selected = invoiceTableView.getSelectionModel().getSelectedItem();
        if (selected != null) showEditDialog(selected);
        else DialogUtils.showWarning("Nije odabrana faktura", "Molimo odaberite fakturu za izmjenu.");
    }

    /**
     * Briše odabranu fakturu nakon potvrde.
     */
    @FXML
    public void handleDeleteInvoice() {
        Invoice selected = invoiceTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtils.showWarning("Nije odabrana faktura", "Molimo odaberite fakturu za brisanje.");
            return;
        }
        if (DialogUtils.showConfirmation("Potvrda brisanja", "Jeste li sigurni da želite obrisati ovu fakturu?")) {
            try {
                invoiceRepository.deleteById(selected.getId());
                loadAndDisplayInvoices();
            } catch (RepositoryAccessException e) {
                handleRepositoryError("Brisanje fakture nije uspjelo.", e);
            }
        }
    }

    /**
     * Prikazuje modalni prozor za dodavanje ili izmjenu fakture.
     * @param invoice Faktura za izmjenu, ili null ako se dodaje nova.
     */
    private void showEditDialog(Invoice invoice) {
        String title = (invoice == null) ? "Dodaj Novu Fakturu" : "Izmijeni Fakturu";
        Optional<Navigation.Popup<InvoiceEditController>> popupOpt = Navigation.createPopup("invoice_edit.fxml", title);
        popupOpt.ifPresent(popup -> {
            if (invoice != null) popup.controller().setInvoiceToEdit(invoice);
            popup.stage().showAndWait();
            loadAndDisplayInvoices();
        });
    }

    /**
     * Centralizirano obrađuje i logira greške iz repozitorija.
     * @param message Poruka koja se prikazuje korisniku.
     * @param e Iznimka koja se logira.
     */
    private void handleRepositoryError(String message, RepositoryAccessException e) {
        log.error(message, e);
        DialogUtils.showError("Greška", message);
    }
}