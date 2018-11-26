package shoppingList;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
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
    private Application application;

     BorderPane generateBorderPanel() {
        BorderPane borderPane = new BorderPane();

        borderPane.setTop(generateTopMenuBar());
        table = generateCenterTable();
        borderPane.setCenter(table);
        Button add = generateAddButton();
        Button remove = generateRemoveButton();
        BorderPane innerPane = new BorderPane();
        add.setAlignment(Pos.CENTER_LEFT);
        remove.setAlignment(Pos.CENTER_RIGHT);
        innerPane.setLeft(add);
        innerPane.setRight(remove);
        borderPane.setBottom(innerPane);


        return borderPane;
    }

    private Button generateAddButton() {
        Button b = new Button("Add");
        b.setOnAction(e -> table.getItems().add(new Product("-", 1)));

        return b;
    }

    private Button generateRemoveButton() {
         Button b = new Button("Remove selected");
         b.setOnAction(e -> {
             table.getItems().remove(table.getFocusModel().getFocusedCell().getRow());
         });

         return b;
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
                table.getItems().add(new Product("-", 1));
            }
        });
        table.setItems(products);
        table.getColumns().addAll(quantityColumn,nameColumn);
        table.setEditable(true);

        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.TAB) {
                TablePosition pos = table.getFocusModel().getFocusedCell();
                if(pos.getTableColumn().equals(nameColumn)) {
                    if(pos.getRow() < table.getItems().size() -1) {
                        table.edit(pos.getRow()+1, quantityColumn);
                        table.getFocusModel().focus(pos.getRow()+1, quantityColumn);
                        table.getSelectionModel().select(pos.getRow()+1, quantityColumn);
                    } else {
                        table.edit(0, quantityColumn);
                        table.getFocusModel().focus(0, quantityColumn);
                        table.getSelectionModel().select(0, quantityColumn);
                    }
                } else if(pos.getTableColumn().equals(quantityColumn)) {
                    table.getFocusModel().focus(pos.getRow(), nameColumn);
                    table.edit(pos.getRow(), nameColumn);
                    table.getSelectionModel().select(pos.getRow(), nameColumn);
                }
            }
        });

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
        uploadItem.setOnAction(actionEvent -> new DropboxUpload().uploadCurrentListToDropbox(application));
        uploadItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT+D"));

        //READ FILE
        MenuItem readFile = new MenuItem("Read File");
        readFile.setOnAction((event -> {
            File chosenFile = generateFileChooser();
            if(chosenFile != null) {
                table.getItems().clear();
                table.getItems().addAll(new JSONHandler().readJsonFile(chosenFile));
            }
        }));
        readFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+R"));
        //SAVE FILE
        MenuItem save = new MenuItem("Save File");
        save.setAccelerator(KeyCombination.keyCombination("SHORTCUT+S"));
        save.setOnAction(actionEvent -> new JSONHandler().saveTableViewAsJson("list.json", table));
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

    private File generateFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("choose JSON File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        return selectedFile;
    }

    private void generateAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Author - Tuukka Juusela");
        alert.setHeaderText("This program is part of school project\nin Tampere University of Applied Sciences.");
        alert.setContentText("Used to save shopping list as a json file.");
        alert.showAndWait();
    }

    //Remove in final release.
    private void printTableContents() {
        table.getItems().forEach(p -> System.out.println("PRODUCT: " + p.getName() + " QUANTITY: " + p.getQuantity()));
    }

    Components(Stage stage, Application app) {
        this.stage = stage;
        this.application = app;
    }
}
