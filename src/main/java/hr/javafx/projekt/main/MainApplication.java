package hr.javafx.projekt.main;

import hr.javafx.projekt.service.InvoiceStatusMonitor;
import hr.javafx.projekt.service.ProgressBarUpdaterService;
import hr.javafx.projekt.service.StatusBarState;
import hr.javafx.projekt.utils.Navigation;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Glavna klasa aplikacije koja nasljeđuje JavaFX Application.
 * Odgovorna je za pokretanje aplikacije, inicijalizaciju glavnog prozora i
 * pokretanje i gašenje pozadinskih servisa.
 */
public class MainApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);
    private static final int UPDATE_INTERVAL_SECONDS = 10;
    private static final StatusBarState statusBarState = new StatusBarState();

    private static ScheduledExecutorService backgroundScheduler;

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
     * Metoda koju JavaFX poziva kada se aplikacija zatvara.
     * Ovdje sigurno gasimo sve pokrenute pozadinske servise.
     */
    @Override
    public void stop() {
        log.info("Aplikacija se zatvara. Gasim pozadinski servis.");
        shutdownExecutor(backgroundScheduler);
    }

    /**
     * Pokreće pozadinske servise za praćenje statusa faktura i ažuriranje progress bara.
     * Koristi jedan single-thread scheduler za oba zadatka.
     * Servis se pokreće samo ako već nije aktivan.
     */
    public static void startBackgroundServices() {
        if (backgroundScheduler == null || backgroundScheduler.isShutdown()) {
            backgroundScheduler = Executors.newSingleThreadScheduledExecutor();

            backgroundScheduler.scheduleAtFixedRate(
                    new InvoiceStatusMonitor(statusBarState),
                    0,
                    UPDATE_INTERVAL_SECONDS,
                    TimeUnit.SECONDS
            );

            backgroundScheduler.scheduleAtFixedRate(
                    new ProgressBarUpdaterService(statusBarState, UPDATE_INTERVAL_SECONDS),
                    0,
                    1,
                    TimeUnit.SECONDS
            );
        }
    }

    /**
     * Pomoćna metoda za sigurno gašenje ExecutorService-a.
     * @param scheduler Servis koji treba ugasiti.
     */
    private void shutdownExecutor(ScheduledExecutorService scheduler) {
        if (scheduler != null && !scheduler.isShutdown()) {
            try {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Servis se nije ugasio u 5 sekundi, pokušavam prisilno...");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Prekinut proces čekanja na gašenje servisa.", e);
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
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