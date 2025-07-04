package hr.javafx.projekt.model;

import hr.javafx.projekt.enums.UserRole;

/**
 * Predstavlja korisnika sustava.
 * Klasa je `non-sealed`, što znači da može biti slobodno naslijeđena.
 */
public non-sealed class User extends Entity {
    private final String username;
    private final String hashedPassword;
    private final UserRole role;

    /**
     * Konstruktor za kreiranje novog korisnika.
     * @param id Jedinstveni identifikator korisnika.
     * @param username Korisničko ime.
     * @param hashedPassword Hashirana lozinka korisnika.
     * @param role Korisnička rola (npr. ADMIN, USER).
     */
    public User(Long id, String username, String hashedPassword, UserRole role) {
        super(id);
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    /**
     * Vraća korisničko ime.
     * @return Korisničko ime.
     */
    public String getUsername() { return username; }

    /**
     * Vraća hashiranu lozinku.
     * @return Hashirana lozinka.
     */
    public String getHashedPassword() { return hashedPassword; }

    /**
     * Vraća korisničku rolu.
     * @return Korisnička rola.
     */
    public UserRole getRole() { return role; }
}