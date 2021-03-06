package by.pavka.library.model;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DBConnectionPool {
  private static final Logger LOGGER = LogManager.getLogger(DBConnectionPool.class);
  private static final DBConnectionPool instance = new DBConnectionPool();
  private static final int TIMEOUT = 3;

  private BlockingQueue<Connection> connections;
  private BlockingQueue<Connection> usedConnections;
  private int maxSize;
  private String url;
  private String login;
  private String password;

  private DBConnectionPool() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("database");
    String driver = resourceBundle.getString("driver");
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException e) {
      LOGGER.fatal("Database Driver not found");
      throw new LibraryFatalException("Database Driver not found", e);
    }
    url = resourceBundle.getString("url");
    login = resourceBundle.getString("user");
    password = resourceBundle.getString("pass");
    String num = resourceBundle.getString("connections");
    String maxNum = resourceBundle.getString("max_connections");
    int connectionsNumber;
    try {
      connectionsNumber = Integer.parseInt(num);
    } catch (NumberFormatException e) {
      LOGGER.error("Integer parcing failed");
      connectionsNumber = 5;
    }
    try {
      maxSize = Integer.parseInt(maxNum);
    } catch (NumberFormatException e) {
      LOGGER.error("Max size connection pool not parsed");
      maxSize = connectionsNumber;
    }
    connections = new ArrayBlockingQueue<>(maxSize);
    usedConnections = new ArrayBlockingQueue<>(maxSize);
    for (int i = 0; i < connectionsNumber; i++) {
      connections.add(createConnection(url, login, password));
    }
  }

  public static DBConnectionPool getInstance() {
    return instance;
  }

  private Connection createConnection(String url, String login, String password) {
    try {
      return DriverManager.getConnection(url, login, password);
    } catch (SQLException e) {
      LOGGER.fatal("Database Connection not created");
      throw new LibraryFatalException("Database Connection not created", e);
    }
  }

  private synchronized boolean addConnection() {
    if (connections.size() >= maxSize) {
      return false;
    }
    try {
      connections.add(DriverManager.getConnection(url, login, password));
    } catch (SQLException e) {
      LOGGER.error("Can't add a connection");
      return false;
    }
    return true;
  }

  public Connection obtainConnection() {
    // TODO
    Connection connection = pickConnection();
    if (connection == null) {
      if (addConnection()) {
        connection = pickConnection();
      }
    }
    if (connection != null) {
      usedConnections.offer(connection);
    }
    return connection;
  }

  private Connection pickConnection() {
    Connection connection = null;
    try {
      connection = connections.poll(TIMEOUT, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return connection;
  }

  public boolean releaseConnection(Connection connection) {
    if (connection != null) {
      connections.offer(connection);
      return usedConnections.remove(connection);
    }
    return false;
  }

  public void disconnect() {
    for (Connection connection : usedConnections) {
      releaseConnection(connection);
    }
    for (Connection connection :  connections) {
      try {
        connection.close();
      } catch (SQLException throwables) {
        LOGGER.error("Connection not closed while destroying the app");
      }
    }
    Enumeration<Driver> drivers = DriverManager.getDrivers();
    Driver driver;
    while(drivers.hasMoreElements()) {
      try {
        driver = drivers.nextElement();
        DriverManager.deregisterDriver(driver);
      } catch (SQLException ex) {
        LOGGER.error("Driver not deregistered while destroying the app");
      }
    }
  }
}
