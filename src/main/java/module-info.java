module hr.javafx.projekt {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;
    requires bcrypt;


    opens hr.javafx.projekt to javafx.fxml;
    exports hr.javafx.projekt;
    exports hr.javafx.projekt.main;
    opens hr.javafx.projekt.main to javafx.fxml;
    exports hr.javafx.projekt.controller;
    opens hr.javafx.projekt.controller to javafx.fxml;
}