package hr.javafx.projekt.repository;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.ChangeLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij za upravljanje zapisima o promjenama (ChangeLog) korištenjem serijalizacije.
 * Osigurava siguran pristup datoteci iz više niti koristeći 'synchronized' metode.
 */
public class ChangeLogRepository {

    private static final String FILE_NAME = "dat/changelog.dat";
    private static final Logger log = LoggerFactory.getLogger(ChangeLogRepository.class);

    /**
     * Sprema zapis o promjeni u binarnu datoteku.
     * Metoda je sinkronizirana kako bi se osigurala cjelovitost podataka pri višenitnom pristupu.
     *
     * @param entry Zapis o promjeni koji treba spremiti.
     * @throws RepositoryAccessException ako dođe do greške pri pisanju u datoteku.
     */
    public synchronized void saveChange(ChangeLogEntry entry) throws RepositoryAccessException {
        List<ChangeLogEntry> changes = readChanges();
        changes.add(entry);

        File file = new File(FILE_NAME);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                String message = "Nije moguće kreirati direktorij: " + parentDir.getAbsolutePath();
                log.error(message);
                throw new RepositoryAccessException(message);
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(changes);
        } catch (IOException e) {
            String message = "Greška prilikom serijalizacije zapisa o promjeni.";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }

    /**
     * Čita sve zapise o promjenama iz binarne datoteke.
     * Metoda je sinkronizirana kako bi se osigurala cjelovitost podataka pri višenitnom pristupu.
     *
     * @return Lista svih zapisa o promjenama. Vraća praznu listu ako datoteka ne postoji ili je prazna.
     * @throws RepositoryAccessException ako dođe do greške pri čitanju datoteke ili ako je sadržaj oštećen.
     */
    public synchronized List<ChangeLogEntry> readChanges() throws RepositoryAccessException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object readObject = ois.readObject();
            if (readObject instanceof List<?>) {
                List<?> rawList = (List<?>) readObject;
                List<ChangeLogEntry> changeLogEntries = new ArrayList<>();
                for (Object item : rawList) {
                    if (item instanceof ChangeLogEntry) {
                        changeLogEntries.add((ChangeLogEntry) item);
                    } else {
                        throw new RepositoryAccessException("Datoteka s logovima sadrži neispravan tip objekta.");
                    }
                }
                return changeLogEntries;
            } else {
                throw new RepositoryAccessException("Datoteka s logovima je oštećena: ne sadrži listu.");
            }
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            String message = "Greška prilikom deserijalizacije zapisa o promjenama.";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }
}