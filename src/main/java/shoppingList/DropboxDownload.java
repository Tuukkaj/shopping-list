package shoppingList;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Optional;

public class DropboxDownload {
    Application app;

    void download(Application app) {
        try {
            this.app = app;
            DbxRequestConfig requestConfig = new DbxRequestConfig("Tuukka Lister/1.0");
            DbxWebAuth auth = new DbxWebAuth(requestConfig, DbxAppInfo.Reader.readFully(getClass().getResourceAsStream("auth.json")));
            DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
                    .withNoRedirect()
                    .build();
            String authorizeUrl = auth.authorize(authRequest);
            Optional<String> userToken = getAuthUrl(authorizeUrl);
            if (userToken.isPresent()) {
                System.out.print(userToken.get());
                generateFilePicker();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Optional<String> getAuthUrl(String url) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Dropbox Authentication");
        dialog.setHeaderText("Click \"Open Link\" and open browser.\nGo through authentication and copy the code");

        dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/dropbox.png"))));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/dropbox.png")));
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType showLink = new ButtonType("Open Link", ButtonBar.ButtonData.FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL, showLink);
        dialog.getDialogPane().addEventFilter(ActionEvent.ACTION, e -> {
            if(e.getTarget().toString().contains("Open Link")) {
                e.consume();
                app.getHostServices().showDocument(url);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tokenField = new TextField();
        tokenField.setPromptText("Dropbox code");

        grid.add(new Label("Dropbox code:"), 0, 1);
        grid.add(tokenField, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        tokenField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new String(tokenField.getText());
            }
            return null;
        });

        Platform.runLater(tokenField::requestFocus);
        Optional<String> result = dialog.showAndWait();
        return result;
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
