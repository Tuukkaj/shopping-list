package shoppingList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jsonParser.JSONComponent.JSONArray;
import jsonParser.JSONComponent.JSONFileData;
import jsonParser.JSONComponent.JSONItem;
import jsonParser.JSONParser;

import java.io.File;
import java.util.ArrayList;

class Components {
    private TableView<Product> table;
    private Stage stage;

    TableView<Product> getTable() {
        return table;
    }



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
         products.add(new Product("Cat",1));
         products.add(new Product("Cat food 1kg",2));
         products.add(new Product("Cat launcher",42));

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
        table.setItems(products);
        table.getColumns().addAll(quantityColumn,nameColumn);
        table.setEditable(true);
        return table;
    }

    private VBox generateTopMenuBar() {
        VBox v = new VBox();
        Menu file = new Menu("File");
        //FILE---

        //PRINT TABLE
        MenuItem printTable = new MenuItem("Print Table");
        printTable.setOnAction(e -> printTableContents());

        //READ FILE
        MenuItem readFile = new MenuItem("Read File");
        readFile.setOnAction((event -> readJsonFile(new File("resources/list.json"))));

        //SAVE FILE
        MenuItem save = new MenuItem("Save File");
        save.setOnAction(actionEvent -> saveTableViewAsJson());

        //Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction((e) -> Platform.exit());

        file.getItems().addAll(printTable, readFile, save, exitItem);

        //ABOUT---
        Menu about = new Menu("About");
        MenuItem aboutItem = new MenuItem("About Shopping list App");
        aboutItem.setOnAction(actionEvent -> {
            generateAboutDialog();
        });



        about.getItems().addAll(aboutItem);
        MenuBar menubar = new MenuBar();
        menubar.getMenus().addAll(file,about);

        v.getChildren().add(menubar);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(20);
        return v;
    }

    private void readJsonFile(File file) {
        JSONFileData fileData = new JSONParser().read(file);
        JSONArray array = ((JSONArray) fileData.getComponent("shoppingList"));
        table.getItems().clear();

        array.getData().forEach(linkedList -> {
            System.out.println(String.valueOf(linkedList.get("product")) + Integer.valueOf(String.valueOf(linkedList.get("quantity"))));
            table.getItems().add(new Product(String.valueOf(linkedList.get("product")), Integer.valueOf(String.valueOf(linkedList.get("quantity")))));
        });
    }

    private void saveTableViewAsJson() {
        JSONParser parser = new JSONParser();
        JSONFileData data = new JSONFileData();
        JSONArray array = new JSONArray("shoppingList");

        table.getItems().forEach(product -> {
            ArrayList<JSONItem> itemList = new ArrayList<>();
            itemList.add(new JSONItem("product", product.getName()));
            itemList.add(new JSONItem("quantity", product.getQuantity()));
            array.add(itemList);
        });
        data.add(array);
        parser.write(data, new File("resources/list.json"));
    }

    private void generateFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
        }
    }

    private void generateAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Author - Tuukka Juusela");
        alert.setHeaderText("This program is part of school project\nin Tampere University of Applied Sciences.");
        alert.setContentText("Used to save shopping list as a json file.");
        alert.showAndWait();
    }

    void printTableContents() {
        table.getItems().forEach(p -> System.out.println("PRODUCT: " + p.getName() + " QUANTITY: " + p.getQuantity()));
    }
}
