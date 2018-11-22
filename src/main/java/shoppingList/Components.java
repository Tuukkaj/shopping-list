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
import javafx.util.StringConverter;

class Components {
    private TableView<Product> table;
    private ObservableList<Product> products;

    TableView<Product> getTable() {
        return table;
    }

    void printTableContents() {
        table.getItems().forEach(p -> System.out.println("PRODUCT: " + p.getName() + " QUANTITY: " + p.getQuantity()));
    }

     BorderPane generateBorderPanel() {
        BorderPane borderPane = new BorderPane();

        borderPane.setTop(generateVBox());
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

    public ObservableList<Product> createObservableList() {
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

    private VBox generateVBox() {
        VBox v = new VBox();
        Menu file = new Menu("File");
        //FILE---

        MenuItem printTable = new MenuItem("Print table");
        printTable.setOnAction(e -> printTableContents());
        file.getItems().add(printTable);

        //READ FILE
        MenuItem readFile = new MenuItem("Read file");
        readFile.setOnAction((event -> System.out.println("File menu opens")));

        //Exit
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction((e) -> Platform.exit());

        file.getItems().addAll(readFile, exitItem);

        //ABOUT---
        Menu about = new Menu("About");
        MenuItem aboutItem = new MenuItem("About Shopping list App");
        aboutItem.setOnAction(e -> System.out.print("Shopping menu opens"));


        about.getItems().addAll(aboutItem);
        MenuBar menubar = new MenuBar();
        menubar.getMenus().addAll(file,about);

        v.getChildren().add(menubar);
        v.setAlignment(Pos.CENTER);
        v.setSpacing(20);
        return v;
    }
}
