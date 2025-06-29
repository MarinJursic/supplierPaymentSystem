package hr.javafx.projekt.repository;

import hr.javafx.projekt.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Definira standardne operacije za rad s entitetima u bazi podataka.
 *
 * @param <T> Tip entiteta koji nasljeđuje BaseEntity.
 */
public abstract class AbstractRepository<T extends Entity> {

    /**
     * Sprema novi entitet u bazu podataka.
     * @param entity Entitet za spremanje.
     * @return Spremljeni entitet, obično s dodijeljenim ID-em.
     */
    public abstract T save(T entity);

    /**
     * Dohvaća sve entitete određenog tipa iz baze.
     * @return Lista svih entiteta.
     */
    public abstract List<T> findAll();

    /**
     * Pronalazi entitet prema njegovom jedinstvenom ID-u.
     * @param id ID entiteta.
     * @return Optional koji sadrži entitet ako je pronađen, inače prazan.
     */
    public abstract Optional<T> findById(Long id);

    /**
     * Ažurira postojeći entitet u bazi.
     * @param entity Entitet s ažuriranim podacima.
     */
    public abstract void update(T entity);

    /**
     * Briše entitet iz baze podataka.
     * @param id ID entiteta za brisanje.
     */
    public abstract void deleteById(Long id);
}