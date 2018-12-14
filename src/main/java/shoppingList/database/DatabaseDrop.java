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

import java.io.File;
import java.sql.*;
import java.util.Optional;

public class DatabaseDrop {
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:~/TuukkaLister;";
    private static final String USER = "sa";
    private static final String PASS = "";

    public void drop() {
        Optional<ObservableList<FileItem>> table = getTables();
        if(table.isPresent()) {
            Optional<String> chosenTable = generateFilePicker(table.get());
            chosenTable.ifPresent(this::dropTable);
        }
    }

    private void dropTable(String tableName) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();

            String sqlDrop = "DROP TABLE " + tableName+";";
            stmt.executeUpdate(sqlDrop);

            stmt.close();
            conn.close();
        } catch(SQLException se) {
            se.printStackTrace();
        } catch(Exception e) {
            new DatabaseErrorDialogs().generateError();
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

    private Optional<ObservableList<FileItem>> getTables() {
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

            stmt.close();
            conn.close();
        } catch(SQLException se) {
            new DatabaseErrorDialogs().generateSQLError("Something went wrong when dropping table\n" +
                    "Make sure that you don't have other connections to H2 database.");
            se.printStackTrace();
            return Optional.empty();
        } catch(Exception e) {
            new DatabaseErrorDialogs().generateError();
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
        System.out.println("Goodbye!");


        return Optional.of(tables);
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
