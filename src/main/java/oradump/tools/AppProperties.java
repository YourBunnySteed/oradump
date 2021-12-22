package main.java.oradump.tools;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class AppProperties {

  private static Properties properties;

  static {
    try {
      properties = new Properties();
      properties.load(AppProperties.class.getResourceAsStream("/app.properties"));
      checkProperties();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private AppProperties() {
    throw new UnsupportedOperationException("Utility class!");
  }

  public static Properties getProperties() {
    return properties;
  }

  public static String getProperty(String property) {
    return properties.getProperty(property);
  }

  private static void checkProperties() {
    for (Object key : properties.keySet()) {
      if (properties.getProperty(key.toString()) == null
          && key.toString().startsWith("db.")) {
        throw new IllegalStateException(String.format("Property %s is empty", key));
      }
    }

    if (new File(properties.getProperty("out.path")).exists()) {
      throw new IllegalStateException(
          String.format("Path %s already exists", properties.getProperty("out.path")));
    }
  }
}
