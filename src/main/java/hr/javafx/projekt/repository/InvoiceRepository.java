package hr.javafx.projekt.repository;

import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.enums.InvoiceStatus;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Invoice;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.utils.ChangeLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Upravlja operacijama nad fakturama u bazi podataka.
 */
public class InvoiceRepository extends AbstractRepository<Invoice> {

    private static final Logger log = LoggerFactory.getLogger(InvoiceRepository.class);
    private final AbstractRepository<Supplier> supplierRepository = new SupplierRepository();

    /**
     * Sprema novu fakturu u bazu i bilježi promjenu.
     *
     * @param invoice Faktura za spremanje.
     * @return Spremljena faktura s dodijeljenim ID-em.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    @Override
    public Invoice save(Invoice invoice) throws RepositoryAccessException {
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
                    ChangeLogger.logAddition(invoice);
                }
            }
        } catch (SQLException | IOException e) {

            throw new RepositoryAccessException("Greška prilikom spremanja fakture!", e);
        }
        return invoice;
    }

    /**
     * Dohvaća sve fakture iz baze.
     *
     * @return Lista svih faktura.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    @Override
    public List<Invoice> findAll() throws RepositoryAccessException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT id, invoice_number, issue_date, due_date, amount, status, supplier_id FROM INVOICE";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                mapResultSetToInvoice(rs).ifPresent(invoices::add);
            }
        } catch (SQLException | IOException e) {

            throw new RepositoryAccessException("Greška prilikom dohvaćanja svih faktura!", e);
        }
        return invoices;
    }

    /**
     * Pronalazi fakturu prema ID-u.
     *
     * @param id ID fakture.
     * @return Optional koji sadrži fakturu ako je pronađena.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    @Override
    public Optional<Invoice> findById(Long id) throws RepositoryAccessException {
        String sql = "SELECT id, invoice_number, issue_date, due_date, amount, status, supplier_id FROM INVOICE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (SQLException | IOException e) {

            throw new RepositoryAccessException("Greška prilikom dohvaćanja fakture po ID-u!", e);
        }
        return Optional.empty();
    }

    /**
     * Ažurira postojeću fakturu u bazi i bilježi promjenu.
     *
     * @param invoice Faktura s ažuriranim podacima.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    @Override
    public void update(Invoice invoice) throws RepositoryAccessException {
        Optional<Invoice> oldInvoiceOptional = findById(invoice.getId());
        if (oldInvoiceOptional.isEmpty()) {
            throw new RepositoryAccessException("Pokušaj ažuriranja fakture koja ne postoji (ID: " + invoice.getId() + ").");
        }
        Invoice oldInvoice = oldInvoiceOptional.get();

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
            ChangeLogger.logUpdate(oldInvoice, invoice);
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException("Greška prilikom ažuriranja fakture!", e);
        }
    }

    /**
     * Briše fakturu iz baze podataka prema ID-u i bilježi promjenu.
     *
     * @param id ID fakture za brisanje.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
     */
    @Override
    public void deleteById(Long id) throws RepositoryAccessException {
        Optional<Invoice> oldInvoiceOptional = findById(id);
        if (oldInvoiceOptional.isEmpty()) {
            throw new RepositoryAccessException("Pokušaj brisanja fakture koja ne postoji (ID: " + id + ").");
        }
        Invoice oldInvoice = oldInvoiceOptional.get();

        String sql = "DELETE FROM INVOICE WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ChangeLogger.logDeletion(oldInvoice);
            }
        } catch (SQLException | IOException e) {
            throw new RepositoryAccessException("Greška prilikom brisanja fakture!", e);
        }
    }

    /**
     * Mapira result set u fakture
     * @param rs
     * @return
     * @throws SQLException
     */

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