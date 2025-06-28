package hr.javafx.projekt.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Predstavlja nepromjenjivi zapis o promjeni u sustavu.
 * Koristi se za serijalizaciju i praćenje promjena.
 *
 * @param changeType Tip promjene (npr. "ADD", "UPDATE", "DELETE").
 * @param entityName Naziv entiteta koji se mijenja.
 * @param fieldName Naziv polja koje se mijenja (ili "N/A" za ADD/DELETE).
 * @param oldValue Stara vrijednost podatka.
 * @param newValue Nova vrijednost podatka.
 * @param timestamp Vrijeme kada se promjena dogodila.
 * @param userRole Rola korisnika koji je napravio promjenu.
 */
public record ChangeLogEntry(
        String changeType,
        String entityName,
        String fieldName,
        String oldValue,
        String newValue,
        LocalDateTime timestamp,
        String userRole
) implements Serializable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

    @Override
    public String toString() {
        return String.format("[%s] %s: %s -> %s (Polje: %s, Entitet: %s, Korisnik: %s)",
                timestamp.format(FORMATTER),
                changeType,
                oldValue,
                newValue,
                fieldName,
                entityName,
                userRole);
    }
}