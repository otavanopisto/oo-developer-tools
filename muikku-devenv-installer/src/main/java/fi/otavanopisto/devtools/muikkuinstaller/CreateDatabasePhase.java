package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

public class CreateDatabasePhase extends InstallerPhase {
  
  private static final String CREATE_SQL = 
     "create database %s default charset utf8;\n" + 
     "create user '%s'@'localhost' identified by '%s';\n" +
     "grant all privileges on %s.* to '%s'@'localhost';\n" + 
     "flush privileges;\n";

  @Override
  public String getName() {
    return "Create Database";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    if (testMySQLCommand()) {
      String adminUsername = getMySQLAdminUsername(context);
      String adminPassword = getMySQLAdminPassword(context);
      String user = getMySQLUser(context);
      String password = getMySQLPassword(context);
      String database = getMySQLDatabase(context);
      
      String sql = String.format(CREATE_SQL, database, user, password, database, user); 
      
      ProcessBuilder processBuilder = new ProcessBuilder("mysql", "-u" + adminUsername, "-p" + adminPassword);
      Process process = processBuilder.start();
      
      OutputStream outputStream = process.getOutputStream();
      try {
        IOUtils.write(sql, outputStream);
      } finally {
        outputStream.flush();
        outputStream.close();
      }
      
      process.waitFor();
    } else {
      System.out.println("Could not find MySQL executable");
    }
  }

  protected boolean testMySQLCommand() {
    try {
      return runCommand(null, "mysql", "--version") == 0;
    } catch (Exception e) {
      return false;
    }
  }
  

}
