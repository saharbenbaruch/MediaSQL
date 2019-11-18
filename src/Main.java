public class Main {


    public static void main(String[] args){
        //System.out.println("hi")
        DataBase db= new DataBase("jdbc:oracle:thin:@132.72.65.216:1521:oracle","benbsaha","abcd");
        db.fileToDataBase("D:\\documents\\users\\benbsaha\\Downloads\\films.csv");
    }
}
