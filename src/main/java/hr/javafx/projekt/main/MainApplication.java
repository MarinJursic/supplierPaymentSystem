package hr.javafx.projekt.main;

import hr.javafx.projekt.service.InvoiceStatusMonitor;
import hr.javafx.projekt.service.ProgressBarUpdaterService;
import hr.javafx.projekt.service.StatusBarState; // NOVI IMPORT
import hr.javafx.projekt.utils.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Glavna klasa za pokretanje aplikacije.
 */
public class MainApplication extends Application {

    private static final int UPDATE_INTERVAL_SECONDS = 10;

    private static ScheduledExecutorService invoiceScheduler;
    private static ScheduledExecutorService progressScheduler;
    private static final StatusBarState statusBarState = new StatusBarState(); // JEDNA INSTANCA

    /**
     * Vraća centralnu instancu stanja statusne trake.
     * @return Instanca StatusBarState.
     */
    public static StatusBarState getStatusBarState() {
        return statusBarState;
    }

    @Override
    public void start(Stage stage) {
        Navigation.setPrimaryStage(stage);
        startBackgroundServices(); // Pokreni servise odmah pri startu
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    private static void startBackgroundServices() {
        if (invoiceScheduler == null || invoiceScheduler.isShutdown()) {
            invoiceScheduler = Executors.newSingleThreadScheduledExecutor();
            invoiceScheduler.scheduleAtFixedRate(new InvoiceStatusMonitor(statusBarState), 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);

            progressScheduler = Executors.newSingleThreadScheduledExecutor();
            progressScheduler.scheduleAtFixedRate(new ProgressBarUpdaterService(statusBarState, UPDATE_INTERVAL_SECONDS), 0, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void stop() {
        shutdownSchedulers();
    }

    public static void shutdownSchedulers() {
        if (invoiceScheduler != null && !invoiceScheduler.isShutdown()) {
            invoiceScheduler.shutdownNow();
        }
        if (progressScheduler != null && !progressScheduler.isShutdown()) {
            progressScheduler.shutdownNow();
        }
        // Resetiraj schedulere da se mogu ponovno pokrenuti ako se korisnik odjavi pa prijavi
        invoiceScheduler = null;
        progressScheduler = null;
    }

    public static void main(String[] args) {
        launch();
    }
}