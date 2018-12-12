package shoppingList;


import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class DatabaseManager {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/TuukkaLister;";
    static final String USER = "sa";
    static final String PASS = "";

    public void upload(ObservableList<Product> list) {
        System.out.println(generateTableNameDialog());
        executeUpload(list, "ostoslista");
    }

    public Optional<String> generateTableNameDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("H2 Upload");
        dialog.setHeaderText("Enter table's name\n\nNOTICE: If table exists with same name it will be overwritten");

        dialog.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("icons/h2.png"))));
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("icons/h2.png")));
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
    }
}

