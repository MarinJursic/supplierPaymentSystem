package hr.javafx.projekt.controller;

import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Kontroler za glavni (dashboard) ekran aplikacije.
 */
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private VBox adminCard;


    /**
     * Inicijalizira dashboard.
     * Postavlja poruke dobrodošlice i rolu prijavljenog korisnika.
     * Sakriva admin funkcionalnosti ako korisnik nije admin.
     */
    public void initialize() {
        if (SessionManager.getLoggedInUserId() != null) {
            String role = SessionManager.isAdmin() ? "Administrator" : "Korisnik";
            welcomeLabel.setText("Dobrodošli u sustav!");
            roleLabel.setText("Prijavljeni ste kao: " + role);
        }

        if (!SessionManager.isAdmin()) {
            adminCard.setVisible(false);
            adminCard.setManaged(false);
        }
    }

    /**
     * Prikazuje ekran za dobavljače. Poziva se klikom na karticu.
     */
    @FXML
    private void showSuppliers() {
        Navigation.showScene("suppliers.fxml", "Pregled Dobavljača");
    }

    /**
     * Prikazuje ekran za fakture. Poziva se klikom na karticu.
     */
    @FXML
    private void showInvoices() {
        Navigation.showScene("invoices.fxml", "Pregled Faktura");
    }

    /**
     * Prikazuje ekran za promjene. Poziva se klikom na karticu.
     */
    @FXML
    private void showChangeLog() {
        Navigation.showScene("changelog.fxml", "Pregled Promjena");
    }
}