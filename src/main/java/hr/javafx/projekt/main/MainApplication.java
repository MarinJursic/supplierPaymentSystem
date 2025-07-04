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

/**
 * Glavna klasa aplikacije koja nasljeđuje JavaFX Application.
 * Odgovorna je za pokretanje aplikacije, inicijalizaciju glavnog prozora i
 * pokretanje pozadinskih servisa.
 */
public class MainApplication extends Application {

    private static final int UPDATE_INTERVAL_SECONDS = 10;
    private static final StatusBarState statusBarState = new StatusBarState();

    private static ScheduledExecutorService invoiceScheduler;
    private static ScheduledExecutorService progressScheduler;

    /**
     * Vraća jedinstvenu, statičku instancu stanja statusne trake.
     * @return Instanca StatusBarState.
     */
    public static StatusBarState getStatusBarState() {
        return statusBarState;
    }

    /**
     * Glavna ulazna točka za JavaFX aplikaciju.
     * Postavlja primarni Stage, pokreće pozadinske servise i prikazuje početni ekran za prijavu.
     * @param stage Glavni prozor (Stage) aplikacije.
     */
    @Override
    public void start(Stage stage) {
        Navigation.setPrimaryStage(stage);
        startBackgroundServices();
        Navigation.showScene("login.fxml", "Supplier Payment System - Login");
    }

    /**
     * Pokreće pozadinske servise za praćenje statusa faktura i ažuriranje progress bara.
     * Servisi se pokreću samo ako već nisu aktivni.
     */
    public static void startBackgroundServices() {
        if (invoiceScheduler == null || invoiceScheduler.isShutdown()) {
            invoiceScheduler = Executors.newSingleThreadScheduledExecutor();
            invoiceScheduler.scheduleAtFixedRate(new InvoiceStatusMonitor(statusBarState), 4, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);

            progressScheduler = Executors.newSingleThreadScheduledExecutor();
            progressScheduler.scheduleAtFixedRate(new ProgressBarUpdaterService(statusBarState, UPDATE_INTERVAL_SECONDS), 0, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * Glavna metoda koja pokreće JavaFX aplikaciju.
     * @param args Argumenti komandne linije (ne koriste se).
     */
    public static void main(String[] args) {
        launch();
    }
}