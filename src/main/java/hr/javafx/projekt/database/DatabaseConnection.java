package hr.javafx.projekt.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Upravlja konekcijom prema bazi podataka.
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    /**
     * Uspostavlja i vraća novu konekciju prema bazi podataka.
     * @return Objekt nove konekcije.
     * @throws SQLException Ako dođe do greške pri spajanju.
     * @throws IOException Ako dođe do greške pri čitanju konfiguracijske datoteke.
     */
    public static Connection getConnection() throws SQLException, IOException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader("database.properties")) {
            props.load(reader);
        }

        return DriverManager.getConnection(
                props.getProperty("databaseUrl"),
                props.getProperty("username"),
                props.getProperty("password")
        );
    }
}