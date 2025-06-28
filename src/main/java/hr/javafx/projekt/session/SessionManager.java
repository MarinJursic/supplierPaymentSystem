package hr.javafx.projekt.session;

import hr.javafx.projekt.enums.UserRole;

/**
 * Upravlja sesijom prijavljenog korisnika.
 */
public class SessionManager {
    private static Long loggedInUserId;
    private static UserRole userRole;

    private SessionManager() {}

    /**
     * Zapisuje podatke o prijavljenom korisniku.
     * @param userId ID korisnika.
     * @param role Rola korisnika.
     */
    public static void login(Long userId, UserRole role) {
        loggedInUserId = userId;
        userRole = role;
    }

    /**
     * Odjavljuje trenutnog korisnika.
     */
    public static void logout() {
        loggedInUserId = null;
        userRole = null;
    }

    /**
     * Vraća ID prijavljenog korisnika.
     * @return ID korisnika ili null ako nitko nije prijavljen.
     */
    public static Long getLoggedInUserId() {
        return loggedInUserId;
    }

    /**
     * Vraća rolu prijavljenog korisnika.
     * @return Rola korisnika ili null ako nitko nije prijavljen.
     */
    public static UserRole getUserRole() {
        return userRole;
    }

    /**
     * Provjerava je li prijavljeni korisnik administrator.
     * @return True ako je korisnik admin, inače false.
     */
    public static boolean isAdmin() {
        return UserRole.ADMIN.equals(userRole);
    }
}