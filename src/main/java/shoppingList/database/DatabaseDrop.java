package shoppingList.database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import shoppingList.Product;

import java.sql.*;
import java.util.Optional;

public class DatabaseDrop {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/TuukkaLister;";
    static final String USER = "sa";
    static final String PASS = "";

    public void drop() {
        Optional<String> chosenTable = generateFilePicker(getTables());
        if(chosenTable.isPresent()) {
            loadTable(chosenTable.get());
        }
    }

    private ObservableList<Product> loadTable(String tableName) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();

            String sqlSelectTable = "SELECT * FROM " + tableName + ";";
            ResultSet selectResult = stmt.executeQuery(sqlSelectTable);

            while (selectResult.next()) {
                products.add(new Product(selectResult.getString("PRODUCT"), selectResult.getInt("AMOUNT")));
            }

            stmt.close();
            conn.close();
        } catch(SQLException se) {
            se.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(stmt!=null) stmt.close();
            } catch(SQLException se2) {
            }
            try {
                if(conn!=null) conn.close();
            } catch(SQLException se){
                se.printStackTrace();
            }
        }

        return products;
    }

    private Optional<String> generateFilePicker(ObservableList<FileItem> tables) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png"))));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        dialog.setTitle("Choose table to load");
        TableView<FileItem> tableBox = generateTable(tables);
        dialog.getDialogPane().setContent(tableBox);
        ButtonType chooseButtonType = new ButtonType("Choose", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, chooseButtonType);

        Node chooseButton = dialog.getDialogPane().lookupButton(chooseButtonType);
        chooseButton.setDisable(true);

        tableBox.setOnMouseClicked(e -> chooseButton.setDisable(tableBox.getSelectionModel().getSelectedCells().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == chooseButtonType) {
                return tableBox.getItems().get(tableBox.getSelectionModel().getSelectedIndex()).getName();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        return result;
    }

    private TableView<FileItem> generateTable(ObservableList<FileItem> tables) {
        TableView<FileItem> tableView = new TableView<>();

        TableColumn<FileItem, String> fileColumn = new TableColumn<>("Table");
        fileColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        fileColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fileColumn.setPrefWidth(200);
        tableView.getColumns().addAll(fileColumn);
        tableView.setItems(tables);

        return tableView;
    }

    private ObservableList<FileItem> getTables() {
        ObservableList<FileItem> tables = FXCollections.observableArrayList();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();
            ResultSet tableResult = stmt.executeQuery("SHOW TABLES;");
            while (tableResult.next()) {
                tables.add(new FileItem(tableResult.getString("TABLE_NAME")));
            }

            // STEP 4: Clean-up environment
            stmt.close();
            conn.close();
        } catch(SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch(Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try{
                if(stmt!=null) stmt.close();
            } catch(SQLException se2) {
            } // nothing we can do
            try {
                if(conn!=null) conn.close();
            } catch(SQLException se){
                se.printStackTrace();
            } //end finally try
        } //end try
        System.out.println("Goodbye!");

        return tables;
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
