package hr.javafx.projekt.controller;

import hr.javafx.projekt.main.MainApplication;
import hr.javafx.projekt.service.StatusBarState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/**
 * Kontroler za statusnu traku koja se prikazuje na dnu glavnih ekrana.
 */
public class StatusBarController {

    @FXML
    private ProgressBar statusProgressBar;
    @FXML
    private Label lastUpdateLabel;

    /**
     * Inicijalizira kontroler i povezuje UI elemente na centralno stanje.
     * Dohvaća statičku instancu StatusBarState iz MainApplication klase i
     * povezuje (bind) svojstva UI elemenata na svojstva centralnog stanja.
     */
    public void initialize() {
        StatusBarState state = MainApplication.getStatusBarState();

        if (state != null) {
            statusProgressBar.progressProperty().bind(state.progressProperty());
            lastUpdateLabel.textProperty().bind(state.lastUpdateTextProperty());
        }
    }
}