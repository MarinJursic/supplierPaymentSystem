package hr.javafx.projekt.controller;

import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.InvoiceStatus;
import hr.javafx.projekt.repository.InvoiceRepository;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler za ekran za prikaz i upravljanje fakturama.
 */
public class InvoiceController {

    private static InvoiceController activeInstance;

    @FXML private TextField invoiceNumberFilterField;
    @FXML private TextField supplierFilterField;
    @FXML private TableView<Invoice> invoiceTableView;
    @FXML private TableColumn<Invoice, String> invoiceNumberColumn;
    @FXML private TableColumn<Invoice, String> supplierColumn;
    @FXML private TableColumn<Invoice, String> amountColumn;
    @FXML private TableColumn<Invoice, String> issueDateColumn;
    @FXML private TableColumn<Invoice, String> dueDateColumn;
    @FXML private TableColumn<Invoice, String> statusColumn;
    @FXML private Button deleteInvoiceButton;
    @FXML private MenuController menuController;

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();

    /**
     * Inicijalizira kontroler, postavlja stupce tablice, bojanje redaka i učitava podatke.
     */
    public void initialize() {
        activeInstance = this;
        if (menuController != null) menuController.initialize();
        if (!SessionManager.isAdmin()) deleteInvoiceButton.setVisible(false);

        setupTableColumns();
        setupRowColoring();
        loadAndDisplayInvoices();
    }

    private void setupTableColumns() {
        invoiceNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        supplierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplier().getName()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAmount().toString()));
        issueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssueDate().toString()));
        dueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDescription()));
    }

    /**
     * Postavlja logiku za bojanje redaka u tablici.
     * Redci s 'Overdue' statusom bit će obojani crveno.
     */
    private void setupRowColoring() {
        invoiceTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getStatus() instanceof InvoiceStatus.Overdue) {
                    setStyle("-fx-background-color: #ffcccc;"); // Svijetlo crvena
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadAndDisplayInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        Platform.runLater(() -> {
            String invoiceNumberFilter = invoiceNumberFilterField.getText();
            String supplierFilter = supplierFilterField.getText();

            // Primijeni filtere ako postoje, inače prikaži sve
            if (!invoiceNumberFilter.isBlank() || !supplierFilter.isBlank()) {
                handleFilter();
            } else {
                invoiceTableView.setItems(FXCollections.observableArrayList(invoices));
            }
        });
    }

    /**
     * Statička metoda koju poziva pozadinski servis za osvježavanje prikaza.
     */
    public static void refreshActiveInstance() {
        if (activeInstance != null) {
            activeInstance.loadAndDisplayInvoices();
        }
    }

    /**
     * Filtrira prikazane fakture na temelju unesenih kriterija.
     */
    @FXML
    private void handleFilter() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        String invoiceNumberFilter = invoiceNumberFilterField.getText().toLowerCase();
        String supplierFilter = supplierFilterField.getText().toLowerCase();

        List<Invoice> filteredList = allInvoices.stream()
                .filter(invoice -> invoice.getInvoiceNumber().toLowerCase().contains(invoiceNumberFilter))
                .filter(invoice -> invoice.getSupplier().getName().toLowerCase().contains(supplierFilter))
                .collect(Collectors.toList());

        invoiceTableView.setItems(FXCollections.observableArrayList(filteredList));
    }

    /**
     * Otvara prozor za unos nove fakture.
     */
    @FXML
    private void handleNewInvoice() {
        showEditDialog(null);
    }

    /**
     * Otvara prozor za izmjenu odabrane fakture.
     */
    @FXML
    private void handleEditInvoice() {
        Invoice selectedInvoice = invoiceTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            showEditDialog(selectedInvoice);
        } else {
            showAlert(Alert.AlertType.WARNING, "Nije odabrana faktura", "Molimo odaberite fakturu za izmjenu.");
        }
    }

    /**
     * Briše odabranu fakturu nakon potvrde.
     */
    @FXML
    private void handleDeleteInvoice() {
        Invoice selectedInvoice = invoiceTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Jeste li sigurni da želite obrisati ovu fakturu?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                invoiceRepository.deleteById(selectedInvoice.getId());
                loadAndDisplayInvoices(); // Osvježi prikaz
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Nije odabrana faktura", "Molimo odaberite fakturu za brisanje.");
        }
    }

    /**
     * Ručno osvježava prikaz tablice.
     */
    @FXML
    private void handleRefresh() {
        loadAndDisplayInvoices();
    }

    private void showEditDialog(Invoice invoice) {
        String title = (invoice == null) ? "Dodaj Novu Fakturu" : "Izmijeni Fakturu";
        Optional<Navigation.Popup<InvoiceEditController>> popupOptional = Navigation.createPopup("invoice_edit.fxml", title);

        if (popupOptional.isPresent()) {
            Navigation.Popup<InvoiceEditController> popup = popupOptional.get();
            if (invoice != null) {
                popup.controller().setInvoiceToEdit(invoice);
            }
            popup.stage().showAndWait();
            loadAndDisplayInvoices();
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