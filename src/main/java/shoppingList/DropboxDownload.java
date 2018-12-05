package shoppingList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DropboxDownload {
    void download() {
        generateFilePicker();
    }

    private void generateFilePicker() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/dropbox.png"))));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getResourceAsStream("icons/dropbox.png")));

        dialog.getDialogPane().setContent(generateTable());
        dialog.showAndWait();

    }

    private HBox generateTable() {
        HBox tableBox = new HBox();
        TableView<String> tableView = new TableView<>();
        ObservableList<String> oList = FXCollections.observableArrayList();
        tableView.setItems(oList);
        TableColumn<String, String> fileColumn = new TableColumn<>("File");
        tableBox.getChildren().addAll(tableView);

        return tableBox;
    }
}
