package hr.javafx.projekt.model;

/**
 * Zapečaćeno sučelje koje predstavlja statuse fakture.
 */
public sealed interface InvoiceStatus {
    String getDescription();

    /**
     * Predstavlja plaćeni status.
     */
    record Paid() implements InvoiceStatus {
        @Override
        public String getDescription() { return "Paid"; }
    }

    /**
     * Predstavlja neplaćeni status.
     */
    record Unpaid() implements InvoiceStatus {
        @Override
        public String getDescription() { return "Unpaid"; }
    }

    /**
     * Predstavlja status kašnjenja.
     */
    record Overdue() implements InvoiceStatus {
        @Override
        public String getDescription() { return "Overdue"; }
    }
}