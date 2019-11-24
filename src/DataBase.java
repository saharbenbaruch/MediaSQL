import com.sun.jdi.FloatType;
import oracle.jdbc.OracleTypes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static java.sql.JDBCType.FLOAT;
import static java.sql.JDBCType.REAL;

public class DataBase {
    Connection conn;
    String username;
    String password;
    static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    String DB_URL;

    public DataBase(String connection, String db_username, String db_pass) {

        username = db_username;
        password = db_pass;
        DB_URL = connection;

    }

    public void connect(){
        try {
            try {
                Class.forName(JDBC_DRIVER);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            conn = DriverManager.getConnection(DB_URL, username, password);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("connecting to database..");
    }

    public void disconnect(){
        if (conn !=null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void fileToDataBase(String path) {
        if (conn==null)
            connect();

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(path));
            while (true) {
                try {
                    if (!((line = br.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // use comma as separator
                if (conn != null) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void calculateSimilarity() {
        if (conn==null)
            connect();
        try {

            // get all MID s from table to ArrayList
            PreparedStatement stat = conn.prepareStatement("select MID from MEDIAITEMS");
            ResultSet rs = stat.executeQuery();
            ArrayList<Long> listOfMID = new ArrayList();
            //add all MIT from table to listOfMID
            while (rs.next())
                listOfMID.add(rs.getLong("MID"));

            //System.out.println(listOfMID);


            //get max distance
            int maxDist = 0;
            CallableStatement getmax = conn.prepareCall("{? = call MaximalDistance()}");
            getmax.registerOutParameter(1, oracle.jdbc.OracleTypes.NUMBER);
            getmax.execute();
            maxDist = getmax.getInt(1);

            //start calculate all pairs
            for (int i = 0; i < listOfMID.size(); i++) {
                for (int j = 0; j < listOfMID.size(); j++) {
                    if (i != j) {
                        CallableStatement callStat = conn.prepareCall("{? = call SimCalculation(?,?,?)}");
                        //set arguments of SimCalculation()
                        callStat.setLong(2, listOfMID.get(i));
                        callStat.setLong(3, listOfMID.get(j));
                        callStat.setInt(4, maxDist);
                        callStat.registerOutParameter(1, OracleTypes.FLOAT);
                        callStat.execute();
                        float sim = callStat.getFloat(1);
                        callStat.close();
                        PreparedStatement ps = conn.prepareStatement("INSERT INTO SIMILARITY (MID1,MID2,SIMILARITY) VALUES (?,?,?)");
                        //insert to SIMILARITY table
                        ps.setLong(1, listOfMID.get(i));
                        ps.setLong(2, listOfMID.get(j));
                        ps.setFloat(3, sim);
                        ps.executeUpdate();
                        conn.commit();
                        ps.close();
                    }
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public class Pair implements Comparable {
        String title;
        float sim;

        public Pair(String title, float sim) {
            this.title = title;
            this.sim = sim;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public float getSim() {
            return sim;
        }

        public void setSim(float sim) {
            this.sim = sim;
        }

        @Override
        public int compareTo(Object o) {
            Float mySim=getSim();
            Float otherSim=((Pair)o).getSim();
            if (mySim>otherSim)
                return 1;

            else if (mySim<otherSim)
                return -1;

            else
                return 0;

        }
    }

    public void printSimilarItems(long MID) {
        if (conn==null)
            connect();
        try {

            // get all MID s from table to ArrayList
            PreparedStatement stat = conn.prepareStatement("select MID from MEDIAITEMS");
            ResultSet rs = stat.executeQuery();
            ArrayList<Long> listOfMID = new ArrayList();
            //add all MIT from table to listOfMID
            while (rs.next())
                listOfMID.add(rs.getLong("MID"));

            //System.out.println(listOfMID);


            //get max distance
            int maxDist = 0;
            CallableStatement getmax = conn.prepareCall("{? = call MaximalDistance()}");
            getmax.registerOutParameter(1, oracle.jdbc.OracleTypes.NUMBER);
            getmax.execute();
            maxDist = getmax.getInt(1);

            //start calculate all pairs

            ArrayList<Pair> results = new ArrayList<>();

            for (int j = 0; j < listOfMID.size(); j++) {
                if (MID != j) {

                    CallableStatement callStat = conn.prepareCall("{? = call SimCalculation(?,?,?)}");
                    //set arguments of SimCalculation()
                    callStat.setLong(2, MID);
                    callStat.setLong(3, listOfMID.get(j));
                    callStat.setInt(4, maxDist);
                    callStat.registerOutParameter(1, OracleTypes.FLOAT);
                    callStat.execute();
                    float sim = callStat.getFloat(1);
                    callStat.close();

                    if (sim >= 0.3) {
                        PreparedStatement statGetTitle = conn.prepareStatement("select TITLE from MEDIAITEMS where MID=" + listOfMID.get(j));
                        ResultSet rsTitle = statGetTitle.executeQuery();
                        rsTitle.next();
                        String title=rsTitle.getString("TITLE");

                        statGetTitle.close();
                        Pair tempPair = new Pair(title, sim);
                        results.add(tempPair);
                    }
                }
            }


            //sort

            results.sort(Pair::compareTo);

            //print
            for (int i=0; i<results.size();i++){
                System.out.println(results.get(i).title+" "+results.get(i).getSim());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
