package hr.javafx.projekt.controller;

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

    /**
     * Inicijalizira izbornik i postavlja vidljivost administratorskih opcija.
     */
    public void initialize() {
        if (adminMenu != null) {
            adminMenu.setVisible(SessionManager.isAdmin());
        }
    }

    /**
     * Odjavljuje korisnika i prikazuje ekran za prijavu.
     */
    @FXML
    private void logout() {
        SessionManager.logout();
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    /**
     * Prikazuje ekran za pregled faktura.
     */

    @FXML
    private void showInvoices() {
        Navigation.showScene("invoices.fxml", "Pregled Faktura");
    }

    /**
     * Prikazuje ekran za pregled dobavljača.
     */
    @FXML
    private void showSuppliers() {
        Navigation.showScene("suppliers.fxml", "Pregled Dobavljača");
    }

}