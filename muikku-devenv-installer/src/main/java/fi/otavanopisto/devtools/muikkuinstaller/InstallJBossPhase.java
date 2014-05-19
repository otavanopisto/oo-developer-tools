package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

public class InstallJBossPhase extends InstallerPhase {
  
  private static final String JBOSS_URL_WINDOWS = "http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip";
  private static final String JBOSS_URL_LINUX = "http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.tar.gz";
  private static final String JBOSS_URL_MAC = "http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.tar.gz";
  private static final String JBOSS_ZIP_WINDOWS = "jboss.zip";
  private static final String JBOSS_ZIP_LINUX = "jboss.tar.gz";
  private static final String JBOSS_ZIP_MAC = "jboss.tar.gz";
  
  @Override
  public String getName() {
    return "Install JBoss";
  }

  @Override
  public void execute(InstallerContext context) throws Exception {
    String downloadTaskId = startTask("Download and extract JBoss");
    try {
      File jbossZip = new File(getTempFolder(), getZipName());
      if (!jbossZip.exists()) {
        download(jbossZip, getDownloadUrl());
      }
      
      unzip(jbossZip, getTempFolder());
      
      File tmpJbossFolder = new File(getTempFolder(), "jboss-as-7.1.1.Final");
      File jbossFolder = null;
      
      if (!context.isOptionSet(InstallerContext.JBOSS_FOLDER)) {
        File baseFolder = getBaseFolder(context);
        jbossFolder = new File(baseFolder, "jboss-as-7.1.1.Final");
      } else {
        jbossFolder = context.getFileOption(InstallerContext.JBOSS_FOLDER);
      }
      
      if (jbossFolder.exists()) {
        jbossFolder.delete();
      }
      
      jbossFolder.mkdirs();
      
      tmpJbossFolder.renameTo(jbossFolder);
      context.setOption(InstallerContext.JBOSS_FOLDER, jbossFolder.getAbsolutePath());      
    } finally {
      endTask(downloadTaskId);
    }
  }
  
  private String getZipName() {
    if (SystemUtils.IS_OS_LINUX) {
      return JBOSS_ZIP_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return JBOSS_ZIP_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return JBOSS_ZIP_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }
  
  private String getDownloadUrl() throws SystemNotSupportedException {
    if (SystemUtils.IS_OS_LINUX) {
      return JBOSS_URL_LINUX;
    } else if (SystemUtils.IS_OS_MAC) {
      return JBOSS_URL_MAC;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return JBOSS_URL_WINDOWS;
    } else {
      throw new SystemNotSupportedException();
    }
  }
  

}
