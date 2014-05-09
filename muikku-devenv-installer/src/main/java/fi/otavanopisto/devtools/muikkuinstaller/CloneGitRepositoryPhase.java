package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;

public class CloneGitRepositoryPhase extends InstallerPhase {
  
  private static final String REPOSITORY_URL = "https://github.com/otavanopisto/muikku.git";
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    // TODO: Configurable source folder
    File baseFolder = getBaseFolder(context);
    File sourceFolder = new File(baseFolder, "muikku");
    
    runCommand(baseFolder, "git", "clone", REPOSITORY_URL, sourceFolder.getAbsolutePath());
    runCommand(sourceFolder, "git", "checkout", "--track", "origin/devel");
    context.setOption(InstallerContext.SOURCE_FOLDER, sourceFolder.getAbsolutePath());
  }

}
