package hr.javafx.projekt.controller;

import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.InvoiceStatus;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.InvoiceRepository;
import hr.javafx.projekt.repository.SupplierRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kontroler za prozor za dodavanje i izmjenu faktura.
 */
public class InvoiceEditController {

    @FXML private Label titleLabel;
    @FXML private TextField invoiceNumberField;
    @FXML private ComboBox<Supplier> supplierComboBox;
    @FXML private TextField amountField;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<InvoiceStatus> statusComboBox;

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final SupplierRepository supplierRepository = new SupplierRepository();
    private Invoice invoiceToEdit = null;

    /**
     * Inicijalizira prozor, popunjava ComboBox-eve s dobavljačima i statusima.
     */
    public void initialize() {
        supplierComboBox.setItems(FXCollections.observableArrayList(supplierRepository.findAll()));
        statusComboBox.setItems(FXCollections.observableArrayList(
                new InvoiceStatus.Unpaid(),
                new InvoiceStatus.Paid(),
                new InvoiceStatus.Overdue()
        ));
    }

    /**
     * Postavlja podatke fakture za izmjenu i popunjava polja u formi.
     * @param invoice Faktura koja se mijenja.
     */
    public void setInvoiceToEdit(Invoice invoice) {
        this.invoiceToEdit = invoice;
        titleLabel.setText("Izmijeni Fakturu");

        invoiceNumberField.setText(invoice.getInvoiceNumber());
        supplierComboBox.setValue(invoice.getSupplier());
        amountField.setText(invoice.getAmount().toString());
        issueDatePicker.setValue(invoice.getIssueDate());
        dueDatePicker.setValue(invoice.getDueDate());
        statusComboBox.setValue(invoice.getStatus());
    }

    /**
     * Rukuje spremanjem podataka. Validira unos i sprema novu ili ažurira postojeću fakturu.
     */
    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        String invoiceNumber = invoiceNumberField.getText();
        Supplier supplier = supplierComboBox.getValue();
        BigDecimal amount = new BigDecimal(amountField.getText());
        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        InvoiceStatus status = statusComboBox.getValue();

        if (invoiceToEdit != null) {
            // Ažuriranje postojeće fakture
            Invoice updatedInvoice = new Invoice.Builder(invoiceToEdit.getId(), invoiceNumber, amount, supplier)
                    .withIssueDate(issueDate)
                    .withDueDate(dueDate)
                    .withStatus(status)
                    .build();
            invoiceRepository.update(updatedInvoice);
        } else {
            // Kreiranje nove fakture
            Invoice newInvoice = new Invoice.Builder(null, invoiceNumber, amount, supplier)
                    .withIssueDate(issueDate)
                    .withDueDate(dueDate)
                    .withStatus(status)
                    .build();
            invoiceRepository.save(newInvoice);
        }

        closeWindow();
    }

    /**
     * Zatvara prozor bez spremanja promjena.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Provjerava jesu li svi uneseni podaci ispravni.
     * @return True ako je unos ispravan, inače false.
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (invoiceNumberField.getText() == null || invoiceNumberField.getText().isBlank()) {
            errorMessage.append("Broj fakture je obavezan.\n");
        }
        if (supplierComboBox.getValue() == null) {
            errorMessage.append("Dobavljač je obavezan.\n");
        }
        if (amountField.getText() == null || amountField.getText().isBlank()) {
            errorMessage.append("Iznos je obavezan.\n");
        } else {
            try {
                new BigDecimal(amountField.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("Iznos mora biti ispravan broj (npr. 123.45).\n");
            }
        }
        if (issueDatePicker.getValue() == null) {
            errorMessage.append("Datum izdavanja je obavezan.\n");
        }
        if (dueDatePicker.getValue() == null) {
            errorMessage.append("Datum dospijeća je obavezan.\n");
        }
        if (statusComboBox.getValue() == null) {
            errorMessage.append("Status je obavezan.\n");
        }

        if (!errorMessage.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Neispravan unos");
            alert.setHeaderText("Molimo ispravite sljedeće greške:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }
}