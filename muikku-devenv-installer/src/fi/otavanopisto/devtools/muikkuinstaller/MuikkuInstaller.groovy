package fi.otavanopisto.devtools.muikkuinstaller

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
@Grab(group='commons-cli', module='commons-cli', version='1.2')
import org.apache.commons.cli.ParseException
@Grab(group='org.apache.commons', module='commons-lang3', version='3.0')
import org.apache.commons.lang3.SystemUtils

class SystemNotSupportedException extends Exception {};

// CONFIGURATION
def configure() {
  INSTALLER_EXECUTABLE = "muikku-devenv-installer"
  
  def cli = new CliBuilder(usage:INSTALLER_EXECUTABLE + " [options]",
    header: "Install Muikku development environment\n")
  cli.e('install Eclipse')
  cli.p('install required plugins for Eclipse')
  cli.h('print this message', longOpt: 'help')
  
  def commandLine = []
  
  if (System.getProperty("muikkudevenvinstaller.ask").equals("true")) {
    println "Enter command line arguments:"
    commandLine = readLine().split()
  } else {
    if (args.length == 0) {
      cli.usage()
      return false
    }
    
  	commandLine = args
  }
  
  COMMAND_LINE_OPTS = cli.parse(commandLine)
  
  if (COMMAND_LINE_OPTS.h) {
    cli.usage()
    return false
  }
  
  if (SystemUtils.IS_OS_WINDOWS) {
    ECLIPSE_URL      = "http://eclipse.mirror.triple-it.nl/technology/epp/" + 
    											 "downloads/release/kepler/SR1/eclipse-standard-kepler" +
                           "-SR1-win32-x86_64.zip"
    ECLIPSE_FILENAME = "eclipse.zip"
    ECLIPSE_DIRNAME  = "eclipse"
    ECLIPSE_EXECUTABLE = "eclipsec.exe"
    DIR_SEPARATOR = "\\"
  } else if (SystemUtils.IS_OS_LINUX) {
  	ECLIPSE_URL      = "http://eclipse.mirror.triple-it.nl/technology/epp/" + 
  												 "downloads/release/kepler/SR1/eclipse-standard-kepler" +
                           "-SR1-linux-gtk-x86_64.tar.gz"
    ECLIPSE_FILENAME = "eclipse.tar.gz"
    ECLIPSE_DIRNAME  = "eclipse"
    ECLIPSE_EXECUTABLE = "eclipse"
    DIR_SEPARATOR = "/"
  } else {
  	throw new SystemNotSupportedException();
  }
    
  ECLIPSE_PLUGIN_INSTALL_ARGS = """
  -application org.eclipse.equinox.p2.director
  -repository http://download.eclipse.org/releases/kepler/
  -repository http://download.jboss.org/jbosstools/updates/stable/kepler
  -repository http://download.jboss.org/jbosstools/targetplatforms/jbosstoolstarget/kepler/
  -repository https://repository.sonatype.org/content/repositories/forge-sites/m2e-extras/0.15.0/N/0.15.0.201206251206/
  -installIU org.jboss.tools.cdi.deltaspike.feature.feature.group/1.5.1.Final-v20131204-0116-B137
  -installIU org.jboss.tools.cdi.feature.feature.group/1.5.1.Final-v20131204-0116-B137
  -installIU org.eclipse.jpt.jpadiagrameditor.feature.feature.group/1.2.0.v201303120344-45-9oB58D6EBP8KDG
  -installIU org.eclipse.jpt.jpa.feature.feature.group/3.3.1.v201308261920-7V7_5FC7sRe5TdgZcI05646d
  -installIU org.hibernate.eclipse.feature.feature.group/3.7.1.Final-v20131205-0918-B107
  -installIU org.jboss.tools.ws.jaxrs.feature.feature.group/1.5.1.Final-v20131206-1905-B127
  -installIU org.jboss.tools.maven.cdi.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  -installIU org.jboss.tools.maven.jdt.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  -installIU org.jboss.tools.maven.hibernate.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  -installIU org.jboss.tools.maven.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  -installIU org.jboss.tools.runtime.core.feature.feature.group/2.1.0.Final-v20131204-1734-B141
  -installIU org.jboss.tools.stacks.core.feature.feature.group/1.0.1.Final-v20131204-1734-B141
  -installIU org.jboss.tools.openshift.egit.integration.feature.feature.group/2.5.1.Final-v20131206-2048-B128
  -installIU org.jboss.tools.foundation.feature.feature.group/1.0.1.Final-v20131204-1734-B141
  -installIU org.jboss.tools.foundation.security.linux.feature.feature.group/1.0.1.Final-v20131204-1734-B141
  -installIU org.jboss.tools.jst.feature.feature.group/3.5.1.Final-v20131203-2300-B112
  -installIU org.jboss.tools.common.jdt.feature.feature.group/3.5.2.Final-v20131204-1734-B141
  -installIU org.jboss.tools.jsf.feature.feature.group/3.5.1.Final-v20131204-0116-B137
  -installIU org.jboss.tools.livereload.feature.feature.group/1.1.0.Final-v20131204-2352-B107
  -installIU org.jboss.tools.maven.jbosspackaging.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  -installIU org.jboss.tools.maven.sourcelookup.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  -installIU org.jboss.tools.ws.feature.feature.group/1.5.1.Final-v20131206-1905-B127
  -installIU org.jboss.ide.eclipse.as.feature.feature.group/2.4.101.Final-v20131206-1843-B159
  -installIU org.jboss.tools.jmx.feature.feature.group/1.4.1.Final-v20131206-1843-B159
  -installIU org.sonatype.m2e.buildhelper.feature.feature.group/0.15.0.201206251206
  -installIU org.jboss.tools.maven.apt.feature.feature.group/1.0.1.201209200721
  -installIU org.jboss.tools.maven.profiles.feature.feature.group/1.5.4.Final-v20131204-2329-B126
  """.replace('\n', ' ')
  BASEDIR = new File(".").getCanonicalPath();
  return true
}

