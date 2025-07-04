package hr.javafx.projekt.repository;

/**
 * Generička klasa koja pohranjuje par vrijednosti.
 */
public class Pair<K, V> {
    private final K key;
    private final V value;

    /**
     * Konstruktor za stvaranje novog para.
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Vraća prvi element (ključ).
     */
    public K getKey() {
        return key;
    }

    /**
     * Vraća drugi element (vrijednost).
     */
    public V getValue() {
        return value;
    }
}