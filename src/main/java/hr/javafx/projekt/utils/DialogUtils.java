package hr.javafx.projekt.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Pomoćna klasa za prikazivanje standardiziranih JavaFX dijaloga.
 */
public final class DialogUtils {

    private static final Logger log = LoggerFactory.getLogger(DialogUtils.class);

    private DialogUtils() {
    }

    /**
     * Prikazuje dijalog s porukom o grešci.
     */
    public static void showError(String title, String content) {
        log.error("Prikazana greška korisniku: Naslov='{}', Sadržaj='{}'", title, content);
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    /**
     * Prikazuje dijalog s porukom upozorenja.
     */
    public static void showWarning(String title, String content) {
        log.warn("Prikazano upozorenje korisniku: Naslov='{}', Sadržaj='{}'", title, content);
        showAlert(Alert.AlertType.WARNING, title, content);
    }

    /**
     * Prikazuje dijalog s informativnom porukom.
     */
    public static void showInformation(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    /**
     * Prikazuje dijalog za potvrdu s opcijama DA/NE.
     * Vraća true ako je korisnik kliknuo DA (YES).
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Privatna metoda koja kreira i prikazuje Alert dijalog.
     */
    private static void showAlert(Alert.AlertType type, String title, String content) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            log.error("Greška prilikom prikazivanja Alert dijaloga.", e);
        }
    }
}