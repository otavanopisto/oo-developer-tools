package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ImportEclipseProjectsPhase extends AbstractEclipseConfigurationPhase {
  
  private static final String[] MUIKKU_PROJECTS = new String[]{
    "muikku-bom/pom.xml",
    "pom.xml",
    "muikku-core/pom.xml",
    "muikku-persistence/pom.xml",
    "muikku-security/pom.xml",
    "muikku-plugin-core/pom.xml",
    "muikku-plugin/pom.xml",
    "muikku-rest/pom.xml",
    "muikku/pom.xml",
    "muikku-core-plugins/pom.xml",
    "muikku-dummy-mail-bridge-plugin/pom.xml",
    "muikku-jndi-mail-bridge-plugin/pom.xml",
    "muikku-facebook-oauth/pom.xml",
    "muikku-google-oauth/pom.xml",
    "muikku-h2db-plugin/pom.xml",
    "muikku-school-data-mock/pom.xml"
  };
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File eclipseFolder = getEclipseFolder(context);
    File eclipseWorkspaceFolder = getEclipseWorkspaceFolder(context, true);
    File eclipseDropinsFolder = getEclipseDropinsFolder(eclipseFolder);
    File eclipseExecutable = getEclipseExecutable(context, eclipseFolder);
    File sourceFolder = context.getFileOption(InstallerContext.SOURCE_FOLDER);
    
    File yellowSheepJar = installYellowSheep(eclipseDropinsFolder);
    try {
      List<String> poms = new ArrayList<String>();
      for (String muikkuProject : MUIKKU_PROJECTS) {
        poms.add(sourceFolder.getAbsolutePath() + '/' + muikkuProject);
      }
      
      List<String> arguments = new ArrayList<String>();
//      arguments.add("-nosplash");
      arguments.add("-application");
      arguments.add("yellow-sheep-project.yellow-sheep-project");
      arguments.add("-data");
      arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
      arguments.add("-m2e-import-poms");
      arguments.add(StringUtils.join(poms, ','));

      runEclipse(context, eclipseFolder, eclipseExecutable, arguments);      
    } finally {
      yellowSheepJar.delete();
    }
  }
  
}
