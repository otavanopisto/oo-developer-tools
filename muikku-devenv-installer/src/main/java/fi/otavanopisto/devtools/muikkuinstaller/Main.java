package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

public class Main {

  public static final String INSTALLER_EXECUTABLE = "java -jar muikku-devenv-installer.jar";

  public static void main(String[] args) {
    try {
      ArrayList<InstallerPhase> phases = new ArrayList<InstallerPhase>();

      CommandLineParser parser = new GnuParser();
      Options options = new Options();
      options.addOption("e", false, "install Eclipse");
      options.addOption("E", false, "install required plugins for Eclipse");
      options.addOption("c", false, "configure eclipse");
      options.addOption("j", false, "install JBoss AS");
      options.addOption("J", false, "configure JBoss AS");
      options.addOption("D", false, "drop and create MySQL/MariaDB database and user (requires mysql)");
      options.addOption("r", false, "clone Muikku git repository (requires git)");
      options.addOption("i", false, "import muikku projects into Eclipse");
      options.addOption("a", false, "install and configure all");
      options.addOption("h", "help", false, "print this message");

      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption('h') || cmd.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(INSTALLER_EXECUTABLE + " [options] basedir", options);
        System.exit(0);
      }
        
      phases.add(new AssignOptionsPhase());
    
      if (cmd.hasOption('a')) {
        phases.add(new CreateDatabasePhase());
        phases.add(new InstallJBossPhase());
        phases.add(new InstallEclipsePhase());
        phases.add(new InstallEclipsePluginsPhase());
        phases.add(new ConfigureEclipsePhase());
        phases.add(new ConfigureJBossPhase());
        phases.add(new CloneGitRepositoryPhase());
        phases.add(new ImportEclipseProjectsPhase());
      } else {
        if (cmd.hasOption('D')) {
          phases.add(new CreateDatabasePhase());
        }
        if (cmd.hasOption('j')) {
          phases.add(new InstallJBossPhase());
        }
        if (cmd.hasOption('e')) {
          phases.add(new InstallEclipsePhase());
        }
        if (cmd.hasOption('E')) {
          phases.add(new InstallEclipsePluginsPhase());
        }
        if (cmd.hasOption('c')) {
          phases.add(new ConfigureEclipsePhase());
        }
        if (cmd.hasOption('J')) {
          phases.add(new ConfigureJBossPhase());
        }
        if (cmd.hasOption('r')) {
          phases.add(new CloneGitRepositoryPhase());
        }
        if (cmd.hasOption('i')) {
          phases.add(new ImportEclipseProjectsPhase());
        }
      }

      if (cmd.getArgList().size() > 0) {
        File basedir = new File(cmd.getArgList().get(0).toString());
        executePhases(phases, basedir);
      } else {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(INSTALLER_EXECUTABLE + " [options] basedir", options);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void executePhases(ArrayList<InstallerPhase> phases, File basedir) throws Exception {
    InstallerContext installerContext = new InstallerContext();
    installerContext.setOption(InstallerContext.BASEDIR, basedir.getAbsolutePath());
    for (InstallerPhase phase : phases) {
      String line = StringUtils.repeat('-', phase.getName().length() + 4);
      System.out.println('\n' + line + "\n  " + phase.getName() + "\n" + line + '\n');
      phase.execute(installerContext);
    }
  }

}
