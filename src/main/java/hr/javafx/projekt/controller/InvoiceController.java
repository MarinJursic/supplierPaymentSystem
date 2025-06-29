package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.repository.InvoiceRepository;
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.DialogUtils;
import hr.javafx.projekt.utils.Navigation;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kontroler za ekran za prikaz i upravljanje fakturama.
 */
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    @FXML private TextField invoiceNumberFilterField;
    @FXML private TextField supplierFilterField;
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
    private Timeline refreshTimeline;

    /**
     * Inicijalizira kontroler, postavlja stupce tablice i pokreće periodično osvježavanje.
     */
    public void initialize() {
        if (menuController != null) menuController.initialize();
        if (!SessionManager.isAdmin()) deleteInvoiceButton.setVisible(false);

        setupTableColumns();
        setupRowColoring();
        loadAndDisplayInvoices();
        setupAutoRefresh();
    }

    /**
     * Konfigurira stupce tablice za prikaz podataka o fakturama.
     */
    private void setupTableColumns() {
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
    private void setupRowColoring() {
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
     * Postavlja i pokreće Timeline za automatsko osvježavanje podataka.
     */
    private void setupAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> loadAndDisplayInvoices()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Učitava i prikazuje fakture, primjenjujući filtere ako su aktivni.
     */
    private void loadAndDisplayInvoices() {
        if (isFilterActive()) {
            handleFilter();
        } else {
            try {
                invoiceTableView.setItems(FXCollections.observableArrayList(invoiceRepository.findAll()));
            } catch (RepositoryAccessException e) {
                handleRepositoryError("Nije moguće dohvatiti podatke o fakturama.", e);
            }
        }
    }

    /**
     * Filtrira fakture na temelju unesenih kriterija.
     */
    private void handleFilter() {
        if (refreshTimeline != null) refreshTimeline.stop();
        try {
            List<Invoice> allInvoices = invoiceRepository.findAll();
            String invoiceFilter = invoiceNumberFilterField.getText().toLowerCase();
            String supplierFilter = supplierFilterField.getText().toLowerCase();
            List<Invoice> filteredList = allInvoices.stream()
                    .filter(inv -> inv.getInvoiceNumber().toLowerCase().contains(invoiceFilter))
                    .filter(inv -> inv.getSupplier().getName().toLowerCase().contains(supplierFilter))
                    .toList();
            invoiceTableView.setItems(FXCollections.observableArrayList(filteredList));
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Nije moguće filtrirati podatke.", e);
        }
    }

    /**
     * Otvara prozor za unos nove fakture.
     */
    private void handleNewInvoice() {
        showEditDialog(null);
    }

    /**
     * Otvara prozor za izmjenu odabrane fakture.
     */
    private void handleEditInvoice() {
        Invoice selected = invoiceTableView.getSelectionModel().getSelectedItem();
        if (selected != null) showEditDialog(selected);
        else DialogUtils.showWarning("Nije odabrana faktura", "Molimo odaberite fakturu za izmjenu.");
    }

    /**
     * Briše odabranu fakturu nakon potvrde.
     */
    private void handleDeleteInvoice() {
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

    private boolean isFilterActive() {
        return !invoiceNumberFilterField.getText().isBlank() || !supplierFilterField.getText().isBlank();
    }

    private void handleRepositoryError(String message, RepositoryAccessException e) {
        log.error(message, e);
        DialogUtils.showError("Greška", message);
    }
}