// UTILITY FUNCTIONS

def readLine() {
  def s = new Scanner(System.in);
  return s.nextLine();
}

def download(address, fname) {
  def file = new FileOutputStream(fname)
  def out = new BufferedOutputStream(file)
  out << new URL(address).openStream()
  out.close()
}

def unzip(fname, dest) {
  def result = new ZipInputStream(new FileInputStream(fname))
  def destFile = new File(dest)
  if(!destFile.exists()){
    destFile.mkdir();
  }
  result.withStream{
    def entry
    while(entry = result.nextEntry){
      if (!entry.isDirectory()){
        new File(dest + File.separator + entry.name).parentFile?.mkdirs()
        def output = new FileOutputStream(dest + File.separator
            + entry.name)
        output.withStream{
          int len = 0;
          byte[] buffer = new byte[4096]
          while ((len = result.read(buffer)) > 0){
            output.write(buffer, 0, len); 
          }
        }
      }
      else {
        new File(dest + File.separator + entry.name).mkdir()
      }
    }
  }
}

def uncompress(fname, dest) throws SystemNotSupportedException {
  if (fname =~ /.*\.tar\.gz/) {
    ["tar", "-xzf", fname].execute().waitFor()
  } else if (fname =~ /.*\.zip/) {
  	unzip(fname, dest)
  } else {
    throw new SystemNotSupportedException();
  }
}

// MAIN SCRIPT
try {
  def ok = configure()
  if (!ok) return
  if (COMMAND_LINE_OPTS.e) {
    println "Downloading Eclipse..."
    download(ECLIPSE_URL, ECLIPSE_FILENAME)
    println "Uncompressing Eclipse..."
    uncompress(ECLIPSE_FILENAME, ECLIPSE_DIRNAME)
  }
  if (COMMAND_LINE_OPTS.p) {
    println "Installing Eclipse plugins..."
    def eclipse_exc_path = ""
    if (COMMAND_LINE_OPTS.e) {
      eclipse_exc_path = BASEDIR +
                DIR_SEPARATOR +
                ECLIPSE_DIRNAME +
                DIR_SEPARATOR +
                ECLIPSE_EXECUTABLE
    } else {
    	println "Please enter the path to Eclipse directory"
      def path = readLine().replaceAll(DIR_SEPARATOR + /+$/, "")
      eclipse_exc_path = path + DIR_SEPARATOR + ECLIPSE_EXECUTABLE
    }
    def proc = (eclipse_exc_path + ECLIPSE_PLUGIN_INSTALL_ARGS).execute()
    proc.in.eachLine { println it }
  }
  println "Done."
} catch (SystemNotSupportedException ex) {
	println "Your system is not supported."
}