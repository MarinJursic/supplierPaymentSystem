package hr.javafx.projekt.controller;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.SupplierRepository;
import hr.javafx.projekt.utils.DialogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kontroler za prozor za dodavanje i izmjenu dobavljača.
 */
public class SupplierEditController {

    private static final Logger log = LoggerFactory.getLogger(SupplierEditController.class);

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField oibField;

    private final SupplierRepository supplierRepository = new SupplierRepository();
    private Supplier supplierToEdit = null;

    /**
     * Postavlja podatke dobavljača za izmjenu i poziva metodu za popunjavanje forme.
     * @param supplier Dobavljač koji se mijenja.
     */
    public void setSupplierToEdit(Supplier supplier) {
        this.supplierToEdit = supplier;
        populateFormFields();
    }

    /**
     * Popunjava polja na formi podacima iz supplierToEdit objekta.
     */
    private void populateFormFields() {
        titleLabel.setText("Izmijeni Dobavljača");
        nameField.setText(supplierToEdit.getName());
        addressField.setText(supplierToEdit.getAddress());
        oibField.setText(supplierToEdit.getOib());
    }

    /**
     * Rukuje spremanjem podataka. Validira unos, traži potvrdu za izmjene,
     * i sprema novog ili ažurira postojećeg dobavljača.
     */
    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        try {
            if (supplierToEdit != null) {
                if (DialogUtils.showConfirmation("Potvrda izmjene", "Jeste li sigurni da želite spremiti promjene?")) {
                    supplierToEdit.setName(nameField.getText());
                    supplierToEdit.setAddress(addressField.getText());
                    supplierToEdit.setOib(oibField.getText());
                    supplierRepository.update(supplierToEdit);
                    closeWindow();
                }
            } else {
                Supplier newSupplier = new Supplier(null, nameField.getText(), addressField.getText(), oibField.getText());
                supplierRepository.save(newSupplier);
                closeWindow();
            }
        } catch (RepositoryAccessException e) {
            handleRepositoryError(e);
        }
    }

    /**
     * Zatvara prozor bez spremanja promjena.
     */
    @FXML
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
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errorMessage.append("Naziv je obavezan.\n");
        }
        if (addressField.getText() == null || addressField.getText().isBlank()) {
            errorMessage.append("Adresa je obavezna.\n");
        }
        if (oibField.getText() == null || oibField.getText().isBlank()) {
            errorMessage.append("OIB je obavezan.\n");
        } else if (!oibField.getText().matches("\\d{11}")) {
            errorMessage.append("OIB mora imati točno 11 znamenki.\n");
        }

        if (!errorMessage.isEmpty()) {
            DialogUtils.showError("Neispravan unos", errorMessage.toString());
            return false;
        }
        return true;
    }

    /**
     * Centralizirano rukuje greškama iz repozitorija i prikazuje odgovarajuću poruku.
     * @param e Iznimka uhvaćena iz repozitorija.
     */
    private void handleRepositoryError(RepositoryAccessException e) {
        log.error("Greška pri spremanju dobavljača.", e);
        Throwable cause = e.getCause();
        if (cause instanceof java.sql.SQLException sqlEx && "23505".equals(sqlEx.getSQLState())) {
            DialogUtils.showError("Greška pri spremanju", "Dobavljač s tim OIB-om već postoji!");
        } else {
            DialogUtils.showError("Greška pri spremanju", "Spremanje nije uspjelo. Provjerite log za detalje.");
        }
    }
}