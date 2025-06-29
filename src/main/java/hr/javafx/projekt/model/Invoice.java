package hr.javafx.projekt.model;

import hr.javafx.projekt.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Predstavlja fakturu. Koristi Builder Pattern za kreiranje objekata.
 * Implementira Payable i zapečaćeno sučelje DueDatable.
 */
public final class Invoice extends Entity implements Payable, DueDatable {
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal amount;
    private InvoiceStatus status;
    private Supplier supplier;

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

    @Override
    public BigDecimal getAmountPayable() {
        return this.amount;
    }

    /**
     * Implementacija metode iz DueDatable sučelja.
     * Izračunava preostale dane do dospijeća samo za neplaćene fakture.
     * @return Broj preostalih dana, ili 0 za plaćene/dospjele.
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
     */
    public static class Builder {
        private Long id;
        private final String invoiceNumber;
        private LocalDate issueDate;
        private LocalDate dueDate;
        private final BigDecimal amount;
        private InvoiceStatus status;
        private final Supplier supplier;

        public Builder(Long id, String invoiceNumber, BigDecimal amount, Supplier supplier) {
            this.id = id;
            this.invoiceNumber = invoiceNumber;
            this.amount = amount;
            this.supplier = supplier;
        }

        public Builder withIssueDate(LocalDate date) {
            this.issueDate = date;
            return this;
        }

        public Builder withDueDate(LocalDate date) {
            this.dueDate = date;
            return this;
        }

        public Builder withStatus(InvoiceStatus status) {
            this.status = status;
            return this;
        }

        public Invoice build() {
            return new Invoice(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Invoice[invoiceNumber=%s, amount=%s, supplier=%s, status=%s]",
                invoiceNumber, amount, supplier.getName(), status.name());
    }
}