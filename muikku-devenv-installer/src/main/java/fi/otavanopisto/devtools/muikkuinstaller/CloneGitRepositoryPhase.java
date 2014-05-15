package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;

public class CloneGitRepositoryPhase extends InstallerPhase {
  
  private static final String REPOSITORY_URL = "https://github.com/otavanopisto/muikku.git";
  
  @Override
  public String getName() {
    return "Import Projects";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    File baseFolder = getBaseFolder(context);
    File sourceFolder = new File(baseFolder, "muikku");
    
    String taskId = startTask("Cloning git repository");
    try {
      runCommand(baseFolder, "git", "clone", REPOSITORY_URL, sourceFolder.getAbsolutePath());
      runCommand(sourceFolder, "git", "checkout", "--track", "origin/devel");
      context.setOption(InstallerContext.SOURCE_FOLDER, sourceFolder.getAbsolutePath());
    } finally {
      endTask(taskId); 
    }
  }

}
