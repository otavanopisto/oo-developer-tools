package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;

import org.apache.commons.io.FileUtils;


public class AssignOptionsPhase extends InstallerPhase {

  @Override
  public String getName() {
    return "Options";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    context.setOption(InstallerContext.BASEDIR, "/tmp/e/");
    context.setOption(InstallerContext.ECLIPSE_FOLDER, "/tmp/e/eclipse");
    context.setOption(InstallerContext.ECLIPSE_WORKSPACE_FOLDER, "/tmp/e/workspace");
    context.setOption(InstallerContext.SOURCE_FOLDER, "/tmp/e/muikku");
    context.setOption(InstallerContext.JBOSS_FOLDER, "/tmp/e/jboss");
    
    File baseDir = context.getFileOption(InstallerContext.BASEDIR);
    if (baseDir.exists()) {
      FileUtils.deleteDirectory(baseDir);
    }
    
    baseDir.mkdirs();
  }
  

}
