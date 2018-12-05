package shoppingList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        dialog.setTitle("Choose file to download");
        VBox tableBox = generateTable();
        dialog.getDialogPane().setContent(tableBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        dialog.showAndWait();
    }

    private VBox generateTable() {
        VBox tableBox = new VBox();

        TableView<FileItem> tableView = new TableView<>();

        ObservableList<FileItem> oList = FXCollections.observableArrayList();
        oList.addAll(new FileItem("example.json"));

        TableColumn<FileItem, String> fileColumn = new TableColumn<>("File");
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        fileColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fileColumn.setPrefWidth(200);
        tableView.getColumns().addAll(fileColumn);
        tableView.setItems(oList);
        tableBox.getChildren().addAll(tableView);
        return tableBox;
    }

    public class FileItem {
        String name;

        FileItem(String name) {
            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
