import java.sql.Connection;
import java.sql.DriverManager;

public class TestJdbc {
    public static void main(String[] args) {

        String jdbcUrl = "jdbc:mysql://localhost:3306/cosmoport?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "root";
        try {
            System.out.println("Connecting to database: " + jdbcUrl);

            Connection myConn = DriverManager.getConnection(jdbcUrl, user, pass);

            System.out.println("Connection success!");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
