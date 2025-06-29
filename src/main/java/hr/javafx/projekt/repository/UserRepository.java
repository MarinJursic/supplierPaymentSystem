package hr.javafx.projekt.repository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hr.javafx.projekt.database.DatabaseConnection;
import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.InvalidLoginException;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.exception.UsernameAlreadyExistsException;
import hr.javafx.projekt.model.User;
import hr.javafx.projekt.repository.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Manages reading, authenticating, and registering users from both file and database.
 */
public class UserRepository {

    private static final String USERS_FILE_PATH = "dat/users.txt";
    private static final int LINES_PER_USER = 4;
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    /**
     * Authenticates a user against the text file.
     * @param username The username.
     * @param password The raw password.
     * @return An Optional containing a Pair of user ID and role if successful.
     * @throws InvalidLoginException if authentication fails.
     */
    public Optional<Pair<Long, UserRole>> authenticate(String username, String password) throws InvalidLoginException {
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
     * Registers a new user in both the database and the text file.
     * @param username The username.
     * @param password The raw password.
     * @param role The user's role.
     * @throws UsernameAlreadyExistsException If the username already exists.
     */
    public synchronized void registerUser(String username, String password, UserRole role) throws UsernameAlreadyExistsException {
        // 1. Check if user exists in the database
        if (findUserIdByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Korisničko ime '" + username + "' je već zauzeto.");
        }

        // 2. Save user to the database
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User newUser = new User(null, username, hashedPassword, role);
        long newId = saveUserToDatabase(newUser);
        newUser.setId(newId);

        // 3. Append user to the text file
        appendUserToFile(newUser);
    }

    private long saveUserToDatabase(User user) {
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
            throw new RepositoryAccessException("Greška pri spremanju korisnika u bazu.", e);
        }
    }

    private void appendUserToFile(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE_PATH, true))) {
            writer.newLine();
            writer.write(String.valueOf(user.getId()));
            writer.newLine();
            writer.write(user.getUsername());
            writer.newLine();
            writer.write(user.getHashedPassword());
            writer.newLine();
            writer.write(user.getRole().name());
        } catch (IOException e) {
            throw new RepositoryAccessException("Greška pri pisanju u korisničku datoteku!", e);
        }
    }

    private Optional<Long> findUserIdByUsername(String username) {
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
            log.error("Greška pri pronalasku korisnika u bazi.", e);
        }
        return Optional.empty();
    }
}