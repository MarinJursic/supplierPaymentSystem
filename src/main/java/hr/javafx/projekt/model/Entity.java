package hr.javafx.projekt.model;

/**
 * Zapečaćena (sealed) klasa koja služi kao osnova za sve entitete.
 * Sadrži zajedničko svojstvo 'id' i implementira Serializable.
 */
public sealed class Entity permits Supplier, Invoice, User {
    private Long id;

    public Entity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}