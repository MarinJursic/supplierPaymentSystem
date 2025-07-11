package hr.javafx.projekt.utils;

import hr.javafx.projekt.main.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Optional;

/**
 * Pomoćna klasa za upravljanje navigacijom između scena u aplikaciji.
 */
public final class Navigation {

    private static final Logger log = LoggerFactory.getLogger(Navigation.class);
    private static Stage primaryStage;

    private Navigation() {}

    /**
     * Pomoćni zapis (record) za vraćanje Stage-a i kontrolera iz popup metode.
     */
    public record Popup<T>(Stage stage, T controller) {}

    /**
     * Postavlja glavni prozor (Stage) aplikacije.
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Učitava i prikazuje novu scenu na glavnom prozoru.
     */
    public static void showScene(String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = loadFXML(fxmlFile);
            Scene scene = new Scene(fxmlLoader.load());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            log.error("Greška prilikom učitavanja scene: {}", fxmlFile, e);
            DialogUtils.showError("Greška Navigacije", "Nije moguće učitati ekran: " + fxmlFile);
        }
    }

    /**
     * Kreira novi prozor (popup) kao modalni dijalog.
     * Vraća Stage i kontroler kako bi se podaci mogli postaviti prije prikaza.
     */
    public static <T> Optional<Popup<T>> createPopup(String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = loadFXML(fxmlFile);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            Scene scene = new Scene(fxmlLoader.load());
            dialogStage.setScene(scene);

            return Optional.of(new Popup<>(dialogStage, fxmlLoader.getController()));
        } catch (IOException e) {
            log.error("Greška prilikom kreiranja popup prozora: {}", fxmlFile, e);
            DialogUtils.showError("Greška Navigacije", "Nije moguće kreirati popup prozor: " + fxmlFile);
            return Optional.empty();
        }
    }

    /**
     * Pomoćna metoda za učitavanje FXML datoteke.
     */
    private static FXMLLoader loadFXML(String fxmlFile) throws IOException {
        String resourcePath = "/hr/javafx/projekt/" + fxmlFile;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(resourcePath));
        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Ne mogu pronaći FXML datoteku na putanji: " + resourcePath);
        }
        return fxmlLoader;
    }
}