import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    Connection conn;
    String username;
    String password;
    static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    //static final String DB_URL="jdbc:oracle:thin:@localhost:1521:orcl";
    String DB_URL;

    public DataBase(String connection, String db_username, String pass)  {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        username=db_username;
        password=pass;
        DB_URL=connection;
        try {
            conn= DriverManager.getConnection(DB_URL,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("connecting to database..");
    }

    public void fileToDataBase(String path){

    }
}
