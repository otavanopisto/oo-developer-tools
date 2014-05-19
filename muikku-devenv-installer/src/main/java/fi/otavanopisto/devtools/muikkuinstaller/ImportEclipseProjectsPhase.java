package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    LinkedHashMap<String, String> projects = new LinkedHashMap<String, String>();
    projects.put("fi.muikku.bom", "muikku-bom/pom.xml");
    projects.put("fi.muikku.parent", "pom.xml");      
    projects.put("fi.muikku.security", "muikku-security/pom.xml"); 
    projects.put("fi.muikku.persistence", "muikku-persistence/pom.xml");
    projects.put("fi.muikku.plugin", "muikku-plugin/pom.xml");
    projects.put("fi.muikku.plugin-core", "muikku-plugin-core/pom.xml");
    projects.put("fi.muikku.rest", "muikku-rest/pom.xml");
    projects.put("fi.muikku.core", "muikku-core/pom.xml ");
    projects.put("fi.muikku.muikku", "muikku/pom.xml");
    projects.put("fi.muikku.core-plugins", "muikku-core-plugins/pom.xml");
    projects.put("fi.muikku.dummy-mail-bridge-plugin", "muikku-jndi-mail-bridge-plugin/pom.xml");
    projects.put("fi.muikku.facebook-oauth-plugin", "muikku-facebook-oauth/pom.xml");
    projects.put("fi.muikku.google-oauth-plugin", "muikku-google-oauth/pom.xml");
    projects.put("fi.muikku.h2db-plugin", "muikku-h2db-plugin/pom.xml");
    projects.put("fi.muikku.jndi-mail-bridge-plugin", "muikku-dummy-mail-bridge-plugin/pom.xml");
    projects.put("fi.muikku.school-data-mock", "muikku-school-data-mock/pom.xml");
    
    for (String project : projects.keySet()) {
      String taskId = startTask("Import project " + project);
      try {
        String pom = projects.get(project);
        importProjects(context, eclipseWorkspaceFolder, eclipseFolder, eclipseExecutable, sourceFolder, pom);
        updateProjects(context, eclipseWorkspaceFolder, eclipseFolder, eclipseExecutable, project);
      } finally {
        endTask(taskId);
      }
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

  private void updateProjects(InstallerContext context, File eclipseWorkspaceFolder, File eclipseFolder, File eclipseExecutable, String... projects) throws IOException, InterruptedException {
    List<String> arguments = new ArrayList<String>();
    arguments.add("-nosplash");
    arguments.add("-application");
    arguments.add("yellow-sheep-project.yellow-sheep-project");
    arguments.add("-data");
    arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
    arguments.add("-update-projects");
    arguments.add(StringUtils.join(projects, ','));
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
