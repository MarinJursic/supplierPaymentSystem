package hr.javafx.projekt.repository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.InvalidLoginException;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.exception.UsernameAlreadyExistsException;
import hr.javafx.projekt.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Upravlja čitanjem, autentifikacijom i registracijom korisnika.
 * Koristi tekstualnu datoteku za provjeru lozinki i bazu podataka za pohranu korisnika.
 */
public class UserRepository {

    private static final String USERS_FILE_PATH = "dat/users.txt";
    private static final int LINES_PER_USER = 4;
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    /**
     * Autentificira korisnika usporedbom s podacima iz tekstualne datoteke.
     */
    public Optional<Pair<Long, UserRole>> authenticate(String username, String password) throws InvalidLoginException, RepositoryAccessException {
        try {
            List<String> lines = Files.readAllLines(Path.of(USERS_FILE_PATH));
            lines.removeIf(String::isEmpty);

            for (int i = 0; i <= lines.size() - LINES_PER_USER; i += LINES_PER_USER) {
                String storedUsername = lines.get(i + 1);
                if (storedUsername.equals(username)) {
                    String storedHash = lines.get(i + 2);
                    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
                    if (result.verified) {
                        long id = findUserIdByUsername(username).orElse(-1L);
                        UserRole role = UserRole.valueOf(lines.get(i + 3));
                        return Optional.of(new Pair<>(id, role));
                    } else {
                        throw new InvalidLoginException("Netočna lozinka.");
                    }
                }
            }
        } catch (IOException e) {
            String message = "Greška pri čitanju korisničke datoteke!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        throw new InvalidLoginException("Korisnik s imenom '" + username + "' ne postoji.");
    }

    /**
     * Registrira novog korisnika u bazu podataka i tekstualnu datoteku.
     */
    public synchronized void registerUser(String username, String password, UserRole role) throws UsernameAlreadyExistsException, RepositoryAccessException {
        if (findUserIdByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Korisničko ime '" + username + "' je već zauzeto.");
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User newUser = new User(null, username, hashedPassword, role);
        long newId = saveUserToDatabase(newUser);
        newUser.setId(newId);

        appendUserToFile(newUser);
    }

    /**
     * Sprema novog korisnika u bazu podataka.
     */
    private long saveUserToDatabase(User user) throws RepositoryAccessException {
        String sql = "INSERT INTO USERS (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getHashedPassword());
            stmt.setString(3, user.getRole().name());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new RepositoryAccessException("Spremanje korisnika nije uspjelo, ID nije dobiven.");
                }
            }
        } catch (SQLException | IOException e) {
            log.error("Greška pri spremanju korisnika u bazu.", e);
            throw new RepositoryAccessException("Greška pri spremanju korisnika u bazu.", e);
        }
    }

    /**
     * Dodaje novog korisnika na kraj tekstualne datoteke.
     */
    private void appendUserToFile(User user) throws RepositoryAccessException {
        File file = new File(USERS_FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                String message = "Nije moguće kreirati direktorij: " + parentDir.getAbsolutePath();
                log.error(message);
                throw new RepositoryAccessException(message);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.newLine();
            writer.write(String.valueOf(user.getId()));
            writer.newLine();
            writer.write(user.getUsername());
            writer.newLine();
            writer.write(user.getHashedPassword());
            writer.newLine();
            writer.write(user.getRole().name());
        } catch (IOException e) {
            log.error("Greška pri pisanju u korisničku datoteku!", e);
            throw new RepositoryAccessException("Greška pri pisanju u korisničku datoteku!", e);
        }
    }

    /**
     * Pronalazi ID korisnika u bazi podataka prema korisničkom imenu.
     */
    private Optional<Long> findUserIdByUsername(String username) throws RepositoryAccessException {
        String sql = "SELECT id FROM USERS WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("id"));
                }
            }
        } catch (SQLException | IOException e) {
            String message = "Greška pri pronalasku korisnika u bazi.";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
        return Optional.empty();
    }
}