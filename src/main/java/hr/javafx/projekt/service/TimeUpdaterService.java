package hr.javafx.projekt.service;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servis koji periodički ažurira labelu s trenutnim vremenom.
 */
public class TimeUpdaterService implements Runnable {

    private final Label timeLabel;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TimeUpdaterService(Label timeLabel) {
        this.timeLabel = timeLabel;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            if (timeLabel != null) {
                String currentText = timeLabel.getText().split("\\(")[0];
                timeLabel.setText(currentText + " (Vrijeme: " + LocalDateTime.now().format(TIME_FORMATTER) + ")");
            }
        });
    }
}