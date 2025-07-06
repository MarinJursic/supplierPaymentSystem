package hr.javafx.projekt.controller;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.exception.ValidationException;
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
     * Stavlja suppliera za edit
     * @param supplier
     */
    public void setSupplierToEdit(Supplier supplier) {
        this.supplierToEdit = supplier;
        populateFormFields();
    }

    /**
     * Stavlja vrijednosti u polja za edit
     */

    private void populateFormFields() {
        titleLabel.setText("Izmijeni Dobavljača");
        nameField.setText(supplierToEdit.getName());
        addressField.setText(supplierToEdit.getAddress());
        oibField.setText(supplierToEdit.getOib());
    }

    /**
     * Sprema podatke dobavljača
     */
    @FXML
    private void handleSave() {
        try {
            validateInput();
            String name = nameField.getText();
            String address = addressField.getText();
            String oib = oibField.getText();

            if (supplierToEdit != null) {
                if (DialogUtils.showConfirmation("Potvrda izmjene", "Jeste li sigurni da želite spremiti promjene?")) {
                    Supplier updatedSupplier = new Supplier(supplierToEdit.getId(), name, address, oib);
                    supplierRepository.update(updatedSupplier);
                    closeWindow();
                }
            } else {
                Supplier newSupplier = new Supplier(null, name, address, oib);
                supplierRepository.save(newSupplier);
                closeWindow();
            }
        } catch (ValidationException e) {
            log.warn("Neispravan unos prilikom spremanja dobavljača.", e);
            DialogUtils.showError("Neispravan unos", e.getMessage());
        } catch (RepositoryAccessException e) {
            handleRepositoryError(e);
        }
    }

    /**
     * Zatvara prozor bez spremanja
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }
    /**
     * Zatvara prozor
     */
    private void closeWindow() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    /**
     * Provjerava ispravnost unesenih podataka.
     * @throws ValidationException ako podaci nisu ispravni.
     */
    private void validateInput() throws ValidationException {
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
            throw new ValidationException(errorMessage.toString());
        }
    }

    /**
     * Centralizirano rukuje greškama iz repozitorija.
     */
    private void handleRepositoryError(RepositoryAccessException e) {
        log.error("Greška pri spremanju dobavljača.", e);
        if (e.getMessage() != null && e.getMessage().contains("23505")) {
            DialogUtils.showError("Greška pri spremanju", "Dobavljač s tim OIB-om već postoji!");
        } else {
            DialogUtils.showError("Greška pri spremanju", "Spremanje nije uspjelo. Provjerite log za detalje.");
        }
    }
}