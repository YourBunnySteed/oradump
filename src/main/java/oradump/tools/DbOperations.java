package main.java.oradump.tools;

import static main.java.oradump.tools.ResultWriter.writeTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.java.oradump.objects.Column;
import main.java.oradump.objects.Table;

public class DbOperations {

  private static final Logger LOGGER = Logger.getLogger(DbOperations.class.getName());
  static Connection connection = JDBCConnector.getConnection();

  private DbOperations() {
    throw new IllegalStateException("Utility class");
  }

  public static List<Table> getTables() {
    ArrayList<Table> result = new ArrayList<>();
    String userCondition = AppProperties.getProperty("tables.condition");
    try (Statement statement = connection.createStatement()) {
      String query = String.format("""
          SELECT S.OWNER,
          U.TABLE_NAME,
          U.NUM_ROWS,
          SUM(S.BYTES) as "SIZE"
          FROM DBA_SEGMENTS S
          JOIN USER_TABLES U ON U.TABLE_NAME = S.SEGMENT_NAME
          WHERE S.SEGMENT_TYPE IN ('TABLE', 'TABLE PARTITION', 'TABLE SUBPARTITION')
          AND NUM_ROWS > 0
          AND U.STATUS = 'VALID'
          AND U.TEMPORARY = 'N'
          %s
          GROUP BY S.OWNER, U.TABLE_NAME, U.NUM_ROWS
          ORDER BY NUM_ROWS
          """, userCondition == null ? "" : "AND " + userCondition);
      ResultSet resultSet = statement.executeQuery(query);
      while (resultSet.next()) {
        result.add(tableMapper(resultSet));

      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "SQLException while getting list of Tables:", e);
    }

    LOGGER.log(Level.FINE, "Getting Tables is done! {0} tables processed", result.size());
    return result;

  }

  private static Table tableMapper(ResultSet resultSet) throws SQLException {
    String owner = resultSet.getString("OWNER");
    String tableName = resultSet.getString("TABLE_NAME");
    int numRows = resultSet.getInt("NUM_ROWS");
    Long size = resultSet.getLong("SIZE");

    LOGGER.log(Level.FINER, "Table {1} is mapped: owner={0}; numrows={2}, size={3}",
        new Object[]{owner, tableName, numRows, size});
    return new Table(owner, tableName, numRows, size);
  }

  public static List<Column> getColumns(Table table) {
    ArrayList<Column> columns = new ArrayList<>();
    try (Statement statement = connection.createStatement()) {
      String query = String.format("""
          SELECT COLUMN_NAME,
          DATA_TYPE
          FROM ALL_TAB_COLUMNS
          WHERE TABLE_NAME = '%s'""", table.getName());
      ResultSet resultSet = statement.executeQuery(query);
      while (resultSet.next()) {
        columns.add(columnMapper(resultSet));
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "SQLException while getting list of Tables:", e);
    }
    return columns;
  }

  private static Column columnMapper(ResultSet resultSet) throws SQLException {
    String name = resultSet.getString("COLUMN_NAME");
    String type = resultSet.getString("DATA_TYPE");

    return new Column(name, type);
  }


  public static void dumpTableData(Table table) {
    try (Statement statement = connection.createStatement()) {
      String query = String.format("SELECT %s from %s",
          table.getColumnsForSelect(), table.getQuotedName());
      writeTable(statement.executeQuery(query), table);
      LOGGER.log(Level.FINE, "Table {0} dumped", table.getName());
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "SQLException while dumping table:", e);
    }
  }


}
