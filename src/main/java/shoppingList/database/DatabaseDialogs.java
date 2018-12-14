package shoppingList.database;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * Contains dialog window generation for Database classes.
 *
 * @author Tuukka Juusela
 * @version 2018.1412
 * @since 1.8
 */
public class DatabaseDialogs {
    /**
     * Generates general error.
     */
    public void generateError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Something went horribly wrong.\nPlease contact the developer");
        alert.showAndWait();
    }

    /**
     * Generates SQL error containing message.
     * @param message Message to use in dialog.
     */
    public void generateSQLError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        alert.setTitle("SQL Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Generates Database dialog with given parameters.
     */
    public void generateDialog(String title, String message) {
        Dialog dialog = new Dialog();

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png"))));
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        dialog.showAndWait();
    }
}
