package shoppingList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

class Components {
    private TableView<Product> table;

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
        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setMinWidth(60);
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Product");
        nameColumn.setMinWidth(260);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableView<Product> table = new TableView<>();
        table.setItems(createObservableList());
        table.getColumns().addAll(quantityColumn,nameColumn);
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
