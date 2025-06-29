package hr.javafx.projekt.model;

/**
 * Zapečaćeno sučelje koje definira ugovor za entitete koji imaju rok dospijeća.
 * Ograničava implementaciju isključivo na klasu Invoice.
 */
public sealed interface DueDatable permits Invoice {

    /**
     * Izračunava broj preostalih dana do dospijeća.
     * Logika izračuna ovisi o klasi koja implementira sučelje.
     *
     * @return Broj preostalih dana do dospijeća.
     */
    long calculateRemainingDays();
}