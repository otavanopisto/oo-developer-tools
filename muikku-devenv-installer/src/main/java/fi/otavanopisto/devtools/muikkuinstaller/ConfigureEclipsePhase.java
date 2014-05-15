package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConfigureEclipsePhase extends AbstractEclipseConfigurationPhase {
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    System.out.println("Configuring Eclipse");
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
    System.out.println(" > Importing preferences");
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
  }
  
  private void setMavenAnnotationProcessing(InstallerContext context, File eclipseFolder, File eclipseExecutable, File eclipseWorkspaceFolder) throws IOException, InterruptedException {
    System.out.println(" > Changing m2e settings");
    List<String> arguments = new ArrayList<String>();
    arguments.add("-nosplash");
    arguments.add("-application");
    arguments.add("yellow-sheep-project.yellow-sheep-project");
    arguments.add("-data");
    arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
    arguments.add("-m2e-annotation-processing-mode");
    arguments.add("maven_execution");
    runEclipse(context, eclipseFolder, eclipseExecutable, arguments);  
  }
  
  private void createServer(InstallerContext context, File eclipseFolder, File eclipseExecutable, File jbossHome, File eclipseWorkspaceFolder) throws IOException, InterruptedException {
    System.out.println(" > Adding JBoss AS 7.1 server");
    List<String> arguments = new ArrayList<String>();
    arguments.add("-nosplash");
    arguments.add("-application");
    arguments.add("yellow-sheep-project.yellow-sheep-project");
    arguments.add("-data");
    arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
    arguments.add("-configure-jbossas71");
    arguments.add(jbossHome.getAbsolutePath());
    runEclipse(context, eclipseFolder, eclipseExecutable, arguments);
  }
  
}
