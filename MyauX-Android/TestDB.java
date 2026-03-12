import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://students-data-mysql-studentsdatamysql.c.aivencloud.com:11529/students_data_mysql" +
                     "?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC&connectTimeout=8000&socketTimeout=8000";
        String user = "avnadmin";
        String password = "AVNS_X8ZLHz18HP2o0JB2eGv";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("SUCCESS: Connected to the database with MySQL 5.1.49 Driver!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILURE: " + e.getMessage());
        }
    }
}
