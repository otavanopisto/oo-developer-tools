package fi.otavanopisto.devtools;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.jboss.tools.maven.apt.MavenJdtAptPlugin;
import org.jboss.tools.maven.apt.preferences.AnnotationProcessingMode;
import org.jboss.tools.maven.apt.preferences.IPreferencesManager;

public class Application implements IApplication {

  @Override
  public Object start(IApplicationContext context) throws Exception {
    Map<String, String> options = parseOptions();
    String projectNameTemplate = "[groupId].[artifactId]";

    System.out.println("Yellow Sheep Project Started...");

    switch (options.get("action")) {
      case "annotation-processing-mode":
        String mode = options.get("annotation-processing-mode");
        selectAnnotationProcessingMode(mode);
        System.out.println("Set Maven Annotation Processing Mode into " + mode);
      break;
      case "import-poms":
        if (StringUtils.isNotBlank(options.get("import-poms"))) {
          String[] pomFileNames = options.get("import-poms").split(",");
          
          List<File> pomFiles = new ArrayList<>();
          for (String pomFileName : pomFileNames) {
            File pomFile = new File(pomFileName);
            if (pomFile.exists()) {
              if (pomFile.isDirectory()) {
                pomFile = new File(pomFile, "pom.xml");
              }
              
              if (pomFile.exists()) {
                pomFiles.add(pomFile);                
              } else {
                throw new IOException("File not found: " + pomFileName);
              }
            } else {
              throw new IOException("File not found: " + pomFileName);
            }
          }
          
          importPomFiles(projectNameTemplate, pomFiles);
          System.out.println("Projects imported");
        }
      break;
    }

    System.out.println("Waiting for background processes to end...");
    waitForBackingJobs();
    
    return EXIT_OK;
  }

  private Map<String, String> parseOptions() {
    Map<String, String> options = new HashMap<>();

    String[] commandLineArgs = Platform.getCommandLineArgs();
    int i = 0;
    while (i < commandLineArgs.length) {
      if ("-m2e-import-poms".equals(commandLineArgs[i])) {
        options.put("import-poms", commandLineArgs[i + 1]);
        options.put("action", "import-poms");
        i++;
      } else if ("-m2e-update-projects".equals(commandLineArgs[i])) {
        options.put("action", "update-projects");
      } else if ("-m2e-annotation-processing-mode".equals(commandLineArgs[i])) {
        options.put("annotation-processing-mode", commandLineArgs[i + 1]);
        options.put("action", "annotation-processing-mode");
        i++;
      }

      i++;
    }

    return options;
  }

  private void importPomFiles(String projectNameTemplate, List<File> pomFiles) throws Exception {
    ProjectImportConfiguration configuration = new ProjectImportConfiguration();
    configuration.setProjectNameTemplate(projectNameTemplate);
    configuration.getResolverConfiguration().setResolveWorkspaceProjects(true);
    
    List<MavenProjectInfo> projectInfos = new ArrayList<>();
    for (File pomFile : pomFiles) {
      MavenProjectInfo projectInfo = new MavenProjectInfo(pomFile.getParentFile().getName(), pomFile, null, null);
      projectInfos.add(projectInfo);
    }
    
    waitForBackingJobs();
    
    MavenPlugin.getProjectConfigurationManager().importProjects(projectInfos, configuration, new ConsoleProgressMonitor());
  }

  private void selectAnnotationProcessingMode(String mode) {
    IPreferencesManager preferencesManager = MavenJdtAptPlugin.getDefault().getPreferencesManager();
    preferencesManager.setAnnotationProcessorMode(null, AnnotationProcessingMode.valueOf(mode));
  }

  @Override
  public void stop() {

  }

  @SuppressWarnings("unused")
  private static List<File> discoverProjectFiles(File parent) {
    List<File> result = new ArrayList<File>();

    List<File> files = Arrays.asList(parent.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return "pom.xml".equals(name);
      }
    }));

    File[] folders = parent.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.isDirectory() && !"target".equalsIgnoreCase(file.getName());
      }
    });

    result.addAll(files);

    for (File folder : folders) {
      result.addAll(discoverProjectFiles(folder));
    }

    return Collections.unmodifiableList(result);
  }

  private void waitForBackingJobs() throws InterruptedException {
    while (!Job.getJobManager().isIdle()) {
      TimeUnit.SECONDS.sleep(10);
    }
  }
  
  private class ConsoleProgressMonitor extends NullProgressMonitor {

    public ConsoleProgressMonitor() {
      super();
    }

    public void beginTask(String name, int totalWork) {
      this.name = name;
      this.totalWork = totalWork;
      System.out.println("Started task '" + name + "'");
    }

    public void done() {
      System.out.println("End task '" + name + "'");
    }

    public void worked(int work) {
      worked += work;
      if (totalWork > 0) {
        System.out.println(String.format("Task '%s' %,.2f%% done", name, ((worked / totalWork) * 100)));
      }
    }

    private String name;
    private double worked;
    private double totalWork;
  }

}
