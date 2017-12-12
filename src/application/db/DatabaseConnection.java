package application.db;
import java.sql.*;
public class DatabaseConnection {
	private DatabaseConnection() {}
	private static final class SingletonHelper {
		private static final DatabaseConnection INSTANCE = new DatabaseConnection();
	}
	public static DatabaseConnection getInstance() {
		return SingletonHelper.INSTANCE;
	}
	private Connection conn;
	public Connection getConnection() {
		return conn;
	}
	public void connectDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp","root","");
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	public void disconnectDatabase() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
