package main.java.oradump.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Table {

  private final String schema;
  private final int rowcount;
  private final Long size;
  List<Column> columns = new ArrayList<>();
  private final String name;

  public Table(String schema, String name, int rowcount, Long size) {
    this.schema = schema;
    this.name = name;
    this.rowcount = rowcount;
    this.size = size;
  }

  public Long getSize() {
    return size;
  }

  public String getName() {
    return name;
  }

  public String getColumnsForSelect() {
    return this.columns.stream().map(column ->
        (column.isBinary() ? "'(BLOB)' as " : "") +
            column.getQuotedName()).collect(Collectors.joining(","));
  }

  public List<Column> getColumns() {
    return columns;
  }

  public void setColumns(List<Column> columns) {
    this.columns = columns;
  }

  public String getQuotedName() {
    return "\"" + this.name + "\"";
  }

}
