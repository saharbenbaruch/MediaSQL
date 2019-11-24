import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {


    public static void main(String[] args){
        //System.out.println("hi")
        DataBase db= new DataBase("jdbc:oracle:thin:@132.72.65.216:1521:oracle","benbsaha","abcd");
        db.connect();
      //  db.fileToDataBase(Paths.get("").toAbsolutePath() +"\\films.csv");
       // db.calculateSimilarity();
        db.printSimilarItems(5);
    }
}
