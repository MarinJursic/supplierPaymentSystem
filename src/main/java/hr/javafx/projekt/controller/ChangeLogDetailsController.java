package hr.javafx.projekt.controller;

import hr.javafx.projekt.model.ChangeLogEntry;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Kontroler za prikaz detalja pojedinog zapisa o promjeni.
 */
public class ChangeLogDetailsController {

    @FXML private Label timestampLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label changeDetailsLabel;
    @FXML private VBox oldValueBox; // Ispravno, ovo je VBox
    @FXML private VBox newValueBox; // Ispravno, ovo je VBox

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

    /**
     * Postavlja podatke o promjeni i dinamički popunjava UI.
     * @param entry Zapis o promjeni koji se prikazuje.
     */
    public void setChangeLogEntry(ChangeLogEntry entry) {
        if (entry == null) return;

        timestampLabel.setText(entry.timestamp().format(FORMATTER));
        userRoleLabel.setText(entry.userRole());

        String changeDetails = String.format("%s na entitetu '%s'", entry.changeType(), entry.entityName());
        if (!"N/A".equals(entry.fieldName())) {
            changeDetails += String.format(" (polje: %s)", entry.fieldName());
        }
        changeDetailsLabel.setText(changeDetails);

        oldValueBox.getChildren().clear();
        newValueBox.getChildren().clear();

        switch (entry.changeType()) {
            case "ADD" -> displayAdded(entry);
            case "DELETE" -> displayDeleted(entry);
            case "UPDATE" -> displayUpdated(entry);
            default -> {
                oldValueBox.getChildren().add(new Label("N/A"));
                newValueBox.getChildren().add(new Label("N/A"));
            }
        }
    }

    private void displayAdded(ChangeLogEntry entry) {
        oldValueBox.getChildren().add(new Label("Nema stare vrijednosti."));
        // Parsiramo novi objekt da ga lijepo prikažemo
        Map<String, String> newValues = parseStringToObjectMap(entry.newValue());
        newValues.forEach((key, value) ->
                newValueBox.getChildren().add(createStyledLabel(key + ": " + value, "green")));
    }

    private void displayDeleted(ChangeLogEntry entry) {
        // Parsiramo stari objekt
        Map<String, String> oldValues = parseStringToObjectMap(entry.oldValue());
        oldValues.forEach((key, value) ->
                oldValueBox.getChildren().add(createStyledLabel(key + ": " + value, "red")));
        newValueBox.getChildren().add(new Label("Nema nove vrijednosti."));
    }

    private void displayUpdated(ChangeLogEntry entry) {
        Map<String, String> oldValues = parseStringToObjectMap(entry.oldValue());
        Map<String, String> newValues = parseStringToObjectMap(entry.newValue());

        // Koristimo zajednički skup ključeva da prođemo kroz sva polja
        oldValues.keySet().forEach(key -> {
            String oldValue = oldValues.getOrDefault(key, "N/A");
            String newValue = newValues.getOrDefault(key, "N/A");

            Label oldLabel = new Label(key + ": " + oldValue);
            Label newLabel = new Label(key + ": " + newValue);

            if (!Objects.equals(oldValue, newValue)) {
                oldLabel.setTextFill(Color.RED);
                oldLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                newLabel.setTextFill(Color.GREEN);
                newLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            }

            oldValueBox.getChildren().add(oldLabel);
            newValueBox.getChildren().add(newLabel);
        });
    }

    private Label createStyledLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        return label;
    }

    private Map<String, String> parseStringToObjectMap(String objectString) {
        Map<String, String> map = new HashMap<>();
        if (objectString == null || objectString.isBlank() || !objectString.contains("[")) {
            map.put("Vrijednost", objectString);
            return map;
        }

        String content = objectString.substring(objectString.indexOf('[') + 1, objectString.lastIndexOf(']'));
        Arrays.stream(content.split(",\\s*"))
                .map(pair -> pair.split("=", 2))
                .filter(parts -> parts.length == 2)
                .forEach(parts -> map.put(parts[0].trim(), parts[1].trim()));

        return map;
    }
}