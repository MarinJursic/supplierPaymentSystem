package hr.javafx.projekt.main;

import hr.javafx.projekt.service.InvoiceStatusMonitor;
import hr.javafx.projekt.service.ProgressBarUpdaterService;
import hr.javafx.projekt.service.StatusBarState;
import hr.javafx.projekt.utils.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application {

    private static final int UPDATE_INTERVAL_SECONDS = 10;
    // JEDNA JEDINA, STATIČKA INSTANCA STANJA ZA CIJELU APLIKACIJU
    private static final StatusBarState statusBarState = new StatusBarState();

    private static ScheduledExecutorService invoiceScheduler;
    private static ScheduledExecutorService progressScheduler;

    // Metoda za dohvat te jedinstvene instance
    public static StatusBarState getStatusBarState() {
        return statusBarState;
    }

    @Override
    public void start(Stage stage) {
        Navigation.setPrimaryStage(stage);
        startBackgroundServices(); // Pokrećemo servise samo jednom
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    public static void startBackgroundServices() {
        if (invoiceScheduler == null || invoiceScheduler.isShutdown()) {
            invoiceScheduler = Executors.newSingleThreadScheduledExecutor();
            invoiceScheduler.scheduleAtFixedRate(new InvoiceStatusMonitor(statusBarState), 5, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);

            progressScheduler = Executors.newSingleThreadScheduledExecutor();
            progressScheduler.scheduleAtFixedRate(new ProgressBarUpdaterService(statusBarState, UPDATE_INTERVAL_SECONDS), 0, 1, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}