package org.jetbrains.jdba.jdbc;

import org.jetbrains.jdba.core.DBErrorRecognizer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;



/**
 * @author Leonid Bushuev from JetBrains
 */
public class BaseHyperSonicCase {

  protected static final String HSQL_URL = "jdbc:hsqldb:mem:mymemdb?user=SA";

  protected static Driver ourHSDriver;

  protected static DBErrorRecognizer ourErrorRecognizer = new BaseErrorRecognizer();


  @BeforeClass
  public static void instantiateDriver() {
    System.setProperty("java.awt.headless", "true");

    try {
      //noinspection unchecked
      Class<Driver> driverClass = (Class<Driver>) Class.forName("org.hsqldb.jdbc.JDBCDriver");
      ourHSDriver = driverClass.newInstance();
      DriverManager.registerDriver(ourHSDriver);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterClass
  public static void deregisterDriver() throws SQLException {
    assert ourHSDriver != null;
    DriverManager.deregisterDriver(ourHSDriver);
  }


  protected static Connection obtainConnection() {
    assert ourHSDriver != null;
    final Connection connection;
    try {
      connection = ourHSDriver.connect(HSQL_URL, new Properties());
      connection.setAutoCommit(true);
    }
    catch (SQLException e) {
      throw new RuntimeException("Failed to get an HSQL connection: "+e.getMessage(), e);
    }
    return connection;
  }


  protected JdbcInterSession openSession() {
    Connection connection = obtainConnection();
    JdbcInterSession session = new JdbcInterSession(null, ourErrorRecognizer, connection, true);
    return session;
  }

}