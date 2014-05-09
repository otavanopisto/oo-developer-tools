package fi.otavanopisto.devtools.muikkuinstaller;


public class AssignOptionsPhase extends InstallerPhase {
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    context.setOption(InstallerContext.BASEDIR, "/tmp/e/");
//    context.setOption(InstallerContext.ECLIPSE_FOLDER, "/tmp/e/eclipse");
    context.setOption(InstallerContext.ECLIPSE_WORKSPACE_FOLDER, "/tmp/e/workspace");
    context.setOption(InstallerContext.SOURCE_FOLDER, "/tmp/e/muikku");
  }
  

}
