package hr.javafx.projekt.service;

import hr.javafx.projekt.controller.InvoiceController;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.InvoiceStatus;
import hr.javafx.projekt.repository.InvoiceRepository;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pozadinski servis koji periodički provjerava i ažurira status dospjelih faktura.
 */
public class InvoiceStatusMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusMonitor.class);
    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final StatusBarState state;

    /**
     * Konstruktor koji prima referencu na centralno stanje statusne trake.
     * @param state Instanca StatusBarState za ažuriranje.
     */
    public InvoiceStatusMonitor(StatusBarState state) {
        this.state = state;
    }

    @Override
    public void run() {
        log.info("Pokretanje provjere statusa faktura...");
        Platform.runLater(() -> state.setLastUpdateText("Provjera u tijeku..."));

        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            long overdueCount = invoices.stream()
                    .filter(inv -> inv.getStatus() instanceof InvoiceStatus.Unpaid && inv.getDueDate().isBefore(LocalDate.now()))
                    .peek(invoice -> {
                        log.info("Faktura {} je dospjela. Mijenjam status u 'Overdue'.", invoice.getInvoiceNumber());
                        Invoice updatedInvoice = new Invoice.Builder(
                                invoice.getId(), invoice.getInvoiceNumber(), invoice.getAmount(), invoice.getSupplier())
                                .withIssueDate(invoice.getIssueDate())
                                .withDueDate(invoice.getDueDate())
                                .withStatus(new InvoiceStatus.Overdue())
                                .build();
                        invoiceRepository.update(updatedInvoice);
                    })
                    .count();

            if (overdueCount > 0) {
                log.info("Ažurirano {} dospjelih faktura.", overdueCount);
            }

            // Nakon što je provjera gotova, osvježi prikaz ako je otvoren
            InvoiceController.refreshActiveInstance();

        } catch (Exception e) {
            log.error("Greška prilikom automatskog ažuriranja statusa faktura.", e);
        }

        String statusText = "Zadnja provjera: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            state.setProgress(0.0); // Resetiraj progress
            state.setLastUpdateText(statusText);
        });
        log.info("Završena provjera statusa faktura.");
    }
}