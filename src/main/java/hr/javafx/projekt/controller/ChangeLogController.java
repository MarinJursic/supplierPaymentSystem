package hr.javafx.projekt.controller;

import hr.javafx.projekt.model.ChangeLogEntry;
import hr.javafx.projekt.repository.ChangeLogRepository;
import hr.javafx.projekt.utils.Navigation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Kontroler za prikaz zapisa o promjenama u sustavu.
 */
public class ChangeLogController {

    @FXML private TableView<ChangeLogEntry> changeLogTableView;
    @FXML private TableColumn<ChangeLogEntry, String> timestampColumn;
    @FXML private TableColumn<ChangeLogEntry, String> changeTypeColumn;
    @FXML private TableColumn<ChangeLogEntry, String> entityNameColumn;
    @FXML private TableColumn<ChangeLogEntry, String> userRoleColumn;
    @FXML private TableColumn<ChangeLogEntry, String> detailsColumn;

    private final ChangeLogRepository changeLogRepository = new ChangeLogRepository();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

    /**
     * Inicijalizira kontroler, postavlja stupce tablice i učitava podatke.
     */
    public void initialize() {
        setupTableColumns();
        setupRowClickListener();
        loadChanges();
    }

    /**
     * Konfigurira stupce tablice i način prikaza podataka.
     */
    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().timestamp().format(FORMATTER)));
        changeTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().changeType()));
        entityNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().entityName()));
        userRoleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().userRole()));

        detailsColumn.setCellValueFactory(data -> {
            ChangeLogEntry entry = data.getValue();
            String changeType = entry.changeType();

            if (changeType == null) {
                return new SimpleStringProperty("Nepoznata promjena");
            }

            String details = switch (changeType) {
                case "ADD" -> "Dodan: " + entry.newValue();
                case "DELETE" -> "Obrisan: " + entry.oldValue();
                case "UPDATE" -> String.format("Polje '%s' promijenjeno", entry.fieldName());
                default -> "Nepoznata akcija";
            };
            return new SimpleStringProperty(details);
        });
    }

    /**
     * Učitava zapise o promjenama iz repozitorija i prikazuje ih u tablici.
     */
    private void loadChanges() {
        List<ChangeLogEntry> changes = changeLogRepository.readChanges();
        changeLogTableView.setItems(FXCollections.observableArrayList(changes));
    }

    /**
     * Postavlja osluškivač za dvostruki klik na redak tablice, što otvara detalje promjene.
     */
    private void setupRowClickListener() {
        changeLogTableView.setRowFactory(tv -> {
            TableRow<ChangeLogEntry> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ChangeLogEntry rowData = row.getItem();
                    showDetailsPopup(rowData);
                }
            });
            return row;
        });
    }

    /**
     * Prikazuje popup prozor s detaljima odabranog zapisa o promjeni.
     *
     * @param entry Zapis o promjeni za koji se prikazuju detalji.
     */
    private void showDetailsPopup(ChangeLogEntry entry) {
        Optional<Navigation.Popup<ChangeLogDetailsController>> popupOptional =
                Navigation.createPopup("changelog_details.fxml", "Detalji Promjene");

        if (popupOptional.isPresent()) {
            Navigation.Popup<ChangeLogDetailsController> popup = popupOptional.get();
            popup.controller().setChangeLogEntry(entry);
            popup.stage().showAndWait();
        }
    }
}