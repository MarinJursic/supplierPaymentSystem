package hr.javafx.projekt.model;

import hr.javafx.projekt.enums.UserRole;

/**
 * Predstavlja korisnika sustava.
 */
public final class User extends BaseEntity {
    private String username;
    private String hashedPassword;
    private UserRole role;

    public User(Long id, String username, String hashedPassword, UserRole role) {
        super(id);
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public UserRole getRole() { return role; }
}