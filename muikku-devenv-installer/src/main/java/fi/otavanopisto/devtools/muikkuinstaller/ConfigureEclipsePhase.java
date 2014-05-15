package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConfigureEclipsePhase extends AbstractEclipseConfigurationPhase {
  
  @Override
  public String getName() {
    return "Configure Eclipse";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File eclipseFolder = getEclipseFolder(context);
    File eclipseWorkspaceFolder = getEclipseWorkspaceFolder(context, true);
    File eclipseDropinsFolder = getEclipseDropinsFolder(eclipseFolder);
    File eclipseExecutable = getEclipseExecutable(context, eclipseFolder);
    File jbossHome = getJBossHome(context);
    
    File yellowSheepJar = installYellowSheep(eclipseDropinsFolder);
    try {
      importPreferences(context, eclipseFolder, eclipseExecutable, eclipseWorkspaceFolder);
      setMavenAnnotationProcessing(context, eclipseFolder, eclipseExecutable, eclipseWorkspaceFolder);
      createServer(context, eclipseFolder, eclipseExecutable, jbossHome, eclipseWorkspaceFolder);
    } finally {
      yellowSheepJar.delete();
    }
  }

  private void importPreferences(InstallerContext context, File eclipseFolder, File eclipseExecutable, File eclipseWorkspaceFolder) throws IOException, InterruptedException, URISyntaxException {
    String taskId = startTask("Import Preferences");
    try {
      String preferencesFile = new File(getClass().getResource("/resources/preferences.epf").toURI()).getAbsolutePath();
      
      List<String> arguments = new ArrayList<String>();
      arguments.add("-nosplash");
      arguments.add("-application");
      arguments.add("yellow-sheep-project.yellow-sheep-project");
      arguments.add("-data");
      arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
      arguments.add("-import-preferences");
      arguments.add(preferencesFile);
      runEclipse(context, eclipseFolder, eclipseExecutable, arguments);  
    } finally {
      endTask(taskId);
    }
  }
  
  private void setMavenAnnotationProcessing(InstallerContext context, File eclipseFolder, File eclipseExecutable, File eclipseWorkspaceFolder) throws IOException, InterruptedException {
    String taskId = startTask("Changing m2e settings");
    try {
      List<String> arguments = new ArrayList<String>();
      arguments.add("-nosplash");
      arguments.add("-application");
      arguments.add("yellow-sheep-project.yellow-sheep-project");
      arguments.add("-data");
      arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
      arguments.add("-m2e-annotation-processing-mode");
      arguments.add("maven_execution");
      runEclipse(context, eclipseFolder, eclipseExecutable, arguments);  
    } finally {
      endTask(taskId);
    }
  }
  
  private void createServer(InstallerContext context, File eclipseFolder, File eclipseExecutable, File jbossHome, File eclipseWorkspaceFolder) throws IOException, InterruptedException {
    String taskId = startTask("Adding JBoss AS 7.1 server");
    try {
      List<String> arguments = new ArrayList<String>();
      arguments.add("-nosplash");
      arguments.add("-application");
      arguments.add("yellow-sheep-project.yellow-sheep-project");
      arguments.add("-data");
      arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
      arguments.add("-configure-jbossas71");
      arguments.add(jbossHome.getAbsolutePath());
      runEclipse(context, eclipseFolder, eclipseExecutable, arguments);
    } finally {
      endTask(taskId);
    }
  }
  
}
