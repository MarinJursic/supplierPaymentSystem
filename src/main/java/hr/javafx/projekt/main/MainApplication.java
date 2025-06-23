package hr.javafx.projekt.main;

import hr.javafx.projekt.utils.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Glavna klasa za pokretanje aplikacije.
 */
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) {
        Navigation.setPrimaryStage(stage);
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    public static void main(String[] args) {
        launch();
    }
}