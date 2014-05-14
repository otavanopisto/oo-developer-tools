package fi.otavanopisto.devtools.muikkuinstaller;


public class AssignOptionsPhase extends InstallerPhase {
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    context.setOption(InstallerContext.BASEDIR, "/home/heikkikurhinen/muikkuimporter");
//    context.setOption(InstallerContext.ECLIPSE_FOLDER, "/tmp/e/eclipse");
    context.setOption(InstallerContext.ECLIPSE_WORKSPACE_FOLDER, "/home/heikkikurhinen/muikkuimporter/workspace");
    context.setOption(InstallerContext.SOURCE_FOLDER, "/home/heikkikurhinen/muikkuimporter/muikku");
  }
  

}
