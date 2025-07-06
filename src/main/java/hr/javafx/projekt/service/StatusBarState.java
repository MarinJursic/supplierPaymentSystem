package hr.javafx.projekt.service;

import javafx.application.Platform;
import javafx.beans.property.*;

/**
 * Sadrži stanje statusne trake kao JavaFX svojstva (properties).
 * Omogućuje povezivanje (binding) UI elemenata na ove vrijednosti.
 */
public class StatusBarState {
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty lastUpdateText = new SimpleStringProperty("Inicijalizacija...");

    private final LongProperty refreshSignal = new SimpleLongProperty(0L);

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

    /**
     * Vraća read-only svojstvo signala za osvježavanje na koje se UI može pretplatiti.
     * @return Read-only svojstvo signala.
     */
    public ReadOnlyLongProperty refreshSignalProperty() {
        return refreshSignal;
    }

    /**
     * Okidač koji pozivaju pozadinski servisi kako bi signalizirali potrebu za osvježavanjem.
     * Postavlja novu vrijednost na signal property, što pokreće sve listenere.
     * Koristi se Platform.runLater kako bi se osiguralo da se promjena dogodi na JavaFX niti.
     */
    public void triggerRefresh() {
        Platform.runLater(() -> refreshSignal.set(System.currentTimeMillis()));
    }
}