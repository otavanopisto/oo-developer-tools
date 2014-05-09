package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigureEclipsePhase extends AbstractEclipseConfigurationPhase {
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File eclipseFolder = getEclipseFolder(context);
    File eclipseWorkspaceFolder = getEclipseWorkspaceFolder(context, true);
    File eclipsePluginsFolder = getEclipsePluginsFolder(eclipseFolder);
    File eclipseExecutable = getEclipseExecutable(context, eclipseFolder);
    
    File yellowSheepJar = installYellowSheep(eclipsePluginsFolder);
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
      yellowSheepJar.delete();
    }
          
    
  }
  
}
