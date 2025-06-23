package hr.javafx.projekt.repository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import hr.javafx.projekt.enums.UserRole;
import hr.javafx.projekt.exception.InvalidLoginException;
import hr.javafx.projekt.exception.RepositoryAccessException;
import hr.javafx.projekt.exception.UsernameAlreadyExistsException;
import hr.javafx.projekt.model.User;
import hr.javafx.projekt.generics.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Upravlja čitanjem, autentifikacijom i registracijom korisnika iz datoteke.
 */
public class UserRepository {

    private static final String USERS_FILE_PATH = "dat/users.txt";
    private static final int LINES_PER_USER = 4;
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

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
                        Long id = Long.parseLong(lines.get(i));
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
     * Registrira novog korisnika i sprema ga u datoteku.
     * @param username Korisničko ime.
     * @param password Sirova lozinka.
     * @param role Rola novog korisnika.
     * @throws UsernameAlreadyExistsException Ako korisničko ime već postoji.
     * @throws RepositoryAccessException Ako dođe do greške pri radu s datotekom.
     */
    public synchronized void registerUser(String username, String password, UserRole role) throws UsernameAlreadyExistsException {
        try {
            List<String> lines = Files.readAllLines(Path.of(USERS_FILE_PATH));
            lines.removeIf(String::isEmpty);

            for (int i = 0; i < lines.size(); i += LINES_PER_USER) {
                if (lines.get(i + 1).equals(username)) {
                    throw new UsernameAlreadyExistsException("Korisničko ime '" + username + "' je već zauzeto.");
                }
            }

            long nextId = ((long) lines.size() / LINES_PER_USER) + 1L;
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            User newUser = new User(nextId, username, hashedPassword, role); // Korištenje role iz parametra

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE_PATH, true))) {
                if (!lines.isEmpty()) {
                    writer.newLine();
                }
                writer.write(String.valueOf(newUser.getId()));
                writer.newLine();
                writer.write(newUser.getUsername());
                writer.newLine();
                writer.write(newUser.getHashedPassword());
                writer.newLine();
                writer.write(newUser.getRole().name());
            }

        } catch (IOException e) {
            String message = "Greška pri pisanju u korisničku datoteku!";
            log.error(message, e);
            throw new RepositoryAccessException(message, e);
        }
    }
}