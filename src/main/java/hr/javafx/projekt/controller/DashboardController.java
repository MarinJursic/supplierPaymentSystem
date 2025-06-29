package hr.javafx.projekt.controller;

import hr.javafx.projekt.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Kontroler za glavni (dashboard) ekran aplikacije.
 */
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private MenuController menuController;

    /**
     * Inicijalizira dashboard.
     * Postavlja poruke dobrodošlice i rolu prijavljenog korisnika.
     * Također inicijalizira ugniježđeni menu kontroler.
     */
    public void initialize() {
        if (SessionManager.getLoggedInUserId() != null) {
            String role = SessionManager.isAdmin() ? "Administrator" : "Korisnik";
            welcomeLabel.setText("Dobrodošli u sustav!");
            roleLabel.setText("Prijavljeni ste kao: " + role);
        }

        if (menuController != null) {
            menuController.initialize();
        }
    }
}