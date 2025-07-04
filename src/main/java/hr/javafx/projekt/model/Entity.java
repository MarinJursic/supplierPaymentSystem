package hr.javafx.projekt.model;

import java.io.Serializable;

/**
 * Zapečaćena (sealed) klasa koja služi kao osnova za sve entitete u sustavu.
 * Sadrži zajedničko svojstvo 'id' i dozvoljava nasljeđivanje samo specificiranim klasama.
 * Implementira Serializable sučelje kako bi se omogućila serijalizacija podklasa.
 */
public sealed class Entity implements Serializable permits Supplier, Invoice, User {
    private Long id;

    /**
     * Konstruktor za inicijalizaciju entiteta s ID-em.
     * @param id Jedinstveni identifikator entiteta.
     */
    public Entity(Long id) {
        this.id = id;
    }

    /**
     * Vraća ID entiteta.
     * @return Jedinstveni identifikator entiteta.
     */
    public Long getId() {
        return id;
    }

    /**
     * Postavlja ID entiteta.
     * @param id Jedinstveni identifikator entiteta.
     */
    public void setId(Long id) {
        this.id = id;
    }
}