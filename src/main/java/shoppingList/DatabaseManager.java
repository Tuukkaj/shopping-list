package shoppingList;


import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/TuukkaLister;";
    static final String USER = "sa";
    static final String PASS = "";

    public void upload(ObservableList<Product> list) {
        executeUpload(list, "ostoslista");
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

