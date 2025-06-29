package hr.javafx.projekt.repository;

/**
 * Generička klasa koja pohranjuje par vrijednosti.
 * @param <K> Tip prvog elementa (ključ).
 * @param <V> Tip drugog elementa (vrijednost).
 */
public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}