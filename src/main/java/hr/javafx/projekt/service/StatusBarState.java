package hr.javafx.projekt.service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Sadrži stanje statusne trake (progress i tekst) kao JavaFX svojstva.
 * Omogućuje povezivanje (binding) UI elemenata na ove vrijednosti.
 */
public class StatusBarState {
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty lastUpdateText = new SimpleStringProperty("Inicijalizacija...");

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public String getLastUpdateText() {
        return lastUpdateText.get();
    }

    public StringProperty lastUpdateTextProperty() {
        return lastUpdateText;
    }

    public void setLastUpdateText(String lastUpdateText) {
        this.lastUpdateText.set(lastUpdateText);
    }
}