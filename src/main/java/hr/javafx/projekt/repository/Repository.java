package hr.javafx.projekt.repository;

import hr.javafx.projekt.model.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Definira standardne operacije za rad s entitetima u bazi podataka.
 *
 * @param <T> Tip entiteta koji nasljeđuje BaseEntity.
 */
public interface Repository<T extends BaseEntity> {

    /**
     * Sprema novi entitet u bazu podataka.
     * @param entity Entitet za spremanje.
     * @return Spremljeni entitet, obično s dodijeljenim ID-em.
     */
    T save(T entity);

    /**
     * Dohvaća sve entitete određenog tipa iz baze.
     * @return Lista svih entiteta.
     */
    List<T> findAll();

    /**
     * Pronalazi entitet prema njegovom jedinstvenom ID-u.
     * @param id ID entiteta.
     * @return Optional koji sadrži entitet ako je pronađen, inače prazan.
     */
    Optional<T> findById(Long id);

    /**
     * Ažurira postojeći entitet u bazi.
     * @param entity Entitet s ažuriranim podacima.
     */
    void update(T entity);

    /**
     * Briše entitet iz baze podataka.
     * @param id ID entiteta za brisanje.
     */
    void deleteById(Long id);
}