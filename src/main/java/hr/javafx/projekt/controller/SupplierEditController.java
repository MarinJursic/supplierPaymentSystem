package hr.javafx.projekt.controller;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.SupplierRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Kontroler za prozor za dodavanje i izmjenu dobavljača.
 */
public class SupplierEditController {

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

    @FXML
    private void handleSave() {
        if (!isInputValid()) {
            return;
        }

        // AŽURIRANJE - DODAJEMO POTVRDU
        if (supplierToEdit != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Potvrda izmjene");
            confirmation.setHeaderText("Jeste li sigurni da želite spremiti promjene za ovog dobavljača?");
            Optional<ButtonType> result = confirmation.showAndWait();

            // Nastavljamo samo ako je korisnik kliknuo "OK" (ili YES)
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    supplierToEdit.setName(nameField.getText());
                    supplierToEdit.setAddress(addressField.getText());
                    supplierToEdit.setOib(oibField.getText());
                    supplierRepository.update(supplierToEdit);

                    closeWindow(); // Uspjeh -> zatvori prozor za izmjenu
                } catch (RepositoryAccessException e) {
                    handleRepositoryError(e); // Greška -> prikaži poruku, prozor ostaje otvoren
                }
            }
        } else {
            // KREIRANJE NOVOG - ne treba potvrda
            try {
                Supplier newSupplier = new Supplier(null, nameField.getText(), addressField.getText(), oibField.getText());
                supplierRepository.save(newSupplier);
                closeWindow();
            } catch (RepositoryAccessException e) {
                handleRepositoryError(e);
            }
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

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
            showAlert(Alert.AlertType.ERROR, "Neispravan unos", errorMessage.toString());
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleRepositoryError(RepositoryAccessException e) {
        Throwable cause = e.getCause();
        if (cause instanceof java.sql.SQLException && ((java.sql.SQLException) cause).getSQLState().equals("23505")) {
            showAlert(Alert.AlertType.ERROR, "Greška pri spremanju", "Dobavljač s tim OIB-om već postoji!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Greška pri spremanju", "Spremanje nije uspjelo. Provjerite log za detalje.");
        }
    }
}