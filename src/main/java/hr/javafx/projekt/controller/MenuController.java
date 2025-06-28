package hr.javafx.projekt.controller;

import hr.javafx.projekt.main.MainApplication; // NOVI IMPORT
import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;

/**
 * Kontroler za glavni izbornik aplikacije.
 */
public class MenuController {

    @FXML
    private Menu adminMenu;

    public void initialize() {
        if (adminMenu != null) {
            adminMenu.setVisible(SessionManager.isAdmin());
        }
    }

    /**
     * Odjavljuje korisnika, zaustavlja pozadinske servise i prikazuje ekran za prijavu.
     */
    @FXML
    private void logout() {
        SessionManager.logout();
        MainApplication.shutdownSchedulers(); // Zaustavi servise
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    @FXML
    private void showInvoices() {
        Navigation.showScene("invoices.fxml", "Pregled Faktura");
    }

    @FXML
    private void showSuppliers() {
        Navigation.showScene("suppliers.fxml", "Pregled Dobavljača");
    }

    @FXML
    private void showChangeLog() {
        Navigation.showScene("changelog.fxml", "Pregled Promjena");
    }
}