package shoppingList;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import jsonParser.JSONComponent.JSONFileData;
import jsonParser.JSONComponent.JSONItem;
import jsonParser.JSONParser;

import java.io.*;
import java.util.Optional;

/**
 * Handles everything related to Dropbox.
 *
 * @author Tuukka Juusela
 * @version 2018.0212
 * @since 1.8
 */
public class DropboxUpload {
    /**
     * Asks for users authorization to upload to Dropbox using askDropboxInformation().
     *
     * Asks for users authorization to upload to Dropbox using askDropboxInformation(). Saves file in users dropbox
     * if user grants access to his/her Dropbox.
     * @param application the main JavaFx class. Used to open Dropbox authorization to browser.
     * @param table TableView of products.
     */
    public void uploadCurrentListToDropbox(Application application, TableView<Product> table) {
        JSONFileData authJson = new JSONParser().read(new File(getClass().getResource("auth.json").getPath()));
        final String APP_KEY = String.valueOf(((JSONItem)authJson.getComponent("key")).getData());
        final String APP_SECRET = String.valueOf(((JSONItem)authJson.getComponent("secret")).getData());
        DbxRequestConfig requestConfig = new DbxRequestConfig("Tuukka Lister/1.0");
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxWebAuth auth = new DbxWebAuth(requestConfig, appInfo);
        DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .build();
        String authorizeUrl = auth.authorize(authRequest);

        Optional<Pair<String, String>> dBoxInfo = askDropboxInformation(application, authorizeUrl);


        if(dBoxInfo.isPresent()) {
            String code = dBoxInfo.get().getValue().trim();
            String jsonFileName = dBoxInfo.get().getKey();
            if(!jsonFileName.endsWith(".json")) {
                jsonFileName += ".json";
            }

            new JSONHandler().saveTableViewAsJson(jsonFileName, table);
            System.out.println(jsonFileName);

            try {
                DbxAuthFinish authFinish = auth.finishFromCode(code);
                DbxClientV2 client = new DbxClientV2(requestConfig, authFinish.getAccessToken());
                try (InputStream in = new FileInputStream(jsonFileName)) {
                    FileMetadata metadata = client.files().uploadBuilder("/"+jsonFileName).uploadAndFinish(in);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (DbxException e) {
                generateUploadFailedDialog();
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates dialog window about something going wrong in Upload.
     */
    private void generateUploadFailedDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:icons/dropbox.png"));
        alert.setTitle("Something went wrong :-(");
        alert.setHeaderText(null);
        alert.setContentText("Check that file name is not taken in Tuukka Lister's folder\nand that the Dropbox code is correct");
        alert.showAndWait();
    }

    /**
     * Generates dialog window asking for users authorization and file name.
     *
     * Generates Dialog window. Has button for opening Dropbox authorization. Has two fields for text. One for
     * name of the file which is about to be saved and one for Dropbox authorization url.
     * @param app the main JavaFx class. Used to open Dropbox authorization to browser.
     * @param authorizeUrl URL to Dropbox authorization.
     * @return Pair of Strings. First String is files name and second for Dropbox authorization code.
     */
    private Optional<Pair<String, String>> askDropboxInformation(Application app, String authorizeUrl) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Dropbox upload");
        dialog.setHeaderText("Click \"Open Link\" and open browser.\nGo through authentication and copy the link");

        dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/dropbox.png"))));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/dropbox.png")));
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType showLink = new ButtonType("Open Link", ButtonBar.ButtonData.FINISH);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL, showLink);
        dialog.getDialogPane().addEventFilter(ActionEvent.ACTION, e -> {
            if(e.getTarget().toString().contains("Open Link")) {
                e.consume();
                app.getHostServices().showDocument(authorizeUrl);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField fileNameField = new TextField();
        fileNameField.setPromptText("example.json");
        TextField tokenField = new TextField();
        tokenField.setPromptText("Dropbox code");

        grid.add(new Label("File name:"), 0, 0);
        grid.add(fileNameField, 1, 0);
        grid.add(new Label("Dropbox code:"), 0, 1);
        grid.add(tokenField, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        fileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(fileNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(fileNameField.getText(), tokenField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        return result;
    }
}
