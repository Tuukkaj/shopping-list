package shoppingList;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;

public class DropboxDownload {
    Application app;

    void download(Application app, TableView<Product> table) {
        try {
            this.app = app;
            DbxRequestConfig requestConfig = new DbxRequestConfig("Tuukka Lister/1.0");
            DbxWebAuth auth = new DbxWebAuth(requestConfig, DbxAppInfo.Reader.readFully(getClass().getResourceAsStream("auth.json")));
            DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
                    .withNoRedirect()
                    .build();
            String authorizeUrl = auth.authorize(authRequest);
            Optional<String> code = getAuthUrl(authorizeUrl);

            if (code.isPresent()) {
                System.out.print(code.get());

                try {
                    DbxAuthFinish authFinish = auth.finishFromCode(code.get());
                    DbxClientV2 client = new DbxClientV2(requestConfig, authFinish.getAccessToken());
                    Optional<String> fileChosen = generateFilePicker(getFilesDropbox(client));
                    if(fileChosen.isPresent()) {
                        Optional<ObservableList<Product>> products = dropboxDownloadFile(fileChosen.get(), client);
                        if(products.isPresent()) {
                            table.getItems().clear();
                            table.getItems().addAll(products.get());
                        }
                    }
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Optional<ObservableList<Product>> dropboxDownloadFile(String file, DbxClientV2 client) {
        try {
            DbxDownloader<FileMetadata> downloader = client.files().download("/"+file);
            try (FileOutputStream out = new FileOutputStream(file)){
                downloader.download(out);
                return Optional.of(new JSONHandler().readJsonFile(new File(file)));
            } catch (DbxException ex) {
                System.out.println(ex.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(null);
    }

    ObservableList<FileItem> getFilesDropbox(DbxClientV2 client) {
        ObservableList<FileItem> files = FXCollections.observableArrayList();
        try {
            System.out.println("FILES IN DROPBOX: ");
            client.files().listFolder("").getEntries().forEach(meta -> {
                System.out.println(meta.getName());
                if(meta.getName().endsWith("json")) {
                    files.add(new FileItem(meta.getName()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
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

    private Optional<String> generateFilePicker(ObservableList<FileItem> files) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/dropbox.png"))));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getResourceAsStream("icons/dropbox.png")));
        dialog.setTitle("Choose file to download");
        TableView<FileItem> tableBox = generateTable(files);
        dialog.getDialogPane().setContent(tableBox);
        ButtonType chooseButton = new ButtonType("Choose", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, chooseButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == chooseButton) {
                return tableBox.getItems().get(tableBox.getSelectionModel().getSelectedIndex()).getName();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        return result;
    }

    private TableView<FileItem> generateTable(ObservableList<FileItem> files) {
        TableView<FileItem> tableView = new TableView<>();

        TableColumn<FileItem, String> fileColumn = new TableColumn<>("File");
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        fileColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fileColumn.setPrefWidth(200);
        tableView.getColumns().addAll(fileColumn);
        tableView.setItems(files);

        return tableView;
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
