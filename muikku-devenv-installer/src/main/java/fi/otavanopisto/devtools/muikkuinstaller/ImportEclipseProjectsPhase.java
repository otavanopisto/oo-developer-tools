package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ImportEclipseProjectsPhase extends AbstractEclipseConfigurationPhase {

  @Override
  public String getName() {
    return "Import Eclipse Projects";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File eclipseFolder = getEclipseFolder(context);
    File eclipseWorkspaceFolder = getEclipseWorkspaceFolder(context, true);
    File eclipseDropinsFolder = getEclipseDropinsFolder(eclipseFolder);
    File eclipseExecutable = getEclipseExecutable(context, eclipseFolder);
    File sourceFolder = context.getFileOption(InstallerContext.SOURCE_FOLDER);
    
    File yellowSheepJar = installYellowSheep(eclipseDropinsFolder);
    try {
      importProjects(context, eclipseWorkspaceFolder, eclipseFolder, eclipseExecutable, sourceFolder);
      addWebAppToJBoss(context, eclipseWorkspaceFolder, eclipseFolder, eclipseExecutable);
    } finally {
      yellowSheepJar.delete();
    }
  }
  
  protected void importProjects(InstallerContext context, File eclipseWorkspaceFolder, File eclipseFolder, File eclipseExecutable, File sourceFolder) throws IOException, InterruptedException {
    List<String> poms = new ArrayList<String>();
    poms.add("muikku-bom/pom.xml");
    poms.add("pom.xml");      
    poms.add("muikku-security/pom.xml"); 
    poms.add("muikku-persistence/pom.xml");
    poms.add("muikku-plugin/pom.xml");
    poms.add("muikku-plugin-core/pom.xml");
    poms.add("muikku-rest/pom.xml");
    poms.add("muikku-core/pom.xml ");
    poms.add("muikku/pom.xml");
    poms.add("muikku-core-plugins/pom.xml");
    poms.add("muikku-jndi-mail-bridge-plugin/pom.xml");
    poms.add("muikku-facebook-oauth/pom.xml");
    poms.add("muikku-google-oauth/pom.xml");
    poms.add("muikku-h2db-plugin/pom.xml");
    poms.add("muikku-dummy-mail-bridge-plugin/pom.xml");
    poms.add("muikku-school-data-mock/pom.xml");
    
    String importTaskId = startTask("Importing projects to Eclipse");
    try {
      importProjects(context, eclipseWorkspaceFolder, eclipseFolder, eclipseExecutable, sourceFolder, poms.toArray(new String[0]));
    } finally {
      endTask(importTaskId);
    }
  }

  private void importProjects(InstallerContext context, File eclipseWorkspaceFolder, File eclipseFolder, File eclipseExecutable, File sourceFolder, String... poms) throws IOException, InterruptedException {
    List<String> absolutePoms = new ArrayList<String>();
    for (String pom : poms) {
      absolutePoms.add(sourceFolder.getAbsolutePath() + '/' + pom);
    }

    List<String> arguments = new ArrayList<String>();
    arguments.add("-nosplash");
    arguments.add("-application");
    arguments.add("yellow-sheep-project.yellow-sheep-project");
    arguments.add("-data");
    arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
    arguments.add("-m2e-import-poms");
    arguments.add(StringUtils.join(absolutePoms, ','));
    runEclipse(context, eclipseFolder, eclipseExecutable, arguments);
  }

  private void addWebAppToJBoss(InstallerContext context, File eclipseWorkspaceFolder, File eclipseFolder, File eclipseExecutable) throws IOException, InterruptedException {
    List<String> arguments = new ArrayList<String>();
    arguments.add("-nosplash");
    arguments.add("-application");
    arguments.add("yellow-sheep-project.yellow-sheep-project");
    arguments.add("-data");
    arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
    arguments.add("-import-jbossas71-project");
    arguments.add("fi.muikku.muikku");
    
    runEclipse(context, eclipseFolder, eclipseExecutable, arguments);      
  }
}
