package shoppingList;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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

public class DropboxUpload {
    private ObservableList<Product> products;

    public void uploadCurrentListToDropbox(Application application, TableView<Product> table) {
        JSONFileData authJson = new JSONParser().read(new File("resources/auth.json"));
        final String APP_KEY = String.valueOf(((JSONItem)authJson.getComponent("key")).getData());
        final String APP_SECRET = String.valueOf(((JSONItem)authJson.getComponent("secret")).getData());
        DbxRequestConfig requestConfig = new DbxRequestConfig("Tuukka Lister/1.0");
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxWebAuth auth = new DbxWebAuth(requestConfig, appInfo);
        DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .build();
        String authorizeUrl = auth.authorize(authRequest);

        application.getHostServices().showDocument(authorizeUrl);
        Optional<Pair<String, String>> dBoxInfo = askDropboxInformation();


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
                try (InputStream in = new FileInputStream("resources/"+jsonFileName)) {
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

    private void generateUploadFailedDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image("file:icons/dropbox.png"));
        alert.setTitle("Something went wrong :-(");
        alert.setHeaderText(null);
        alert.setContentText("Check that file name is not taken in Tuukka Lister's folder\nand that the Dropbox code is correct");
        alert.showAndWait();
    }

    private Optional<Pair<String, String>> askDropboxInformation() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Dropbox upload");
        dialog.setHeaderText("Tab opened in your browser.\nClick allow and copy the Dropbox code. ");

        dialog.setGraphic(new ImageView("file:icons/dropbox.png"));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image("file:icons/dropbox.png"));
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

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
