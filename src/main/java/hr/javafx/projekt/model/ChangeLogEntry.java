package hr.javafx.projekt.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Predstavlja nepromjenjivi zapis o promjeni u sustavu.
 * Koristi se za serijalizaciju i praćenje promjena.
 *
 * @param description Opis promjene (npr. "ADD", "UPDATE").
 * @param oldValue Stara vrijednost podatka, ili "N/A" ako je novi unos.
 * @param newValue Nova vrijednost podatka.
 * @param timestamp Vrijeme kada se promjena dogodila.
 * @param userRole Rola korisnika koji je napravio promjenu.
 */
public record ChangeLogEntry(
        String description,
        String oldValue,
        String newValue,
        LocalDateTime timestamp,
        String userRole
) implements Serializable {}