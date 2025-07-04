package hr.javafx.projekt.repository;

import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.exception.RepositoryAccessException;
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
 * Upravlja operacijama nad dobavljačima u bazi podataka.
 */
public class SupplierRepository extends AbstractRepository<Supplier> {

    private static final Logger log = LoggerFactory.getLogger(SupplierRepository.class);

    /**
     * Sprema novog dobavljača u bazu i bilježi promjenu.
     */
    @Override
    public Supplier save(Supplier supplier) throws RepositoryAccessException {
        String sql = "INSERT INTO SUPPLIER (name, address, oib) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getAddress());
            stmt.setString(3, supplier.getOib());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    supplier.setId(generatedKeys.getLong(1));
                    ChangeLogger.logAddition(supplier);
                }
            }
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom spremanja dobavljača!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return supplier;
    }

    /**
     * Dohvaća sve dobavljače iz baze.
     */
    @Override
    public List<Supplier> findAll() throws RepositoryAccessException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id, name, address, oib FROM SUPPLIER";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("oib")
                ));
            }
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom dohvaćanja svih dobavljača!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return suppliers;
    }

    /**
     * Pronalazi dobavljača prema ID-u.
     */
    @Override
    public Optional<Supplier> findById(Long id) throws RepositoryAccessException {
        String sql = "SELECT id, name, address, oib FROM SUPPLIER WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Supplier(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getString("oib")
                    ));
                }
            }
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom dohvaćanja dobavljača po ID-u!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return Optional.empty();
    }

    /**
     * Ažurira postojećeg dobavljača u bazi i bilježi promjenu.
     */
    @Override
    public void update(Supplier supplier) throws RepositoryAccessException {
        Optional<Supplier> oldSupplierOptional = findById(supplier.getId());
        if (oldSupplierOptional.isEmpty()) {
            throw new RepositoryAccessException("Pokušaj ažuriranja dobavljača koji ne postoji.");
        }
        Supplier oldSupplier = oldSupplierOptional.get();

        String sql = "UPDATE SUPPLIER SET name = ?, address = ?, oib = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getAddress());
            stmt.setString(3, supplier.getOib());
            stmt.setLong(4, supplier.getId());
            stmt.executeUpdate();
            ChangeLogger.logUpdate(oldSupplier, supplier);
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom ažuriranja dobavljača!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }

    /**
     * Provjerava je li dobavljač povezan s bilo kojom fakturom.
     */
    public boolean isSupplierLinkedToInvoices(Long supplierId) throws RepositoryAccessException {
        String sql = "SELECT COUNT(*) FROM INVOICE WHERE supplier_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException | IOException e) {
            String message = "Greška prilikom provjere povezanosti dobavljača s fakturama!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return false;
    }

    /**
     * Briše dobavljača iz baze i bilježi promjenu.
     */
    @Override
    public void deleteById(Long id) throws RepositoryAccessException {
        Optional<Supplier> oldSupplierOptional = findById(id);
        if (oldSupplierOptional.isEmpty()) {
            throw new RepositoryAccessException("Pokušaj brisanja dobavljača koji ne postoji.");
        }
        Supplier oldSupplier = oldSupplierOptional.get();

        String sql = "DELETE FROM SUPPLIER WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ChangeLogger.logDeletion(oldSupplier);
            }
        } catch (SQLException | IOException e) {
            String message = "Brisanje dobavljača nije uspjelo.";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }
}