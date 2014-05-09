package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

public class InstallEclipsePhase extends InstallerPhase {
  
  private static final String ECLIPSE_URL_WINDOWS = "http://eclipse.mirror.triple-it.nl/technology/epp/downloads/release/kepler/SR1/eclipse-standard-kepler-SR1-win32-x86_64.zip";
  private static final String ECLIPSE_URL_LINUX = "http://eclipse.mirror.triple-it.nl/technology/epp/downloads/release/kepler/SR1/eclipse-standard-kepler-SR1-linux-gtk-x86_64.tar.gz";
  private static final String ECLIPSE_URL_MAC = "http://eclipse.mirror.triple-it.nl/technology/epp/downloads/release/kepler/SR1/eclipse-standard-kepler-SR1-macosx-cocoa-x86_64.tar.gz";
  private static final String ECLIPSE_ZIP_WINDOWS = "eclipse.zip";
  private static final String ECLIPSE_ZIP_LINUX = "eclipse.tar.gz";
  private static final String ECLIPSE_ZIP_MAC = "eclipse.tar.gz";

  @Override
  public void execute(InstallerContext context) throws Exception {
    File eclipseZip = new File(getTempFolder(), getZipName());
    if (!eclipseZip.exists()) {
      download(eclipseZip, getDownloadUrl());
    }
    
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
    
    eclipseFolder.mkdirs();
    
    tmpEclipseFolder.renameTo(eclipseFolder);
    context.setOption(InstallerContext.ECLIPSE_FOLDER, eclipseFolder.getAbsolutePath());
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