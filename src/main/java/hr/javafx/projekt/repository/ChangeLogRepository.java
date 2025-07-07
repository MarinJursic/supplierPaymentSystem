package hr.javafx.projekt.repository;

import hr.javafx.projekt.model.ChangeLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Upravlja zapisima o promjenama (ChangeLog) koristeći serijalizaciju i osigurava siguran pristup datoteci iz više niti.
 */
public class ChangeLogRepository {

    private static final String FILE_NAME = "dat/changelog.dat";
    private static final Logger log = LoggerFactory.getLogger(ChangeLogRepository.class);
    private static boolean isFileLocked = false;

    /**
     * Sinkronizirano sprema zapis o promjeni u datoteku.
     *
     * @param entry Zapis o promjeni za spremanje.
     */
    public synchronized void saveChange(ChangeLogEntry entry) {
        while (isFileLocked) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Nit prekinuta dok ceka za pristup datoteci.", e);
                Thread.currentThread().interrupt();
            }
        }

        isFileLocked = true;
        try {
            List<ChangeLogEntry> changes = readChangesInternal();
            changes.add(entry);

            File file = new File(FILE_NAME);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                log.error("Nije moguće kreirati direktorij: {}", parentDir.getAbsolutePath());
                return;
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(changes);
            } catch (IOException e) {
                log.error("Greška prilikom serijalizacije zapisa o promjeni.", e);
            }
        } finally {
            isFileLocked = false;
            notifyAll();
        }
    }

    /**
     * Sinkronizirano čita sve zapise o promjenama iz datoteke.
     *
     * @return Lista svih zapisa o promjenama.
     */
    public synchronized List<ChangeLogEntry> readChanges() {
        while (isFileLocked) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Nit prekinuta dok ceka za pristup datoteci.", e);
                Thread.currentThread().interrupt();
            }
        }

        isFileLocked = true;
        try {
            return readChangesInternal();
        } finally {
            isFileLocked = false;
            notifyAll();
        }
    }

    /**
     * Cita promjene
     */
    private List<ChangeLogEntry> readChangesInternal() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object readObject = ois.readObject();
            if (readObject instanceof List<?> rawList) {
                return processLogList(rawList);
            } else {
                log.error("Datoteka s logovima je oštećena: ne sadrži listu. Sadrži objekt tipa: {}",
                        (readObject != null) ? readObject.getClass().getName() : "null");
                return new ArrayList<>();
            }
        } catch (EOFException e) {
            log.info("Pronađena prazna datoteka s logovima, što je očekivano pri prvom pokretanju.");
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Greška prilikom deserijalizacije zapisa o promjenama.", e);
            return new ArrayList<>();
        }
    }

    /**
     * Procesira logove
     * @param rawList
     * @return Promjene iz datoteke
     */
    private List<ChangeLogEntry> processLogList(List<?> rawList) {
        List<ChangeLogEntry> changeLogEntries = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof ChangeLogEntry changeLogEntry) {
                changeLogEntries.add(changeLogEntry);
            } else {
                log.warn("Pronađen neočekivani tip objekta u datoteci s logovima: {}",
                        (item != null) ? item.getClass().getName() : "null");
            }
        }
        return changeLogEntries;
    }
}