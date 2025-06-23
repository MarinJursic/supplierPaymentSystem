package hr.javafx.projekt.controller;

import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.repository.SupplierRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
        populateFormFields(); // Pozivamo metodu za popunjavanje polja
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

        String name = nameField.getText();
        String address = addressField.getText();
        String oib = oibField.getText();

        if (supplierToEdit != null) {
            // AŽURIRANJE POSTOJEĆEG
            supplierToEdit.setName(name);
            supplierToEdit.setAddress(address);
            supplierToEdit.setOib(oib);
            supplierRepository.update(supplierToEdit);
        } else {
            // KREIRANJE NOVOG
            Supplier newSupplier = new Supplier(null, name, address, oib);
            supplierRepository.save(newSupplier);
        }

        closeWindow();
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Neispravan unos");
            alert.setHeaderText("Molimo ispravite greške:");
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }
}