package fi.otavanopisto.devtools.muikkuinstaller

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils

class SystemNotSupportedException extends Exception {
};

// CONFIGURATION
def cliOptions() {
  INSTALLER_EXECUTABLE = "java -jar muikku-devenv-installer.jar"

  def cli = new CliBuilder(usage:INSTALLER_EXECUTABLE + " [options] basedir",
  header: "Install Muikku development environment\n")
  cli.e('install Eclipse')
  cli.E('install required plugins for Eclipse')
  cli.j('install JBoss AS')
  cli.J('configure JBoss AS')
  cli.a('install and configure all')
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

  def opts = cli.parse(commandLine)

  if (opts.h) {
    cli.usage()
    return false
  }
  
  if (opts.a) {
    INSTALL_ECLIPSE = true
    CONFIGURE_ECLIPSE = true
    INSTALL_JBOSS = true
    CONFIGURE_JBOSS = true
  } else {
    INSTALL_ECLIPSE = opts.e
    CONFIGURE_ECLIPSE = opts.E
    INSTALL_JBOSS = opts.j
    CONFIGURE_JBOSS = opts.J
  }
  
  if (opts.arguments().size() > 0) {
    BASEDIR = opts.arguments().get(0).replaceAll(DIR_SEPARATOR + /+$/, "")
    BASEDIR = new File(BASEDIR).getCanonicalPath()
  } else {
    cli.usage()
    return false
  }

  return true
}

def configure() {
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
  } else if (SystemUtils.IS_OS_MAC_OSX) {
    ECLIPSE_URL      = "http://eclipse.mirror.triple-it.nl/technology/epp/" +
        "downloads/release/kepler/SR1/eclipse-standard-kepler" +
        "-SR1-macosx-cocoa-x86_64.tar.gz"
    ECLIPSE_FILENAME = "eclipse.tar.gz"
    ECLIPSE_DIRNAME  = "eclipse"
    ECLIPSE_EXECUTABLE = "eclipse"
    DIR_SEPARATOR = "/"
  } else {
    throw new SystemNotSupportedException()
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
  
  
  if (SystemUtils.IS_OS_WINDOWS) {
   JBOSS_URL = "http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip"
   JBOSS_FILENAME = "jboss.zip"
   JBOSS_EXECUTABLE = "bin\\jboss-cli.bat" 
   JBOSS_STANDALONE_EXECUTABLE = "bin\\standalone.bat"
   JBOSS_ADDUSER_EXECUTABLE = "bin\\add-user.bat"
  } else {
   JBOSS_URL = "http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.tar.gz"
   JBOSS_FILENAME = "jboss.tar.gz"
   JBOSS_EXECUTABLE = "bin/jboss-cli.sh"
   JBOSS_STANDALONE_EXECUTABLE = "bin/standalone.sh"
   JBOSS_ADDUSER_EXECUTABLE = "bin/add-user.sh"
  }
  JBOSS_DIRNAME = "jboss-as-7.1.1.Final"
  
  JBOSS_CONFIGURE_SCRIPT = """
  # Muikku CLI script
  
  connect
  batch
  
  # System properties
  
  /system-property=muikku-plugin-libraries:add(value=${-> repository}/muikku-data/muikku-plugin-libraries.properties)
  /system-property=muikku-plugin-repositories:add(value=${-> repository}/muikku-data/muikku-pluginrepositories.properties)
  /system-property=muikku-data:add(value=${-> repository}/muikku-data/muikku-data.xml)
  /system-property=muikku-deus-nex-machina-password:add(value=${-> dnmPassword})

  # MySQL JDBC Driver 

  /subsystem=datasources/jdbc-driver=mysql:add(driver-name="mysql",driver-module-name="com.mysql.jdbc",driver-xa-datasource-class-name="com.mysql.jdbc.jdbc2.optional.MysqlXADataSource")
  
  # Datasources
  
  data-source add --name=muikku --driver-name=mysql --driver-class=com.mysql.jdbc.Driver --connection-url=${-> connectionUrl} --jndi-name=java:/jdbc/muikku --user-name=${-> username} --password=${-> password} --use-ccm=false --jta=true --validate-on-match=false --background-validation=false --share-prepared-statements=false
  data-source enable --name=muikku
  data-source add --name=muikku-h2 --driver-name=h2 --driver-class=org.h2.Driver --connection-url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE --jndi-name=java:/jdbc/muikku-h2 --use-ccm=false --jta=false --use-java-context=true --min-pool-size=1 --max-pool-size=200 --check-valid-connection-sql="SELECT 1" --validate-on-match=true --background-validation=false --share-prepared-statements=false
  
  # Execute and shutdown
  
  run-batch
  :shutdown
  """
  
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
    throw new SystemNotSupportedException()
  }
}

