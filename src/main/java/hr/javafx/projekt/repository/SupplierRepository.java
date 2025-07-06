package hr.javafx.projekt.repository;

import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.model.Supplier;
import hr.javafx.projekt.utils.ChangeLogger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Upravlja operacijama nad dobavljačima u bazi podataka.
 */
public class SupplierRepository extends AbstractRepository<Supplier> {

    /**
     * Sprema novog dobavljača u bazu i bilježi promjenu.
     *
     * @param supplier Dobavljač za spremanje.
     * @return Spremljeni dobavljač s dodijeljenim ID-em.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
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

            throw new RepositoryAccessException("Greška prilikom spremanja dobavljača!", e);
        }
        return supplier;
    }

    /**
     * Dohvaća sve dobavljače iz baze.
     *
     * @return Lista svih dobavljača.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
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

            throw new RepositoryAccessException("Greška prilikom dohvaćanja svih dobavljača!", e);
        }
        return suppliers;
    }

    /**
     * Pronalazi dobavljača prema njegovom jedinstvenom ID-u.
     *
     * @param id ID dobavljača.
     * @return Optional koji sadrži dobavljača ako je pronađen.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
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

            throw new RepositoryAccessException("Greška prilikom dohvaćanja dobavljača po ID-u!", e);
        }
        return Optional.empty();
    }

    /**
     * Ažurira postojećeg dobavljača u bazi i bilježi promjenu.
     *
     * @param supplier Dobavljač s ažuriranim podacima.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
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

            throw new RepositoryAccessException("Greška prilikom ažuriranja dobavljača!", e);
        }
    }

    /**
     * Provjerava je li dobavljač povezan s bilo kojom fakturom.
     *
     * @param supplierId ID dobavljača za provjeru.
     * @return True ako je dobavljač povezan s barem jednom fakturom, inače false.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
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

            throw new RepositoryAccessException("Greška prilikom provjere povezanosti dobavljača s fakturama!", e);
        }
        return false;
    }

    /**
     * Briše dobavljača iz baze podataka prema ID-u i bilježi promjenu.
     *
     * @param id ID dobavljača za brisanje.
     * @throws RepositoryAccessException ako dođe do greške pri pristupu bazi.
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

            throw new RepositoryAccessException("Brisanje dobavljača nije uspjelo.", e);
        }
    }
}