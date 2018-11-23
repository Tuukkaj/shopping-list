package shoppingList;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jsonParser.JSONComponent.JSONArray;
import jsonParser.JSONComponent.JSONFileData;
import jsonParser.JSONComponent.JSONItem;
import jsonParser.JSONParser;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Optional;

class Components {
    private TableView<Product> table;
    private Stage stage;

     BorderPane generateBorderPanel() {
        BorderPane borderPane = new BorderPane();

        borderPane.setTop(generateTopMenuBar());
        table = generateCenterTable();
        borderPane.setCenter(table);
        /*
        borderPane.setBottom(iFeelLuckyButton);
        borderPane.setRight(generateRightBorder());
        borderPane.setCenter(gPane);
        borderPane.setAlignment(iFeelLuckyButton, Pos.BOTTOM_CENTER);
        borderPane.setMargin(iFeelLuckyButton, new Insets(12,12,12,12));
*/
        return borderPane;
    }

    private ObservableList<Product> createObservableList() {
         ObservableList<Product> products = FXCollections.observableArrayList();
         products.add(new Product("Example item",1));

         return products;
    }

    private TableView<Product> generateCenterTable() {
        ObservableList<Product> products = createObservableList();
        //QUALITY COLUMN
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setMinWidth(60);
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return String.valueOf(object);
            }

            @Override
            public Integer fromString(String string) {
                if(string.equalsIgnoreCase("")) {
                    return null;
                }
                return Integer.parseInt(string);
            }
        }));
        quantityColumn.setOnEditCommit(cellEdit -> {
            if(cellEdit.getNewValue() == null) {
                products.remove(cellEdit.getTablePosition().getRow());
            } else {
                cellEdit.getTableView().getItems().get(cellEdit.getTablePosition().getRow()).setQuantity(cellEdit.getNewValue());
            }
        });

        //NAME COLUMN
        TableColumn<Product, String> nameColumn = new TableColumn<>("Product");
        nameColumn.setMinWidth(260);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(cellEdit -> {
            if(cellEdit.getNewValue().equalsIgnoreCase("")) {
                products.remove(cellEdit.getTablePosition().getRow());
            } else {
                cellEdit.getTableView().getItems().get(cellEdit.getTablePosition().getRow()).setName(cellEdit.getNewValue());
            }
        });

        //TABLE Creation
        TableView<Product> table = new TableView<>();
        table.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2) {
                System.out.println("WIDTH : "  + stage.getWidth() + " HEIGHT: " + stage.getHeight());
                table.getItems().add(new Product("-", 1));
            }
        });
        table.setItems(products);
        table.getColumns().addAll(quantityColumn,nameColumn);
        table.setEditable(true);

        return table;
    }

    private VBox generateTopMenuBar() {
        VBox v = new VBox();
        //FILE---
        Menu file = new Menu("File");
        //PRINT TABLE
        MenuItem printTable = new MenuItem("Print Table");
        printTable.setOnAction(e -> printTableContents());
        //UPLOAD DROPBOX
        MenuItem uploadItem = new MenuItem("Upload to Dropbox");
        uploadItem.setOnAction(actionEvent -> uploadToDropBox());
        uploadItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT+D"));

        //READ FILE
        MenuItem readFile = new MenuItem("Read File");
        readFile.setOnAction((event -> generateFileChooser()));
        readFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+R"));
        //SAVE FILE
        MenuItem save = new MenuItem("Save File");
        save.setAccelerator(KeyCombination.keyCombination("SHORTCUT+S"));
        save.setOnAction(actionEvent -> saveTableViewAsJson());
        //Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction((e) -> Platform.exit());
        file.getItems().addAll(printTable, uploadItem, readFile, save, exitItem);


        //HELP--
        Menu help = new Menu("Help");
        MenuItem helpItem = new MenuItem("Help");
        //helpItem.setOnAction(actionEvent -> generateAboutDialog());
        MenuItem aboutItem = new MenuItem("About Shopping list App");
        aboutItem.setOnAction(actionEvent -> generateAboutDialog());
        help.getItems().addAll(helpItem,aboutItem);


        MenuBar menubar = new MenuBar();
        menubar.getMenus().addAll(file,help);

        v.getChildren().add(menubar);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(20);
        return v;
    }

    private void readJsonFile(File file) {
        JSONFileData fileData = new JSONParser().read(file);
        JSONArray array;
        try {
            array = ((JSONArray) fileData.getComponent("shoppingList"));
            table.getItems().clear();
            array.getData().forEach(linkedList -> table.getItems().add(new Product(String.valueOf(linkedList.get("product")), Integer.valueOf(String.valueOf(linkedList.get("quantity"))))));
        } catch (InvalidParameterException e) {
            generateNotProperJSONFileWarning();
        }
    }

    private void saveTableViewAsJson() {
        JSONParser parser = new JSONParser();
        JSONFileData data = new JSONFileData();
        JSONArray array = new JSONArray("shoppingList");

        table.getItems().forEach(product -> {
            if(!product.getName().equalsIgnoreCase("-")) {
                ArrayList<JSONItem> itemList = new ArrayList<>();
                itemList.add(new JSONItem("product", product.getName()));
                itemList.add(new JSONItem("quantity", product.getQuantity()));
                array.add(itemList);
            }
        });
        data.add(array);
        parser.write(data, new File("resources/list.json"));
    }

    private void generateFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open JSON File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            readJsonFile(selectedFile);
        }
    }

    private void generateAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Author - Tuukka Juusela");
        alert.setHeaderText("This program is part of school project\nin Tampere University of Applied Sciences.");
        alert.setContentText("Used to save shopping list as a json file.");
        alert.showAndWait();
    }

    private void generateNotProperJSONFileWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Problem occurred");
        alert.setHeaderText("JSON file you tried to read is\nnot supported by Tuukka Lister.");
        alert.setContentText("Ensure that file you tried to read is made with this program.");
        alert.showAndWait();
    }

    //Remove in final release.
    private void printTableContents() {
        table.getItems().forEach(p -> System.out.println("PRODUCT: " + p.getName() + " QUANTITY: " + p.getQuantity()));
    }

    Components(Stage stage) {
        this.stage = stage;
        System.out.println("WIDTH : "  + stage.getWidth() + " HEIGHT: " + stage.getHeight());
    }

    private void uploadToDropBox() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Upload to Dropbox");
        dialog.setHeaderText("Enter to your Dropbox account token");
        dialog.setContentText("Token: ");

        Optional<String> token = dialog.showAndWait();
        if (token.isPresent()){
            DbxRequestConfig config = DbxRequestConfig.newBuilder("Tuukka Lister/1.0").build();
            DbxClientV2 client = new DbxClientV2(config, token.get());

            try {
                FullAccount account = client.users().getCurrentAccount();
                System.out.println(account.getName().getDisplayName());
                try (InputStream in = new FileInputStream("resources/list.json")) {
                    FileMetadata metadata = client.files().uploadBuilder("/list.json").uploadAndFinish(in);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
    }
}
