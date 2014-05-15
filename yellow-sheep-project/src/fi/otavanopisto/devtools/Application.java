package fi.otavanopisto.devtools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenProjectInfo;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerUtil;
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
      case "configure-jbossas71":
        configureJBossAs71(options.get("server-path"));
      break;      
      case "import-jbossas71-project":
        importJBossAs71Project(options.get("import-project"));
      break;
      case "update-projects":
        updateMavenProjects();
      break;
      case "import-preferences":
        importPreferences(options.get("preferences-file"));
      break;
    }

    System.out.println("Waiting for background processes to end...");
    waitForBackingJobs();
    System.out.println("Yellow Sheep Project Stopping...");
    
    return EXIT_OK;
  }

  private void importPreferences(String fileName) throws CoreException, IOException {
    IPreferencesService service = Platform.getPreferencesService();
    
    FileInputStream fileInputStream = new FileInputStream(fileName);
    try {
      service.importPreferences(fileInputStream);
    } finally {
      fileInputStream.close();
    }
  }
  
  protected void importJBossAs71Project(String projectName) {
    IServer server = findServerByTypeId("org.jboss.ide.eclipse.as.71");

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    if (project != null) {
      IModule module = ServerUtil.getModule(project);
      if (module != null) {
        try {
          IServerWorkingCopy workingCopy = server.createWorkingCopy();
          workingCopy.modifyModules(new IModule[] { module }, new IModule[] {}, new NullProgressMonitor());
          workingCopy.save(false, new NullProgressMonitor());
          server.publish(IServer.PUBLISH_FULL, new NullProgressMonitor());
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        System.out.println("Could not find module for project: " + projectName);
      }
    } else {
      System.out.println("Could not find project: " + projectName);
    }
  }
  
  private IServer findServerByTypeId(String typeId) {
    for (IServer server : ServerCore.getServers()) {
      if (typeId.equals(server.getServerType().getId())) {
        return server;
      }
    }
    
    return null;
  }

  protected void configureJBossAs71(String jbossHome) throws CoreException {
    IRuntimeWorkingCopy runtime = null;
    
    IRuntimeType runtimeType = findRuntimeTypeById("org.jboss.ide.eclipse.as.runtime.71");
    if (runtimeType != null) {
      runtime = findRuntimeByType(runtimeType);
      if (runtime == null) {
        runtime = createRuntime(runtimeType, Path.fromOSString(jbossHome), "JBoss 7.1 Runtime");
      }          
    }
    
    IServerType serverType = findServerTypeById("org.jboss.ide.eclipse.as.71");
    if (serverType != null) {
      createServer(serverType, runtime, "JBoss 7.1 Runtime Server");
    }
  }

  protected void createServer(IServerType serverType, IRuntimeWorkingCopy runtime, String name) throws CoreException {
    IServerWorkingCopy server = serverType.createServer(null, null, new ConsoleProgressMonitor());
    server.setName(name);
    server.setRuntime(runtime);
    server.setReadOnly(false);
    server.save(false, new ConsoleProgressMonitor());
  }
  
  private IServerType findServerTypeById(String id) {
    IServerType[] serverTypes = ServerCore.getServerTypes();
    for (IServerType serverType : serverTypes) {
      if (id.equals(serverType.getId())) {
        return serverType;
      }
    }
    
    return null;
  }

  private IRuntimeWorkingCopy findRuntimeByType(IRuntimeType runtimeType) {
    IRuntime[] runtimes = ServerCore.getRuntimes();
    for (IRuntime runtime : runtimes) {
      if (runtime.getRuntimeType().getId().equals(runtimeType.getId())) {
        return runtime.createWorkingCopy();
      }
    }
    
    return null;
  }
  
  private IRuntimeWorkingCopy createRuntime(IRuntimeType runtimeType, IPath jbossHome, String name) throws CoreException {
    IRuntimeWorkingCopy workingCopy = runtimeType.createRuntime(null, new ConsoleProgressMonitor());
    workingCopy.setLocation(jbossHome);
    workingCopy.setName(name);
    workingCopy.setReadOnly(false);
    workingCopy.save(false, new ConsoleProgressMonitor());
    
    return workingCopy;
  }

  private IRuntimeType findRuntimeTypeById(String id) {
    IRuntimeType[] runtimeTypes = ServerCore.getRuntimeTypes();
    for (IRuntimeType runtimeType : runtimeTypes) {
      if (id.equals(runtimeType.getId())) {
        return runtimeType;
      }
    }
    
    return null;
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
      } else if ("-configure-jbossas71".equals(commandLineArgs[i])) {
        options.put("server-path", commandLineArgs[i + 1]);
        options.put("action", "configure-jbossas71");
      }  else if ("-import-jbossas71-project".equals(commandLineArgs[i])) {
        options.put("import-project", commandLineArgs[i + 1]);
        options.put("action", "import-jbossas71-project");
      } else if ("-m2e-annotation-processing-mode".equals(commandLineArgs[i])) {
        options.put("annotation-processing-mode", commandLineArgs[i + 1]);
        options.put("action", "annotation-processing-mode");
        i++;
      } else if ("-update-projects".equals(commandLineArgs[i])) {
        options.put("action", "update-projects");
      } else if ("-import-preferences".equals(commandLineArgs[i])) {
        options.put("preferences-file", commandLineArgs[i + 1]);
        options.put("action", "import-preferences");
        i++;
      }

      i++;
    }
    
    

    return options;
  }
  
  private void updateMavenProjects() throws CoreException{
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (IProject project : projects) {
      MavenUpdateRequest req = new MavenUpdateRequest(project, false, false);
      MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(req, new ConsoleProgressMonitor());
    }
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
    
    try {
      MavenPlugin.getProjectConfigurationManager().importProjects(projectInfos, configuration, new ConsoleProgressMonitor());
    } catch (Throwable t) {
      System.err.println("Project import threw an exception: ");
      t.printStackTrace();
    }
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
    while (!Job.getJobManager().isIdle() && (Job.getJobManager().currentJob() != null)) {
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
