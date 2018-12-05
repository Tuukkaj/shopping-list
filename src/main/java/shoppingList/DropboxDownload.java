package shoppingList;

import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class DropboxDownload {

    private void generateFilePicker() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/dropbox.png"))));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getResourceAsStream("icons/dropbox.png")));
    }
}
