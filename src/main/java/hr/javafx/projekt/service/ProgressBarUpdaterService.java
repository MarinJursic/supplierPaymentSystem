package hr.javafx.projekt.service;

import javafx.application.Platform;

/**
 * Servis koji periodički ažurira ProgressBar svojstvo u StatusBarState.
 */
public class ProgressBarUpdaterService implements Runnable {

    private final StatusBarState state;
    private final double increment;

    public ProgressBarUpdaterService(StatusBarState state, int totalSeconds) {
        this.state = state;
        this.increment = 1.0 / totalSeconds;
    }

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