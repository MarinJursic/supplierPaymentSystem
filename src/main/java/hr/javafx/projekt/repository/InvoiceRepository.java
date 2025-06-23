package hr.javafx.projekt.repository;

import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.InvoiceStatus;
import hr.javafx.projekt.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacija repozitorija za rad s fakturama u bazi podataka.
 */
public class InvoiceRepository implements Repository<Invoice> {

    private static final Logger log = LoggerFactory.getLogger(InvoiceRepository.class);
    private final Repository<Supplier> supplierRepository = new SupplierRepository();

    /**
     * {@inheritDoc}
     */
    @Override
    public Invoice save(Invoice invoice) {
        String sql = "INSERT INTO INVOICE (invoice_number, issue_date, due_date, amount, status, supplier_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, invoice.getInvoiceNumber());
            stmt.setDate(2, Date.valueOf(invoice.getIssueDate()));
            stmt.setDate(3, Date.valueOf(invoice.getDueDate()));
            stmt.setBigDecimal(4, invoice.getAmount());
            stmt.setString(5, invoice.getStatus().getDescription());
            stmt.setLong(6, invoice.getSupplier().getId());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    invoice.setId(generatedKeys.getLong(1));
                }
            }
        } catch (Exception e) {
            log.error("Greška prilikom spremanja fakture!", e);

        }
        return invoice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Invoice> findAll() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM INVOICE";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mapResultSetToInvoice(rs).ifPresent(invoices::add);
            }
        } catch (Exception e) {
            log.error("Greška prilikom dohvaćanja svih faktura!", e);

        }
        return invoices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Invoice> findById(Long id) {
        String sql = "SELECT * FROM INVOICE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (Exception e) {
            log.error("Greška prilikom dohvaćanja fakture po ID-u!", e);

        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Invoice invoice) {
        String sql = "UPDATE INVOICE SET invoice_number = ?, issue_date = ?, due_date = ?, amount = ?, status = ?, supplier_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, invoice.getInvoiceNumber());
            stmt.setDate(2, Date.valueOf(invoice.getIssueDate()));
            stmt.setDate(3, Date.valueOf(invoice.getDueDate()));
            stmt.setBigDecimal(4, invoice.getAmount());
            stmt.setString(5, invoice.getStatus().getDescription());
            stmt.setLong(6, invoice.getSupplier().getId());
            stmt.setLong(7, invoice.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Greška prilikom ažuriranja fakture!", e);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM INVOICE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Greška prilikom brisanja fakture!", e);

        }
    }

    private Optional<Invoice> mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Optional<Supplier> supplierOptional = supplierRepository.findById(rs.getLong("supplier_id"));
        if (supplierOptional.isEmpty()) {
            log.warn("Dobavljač s ID-em {} nije pronađen za fakturu ID {}. Faktura se preskače.", rs.getLong("supplier_id"), rs.getLong("id"));
            return Optional.empty();
        }
        Supplier supplier = supplierOptional.get();

        InvoiceStatus status;
        String statusDesc = rs.getString("status");
        if ("Paid".equalsIgnoreCase(statusDesc)) {
            status = new InvoiceStatus.Paid();
        } else if ("Overdue".equalsIgnoreCase(statusDesc)) {
            status = new InvoiceStatus.Overdue();
        } else {
            status = new InvoiceStatus.Unpaid();
        }

        Invoice invoice = new Invoice.Builder(
                rs.getLong("id"),
                rs.getString("invoice_number"),
                rs.getBigDecimal("amount"),
                supplier)
                .withIssueDate(rs.getDate("issue_date").toLocalDate())
                .withDueDate(rs.getDate("due_date").toLocalDate())
                .withStatus(status)
                .build();

        return Optional.of(invoice);
    }
}