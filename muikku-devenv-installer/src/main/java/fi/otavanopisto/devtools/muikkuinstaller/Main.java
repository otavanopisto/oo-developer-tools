package fi.otavanopisto.devtools.muikkuinstaller;

import org.apache.commons.lang3.StringUtils;

public class Main {
  
  public static void main(String[] args) {
    try {
      executePhases(
        new AssignOptionsPhase(),
        new InstallJBossPhase(),
        new InstallEclipsePhase(),
        new InstallEclipsePluginsPhase(),
        new ConfigureEclipsePhase(),
        new CloneGitRepositoryPhase(),
        new ImportEclipseProjectsPhase()
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void executePhases(InstallerPhase... phases) throws Exception {
    InstallerContext installerContext = new InstallerContext();

    for (InstallerPhase phase : phases) {
      String line = StringUtils.repeat('-', phase.getName().length() + 4);
      System.out.println('\n' + line + "\n  " + phase.getName() + "\n" + line + '\n');
      phase.execute(installerContext);
    }
  }

}
