package hr.javafx.projekt.model;

import java.io.Serializable;

/**
 * Zapečaćena (sealed) klasa koja služi kao osnova za sve entitete.
 * Sadrži zajedničko svojstvo 'id' i implementira Serializable.
 */
public sealed class BaseEntity implements Serializable permits Supplier, Invoice, User {
    private Long id;

    public BaseEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}