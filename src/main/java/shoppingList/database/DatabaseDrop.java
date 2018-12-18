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
public class DatabaseDrop {
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
     * Main method of the class. Generates Dialog window for user to choose table to drop.
     */
    public void drop() {
        Optional<ObservableList<FileItem>> table = getTables();
        if(table.isPresent()) {
            Optional<String> chosenTable = generateFilePicker(table.get());
            chosenTable.ifPresent(this::dropTable);
        }
    }

    /**
     * Drops table from H2 Database using given parameter tableName.
     * @param tableName Table to drop.
     */
    private void dropTable(String tableName) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stmt = conn.createStatement();

            String sqlDrop = "DROP TABLE " + tableName+";";
            stmt.executeUpdate(sqlDrop);
            new DatabaseDialogs().generateDialog("H2 Drop Table", "Dropped table " + tableName + " successfully");

            stmt.close();
            conn.close();
        } catch(SQLException se) {
            se.printStackTrace();
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
    }

    /**
     * Generates Dialog window with TableView in it with Tables from Database in it. User can choose one table from it.
     * @param tables List of tables in Database.
     * @return Optional of name of the chosen table.
     */
    private Optional<String> generateFilePicker(ObservableList<FileItem> tables) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png"))));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        dialog.setTitle("Choose table to drop");
        TableView<FileItem> tableBox = generateTable(tables);
        dialog.getDialogPane().setContent(tableBox);
        ButtonType chooseButtonType = new ButtonType("Drop", ButtonBar.ButtonData.OK_DONE);
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
     * Generates TableView for generateFileChooser() using given ObservableList.
     * @param tables Tables in database.
     * @return TableView generated using given parameter.
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
     * Creates ObservableList<FileItem> from tables in H2 Database.
     * @return ObservableList created from tables in database.
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
            new DatabaseDialogs().generateSQLError("Something went wrong when dropping table\n" +
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
