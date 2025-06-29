package hr.javafx.projekt.repository;

import hr.javafx.projekt.model.ChangeLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij za upravljanje zapisima o promjenama (ChangeLog) korištenjem serijalizacije.
 */
public class ChangeLogRepository {

    private static final String FILE_NAME = "dat/changelog.dat";
    private static final Logger log = LoggerFactory.getLogger(ChangeLogRepository.class);

    /**
     * Sprema zapis o promjeni u binarnu datoteku.
     * Metoda je sinkronizirana kako bi se osigurala cjelovitost podataka pri višenitnom pristupu.
     * @param entry Zapis o promjeni koji treba spremiti.
     */
    public synchronized void saveChange(ChangeLogEntry entry) {
        List<ChangeLogEntry> changes = readChanges();
        changes.add(entry);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(changes);
        } catch (IOException e) {
            log.error("Greška prilikom serijalizacije zapisa o promjeni.", e);
        }
    }

    /**
     * Čita sve zapise o promjenama iz binarne datoteke.
     * Metoda je sinkronizirana kako bi se osigurala cjelovitost podataka pri višenitnom pristupu.
     * @return Lista svih zapisa o promjenama.
     */
    public synchronized List<ChangeLogEntry> readChanges() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ChangeLogEntry>) ois.readObject();
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Greška prilikom deserijalizacije zapisa o promjenama.", e);
            return new ArrayList<>();
        }
    }
}