package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class ConfigureJBossPhase extends InstallerPhase {

  private static final String JBOSS_CLI_EXECUTABLE_LINUX = "jboss-cli.sh";
  private static final String JBOSS_CLI_EXECUTABLE_MAC = "jboss-cli.sh";
  private static final String JBOSS_CLI_EXECUTABLE_WINDOWS = "jboss-cli.bat";

  private static final String JBOSS_STANDALONE_EXECUTABLE_LINUX = "standalone.sh";
  private static final String JBOSS_STANDALONE_EXECUTABLE_MAC = "standalone.sh";
  private static final String JBOSS_STANDALONE_EXECUTABLE_WINDOWS = "standalone.bat";
  
  @Override
  public String getName() {
    return "Configure JBoss";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File jbossHome = getJBossHome(context);
    File jbossBin = new File(jbossHome, "bin");
    File jbossCliExecutable = new File(jbossBin, getJBossCliExecutable());
    File jbossStandaloneExecutable = new File(jbossBin, getJBossStandaloneExecutable());
    
    String databaseName = getMySQLDatabase(context);
    String databaseUser = getMySQLUser(context);
    String databasePassword = getMySQLPassword(context);
    String connectionUrl = "jdbc:mysql://localhost:3306/" + databaseName;
    File sourceFolder = getSourceFolder(context);
    File pluginLibrariesProperties = new File(sourceFolder, "/muikku-data/muikku-plugin-libraries.properties");
    File pluginRepositoriesProperties = new File(sourceFolder, "/muikku-data/muikku-pluginrepositories.properties");
    File dataXml = new File(sourceFolder, "/muikku-data/muikku-data.xml");
    
    String moduleInstallTaskId = startTask("Installing MySQL Module into JBoss");
    try {
      File moduleFolder = new File(jbossHome,  StringUtils.join(Arrays.asList("modules", "com", "mysql", "jdbc", "main"), File.separatorChar));
      copyResourceToFile("module.xml", new File(moduleFolder, "module.xml"));
      copyResourceToFile("mysql-connector-java-5.1.18-bin.jar", new File(moduleFolder, "mysql-connector-java-5.1.18-bin.jar"));
    } finally {
      endTask(moduleInstallTaskId);
    }
    
    String standaloneTaskId = startTask("Configuring JBoss");
    try {
      if (jbossCliExecutable.exists()) {
        CliScriptBuilder cliScriptBuilder = new CliScriptBuilder();
        cliScriptBuilder
          .addConnect()
          .addBatch()
          .addSystemPropety("muikku-plugin-libraries", pluginLibrariesProperties.getAbsolutePath())
          .addSystemPropety("muikku-plugin-repositories", pluginRepositoriesProperties.getAbsolutePath())
          .addSystemPropety("muikku-data", dataXml.getAbsolutePath())
          .addSystemPropety("muikku-deus-nex-machina-password", "change-this")
          .addXAJDBCDriver("mysql", "mysql", "com.mysql.jdbc", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource")
          .addDataSource("muikku", "mysql", connectionUrl, "java:/jdbc/muikku", databaseUser, databasePassword, true, false, true, false, false, false)
          .addDataSource("muikku-h2", "h2", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", "java:/jdbc/muikku-h2", "", "", true, false, false, false, false, false)
          .addRunBatch()
          .addShutdown();
        
        File cliScriptFile = File.createTempFile("jboss-cli", "script");
        try {
          FileOutputStream cliOutputStream = new FileOutputStream(cliScriptFile);
          try {
            IOUtils.write(cliScriptBuilder.toString(), cliOutputStream);
          } finally {
            cliOutputStream.flush();
            cliOutputStream.close();
          }
          
          ProcessBuilder standaloneProcessBuilder = new ProcessBuilder(jbossStandaloneExecutable.getAbsolutePath());
          standaloneProcessBuilder.environment().put("NOPAUSE", "true");
          Process standaloneProcess = standaloneProcessBuilder.start();
          try {
            // Wait for JBoss to start
            waitForProcessOutput(".*JBoss.*started.*", standaloneProcess.getInputStream());
            ProcessBuilder cliProcessBuilder = new ProcessBuilder(jbossCliExecutable.getAbsolutePath(), "--file=" + cliScriptFile.getAbsolutePath());
            cliProcessBuilder.environment().put("NOPAUSE", "true");
            Process cliProcess = cliProcessBuilder.start();
            waitForProcessOutput(".*success.*", cliProcess.getInputStream());
          } finally {
            standaloneProcess.destroy();
          }
        } finally {
          cliScriptFile.delete();
        }
      } else {
        System.out.println("Could not find JBoss Cli executable");
      }
    } finally {
      endTask(standaloneTaskId);
    }
  }

  private void waitForProcessOutput(String pattern, InputStream inputStream) throws IOException {
    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    try {
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      try {
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
          if (line.matches(pattern)) {
            return;
          }
        }
      } finally {
        bufferedReader.close();
      }
    } finally {
      inputStreamReader.close();
    }
  }
  
  private String getJBossCliExecutable() {
    if (SystemUtils.IS_OS_LINUX) {
      return JBOSS_CLI_EXECUTABLE_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return JBOSS_CLI_EXECUTABLE_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return JBOSS_CLI_EXECUTABLE_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }
  
  private String getJBossStandaloneExecutable() {
    if (SystemUtils.IS_OS_LINUX) {
      return JBOSS_STANDALONE_EXECUTABLE_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return JBOSS_STANDALONE_EXECUTABLE_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return JBOSS_STANDALONE_EXECUTABLE_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }
  
  private class CliScriptBuilder {

    public CliScriptBuilder addConnect() {
      return addCommand("connect");
    }
    
    public CliScriptBuilder addShutdown() {
      return addCommand(":shutdown");
    }
    
    public CliScriptBuilder addBatch() {
      return addCommand("batch");
    }

    public CliScriptBuilder addRunBatch() {
      return addCommand("run-batch");
    }
    
    public CliScriptBuilder addSystemPropety(String name, String value) {
      return addCommand(String.format("/system-property=%s:add(value=%s)", name, value));
    }
    
    public CliScriptBuilder addXAJDBCDriver(String driver, String name, String moduleName, String className) {
      return addCommand(String.format("/subsystem=datasources/jdbc-driver=%s:add(driver-name=\"%s\",driver-module-name=\"%s\",driver-xa-datasource-class-name=\"%s\")", driver, name, moduleName, className));
    }
    
    public CliScriptBuilder addDataSource(String name, String driver, String connectionUrl, String jndiName, String user, String password, boolean enabled, boolean useCcm, boolean jta, boolean validateOnMatch, boolean backgroundValidation, boolean sharePreparedStatements) {
      String command = String.format("data-source add --name=%s --driver-name=%s --connection-url=%s --jndi-name=%s --use-ccm=%b --jta=%b --validate-on-match=%b --background-validation=%b --share-prepared-statements=%b", name, driver, connectionUrl, jndiName, useCcm, jta, validateOnMatch, backgroundValidation, sharePreparedStatements);
      if (StringUtils.isNotBlank(user)) {
        command += String.format(" --user-name=%s", user); 
      }
      
      if (StringUtils.isNotBlank(password)) {
        command += String.format(" --password=%s", password); 
      }
      
      addCommand(command);
      
      if (enabled) {
        addCommand(String.format("data-source enable --name=%s", name));
      }
      
      return this;
    }
    
    public CliScriptBuilder addCommand(String command) {
      if (scriptBuilder.length() > 0) {
        scriptBuilder.append('\n');
      }
      
      scriptBuilder.append(command);
      
      return this;
    }
    
    @Override
    public String toString() {
      return scriptBuilder.toString();
    }
    
    private StringBuilder scriptBuilder = new StringBuilder();
  }
  
}
