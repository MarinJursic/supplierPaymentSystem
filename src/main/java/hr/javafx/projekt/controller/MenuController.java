package hr.javafx.projekt.controller;

import hr.javafx.projekt.session.SessionManager;
import hr.javafx.projekt.utils.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;

/**
 * Kontroler za glavni izbornik aplikacije (MenuBar).
 */
public class MenuController {

    @FXML
    private Menu adminMenu;

    /**
     * Inicijalizira izbornik.
     * Postavlja vidljivost administrativnog izbornika ovisno o roli prijavljenog korisnika.
     */
    public void initialize() {
        if (adminMenu != null) {
            adminMenu.setVisible(SessionManager.isAdmin());
        }
    }

    /**
     * Odjavljuje trenutnog korisnika i prikazuje ekran za prijavu.
     */
    private void logout() {
        SessionManager.logout();
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    /**
     * Prikazuje ekran za pregled faktura.
     */
    private void showInvoices() {
        Navigation.showScene("invoices.fxml", "Pregled Faktura");
    }

    /**
     * Prikazuje ekran za pregled dobavljača.
     */
    private void showSuppliers() {
        Navigation.showScene("suppliers.fxml", "Pregled Dobavljača");
    }

    /**
     * Prikazuje ekran za pregled zapisa o promjenama.
     */
    private void showChangeLog() {
        Navigation.showScene("changelog.fxml", "Pregled Promjena");
    }
}