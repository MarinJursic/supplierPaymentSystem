package hr.javafx.projekt.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * Generička klasa za pohranu i upravljanje listom podataka određenog tipa.
 * @param <T> Tip podataka koji se pohranjuju u listu.
 */
public class DataStore<T> {
    private final List<T> items;

    /**
     * Konstruktor koji inicijalizira praznu listu.
     */
    public DataStore() {
        this.items = new ArrayList<>();
    }

    /**
     * Dodaje element u listu.
     * @param item Element koji se dodaje.
     */
    public void add(T item) {
        items.add(item);
    }

    /**
     * Vraća sve elemente iz liste.
     * @return Lista elemenata.
     */
    public List<T> getAll() {
        return new ArrayList<>(items);
    }
}