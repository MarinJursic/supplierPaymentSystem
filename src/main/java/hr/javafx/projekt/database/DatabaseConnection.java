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
 * Upravlja konekcijom s bazom podataka.
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    /**
     * Privatni konstruktor sprječava instanciranje.
     */
    private DatabaseConnection() {
    }

    /**
     * Uspostavlja konekciju s bazom podataka.
     *
     * @return Objekt konekcije.
     * @throws SQLException Ako dođe do greške pri spajanju.
     * @throws IOException Ako dođe do greške pri čitanju datoteke.
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

    /**
     * Zatvara proslijeđenu konekciju.
     *
     * @param connection Konekcija koju treba zatvoriti.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Greška prilikom zatvaranja konekcije s bazom podataka.", e);
            }
        }
    }
}