package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class InstallEclipsePluginsPhase extends AbstractEclipseConfigurationPhase {
  
  private static final String[] REPOSITORIES = new String[]{
    "http://download.eclipse.org/releases/kepler/",
    "http://download.jboss.org/jbosstools/updates/stable/kepler/",
    "http://download.jboss.org/jbosstools/targetplatforms/jbosstoolstarget/kepler/",
    "https://repository.sonatype.org/content/repositories/forge-sites/m2e-extras/0.15.0/N/0.15.0.201206251206/"
  };

  private static final String[] PLUGINS = new String[] {
    "org.jboss.tools.cdi.deltaspike.feature.feature.group",
    "org.jboss.tools.cdi.feature.feature.group",
    "org.jboss.tools.forge.feature.feature.group",
    "org.jboss.ide.eclipse.freemarker.feature.feature.group",
    "org.hibernate.eclipse.feature.feature.group",
    "org.jboss.ide.eclipse.archives.feature.feature.group",
    "org.jboss.tools.community.central.feature.feature.group",
    "org.jboss.tools.ws.jaxrs.feature.feature.group",
    "org.jboss.tools.maven.cdi.feature.feature.group",
    "org.jboss.tools.maven.jdt.feature.feature.group",
    "org.jboss.tools.maven.hibernate.feature.feature.group",
    "org.jboss.tools.maven.feature.feature.group",
    "org.jboss.tools.maven.portlet.feature.feature.group",
    "org.jboss.tools.maven.project.examples.feature.feature.group",
    "org.jboss.tools.maven.seam.feature.feature.group",
    "org.jboss.tools.openshift.express.feature.feature.group",
    "org.jboss.tools.portlet.feature.feature.group",
    "org.jboss.tools.runtime.core.feature.feature.group",
    "org.jboss.tools.runtime.seam.detector.feature.feature.group",
    "org.jboss.tools.stacks.core.feature.feature.group",
    "org.jboss.tools.wtp.runtimes.tomcat.feature.feature.group",
    "org.jboss.tools.community.project.examples.feature.feature.group",
    "org.jboss.tools.openshift.egit.integration.feature.feature.group",
    "org.jboss.tools.foundation.feature.feature.group",
    "org.jboss.tools.foundation.security.linux.feature.feature.group",
    "org.jboss.tools.jst.feature.feature.group",
    "org.jboss.tools.common.jdt.feature.feature.group",
    "org.jboss.tools.jsf.feature.feature.group",
    "org.jboss.tools.livereload.feature.feature.group",
    "org.jboss.tools.maven.jbosspackaging.feature.feature.group",
    "org.jboss.tools.maven.sourcelookup.feature.feature.group",
    "org.jboss.tools.vpe.browsersim.feature.feature.group",
    "org.jboss.tools.richfaces.feature.feature.group",
    "org.jboss.tools.usage.feature.feature.group",
    "org.jboss.tools.vpe.feature.feature.group",
    "org.jboss.tools.ws.feature.feature.group",
    "org.jboss.ide.eclipse.as.feature.feature.group",
    "org.jboss.tools.jmx.feature.feature.group",
    "org.jboss.tools.maven.apt.feature.feature.group",
    "org.jboss.tools.maven.profiles.feature.feature.group",
    "org.jboss.tools.project.examples.feature.feature.group",
    "org.jboss.tools.cdi.seam.feature.feature.group",
    "org.jboss.tools.seam.feature.feature.group",
    "org.sonatype.m2e.buildhelper.feature.feature.group"
  };  
  
  @Override
  public String getName() {
    return "Install Eclipse Plugins";
  }
  
  @Override
  public void execute(InstallerContext context) throws Exception {
    String taskId = startTask("Installing plugins");
    try {
      boolean installSource = false;
      
      File eclipseFolder = getEclipseFolder(context);
      File eclipseWorkspaceFolder = getEclipseWorkspaceFolder(context, true);
      File eclipseExecutable = getEclipseExecutable(context, eclipseFolder);
  
      List<String> arguments = new ArrayList<String>();
      
      arguments.add("-nosplash");
      arguments.add("-application");
      arguments.add("org.eclipse.equinox.p2.director");
      arguments.add("-data");
      arguments.add(eclipseWorkspaceFolder.getAbsolutePath());
      
      for (String repository : REPOSITORIES) {
        arguments.add("-repository");
        arguments.add(repository);
      }
      
      for (String plugin : PLUGINS) {
        if (installSource || !StringUtils.endsWith(plugin, ".source")) {
          arguments.add("-installIU");
          arguments.add(plugin);
        }
      }
      
      runEclipse(context, eclipseFolder, eclipseExecutable, arguments);
    } finally {
      endTask(taskId);
    }
  }
  
}
