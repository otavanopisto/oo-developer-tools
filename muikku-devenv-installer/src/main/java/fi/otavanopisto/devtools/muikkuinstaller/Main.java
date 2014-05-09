package fi.otavanopisto.devtools.muikkuinstaller;

public class Main {
  
  public static void main(String[] args) {
    try {
      executePhases(
        new AssignOptionsPhase(),
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
      phase.execute(installerContext);
    }
  }

}
