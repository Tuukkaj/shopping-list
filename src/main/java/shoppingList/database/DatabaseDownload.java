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
import shoppingList.FileItem;

import java.sql.*;
import java.util.Optional;

/**
 * Handles Dropping table from H2 Database. Url to H2Database is "jdbc:h2:~/TuukkaLister".
 *
 * @author Tuukka Juusela
 * @version 2018.1412
 * @since 1.8
 */
public class DatabaseDownload {
    /**
     * Driver of H2 database.
     */
    private final String JDBC_DRIVER = "org.h2.Driver";
    /**'
     * Database url for this application.
     */
    private final String DB_URL = "jdbc:h2:~/TuukkaLister;";
    /**
     * User information for Database.
     */
    private final String USER = "sa";
    /**
     * Password for Database.
     */
    private final String PASS = "";

    /**
     * Asks user which table to Download from H2 database and returns chosen table as Optional<ObservableList<FileItem>>
     * @return ObservableList created from table in H2 database.
     */
    public Optional<ObservableList<Product>> download() {
        Optional<ObservableList<FileItem>> table = getTables();
        if(table.isPresent()) {
            Optional<String> chosenTable = generateFilePicker(table.get());
            if (chosenTable.isPresent()) {
                new DatabaseDialogs().generateDialog("H2 Database", "Download from database was successful");
                return Optional.ofNullable(loadTable(chosenTable.get()));
            }
        }

        return Optional.empty();
    }

    /**
     * Loads table from H2 database and turns it to ObservableList.
     * @param tableName table to load from database.
     * @return ObservableList created from table in database.
     */
    private ObservableList<Product> loadTable(String tableName) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL,USER,PASS);

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

    /**
     * Generates dialog window that asks user to choose table to load.
     * @param tables ObservableList of tables in H2 database. Used to create TableView.
     * @return Table in database that user chose to download.
     */
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

    /**
     * Generates TableView from given ObservableList.
     * @param tables Tables in H2 database.
     * @return TableView generated from given ObservableList.
     */
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

    /**
     * Loads table names from H2 database and converts them to ObservableList.
     * @return Optional ObservableList containing table names in H2 database.
     */
    private Optional<ObservableList<FileItem>> getTables() {
        ObservableList<FileItem> tables = FXCollections.observableArrayList();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stmt = conn.createStatement();
            ResultSet tableResult = stmt.executeQuery("SHOW TABLES;");
            while (tableResult.next()) {
                tables.add(new FileItem(tableResult.getString("TABLE_NAME")));
            }

            stmt.close();
            conn.close();
        } catch(SQLException se) {
            new DatabaseDialogs().generateSQLError("Something went wrong when downloading your table\n" +
                    "Make sure that you don't have other connections to H2 database.");
            se.printStackTrace();
            return Optional.empty();
        } catch(Exception e) {
            new DatabaseDialogs().generateError();
            e.printStackTrace();
        } finally {
            try{
                if(stmt!=null) stmt.close();
            } catch(SQLException se2) {
                se2.printStackTrace();
            }
            try {
                if(conn!=null) conn.close();
            } catch(SQLException se){
                se.printStackTrace();
            }
        }

        return Optional.of(tables);
    }
}
