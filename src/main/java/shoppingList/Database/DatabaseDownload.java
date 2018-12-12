package shoppingList.Database;


import java.sql.*;
import java.util.ArrayList;

public class DatabaseDownload {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/TuukkaLister;";
    static final String USER = "sa";
    static final String PASS = "";

    public void download() {
        getTables().forEach(table -> System.out.println(table));
    }

    private ArrayList<String> getTables() {
        ArrayList<String> tables = new ArrayList<>();
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
                tables.add(tableResult.getString("TABLE_NAME"));
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
}
