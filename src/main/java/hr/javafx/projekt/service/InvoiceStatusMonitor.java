package hr.javafx.projekt.service;

import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.repository.InvoiceRepository;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import hr.javafx.projekt.controller.InvoiceController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pozadinski servis (Runnable) koji periodički provjerava i ažurira status dospjelih faktura.
 */
public class InvoiceStatusMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusMonitor.class);
    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final StatusBarState state;

    /**
     * Konstruktor za inicijalizaciju monitora sa stanjem statusne trake.
     * @param state Centralno stanje statusne trake.
     */
    public InvoiceStatusMonitor(StatusBarState state) {
        this.state = state;
    }

    /**
     * Provjere statusa faktura
     */
    @Override
    public void run() {
        log.info("Pokretanje provjere statusa faktura...");
        Platform.runLater(() -> state.setLastUpdateText("Provjera u tijeku..."));

        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            long overdueCount = invoices.stream()
                    .filter(inv -> inv.getStatus() == InvoiceStatus.UNPAID && inv.getDueDate().isBefore(LocalDate.now()))
                    .peek(this::updateInvoiceToOverdue)
                    .count();

            if (overdueCount > 0) {
                log.info("Ažurirano {} dospjelih faktura.", overdueCount);
            }


            InvoiceController.refreshActiveInstance();

        } catch (Exception e) {
            log.error("Greška prilikom automatskog ažuriranja statusa faktura.", e);
        }

        String statusText = "Zadnja provjera: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            state.setProgress(0.0);
            state.setLastUpdateText(statusText);
        });
        log.info("Završena provjera statusa faktura.");
    }


    /**
     * Pomoćna metoda za ažuriranje statusa pojedine fakture na 'OVERDUE'.
     * @param invoice Faktura za ažuriranje.
     */
    private void updateInvoiceToOverdue(Invoice invoice) {
        try {
            log.info("Faktura {} je dospjela. Mijenjam status u 'OVERDUE'.", invoice.getInvoiceNumber());
            Invoice updatedInvoice = new Invoice.Builder(
                    invoice.getId(), invoice.getInvoiceNumber(), invoice.getAmount(), invoice.getSupplier())
                    .withIssueDate(invoice.getIssueDate())
                    .withDueDate(invoice.getDueDate())
                    .withStatus(InvoiceStatus.OVERDUE)
                    .build();
            invoiceRepository.update(updatedInvoice);
        } catch (RepositoryAccessException e) {
            log.error("Neuspjelo ažuriranje statusa za fakturu ID: {}", invoice.getId(), e);
        }
    }
}