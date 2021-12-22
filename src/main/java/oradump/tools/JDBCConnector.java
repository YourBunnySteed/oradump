package main.java.oradump.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class JDBCConnector {

  private static final Logger LOGGER = Logger.getLogger(JDBCConnector.class.getName());
  private static Connection connection = null;

  static {
    try {
      connection = DriverManager.getConnection(String.format("%s:@%s:%s:%s",
              AppProperties.getProperty("db.driver"),
              AppProperties.getProperty("db.host"),
              AppProperties.getProperty("db.port"),
              AppProperties.getProperty("db.sid")),
          AppProperties.getProperty("db.user"),
          AppProperties.getProperty("db.password"));
      LOGGER.log(Level.FINE, "Connection established");
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Error occured while connecting to database:", e);
    }
  }

  private JDBCConnector() {
    throw new UnsupportedOperationException("Utility class!");
  }

  public static Connection getConnection() {
    return connection;
  }


}
