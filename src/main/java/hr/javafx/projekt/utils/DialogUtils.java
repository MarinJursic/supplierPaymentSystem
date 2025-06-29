package hr.javafx.projekt.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Pomoćna klasa za prikazivanje standardiziranih JavaFX dijaloga.
 * Centralizira kreiranje Alert dijaloga za greške, upozorenja i potvrde.
 */
public class DialogUtils {

    private static final Logger log = LoggerFactory.getLogger(DialogUtils.class);

    /**
     * Privatni konstruktor kako bi se spriječilo instanciranje uslužne klase.
     */
    private DialogUtils() {
    }

    /**
     * Prikazuje dijalog s porukom o grešci (ERROR).
     *
     * @param title   Naslov prozora.
     * @param content Sadržaj poruke koja se prikazuje korisniku.
     */
    public static void showError(String title, String content) {
        log.error("Prikazana greška korisniku: Naslov='{}', Sadržaj='{}'", title, content);
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    /**
     * Prikazuje dijalog s porukom upozorenja (WARNING).
     *
     * @param title   Naslov prozora.
     * @param content Sadržaj poruke koja se prikazuje korisniku.
     */
    public static void showWarning(String title, String content) {
        log.warn("Prikazano upozorenje korisniku: Naslov='{}', Sadržaj='{}'", title, content);
        showAlert(Alert.AlertType.WARNING, title, content);
    }

    /**
     * Prikazuje dijalog s informativnom porukom (INFORMATION).
     *
     * @param title   Naslov prozora.
     * @param content Sadržaj poruke koja se prikazuje korisniku.
     */
    public static void showInformation(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    /**
     * Prikazuje dijalog za potvrdu (CONFIRMATION) s opcijama DA/NE.
     *
     * @param title   Naslov prozora.
     * @param content Pitanje koje se postavlja korisniku.
     * @return {@code true} ako je korisnik kliknuo DA (YES), inače {@code false}.
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * Privatna pomoćna metoda koja kreira i prikazuje Alert dijalog.
     *
     * @param type    Tip alerta (ERROR, WARNING, INFORMATION, etc.).
     * @param title   Naslov prozora.
     * @param content Sadržaj poruke.
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