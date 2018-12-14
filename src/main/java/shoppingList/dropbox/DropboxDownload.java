package shoppingList.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
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
import javafx.stage.Stage;
import shoppingList.json.JSONHandler;
import shoppingList.Product;
import shoppingList.FileItem;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;

/**
 * Handles everything related to downloading shopping list from Dropbox.
 *
 * @author Tuukka Juusela
 * @version 2018.1412
 * @since 1.8
 */
public class DropboxDownload {

    /**
     * Main method of the class. Used to load shopping list from user's Dropbox.
     *
     * Loads application's Dropbox authorization from external file and creates url to authenticate this application.
     * Opens Dialog window where user is asked to enter authorization code from Dropbox. After user has entered
     * correct authorization code application opens new dialog for the user to choose json file from his/her Dropbox
     * folder. Loads selected file to TableView.
     * @param app Application to open authorization url with.
     * @param table TableView to load json file to.
     */
    public void download(Application app, TableView<Product> table) {
        try {
            DbxRequestConfig requestConfig = new DbxRequestConfig("Tuukka Lister/1.0");
            DbxWebAuth auth = new DbxWebAuth(requestConfig, DbxAppInfo.Reader.readFully(getClass().getClassLoader().getResourceAsStream("shoppingList/auth.json")));
            DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
                    .withNoRedirect()
                    .build();
            String authorizeUrl = auth.authorize(authRequest);
            Optional<String> code = getAuthUrl(authorizeUrl, app);

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
                            generateDownloadSuccess();
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

    /**
     * Downloads given file from Dropbox and turns it to ObservableList<Product>
     * @param file Files name to download.
     * @param client DbxClientV2 to download file from Dropbox.
     * @return Optional ObservableList<Product> created from downloaded file.
     */
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

    /**
     * Creates ObservableList<FileItem> from user's json files in Dropbox.
     * @param client DbxClientV2 to access user's Dropbox.
     * @return ObservableList<FileItem> created from user's json files in Dropbox.
     */
    private ObservableList<FileItem> getFilesDropbox(DbxClientV2 client) {
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

    /**
     * Creates dialog window for user to open Dropbox authorization in browser and to enter Dropbox authentication code.
     * @param url Authorization url to open.
     * @param app Application to open authorization url with.
     * @return Optional of Authentication code to user's Dropbox.
     */
    private Optional<String> getAuthUrl(String url, Application app) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Dropbox Authentication");
        dialog.setHeaderText("Click \"Open Link\" and open browser.\nGo through authentication and copy the code");

        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/dropbox.png"))));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/dropbox.png")));
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

    /**
     * Creates dialog window for user to choose file to download.
     * @param files Json files in user's Dropbox.
     * @return Optional of chosen json file in user's Dropbox.
     */
    private Optional<String> generateFilePicker(ObservableList<FileItem> files) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/dropbox.png"))));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/dropbox.png")));
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

    /**
     * Generates TableView with given parameter which contains user's json file's names in Dropbox.
     * @param files User's json files in Dropbox.
     * @return TableView created with given parameter.
     */
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

    /**
     * Generates information dialog for successful download. K
     */
    private void generateDownloadSuccess() {
        Dialog dialog = new Dialog();

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/dropbox.png")));
        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/dropbox.png"))));
        dialog.setTitle("Dropbox Download");
        dialog.setHeaderText(null);
        dialog.setContentText("Download was successful");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        dialog.showAndWait();
    }
}
