package shoppingList.database;


import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import shoppingList.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Handles Dropping table from H2 Database. Url to H2Database is "jdbc:h2:~/TuukkaLister".
 *
 * @author Tuukka Juusela
 * @version 2018.1412
 * @since 1.8
 */
public class DatabaseUpload {
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
     * Main method of the class. Uploads given parameter ObservableList to H2Database. Asks user for name of the table
     * in database.
     * @param list
     */
    public void upload(ObservableList<Product> list) {
        Optional<String> tableName = generateTableNameDialog();
        if(tableName.isPresent()) {
            executeUpload(list, tableName.get());
        }
    }

    /**
     * Generates dialog window that ask user for table's name.
     * @return Optional name of the table.
     */
    private Optional<String> generateTableNameDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("H2 Upload");
        dialog.setHeaderText("Enter table's name\n\nNOTICE: If table exists with same name it will be overwritten");

        dialog.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png"))));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("shoppingList/icons/h2.png")));
        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tableNameField = new TextField();
        tableNameField.setPromptText("shoppinglist-1-1-2019");


        grid.add(new Label("Table name:"), 0, 0);
        grid.add(tableNameField, 1, 0);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        tableNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(tableNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new String(tableNameField.getText());
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        return result;
    }

    /**
     * Uploads given parameter ObservableList to database with name given in parameter tableName.
     * @param list ObservableList to upload to database.
     * @param tableName Table's name in database.
     */
    private void executeUpload(ObservableList<Product> list, String tableName) {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            System.out.println("Creating table in given database...");
            stmt = conn.createStatement();
            System.out.println("TABLE: " + tableName);
            String dropSql = "DROP TABLE IF EXISTS " + tableName;
            stmt.executeUpdate(dropSql);
            String sql =  "CREATE TABLE "+ tableName +
                    "(id INTEGER not NULL, " +
                    " product VARCHAR(255), " +
                    " amount INTEGER, " +
                    " PRIMARY KEY ( id ))";
            stmt.executeUpdate(sql);

            StringBuilder sqlBuilder = new StringBuilder();
            for(int i = 0; i < list.size(); i++) {
                if(list.get(i).getName().equals("-") && (list.get(i).getQuantity() == 1)) {
                } else {
                    System.out.println("INSERT INTO "+tableName+" VALUES (" +(i+1)+ ", '"+list.get(i).getName()+"', "+list.get(i).getQuantity()+"); ");
                    sqlBuilder.append("INSERT INTO "+tableName+" VALUES (" +(i+1)+ ", '"+list.get(i).getName()+"', "+list.get(i).getQuantity()+"); ");
                }
            }
            stmt.executeUpdate(sqlBuilder.toString());
            System.out.println("Created table in given database...");
            new DatabaseDialogs().generateDialog("H2 Upload", "Upload to H2 was successful");
            stmt.close();
            conn.close();
        } catch(SQLException se) {
            new DatabaseDialogs().generateSQLError("Something went wrong when uploading your table\n" +
                    "Make sure that you don't have other connections to H2 database.");
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
        System.out.println("Goodbye!");
    }
}

