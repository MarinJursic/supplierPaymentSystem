package hr.javafx.projekt.service;

import javafx.application.Platform;

/**
 * Servis (Runnable) koji periodički ažurira svojstvo napretka (progress) u StatusBarState.
 * Koristi se za vizualni prikaz odbrojavanja do sljedeće provjere.
 */
public class ProgressBarUpdaterService implements Runnable {

    private final StatusBarState state;
    private final double increment;

    /**
     * Konstruktor servisa.
     * @param state Centralno stanje statusne trake.
     * @param totalSeconds Ukupno vrijeme u sekundama za koje ProgressBar treba doći do 100%.
     */
    public ProgressBarUpdaterService(StatusBarState state, int totalSeconds) {
        this.state = state;
        this.increment = 1.0 / totalSeconds;
    }

    /**
     * Izvršava se periodički i ažurira vrijednost ProgressBar-a na glavnoj
     * JavaFX niti koristeći Platform.runLater().
     */
    @Override
    public void run() {
        Platform.runLater(() -> {
            double newProgress = state.getProgress() + increment;
            if (newProgress > 1.0) {
                newProgress = 1.0;
            }
            state.setProgress(newProgress);
        });
    }
}