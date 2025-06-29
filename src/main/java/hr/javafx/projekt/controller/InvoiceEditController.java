package hr.javafx.projekt.controller;

import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.InvoiceRepository;
import hr.javafx.projekt.repository.SupplierRepository;
import hr.javafx.projekt.utils.DialogUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kontroler za prozor za dodavanje i izmjenu faktura.
 */
public class InvoiceEditController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEditController.class);

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
     * Inicijalizira prozor, popunjava ComboBox-eve i postavlja prikaz za dobavljače.
     */
    public void initialize() {
        try {
            supplierComboBox.setItems(FXCollections.observableArrayList(supplierRepository.findAll()));
        } catch (RepositoryAccessException e) {
            handleRepositoryError("Nije moguće učitati dobavljače.", e);
        }
        statusComboBox.setItems(FXCollections.observableArrayList(InvoiceStatus.values()));

        Callback<ListView<Supplier>, ListCell<Supplier>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        };

        supplierComboBox.setCellFactory(cellFactory);
        supplierComboBox.setButtonCell(cellFactory.call(null));
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
            if (DialogUtils.showConfirmation("Potvrda izmjene", "Jeste li sigurni da želite spremiti promjene?")) {
                try {
                    Invoice updatedInvoice = new Invoice.Builder(invoiceToEdit.getId(), invoiceNumber, amount, supplier)
                            .withIssueDate(issueDate).withDueDate(dueDate).withStatus(status).build();
                    invoiceRepository.update(updatedInvoice);
                    closeWindow();
                } catch (RepositoryAccessException e) {
                    handleRepositoryError("Ažuriranje fakture nije uspjelo.", e);
                }
            }
        } else {
            try {
                Invoice newInvoice = new Invoice.Builder(null, invoiceNumber, amount, supplier)
                        .withIssueDate(issueDate).withDueDate(dueDate).withStatus(status).build();
                invoiceRepository.save(newInvoice);
                closeWindow();
            } catch (RepositoryAccessException e) {
                handleRepositoryError("Spremanje nove fakture nije uspjelo.", e);
            }
        }
    }

    /**
     * Zatvara prozor bez spremanja promjena.
     */
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Zatvara trenutni prozor.
     */
    private void closeWindow() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    /**
     * Provjerava jesu li svi uneseni podaci ispravni.
     * @return True ako je unos ispravan, inače false.
     */
    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (invoiceNumberField.getText() == null || invoiceNumberField.getText().isBlank())
            errorMessage.append("Broj fakture je obavezan.\n");
        if (supplierComboBox.getValue() == null)
            errorMessage.append("Dobavljač je obavezan.\n");
        if (amountField.getText() == null || amountField.getText().isBlank())
            errorMessage.append("Iznos je obavezan.\n");
        else {
            try {
                new BigDecimal(amountField.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("Iznos mora biti ispravan broj (npr. 123.45).\n");
            }
        }
        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        if (issueDate == null) errorMessage.append("Datum izdavanja je obavezan.\n");
        if (dueDate == null) errorMessage.append("Datum dospijeća je obavezan.\n");
        if (issueDate != null && dueDate != null && dueDate.isBefore(issueDate)) {
            errorMessage.append("Datum dospijeća ne može biti prije datuma izdavanja.\n");
        }
        if (statusComboBox.getValue() == null) errorMessage.append("Status je obavezan.\n");

        if (!errorMessage.isEmpty()) {
            DialogUtils.showError("Neispravan unos", errorMessage.toString());
            return false;
        }
        return true;
    }

    /**
     * Centralizirano rukuje greškama iz repozitorija i prikazuje odgovarajuću poruku.
     * @param defaultMessage Poruka koja se prikazuje ako greška nije specifična.
     * @param e Iznimka uhvaćena iz repozitorija.
     */
    private void handleRepositoryError(String defaultMessage, RepositoryAccessException e) {
        log.error(defaultMessage, e);
        Throwable cause = e.getCause();
        if (cause instanceof java.sql.SQLException sqlEx && "23505".equals(sqlEx.getSQLState())) {
            DialogUtils.showError("Greška pri spremanju", "Faktura s tim brojem već postoji!");
        } else {
            DialogUtils.showError("Greška pri spremanju", defaultMessage);
        }
    }
}