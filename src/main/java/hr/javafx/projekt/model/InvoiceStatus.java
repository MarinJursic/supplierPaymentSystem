package hr.javafx.projekt.model;

import java.io.Serializable;

/**
 * Zapečaćeno sučelje koje predstavlja statuse fakture.
 * Svaka implementacija je record, čime osiguravamo nepromjenjivost.
 */
public sealed interface InvoiceStatus extends Serializable {
    String getDescription();

    @Override
    String toString();

    /**
     * Predstavlja plaćeni status.
     */
    record Paid() implements InvoiceStatus {
        @Override
        public String getDescription() { return "Paid"; }

        @Override
        public String toString() { return getDescription(); }
    }

    /**
     * Predstavlja neplaćeni status.
     */
    record Unpaid() implements InvoiceStatus {
        @Override
        public String getDescription() { return "Unpaid"; }

        @Override
        public String toString() { return getDescription(); }
    }

    /**
     * Predstavlja status kašnjenja.
     */
    record Overdue() implements InvoiceStatus {
        @Override
        public String getDescription() { return "Overdue"; }

        @Override
        public String toString() { return getDescription(); }
    }
}