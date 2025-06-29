package hr.javafx.projekt.repository;

import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacija repozitorija za rad s fakturama u bazi podataka.
 */
public class InvoiceRepository extends AbstractRepository<Invoice> {

    private static final Logger log = LoggerFactory.getLogger(InvoiceRepository.class);
    private final AbstractRepository<Supplier> supplierRepository = new SupplierRepository();

    @Override
    public Invoice save(Invoice invoice) {
        String sql = "INSERT INTO INVOICE (invoice_number, issue_date, due_date, amount, status, supplier_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, invoice.getInvoiceNumber());
            stmt.setDate(2, Date.valueOf(invoice.getIssueDate()));
            stmt.setDate(3, Date.valueOf(invoice.getDueDate()));
            stmt.setBigDecimal(4, invoice.getAmount());
            stmt.setString(5, invoice.getStatus().name());
            stmt.setLong(6, invoice.getSupplier().getId());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    invoice.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom spremanja fakture!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return invoice;
    }

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
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom dohvaćanja svih faktura!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return invoices;
    }

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
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom dohvaćanja fakture po ID-u!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Invoice invoice) {
        String sql = "UPDATE INVOICE SET invoice_number = ?, issue_date = ?, due_date = ?, amount = ?, status = ?, supplier_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, invoice.getInvoiceNumber());
            stmt.setDate(2, Date.valueOf(invoice.getIssueDate()));
            stmt.setDate(3, Date.valueOf(invoice.getDueDate()));
            stmt.setBigDecimal(4, invoice.getAmount());
            stmt.setString(5, invoice.getStatus().name());
            stmt.setLong(6, invoice.getSupplier().getId());
            stmt.setLong(7, invoice.getId());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom ažuriranja fakture!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM INVOICE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom brisanja fakture!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }

    private Optional<Invoice> mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Optional<Supplier> supplierOptional = supplierRepository.findById(rs.getLong("supplier_id"));
        if (supplierOptional.isEmpty()) {
            log.warn("Dobavljač s ID-em {} nije pronađen za fakturu ID {}. Faktura se preskače.", rs.getLong("supplier_id"), rs.getLong("id"));
            return Optional.empty();
        }
        Supplier supplier = supplierOptional.get();

        InvoiceStatus status = InvoiceStatus.valueOf(rs.getString("status").toUpperCase());

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