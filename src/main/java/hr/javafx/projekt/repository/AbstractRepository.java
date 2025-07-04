package hr.javafx.projekt.repository;

import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Apstraktna generička klasa koja definira standardne CRUD (Create, Read, Update, Delete)
 * operacije za rad s entitetima u bazi podataka.
 *
 * @param <T> Tip entiteta koji nasljeđuje {@link Entity}.
 */
public abstract class AbstractRepository<T extends Entity> {

    /**
     * Sprema novi entitet u bazu podataka.
     * @param entity Entitet za spremanje.
     * @return Spremljeni entitet, obično s dodijeljenim ID-em.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    public abstract T save(T entity) throws RepositoryAccessException;

    /**
     * Dohvaća sve entitete određenog tipa iz baze.
     * @return Lista svih entiteta.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    public abstract List<T> findAll() throws RepositoryAccessException;

    /**
     * Pronalazi entitet prema njegovom jedinstvenom ID-u.
     * @param id ID entiteta.
     * @return {@link Optional} koji sadrži entitet ako je pronađen, inače prazan.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    public abstract Optional<T> findById(Long id) throws RepositoryAccessException;

    /**
     * Ažurira postojeći entitet u bazi.
     * @param entity Entitet s ažuriranim podacima.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    public abstract void update(T entity) throws RepositoryAccessException;

    /**
     * Briše entitet iz baze podataka prema njegovom ID-u.
     * @param id ID entiteta za brisanje.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    public abstract void deleteById(Long id) throws RepositoryAccessException;
}