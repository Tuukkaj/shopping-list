package shoppingList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import shoppingList.database.DatabaseDownload;
import shoppingList.database.DatabaseDrop;
import shoppingList.database.DatabaseUpload;
import shoppingList.dropbox.DropboxDownload;
import shoppingList.dropbox.DropboxUpload;
import shoppingList.json.JSONHandler;
import shoppingList.json.ShoppingListReader;


import java.io.*;
import java.util.Optional;

/**
 * Main functionality of JavaFx. Holds component creation and functionality of JavaFx components.
 *
 * @author Tuukka Juusela
 * @version 2018.0212
 * @since 1.8
 */
class Components {
    /**
     * TableView of the JavaFx. User uses this to list his/hers products.
     */
    private TableView<Product> table;
    /**
     * Stage in which components are.
     */
    private Stage stage;
    /**
     * the main JavaFx class.
     */
    private Application application;

    /**
     * Generates BorderPane for ShoppingListMain's scene.
     * @return ready BorderPane which has all necessary components.
     */
     BorderPane generateBorderPanel() {
        BorderPane borderPane = new BorderPane();

        borderPane.setTop(generateTopMenuBar());
        table = generateCenterTable();
        borderPane.setCenter(table);
        HBox hbox = new HBox();

        hbox.getChildren().addAll(generateAddButton(),generateModifyButton(), generateRemoveButton());

        hbox.setSpacing(10);
        hbox.setPadding(new Insets(5));

        BorderPane innerPane = new BorderPane();
        innerPane.setCenter(hbox);
        hbox.setAlignment(Pos.BOTTOM_CENTER);

        borderPane.setBottom(innerPane);


        return borderPane;
    }

    /**
     * Generates Add button. Has OnActionListener for Adding new row to TableView.
     * @return Add button.
     */
    private Button generateAddButton() {
        Button b = new Button("Add");
        b.setOnAction(e -> tableViewAdd());

        return b;
    }

    /**
     * Generates Remove button. Has OnActionListener for removing focused row in TableView.
     * @return Remove button.
     */
    private Button generateRemoveButton() {
         Button b = new Button("Remove");
         b.setOnAction(e -> table.getItems().remove(table.getFocusModel().getFocusedCell().getRow()));

         return b;
    }

    /**
     * Generates Modify button. Has OnActionListener for modifying focused row in TableView.
     * @return Modify button.
     */
    private Button generateModifyButton() {
         Button b = new Button("Modify");
         b.setOnAction(event -> tableViewModify());

         return b;
    }

    /**
     * Generates observableList for Components TableView. Creates one ready example item to observableList.
     * @return observableList with one example item in it.
     */
    private ObservableList<Product> createObservableList() {
         ObservableList<Product> products = FXCollections.observableArrayList();
         products.add(new Product("Example item",1));

         return products;
    }

