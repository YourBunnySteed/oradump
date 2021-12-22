package main.java.oradump.tools;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import com.opencsv.CSVWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import main.java.oradump.objects.Table;

public class ResultWriter {

  private static final Logger LOGGER = Logger.getLogger(ResultWriter.class.getName());

  private ResultWriter() {
    throw new UnsupportedOperationException("Utility class!");
  }

  private static String generateHeader(Table table) {
    String columns = table.getColumns().stream().map(column ->
        column.getQuotedName().toLowerCase(Locale.ROOT)
    ).collect(Collectors.joining(","));
    return String.format("\\copy %s(%s) from STDIN WITH(FORMAT CSV, NULL '', FORCE_NULL(%s));%n",
        table.getName().toLowerCase(),
        columns,
        columns);
  }

  private static void addLinkToResult(Table table) {
    Path filepath = Paths.get(AppProperties.getProperty("out.path"), "result.sql");
    try (FileWriter fw = new FileWriter(filepath.toString(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)) {
      out.println(String.format("\\ir DATA_TABLES/%s.csv", table.getName()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void writeTable(ResultSet resultSet, Table table) {
    String outPath = AppProperties.getProperty("out.path");
    File dataDirectory = new File(Paths.get(outPath, "DATA_TABLES").toString());
    if (!dataDirectory.exists() && dataDirectory.mkdirs()) {
      LOGGER.log(Level.SEVERE, "Cant create folder");
    }
    Path filepath = Paths.get(outPath, "DATA_TABLES", table.getName() + ".sql");
    try {
      Files.writeString(filepath, generateHeader(table), CREATE, APPEND);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Cant write header:", e);
    }

    try (CSVWriter writer = new CSVWriter(new FileWriter(filepath.toString(), true))) {
      writer.writeAll(resultSet, false, false, true);
      addLinkToResult(table);
    } catch (IOException | SQLException e) {
      LOGGER.log(Level.SEVERE, "Cant write data:", e);
    }
  }

}
