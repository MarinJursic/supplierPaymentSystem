package hr.javafx.projekt.service;

import hr.javafx.projekt.controller.InvoiceController;
import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.repository.InvoiceRepository;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceStatusMonitor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusMonitor.class);
    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final StatusBarState state;

    public InvoiceStatusMonitor(StatusBarState state) {
        this.state = state;
    }

    @Override
    public void run() {
        Platform.runLater(() -> state.setLastUpdateText("Provjera u tijeku..."));

        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            long overdueCount = invoices.stream()
                    .filter(inv -> inv.getStatus() == InvoiceStatus.UNPAID && inv.getDueDate().isBefore(LocalDate.now()))
                    .peek(invoice -> {
                        log.info("Faktura {} je dospjela. Mijenjam status u 'Overdue'.", invoice.getInvoiceNumber());
                        Invoice updatedInvoice = new Invoice.Builder(
                                invoice.getId(), invoice.getInvoiceNumber(), invoice.getAmount(), invoice.getSupplier())
                                .withIssueDate(invoice.getIssueDate())
                                .withDueDate(invoice.getDueDate())
                                .withStatus(InvoiceStatus.OVERDUE)
                                .build();
                        invoiceRepository.update(updatedInvoice);
                    })
                    .count();

            if (overdueCount > 0) {
                log.info("Ažurirano {} dospjelih faktura.", overdueCount);
            }

        } catch (Exception e) {
            log.error("Greška prilikom automatskog ažuriranja statusa faktura.", e);
        }

        // Ažuriranje statusne trake na kraju
        String statusText = "Zadnja provjera: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            state.setProgress(0.0);
            state.setLastUpdateText(statusText);
        });
        log.info("Završena provjera statusa faktura.");
    }
}