package hr.javafx.projekt.repository;

import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementacija repozitorija za rad s dobavljačima u bazi podataka.
 */
public class SupplierRepository implements Repository<Supplier> {

    private static final Logger log = LoggerFactory.getLogger(SupplierRepository.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier save(Supplier supplier) {
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
                }
            }
        } catch (Exception e) {
            log.error("Greška prilikom spremanja dobavljača!", e);
        }
        return supplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id,name,address,oib FROM SUPPLIER";
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
        } catch (Exception e) {
            log.error("Greška prilikom dohvaćanja svih dobavljača!", e);
        }
        return suppliers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Supplier> findById(Long id) {
        String sql = "SELECT id,name,address,oib FROM SUPPLIER WHERE id = ?";
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
        } catch (Exception e) {
            log.error("Greška prilikom dohvaćanja dobavljača po ID-u!", e);
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Supplier supplier) {
        String sql = "UPDATE SUPPLIER SET name = ?, address = ?, oib = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getAddress());
            stmt.setString(3, supplier.getOib());
            stmt.setLong(4, supplier.getId());
            stmt.executeUpdate();
        } catch (Exception e) {
            log.error("Greška prilikom ažuriranja dobavljača!", e);
        }
    }

    /**
     * Provjerava je li dobavljač povezan s bilo kojom fakturom.
     * @param supplierId ID dobavljača.
     * @return True ako postoje povezane fakture, inače false.
     */
    public boolean isSupplierLinkedToInvoices(Long supplierId) {
        String sql = "SELECT COUNT(*) FROM INVOICE WHERE supplier_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            log.error("Greška prilikom provjere povezanosti dobavljača s fakturama!", e);
        }
        return false;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM SUPPLIER WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RepositoryAccessException("Brisanje dobavljača nije uspjelo.", e);
        }
    }
}