def runProgram(String progname) {
  Process process = progname.execute()
  new Thread(new Runnable() {public void run() {
    IOUtils.copy(process.getInputStream(), System.out)
  } } ).start()
  new Thread(new Runnable() {public void run() {
    IOUtils.copy(process.getErrorStream(), System.err)
  } } ).start()
  new Thread(new Runnable() {public void run() {
    IOUtils.copy(System.in, process.getOutputStream())
  } } ).start()
  process.waitFor()
}

def copyResourceToFile(String source, String target) {
  this.getClass().getResource( source ).withInputStream { ris ->
    new File( target ).withOutputStream { fos ->
      fos << ris
    }
  }
}

def expect(Process proc, String regex) {
  def br = new InputStreamReader(proc.inputStream)
  def line
  while ((line = br.readLine()) != null)
  {
    println line
     if (line =~ regex) {
       break
     }
  }
}

// MAIN SCRIPT
try {
  if (!configure()) {return}
  if (!cliOptions()) {return}

  if (INSTALL_ECLIPSE) {
    println "Downloading Eclipse..."
    download(ECLIPSE_URL, BASEDIR + DIR_SEPARATOR + ECLIPSE_FILENAME)
    println "Uncompressing Eclipse..."
    if (SystemUtils.IS_OS_WINDOWS) {
      uncompress(BASEDIR + DIR_SEPARATOR + ECLIPSE_FILENAME,
                 BASEDIR)
    } else {
      uncompress(BASEDIR + DIR_SEPARATOR + ECLIPSE_FILENAME,
        BASEDIR + DIR_SEPARATOR + ECLIPSE_DIRNAME)
    }
  }
  if (CONFIGURE_ECLIPSE) {
    println "Installing Eclipse plugins..."
    def eclipse_exc_path = ""
    if (INSTALL_ECLIPSE) {
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
  if (INSTALL_JBOSS) {
    println "Installing JBoss AS..."
    download(JBOSS_URL, BASEDIR + DIR_SEPARATOR + JBOSS_FILENAME)
    println "Uncompressing JBoss AS..."
    uncompress(BASEDIR + DIR_SEPARATOR + JBOSS_FILENAME, BASEDIR)
  }
  if (CONFIGURE_JBOSS) {
    println "Configuring JBoss AS..."
    def jboss_path = ""
    if (INSTALL_JBOSS) {
      jboss_path = (BASEDIR +
        DIR_SEPARATOR +
        JBOSS_DIRNAME +
        DIR_SEPARATOR)
    } else {
      println "Please enter the path to JBoss directory"
      jboss_path = (readLine().replaceAll(DIR_SEPARATOR + /+$/, "")
                    + DIR_SEPARATOR)
    }
    File tmpFile = File.createTempFile("temp", "cli");
    tmpFile.deleteOnExit()
    println "Please enter the absolute path to the root of Muikku Git repository"
    repository = readLine().replaceAll(DIR_SEPARATOR + /+$/, "")
    println "Please enter the Deus Nex Machina password"
    dnmPassword = readLine()
    println "Please enter the database connection URL"
    connectionUrl = readLine()
    println "Please enter the database username"
    username = readLine()
    println "Please enter the database password"
    password = readLine()
    tmpFile.write(JBOSS_CONFIGURE_SCRIPT.toString().replace(/^\s+/, ""))
    println "Installing MySQL driver..."
    new File(jboss_path +
      "modules" + DIR_SEPARATOR +
      "com" + DIR_SEPARATOR +
      "mysql" + DIR_SEPARATOR +
      "jdbc" + DIR_SEPARATOR +
      "main" + DIR_SEPARATOR).mkdirs()
    copyResourceToFile("/resources/module.xml", jboss_path +
      "modules" + DIR_SEPARATOR +
      "com" + DIR_SEPARATOR +
      "mysql" + DIR_SEPARATOR +
      "jdbc" + DIR_SEPARATOR +
      "main" + DIR_SEPARATOR +
      "module.xml")
    copyResourceToFile("/resources/mysql-connector-java-5.1.18-bin.jar", jboss_path +
      "modules" + DIR_SEPARATOR +
      "com" + DIR_SEPARATOR +
      "mysql" + DIR_SEPARATOR +
      "jdbc" + DIR_SEPARATOR +
      "main" + DIR_SEPARATOR +
      "mysql-connector-java-5.1.18-bin.jar")
    println "Starting JBoss AS..."
    def standaloneProc = (jboss_path +
      JBOSS_STANDALONE_EXECUTABLE).execute()
    // Wait for JBoss to start
    expect(standaloneProc, /JBoss.*started/)
    println "Executing config commands..."
    def cliProc = (jboss_path +
      JBOSS_EXECUTABLE +
      " --file=${tmpFile.getAbsolutePath()}").execute()
    cliProc.in.eachLine { println it }
    expect(cliProc, /{\s*"outcome"\s*=>.*?}/)
    cliProc.waitForOrKill(100)
    expect(standaloneProc, /JBoss.*stopped/)
    standaloneProc.waitForOrKill(100)
  }
  println "Done."
} catch (SystemNotSupportedException ex) {
  println "Your system is not supported."
}