package shoppingList.database;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DatabaseErrorDialogs {
    public void generateError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Something went horribly wrong.\nPlease contact the developer");
        alert.showAndWait();
    }

    public void generateSQLError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        alert.setTitle("SQL Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
