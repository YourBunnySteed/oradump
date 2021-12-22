package main.java.oradump.objects;

public record Column(String name, String type) {

  public String getQuotedName() {
    return "\"" + name + "\"";
  }

  public boolean isBinary() {
    return this.type.equals("BLOB");
  }

}
