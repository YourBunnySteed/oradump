package main.java.oradump;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import main.java.oradump.objects.Table;
import main.java.oradump.tools.DbOperations;

public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) throws IOException {
    LogManager.getLogManager()
        .readConfiguration(Main.class.getResourceAsStream("/logging.properties"));

    List<Table> tables = DbOperations.getTables();
    for (Table table : tables) {
      table.setColumns(DbOperations.getColumns(table));
      LOGGER.log(Level.FINEST, "Table {0} columns is mapped",
          table.getName());
    }

    for (Table table : tables) {
      DbOperations.dumpTableData(table);
    }

  }
}