    /**
     * Generates TableView for generateBorderPanel().
     *
     * TableView has observableList of products for the application. TableView has alot of functionality added to it.
     * Rows can be cycled with tabulator, rows can
     * be modified by double clicking them, new row can be inserted with triple click. Pressing delete removes
     * selected row. Pressing Insert adds new to end.
     * @return generated tableView.
     */
    private TableView<Product> generateCenterTable() {
        ObservableList<Product> products = createObservableList();
        //QUALITY COLUMN
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");

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
            if(e.getClickCount() == 3) {
                table.getItems().add(new Product("-", 1));
            }
        });
        table.setItems(products);
        table.getColumns().addAll(quantityColumn,nameColumn);
        table.setEditable(true);

        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case TAB:
                    System.out.println("TABI");
                    tableViewTab(nameColumn, quantityColumn);
                    break;
                case DELETE:
                    System.out.println("DELETE");
                    tableViewDelete();
                    break;
                case INSERT:
                    System.out.println("INSERT");
                    tableViewAdd();
                    break;
            }
        });

        stage.widthProperty().addListener(e -> updateTableColumnWidth());

        return table;
    }

    /**
     * Generates VBox which holds menu for generateBorderPanel().
     *
     * Vbox has to Menus. File and Help.
     *
     * File contains Print Table, Upload Dropbox, Read File, Save As, Save File and Exit.
     * Print table prints tableView's content to console. Will be removed in final releases.
     * Read File reads .json file and turns file's content to TableView.
     * Save as asks users where to TableView and saves it.
     * Save File saves file to current location with name "list.json"
     * Exit exits the Program.
     *
     * Help contains Help and about Shopping list App.
     * Help opens dialog window which contains shortcuts for the and tricks for using the program.
     * About Shopping List App opens dialog window which has information about the program.
     *
     * @return Ready made Menubar Vbox.
     */
    private VBox generateTopMenuBar() {
        VBox v = new VBox();
        //FILE---
        Menu file = new Menu("File");


        //READ FILE
        MenuItem readFile = new MenuItem("Read File");
        readFile.setOnAction((event -> {
            File chosenFile = generateFileChooserRead();
            if(chosenFile != null) {
                table.getItems().clear();
                table.getItems().addAll(new ShoppingListReader().read(chosenFile));
            }
        }));
        readFile.setAccelerator(KeyCombination.keyCombination("SHORTCUT+R"));
        //SAVE FILE
        MenuItem save = new MenuItem("Save File");
        save.setAccelerator(KeyCombination.keyCombination("SHORTCUT+S"));
        save.setOnAction(actionEvent -> new JSONHandler().saveTableViewAsJson("list.json", table));
        //SAVE FILE AS
        MenuItem saveAs = new MenuItem("Save As");
        saveAs.setAccelerator(KeyCombination.keyCombination("SHORTCUT+SHIFT+S"));
        saveAs.setOnAction(actionEvent -> new JSONHandler().saveAsJSON(generateFileChooserSave(), table));
        //Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction((e) -> Platform.exit());
        file.getItems().addAll(readFile, saveAs, save, exitItem);

        //DROPBOX---
        Menu dropBoxMenu = new Menu("Dropbox");
        MenuItem importItem = new MenuItem("Download from Dropbox");
        importItem.setOnAction(event -> new DropboxDownload().download(application, table));
        importItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT+D"));
        MenuItem uploadItem = new MenuItem("Upload to Dropbox");
        uploadItem.setOnAction(actionEvent -> new DropboxUpload().uploadCurrentListToDropbox(application, table));
        uploadItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT+U"));
        dropBoxMenu.getItems().addAll(importItem,uploadItem);

        //H2 DATABASE
        Menu database = new Menu("database");
        MenuItem uploadDatabase = new MenuItem("Upload to H2 database");
        uploadDatabase.setOnAction(e -> new DatabaseUpload().upload(table.getItems()));
        uploadDatabase.setAccelerator(KeyCombination.keyCombination("SHORTCUT+Q"));
        MenuItem downloadDatabase = new MenuItem("Download from H2 database");
        downloadDatabase.setOnAction(event -> {
            Optional<ObservableList<Product>> databaseDownload = new DatabaseDownload().download();
            if(databaseDownload.isPresent()) {
                table.getItems().clear();
                table.getItems().addAll(databaseDownload.get());
            }
        });
        downloadDatabase.setAccelerator(KeyCombination.keyCombination("SHORTCUT+W"));
        MenuItem dropTable = new MenuItem("Drop H2 Table");
        dropTable.setOnAction(e -> new DatabaseDrop().drop());
        database.getItems().addAll(uploadDatabase, downloadDatabase, dropTable);

        //HELP--
        Menu help = new Menu("Help");
        MenuItem helpItem = new MenuItem("Help");
        helpItem.setOnAction(actionEvent -> generateHelpDialog());
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(actionEvent -> generateAboutDialog());
        MenuItem devGithubItem = new MenuItem("Developers Github");
        devGithubItem.setOnAction(e -> application.getHostServices().showDocument("https://github.com/Tuukkaj"));
        help.getItems().addAll(helpItem,aboutItem, devGithubItem);


        MenuBar menubar = new MenuBar();
        menubar.getMenus().addAll(file, dropBoxMenu, database, help);

        v.getChildren().add(menubar);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(20);
        return v;
    }

    /**
     * Generates FileChooser to read json file.
     * @return file user chose to read.
     */
    private File generateFileChooserRead() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("choose JSON File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"));
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Generates FileChooser to save json file.
     * @return file user chose to save json file to.
     */
    private File generateFileChooserSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("choose JSON File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON files", "*.json"));
        return fileChooser.showSaveDialog(stage);
    }

    /**
     * Generates Help dialog. Dialog holds information to help user use the program.
     */
    private void generateHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Tips:\n" +
                "Use Add, Modify and Delete buttons to alter the list\n" +
                "Save file saves file at current location of the program." +
                "");
        alert.setContentText("Mouse tips: \n" +
                "Modifying - You can modify cells by double clinking them\n" +
                "Adding - You can add new row by triple clicking empty cell");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icons/shoppingListIcon.png")));

        alert.showAndWait();
    }

    /**
     * Generates about dialog. Dialog holds information about the program.
     */
    private void generateAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Author - Tuukka Juusela");
        alert.setHeaderText("This program is part of school project\nin Tampere University of Applied Sciences.");
        alert.setContentText("Used to save shopping list as a json file.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icons/shoppingListIcon.png")));

        alert.showAndWait();
    }

    /**
     * Constructor. Sets stage and application class variables.
     * @param stage current javafx stage.
     * @param app current javafx application.
     */
    Components(Stage stage, Application app) {
        this.stage = stage;
        this.application = app;
    }

    /**
     * Used to go through the TableView.
     *
     * When this is method is called it goes to next modifiable cell and starts modifying it.
     * @param nameColumn Currently selected nameColumn.
     * @param quantityColumn Currently selected quantityColumn.
     */
    private void tableViewTab(TableColumn nameColumn, TableColumn quantityColumn) {
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

    /**
     * Removes currently selected row from tableView.
     */
    private void tableViewDelete() {
        int pos = table.getSelectionModel().getSelectedIndex();
        table.getItems().remove(pos);
    }

    /**
     * Adds new product to TableView.
     */
    private void tableViewAdd() {
         table.getItems().add(new Product("-",1));
    }

    /**
     * Modifies tableViews row.
     */
    private void tableViewModify() {
        TablePosition pos = table.getFocusModel().getFocusedCell();

        if(table.getEditingCell() != null && table.getEditingCell().getColumn() == 0) {
            table.edit(pos.getRow(), table.getColumns().get(1));
        } else {
            table.edit(pos.getRow(), table.getColumns().get(0));
        }
    }

    /**
     * Updates table's width and tableColumns width to fit program.
     */
    void updateTableColumnWidth() {
        table.setMaxWidth(stage.getWidth());
        table.getColumns().get(0).setPrefWidth(table.getWidth()*0.2f);
        table.getColumns().get(1).setPrefWidth(table.getWidth()*0.78f);
    }
}
