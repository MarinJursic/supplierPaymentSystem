package hr.javafx.projekt.service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Sadrži stanje statusne trake kao JavaFX svojstva (properties).
 * Omogućuje povezivanje (binding) UI elemenata na ove vrijednosti.
 */
public class StatusBarState {
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty lastUpdateText = new SimpleStringProperty("Inicijalizacija...");

    /**
     * Vraća trenutnu vrijednost napretka.
     */
    public double getProgress() {
        return progress.get();
    }

    /**
     * Vraća svojstvo (property) napretka za povezivanje.
     */
    public DoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Postavlja vrijednost napretka.
     */
    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    /**
     * Vraća trenutni tekst statusa.
     */
    public String getLastUpdateText() {
        return lastUpdateText.get();
    }

    /**
     * Vraća svojstvo (property) teksta statusa za povezivanje.
     */
    public StringProperty lastUpdateTextProperty() {
        return lastUpdateText;
    }

    /**
     * Postavlja tekst statusa.
     */
    public void setLastUpdateText(String lastUpdateText) {
        this.lastUpdateText.set(lastUpdateText);
    }
}