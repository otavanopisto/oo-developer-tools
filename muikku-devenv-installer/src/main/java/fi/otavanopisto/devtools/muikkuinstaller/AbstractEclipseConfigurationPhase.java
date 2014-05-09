package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

public abstract class AbstractEclipseConfigurationPhase extends InstallerPhase {
  
  private static final String YELLOW_SHEEP_JAR = "yellow-sheep-project_1.0.0.201405091449.jar";
  private static final String ECLIPSE_EXECUTABLE_WINDOWS = "eclipsec.exe";
  private static final String ECLIPSE_EXECUTABLE_LINUX = "eclipse";
  private static final String ECLIPSE_EXECUTABLE_MAC = "eclipse";
  
  protected File getEclipseFolder(InstallerContext context) {  
    File eclipseFolder = context.getFileOption(InstallerContext.ECLIPSE_FOLDER, "Please enter Eclipse install directory", getBaseFolder(context) + "/eclipse", true);
    if (!eclipseFolder.exists()) {
      throw new ConfigurationException("Could not find Eclipse install folder");
    }
    
    return eclipseFolder;
  }

  protected File getEclipsePluginsFolder(File eclipseFolder) {  
    return new File(eclipseFolder, "plugins");
  }
  
  protected File getEclipseWorkspaceFolder(InstallerContext context, boolean createMissing) {
    File eclipseWorkspaceFolder = context.getFileOption(InstallerContext.ECLIPSE_WORKSPACE_FOLDER, "Please enter Eclipse workspace folder", getBaseFolder(context) + "/workspace", true);
    if (createMissing && !eclipseWorkspaceFolder.exists()) {
      eclipseWorkspaceFolder.mkdirs();
    }
    
    return eclipseWorkspaceFolder;
  }
  
  protected File getEclipseExecutable(InstallerContext context, File eclipseFolder) {
    File eclipseExecutable = new File(eclipseFolder, getExecutable());
    if (!eclipseExecutable.exists()) {
      throw new ConfigurationException("Could not find Eclipse executable");
    }
    
    return eclipseExecutable;
  }
  
  protected void runEclipse(InstallerContext context, File eclipseFolder, File eclipseExecutable, String... arguments) throws IOException, InterruptedException {
    runCommand(eclipseFolder, eclipseExecutable.getAbsoluteFile(), arguments);
  }
  
  protected void runEclipse(InstallerContext context, File ecliseFolder, File eclipseExecutable, List<String> arguments) throws IOException, InterruptedException {
    runCommand(ecliseFolder, eclipseExecutable.getAbsoluteFile(), arguments);
  }
  
  protected String getExecutable() {
    if (SystemUtils.IS_OS_LINUX) {
      return ECLIPSE_EXECUTABLE_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return ECLIPSE_EXECUTABLE_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return ECLIPSE_EXECUTABLE_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }

  protected File installYellowSheep(File eclipsePluginsFolder) throws IOException {
    File yellowSheepJar = new File(eclipsePluginsFolder, YELLOW_SHEEP_JAR);
    copyResourceToFile(YELLOW_SHEEP_JAR, yellowSheepJar);
    return yellowSheepJar;
  }

}
