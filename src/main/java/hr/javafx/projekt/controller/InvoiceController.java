package hr.javafx.projekt.controller;

import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.repository.InvoiceRepository;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

/**
 * Kontroler za ekran za prikaz i upravljanje fakturama.
 */
public class InvoiceController {

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

    public void initialize() {
        if (menuController != null) menuController.initialize();
        if (!SessionManager.isAdmin()) deleteInvoiceButton.setVisible(false);

        invoiceNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoiceNumber()));
        supplierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplier().getName()));
        amountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAmount().toString()));
        issueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssueDate().toString()));
        dueDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDescription()));

        loadAndDisplayInvoices();
    }

    private void loadAndDisplayInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        invoiceTableView.setItems(FXCollections.observableArrayList(invoices));
    }

    @FXML
    private void handleFilter() {
        List<Invoice> allInvoices = invoiceRepository.findAll(); // Uvijek dohvati svježe podatke
        String invoiceNumberFilter = invoiceNumberFilterField.getText().toLowerCase();
        String supplierFilter = supplierFilterField.getText().toLowerCase();

        List<Invoice> filteredList = allInvoices.stream()
                .filter(invoice -> invoice.getInvoiceNumber().toLowerCase().contains(invoiceNumberFilter))
                .filter(invoice -> invoice.getSupplier().getName().toLowerCase().contains(supplierFilter))
                .toList();

        invoiceTableView.setItems(FXCollections.observableArrayList(filteredList));
    }

    @FXML
    private void handleNewInvoice() {
        showEditDialog(null);
    }

    @FXML
    private void handleEditInvoice() {
        Invoice selectedInvoice = invoiceTableView.getSelectionModel().getSelectedItem();
        if (selectedInvoice != null) {
            showEditDialog(selectedInvoice);
        } else {
            showAlert(Alert.AlertType.WARNING, "Nije odabrana faktura", "Molimo odaberite fakturu za izmjenu.");
        }
    }

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

    private void showEditDialog(Invoice invoice) {
        String title = (invoice == null) ? "Dodaj Novu Fakturu" : "Izmijeni Fakturu";

        Optional<Navigation.Popup<InvoiceEditController>> popupOptional = Navigation.createPopup("invoice_edit.fxml", title);

        if (popupOptional.isPresent()) {
            Navigation.Popup<InvoiceEditController> popup = popupOptional.get();

            if (invoice != null) {
                popup.controller().setInvoiceToEdit(invoice);
            }

            popup.stage().showAndWait();

            // Osvježi tablicu nakon što se prozor zatvori
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