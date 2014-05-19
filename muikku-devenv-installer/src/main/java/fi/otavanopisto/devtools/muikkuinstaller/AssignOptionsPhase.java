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
    File baseDir = context.getFileOption(InstallerContext.BASEDIR);
    String fileSeparator = File.separator;

    context.setOption(InstallerContext.ECLIPSE_FOLDER, baseDir.getAbsolutePath() + fileSeparator + "eclipse");
    context.setOption(InstallerContext.ECLIPSE_WORKSPACE_FOLDER, baseDir.getAbsolutePath() + fileSeparator + "workspace");
    context.setOption(InstallerContext.SOURCE_FOLDER, baseDir.getAbsolutePath() + fileSeparator + "muikku");
    context.setOption(InstallerContext.JBOSS_FOLDER, baseDir.getAbsolutePath() + fileSeparator + "jboss");

    if (baseDir.exists()) {
      FileUtils.deleteDirectory(baseDir);
    }

    baseDir.mkdirs();
  }

}
