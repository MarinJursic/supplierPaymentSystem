package hr.javafx.projekt.model;

import hr.javafx.projekt.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Predstavlja fakturu u sustavu. Koristi Builder Pattern za kreiranje objekata.
 * Implementira sučelja {@link Payable} i {@link DueDatable}.
 */
public final class Invoice extends Entity implements Payable, DueDatable {
    private final String invoiceNumber;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final BigDecimal amount;
    private final InvoiceStatus status;
    private final Supplier supplier;

    /**
     * Privatni konstruktor koji se poziva iz Buildera.
     * @param builder Builder objekt s postavljenim podacima.
     */
    private Invoice(Builder builder) {
        super(builder.id);
        this.invoiceNumber = builder.invoiceNumber;
        this.issueDate = builder.issueDate;
        this.dueDate = builder.dueDate;
        this.amount = builder.amount;
        this.status = builder.status;
        this.supplier = builder.supplier;
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public BigDecimal getAmount() { return amount; }
    public InvoiceStatus getStatus() { return status; }
    public Supplier getSupplier() { return supplier; }

    /**
     * Vraća iznos za plaćanje fakture.
     * @return Iznos fakture.
     */
    @Override
    public BigDecimal getAmountPayable() {
        return this.amount;
    }

    /**
     * Implementacija metode iz {@link DueDatable} sučelja.
     * Izračunava preostale dane do dospijeća samo za neplaćene fakture.
     * @return Broj preostalih dana. Vraća negativan broj ako je rok prošao,
     *         a 0 za plaćene fakture.
     */
    @Override
    public long calculateRemainingDays() {
        if (this.status == InvoiceStatus.UNPAID) {
            return ChronoUnit.DAYS.between(LocalDate.now(), this.dueDate);
        }
        return 0;
    }

    /**
     * Ugniježđena statička klasa za implementaciju Builder Patterna.
     * Omogućuje postepeno i sigurno kreiranje {@link Invoice} objekata.
     */
    public static class Builder {
        private final Long id;
        private final String invoiceNumber;
        private final BigDecimal amount;
        private final Supplier supplier;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private InvoiceStatus status;

        /**
         * Konstruktor za Builder s obaveznim poljima.
         * @param id ID fakture (može biti null za novu fakturu).
         * @param invoiceNumber Broj fakture.
         * @param amount Iznos fakture.
         * @param supplier Dobavljač povezan s fakturom.
         */
        public Builder(Long id, String invoiceNumber, BigDecimal amount, Supplier supplier) {
            this.id = id;
            this.invoiceNumber = invoiceNumber;
            this.amount = amount;
            this.supplier = supplier;
        }

        /**
         * Postavlja datum izdavanja fakture.
         * @param date Datum izdavanja.
         * @return Referenca na trenutni Builder objekt.
         */
        public Builder withIssueDate(LocalDate date) {
            this.issueDate = date;
            return this;
        }

        /**
         * Postavlja datum dospijeća fakture.
         * @param date Datum dospijeća.
         * @return Referenca na trenutni Builder objekt.
         */
        public Builder withDueDate(LocalDate date) {
            this.dueDate = date;
            return this;
        }

        /**
         * Postavlja status fakture.
         * @param status Status fakture.
         * @return Referenca na trenutni Builder objekt.
         */
        public Builder withStatus(InvoiceStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Kreira i vraća finalni {@link Invoice} objekt.
         * @return Novi, nepromjenjivi objekt fakture.
         */
        public Invoice build() {
            return new Invoice(this);
        }
    }

    /**
     * Vraća string reprezentaciju fakture, pogodnu za logiranje i prikaz.
     * @return String s ključnim podacima o fakturi.
     */
    @Override
    public String toString() {
        return String.format("Invoice[invoiceNumber=%s, amount=%s, supplier=%s, status=%s]",
                invoiceNumber, amount, supplier.getName(), status.name());
    }
}