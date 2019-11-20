import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DataBase {
    Connection conn;
    String username;
    String password;
    static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    //static final String DB_URL="jdbc:oracle:thin:@localhost:1521:orcl";
    String DB_URL;

    public DataBase(String connection, String db_username, String db_pass) {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        username = db_username;
        password = db_pass;
        DB_URL = connection;
        try {
            conn = DriverManager.getConnection(DB_URL, username, password);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("connecting to database..");
    }

    public void fileToDataBase(String path) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            try {
                br = new BufferedReader(new FileReader(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    if (!((line = br.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // use comma as separator
                if (conn!= null) {
                    String[] itemDetails = line.split(cvsSplitBy);
                    String itemTitle = itemDetails[0];
                    String itemYear = itemDetails[1];
                    PreparedStatement stat = conn.prepareStatement("INSERT INTO MEDIAITEMS (TITLE,PROD_YEAR) VALUES (?,?)");
                    stat.setString(1, itemTitle);
                    stat.setString(2, itemYear);
                    stat.executeUpdate();
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void calculateSimilarity(){
        try {
            PreparedStatement stat= conn.prepareStatement("select MID from MEDIAITEMS");
            ResultSet rs=stat.executeQuery();
            System.out.println(rs.);

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
