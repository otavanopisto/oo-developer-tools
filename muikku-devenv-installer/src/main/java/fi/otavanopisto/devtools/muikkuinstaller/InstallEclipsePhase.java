package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

public class InstallEclipsePhase extends InstallerPhase {
  
  private static final String ECLIPSE_URL_WINDOWS = "http://download.eclipse.org/technology/epp/downloads/release/kepler/SR2/eclipse-standard-kepler-SR2-win32-x86_64.zip";
  private static final String ECLIPSE_URL_LINUX = "http://download.eclipse.org/technology/epp/downloads/release/kepler/SR2/eclipse-standard-kepler-SR2-linux-gtk-x86_64.tar.gz";
  private static final String ECLIPSE_URL_MAC = "http://download.eclipse.org/technology/epp/downloads/release/kepler/SR2/eclipse-standard-kepler-SR2-macosx-cocoa-x86_64.tar.gz";
  private static final String ECLIPSE_ZIP_WINDOWS = "eclipse.zip";
  private static final String ECLIPSE_ZIP_LINUX = "eclipse.tar.gz";
  private static final String ECLIPSE_ZIP_MAC = "eclipse.tar.gz";

  
  @Override
  public String getName() {
    return "Install Eclipse";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File eclipseZip = new File(getTempFolder(), getZipName());
    if (!eclipseZip.exists()) {
      String taskId = startTask("Downloading Eclipse");
      try {
        download(eclipseZip, getDownloadUrl());
      } finally {
        endTask(taskId);
      }
    }
    
    String taskId = startTask("Uncompressing Eclipse");
    try {
      unzip(eclipseZip, getTempFolder());
      File tmpEclipseFolder = new File(getTempFolder(), "eclipse");
      File eclipseFolder = null;
      
      if (!context.isOptionSet(InstallerContext.ECLIPSE_FOLDER)) {
        File baseFolder = getBaseFolder(context);
        eclipseFolder = new File(baseFolder, "eclipse");
      } else {
        eclipseFolder = context.getFileOption(InstallerContext.ECLIPSE_FOLDER);
      }
      
      if (eclipseFolder.exists()) {
        eclipseFolder.delete();
      }
      
      
      if (SystemUtils.IS_OS_LINUX) {
        // renameTo doesn't move files properly
        ProcessBuilder pb = new ProcessBuilder("mv",
                                               tmpEclipseFolder.getAbsolutePath(),
                                               eclipseFolder.getAbsolutePath());
        pb.start().waitFor();
      } else {
        eclipseFolder.mkdirs();
        tmpEclipseFolder.renameTo(eclipseFolder);
      }
      context.setOption(InstallerContext.ECLIPSE_FOLDER, eclipseFolder.getAbsolutePath());
    } finally {
      endTask(taskId);
    }
  }
  
  private String getZipName() {
    if (SystemUtils.IS_OS_LINUX) {
      return ECLIPSE_ZIP_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return ECLIPSE_ZIP_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return ECLIPSE_ZIP_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }
  
  private String getDownloadUrl() throws SystemNotSupportedException {
    if (SystemUtils.IS_OS_LINUX) {
      return ECLIPSE_URL_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return ECLIPSE_URL_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return ECLIPSE_URL_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }
  

}
