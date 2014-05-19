package fi.otavanopisto.devtools.pyramusinstaller

import java.lang.ProcessBuilder.Redirect;
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import javax.crypto.interfaces.PBEKey;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

def nullTrustManager = [
  checkClientTrusted: { chain, authType ->  },
  checkServerTrusted: { chain, authType ->  },
  getAcceptedIssuers: { null }
]

def nullHostnameVerifier = [
  verify: { hostname, session -> true }
]

SSLContext sc = SSLContext.getInstance("SSL")
sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

class SystemNotSupportedException extends Exception {
};

// TODO: Conf updates?
String UPDATES_URL = "https://github.com/otavanopisto/pyramus/trunk/pyramus/updates";
String UPDATES_FOLDER = "updates";

// TODO: Check svn
// TODO: Check mvn
// TODO: Check mysql
// TODO: Clean garbage
// TODO: Change installer to log into a file 
// TODO: Add progress indicator

// CONFIGURATION
def cliOptions() {
  INSTALLER_EXECUTABLE = "java -jar pyramus-installer.jar"
  def cli = new CliBuilder(usage:INSTALLER_EXECUTABLE + " [options] basedir", header: "Install Pyramus\n")
  cli.j('install JBoss AS')
  cli.J('configure JBoss AS')
  cli.c('generate self-signed certificate')
  cli.D('drop and create MySQL/MariaDB database and user (requires mysql)')
  cli.U('download and run database updates (requires mysql)')
  cli.w("download and install latest Pyramus webapp")
  cli.a('install and configure all')
  cli.f('attempt to operate without promping the user', longOpt: 'force')
  cli.h('print this message', longOpt: 'help')
  cli._(longOpt:'version', args:1, argName:'version', 'Pyramus version to be installed, defaults to latest stable')
  cli._(longOpt:'jbossDir', args:1, argName:'directory', 'JBoss install directory')
  cli._(longOpt:'hostname', args:1, argName:'e.g. www.example.com', 'Pyramus hostname')
  cli._(longOpt:'databaseUrl', args:1, argName:'url', 'Database connection URL')
  cli._(longOpt:'databaseUser', args:1, argName:'user', 'Database username')
  cli._(longOpt:'databasePassword', args:1, argName:'password', 'Database password')
  cli._(longOpt:'databaseAdminUser', args:1, argName:'user', 'Database admin username')
  cli._(longOpt:'databaseAdminPassword', args:1, argName:'password', 'Database admin password')
  
  def commandLine = []

  if (System.getProperty("pyramusinstaller.ask").equals("true")) {
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
    INSTALL_JBOSS = true
    CONFIGURE_JBOSS = true
    CREATE_DATABASE = true
    UPDATE_DATABASE = true
    SELF_SIGNED_CERT = true
    INSTALL_PYRAMUS = true
   } else {
    INSTALL_JBOSS = opts.j
    CONFIGURE_JBOSS = opts.J
    CREATE_DATABASE = opts.D
    UPDATE_DATABASE = opts.U
    SELF_SIGNED_CERT = opts.c
    INSTALL_PYRAMUS = opts.w
  }
  
  FORCE = opts.f;
  DATABASE_URL = opts.getProperty('databaseUrl')
  DATABASE_USER = opts.getProperty('databaseUser')
  DATABASE_PASSWORD = opts.getProperty('databasePassword')
  JBOSS_DIR = opts.getProperty('jbossDir')
  PYRAMUS_HOSTNAME = opts.getProperty('hostname')
  DATABASE_ADMIN_USER = opts.getProperty('databaseAdminUser')
  DATABASE_ADMIN_PASSWORD = opts.getProperty('databaseAdminPassword')
  VERSION = opts.getProperty('version')
  
  if (opts.arguments().size() > 0) {
    BASEDIR = opts.arguments().get(0).replaceAll(DIR_SEPARATOR + /+$/, "")
    BASEDIR = new File(BASEDIR).getAbsolutePath()
  } else {
    cli.usage()
    return false
  }

  return true
}

def configure() {
  if (SystemUtils.IS_OS_WINDOWS) {
    DIR_SEPARATOR = "\\"
  } else if (SystemUtils.IS_OS_LINUX) {
    DIR_SEPARATOR = "/"
  } else if (SystemUtils.IS_OS_MAC_OSX) {
    DIR_SEPARATOR = "/"
  } else {
    throw new SystemNotSupportedException()
  }

  DATABASE_CREATE_SCRIPT = """
  drop database if exists pyramus_db;
  grant usage on *.* to '${-> username }'@localhost;
  drop user '${-> username }'@localhost;
  create database pyramus_db default charset utf8;
  create user '${-> username }'@localhost identified by '${-> password }';
  grant all on pyramus_db.* to '${-> username }'@localhost;
  """
  
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
  # Pyramus CLI script
  
  connect
  batch

  # System properties
  
  /system-property=PyramusWSAllowedIPs:add(value="127.0.0.1")
  /system-property=pyramus-url:add(value="https://${-> hostname}:8443")
  
  # MySQL JDBC Driver 

  /subsystem=datasources/jdbc-driver=mysql:add(driver-name="mysql",driver-module-name="com.mysql.jdbc",driver-xa-datasource-class-name="com.mysql.jdbc.jdbc2.optional.MysqlXADataSource")
  
  # Datasources
  
  data-source add --name=pyramus --driver-name=mysql --driver-class=com.mysql.jdbc.Driver --connection-url=${-> connectionUrl} --jndi-name=java:/jdbc/pyramus --user-name=${-> username} --password=${-> password} --use-ccm=false --jta=true --validate-on-match=false --background-validation=false --share-prepared-statements=false
  data-source enable --name=pyramus

  # Welcome root and Https

  /subsystem=web/virtual-server=default-host:write-attribute(name=enable-welcome-root,value=false)
  /subsystem=web/connector=http:write-attribute(name=redirect-port,value="8443")
  /subsystem=web/connector=https:add(socket-binding=https,scheme=https,protocol="HTTP/1.1",enabled=true,secure=true)
  /subsystem=web/connector=https/ssl=configuration:add(name="ssl",certificate-key-file="\${jboss.server.config.dir}/pyramus.keystore",password="${-> keyPassword}", key-alias="${-> keyAlias}", protocol="TLS", verify-client="false")

  # WebServices

  /subsystem=security/security-domain=WebServices:add(cache-type=default)
  /subsystem=security/security-domain=WebServices/authentication=classic:add(login-modules=[{"code"=>"RealmUsersRoles","flag"=>"required","module-options"=>[("realm"=>"WebServices"),("password-stacking"=>"useFirstPass"),("rolesProperties"=>"\${jboss.server.config.dir}/application-roles.properties"),("usersProperties"=>"\${jboss.server.config.dir}/application-users.properties")]}])

  # Interfaces

  /interface=public:remove
  /interface=public:add(any-address=true)

  /interface=unsecure:remove
  /interface=unsecure:add(any-address=true)

  # Execute and shutdown
  
  run-batch
  :shutdown
  """
  
  return true
}

// UTILITY FUNCTIONS
String readLine() {
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
    ["tar", "-xzf", fname, "-C", dest].execute().waitFor()
  } else if (fname =~ /.*\.zip/) {
    unzip(fname, dest)
  } else {
    throw new SystemNotSupportedException()
  }
}

def runProgram(List<String> argv, File dir=null) {
  if (dir == null) {
    dir = new File(BASEDIR)
  }
  ProcessBuilder pb = new ProcessBuilder(argv)
  pb.redirectInput(Redirect.INHERIT)
  pb.redirectOutput(Redirect.INHERIT)
  pb.redirectError(Redirect.INHERIT)
  pb.directory(dir)
  Process process = pb.start()
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
  Logger.getRootLogger().setLevel(Level.DEBUG);
  
  if (!configure()) {return}
  if (!cliOptions()) {return}

  File basedirFile = new File(BASEDIR)
  if (!basedirFile.exists()) {
    basedirFile.mkdirs()
  } else {
    if (!FORCE) {
      println "The given directory exists. Continue? (Y/n)"
      line = readLine();
      def c = line.length() > 0 ? line.charAt(0) : 'y';
      if (c != 'y' && c != 'Y') {
        println "Aborting..."
        return
      }
    }
  }

  if (CREATE_DATABASE || UPDATE_DATABASE || CONFIGURE_JBOSS) {
    if (!DATABASE_URL) {
      println "Please enter the database connection URL (leave blank for jdbc:mysql://localhost:3306/pyramus_db)"
      connectionUrl = readLine()
      if (!connectionUrl) {
        connectionUrl = "jdbc:mysql://localhost:3306/pyramus_db";
      }
    } else {
      connectionUrl = DATABASE_URL
    }
      
    if (!DATABASE_USER) {
      println "Please enter the database username (leave blank for pyramus_usr)"
      username = readLine()
      if (!username) {
        username = "pyramus_usr";
      }
    } else {
      username = DATABASE_USER
    }
      
    if (!DATABASE_PASSWORD) {
      println "Please enter the database password"
      password = readLine()
    } else {
      password = DATABASE_PASSWORD
    }
  }
  
  if (!PYRAMUS_HOSTNAME) {
    println "Please enter Pyramus hostname (e.g. pyramus.example.net)"
    hostname = readLine()
  } else {
    hostname = PYRAMUS_HOSTNAME
  }

  if (CREATE_DATABASE) {
    
    adminUsername = null
    adminPassword = null
    
    if (!DATABASE_ADMIN_USER) {
      println "Please enter MySQL administrator username"
      adminUsername = readLine()
    } else {
      adminUsername = DATABASE_ADMIN_USER
    }
    
    if (!DATABASE_ADMIN_PASSWORD) {
      println "Please enter MySQL administrator password"
      adminPassword = readLine()
    } else {
      adminPassword = DATABASE_ADMIN_PASSWORD
    }

    ProcessBuilder pb = new ProcessBuilder('mysql',
                         "-u${adminUsername}",
                         "-p${adminPassword}")
    pb.redirectError(Redirect.INHERIT)
    Process mysqlProc = pb.start()
    println DATABASE_CREATE_SCRIPT
    mysqlProc.withWriter {
      it.write(DATABASE_CREATE_SCRIPT)
    }
    mysqlProc.in.eachLine {println it}
    mysqlProc.waitFor()  
  }

  if (UPDATE_DATABASE) {
    ProcessBuilder pb = new ProcessBuilder('svn',
      'co',
      UPDATES_URL,
      BASEDIR + DIR_SEPARATOR + UPDATES_FOLDER
    )
    pb.redirectError(Redirect.INHERIT)
    Process svnProc = pb.start()
    println "Downloading database updates..."
    svnProc.in.eachLine {println it}
    svnProc.waitFor()
    println "Running database updates..."
    
    updater = new fi.internetix.updater.core.Updater(new fi.internetix.updater.core.Settings(
      new File(BASEDIR, UPDATES_FOLDER), 
      fi.internetix.updater.core.Database.MySQL.getDialect(), 
      connectionUrl, 
      username, 
      password));
    
    if (updater.checkForUpdates().size() > 0) {
      updater.performUpgrade(true);
    }
  }
  
  if (INSTALL_JBOSS) {
    println "Installing JBoss AS..."
    download(JBOSS_URL, BASEDIR + DIR_SEPARATOR + JBOSS_FILENAME)
    println "Uncompressing JBoss AS..."
    uncompress(BASEDIR + DIR_SEPARATOR + JBOSS_FILENAME, BASEDIR)
  }
  
  if (CONFIGURE_JBOSS || SELF_SIGNED_CERT || INSTALL_PYRAMUS) {
    if (INSTALL_JBOSS) {
      jboss_path = (BASEDIR +
        DIR_SEPARATOR +
        JBOSS_DIRNAME +
        DIR_SEPARATOR)
    } else {
      if (JBOSS_DIR == null) {
        println "Please enter the path to JBoss directory"
        jboss_path = (readLine().replaceAll(DIR_SEPARATOR + /+$/, "") + DIR_SEPARATOR)
      } else {
        jboss_path = JBOSS_DIR
      }
    }
  }
  
  if (SELF_SIGNED_CERT) {
    println "Generating self-signed sertificate..."
    keystoreFile = "$jboss_path/standalone/configuration/pyramus.keystore";
    new File(keystoreFile).delete();
    keyAlias = "pyramus";
    keyPassword = "pyramus";

    ProcessBuilder pb;
    
    pb = new ProcessBuilder('keytool',
      "-genkey",
      "-keysize", "2048",
      "-keyalg", "RSA",
      "-keystore", keystoreFile,
      "-dname", "CN=" + hostname + ", OU=Test, O=Pyramus, L=Test, S=Test, C=FI",
      "-alias", keyAlias,
      "-storepass", keyPassword,
      "-keypass", keyPassword
    )

    pb.redirectError(Redirect.INHERIT)
    Process keytoolGenkeyProc = pb.start()
    keytoolGenkeyProc.in.eachLine {println it}
    keytoolGenkeyProc.waitFor()
    
    pb = new ProcessBuilder('keytool',
      "-exportcert",
      "-keystore", keystoreFile,
      "-alias", keyAlias,
      "-storepass", keyPassword,
      "-file", keystoreFile + ".tmp"
    )
    pb.redirectError(Redirect.INHERIT)
    Process keytoolExportCertProc = pb.start()
    keytoolExportCertProc.in.eachLine {println it}
    keytoolExportCertProc.waitFor()
    
    pb = new ProcessBuilder('keytool',
      "-import",
      "-v",
      "-noprompt",
      "-trustcacerts",      
      "-alias", "root",
      "-keystore", keystoreFile,
      "-storepass", keyPassword,
      "-file", keystoreFile + ".tmp"
    )
    pb.redirectError(Redirect.INHERIT)
    Process keytoolImportProc = pb.start()
    keytoolImportProc.in.eachLine {println it}
    keytoolImportProc.waitFor()
    new File(keystoreFile + ".tmp").delete();
  }
  
  if (CONFIGURE_JBOSS) {
    println "Configuring JBoss AS..."
    File tmpFile = File.createTempFile("temp", "cli");
    tmpFile.deleteOnExit()
    
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
    ProcessBuilder standalonePb = new ProcessBuilder(jboss_path +
      JBOSS_STANDALONE_EXECUTABLE)
    standalonePb.environment().put("NOPAUSE", "true")
    def standaloneProc = standalonePb.start()
    try {
      // Wait for JBoss to start
      expect(standaloneProc, /JBoss.*started/)
      println "Executing config commands..."
      ProcessBuilder cliPb = new ProcessBuilder(jboss_path +
        JBOSS_EXECUTABLE, "--file=${tmpFile.getAbsolutePath()}")
      cliPb.environment().put("NOPAUSE", "true")
      Process cliProc = cliPb.start()
      expect(cliProc, /=>/)
      cliProc.waitForOrKill(5000)
    } finally {
      standaloneProc.waitForOrKill(5000)
    }
  }
  
  if (INSTALL_PYRAMUS) {
    println "Installing Pyramus..."
    
    if (VERSION) {
      pyramusVersion = VERSION
    } else {
      pyramusVersion = "LATEST"
    }

    ProcessBuilder pb = new ProcessBuilder('mvn', 
      "org.apache.maven.plugins:maven-dependency-plugin:2.8:get",
      "-DremoteRepositories=http://maven.otavanopisto.fi:7070/nexus/content/repositories/releases,http://maven.otavanopisto.fi:7070/nexus/content/repositories/snapshots",
      "-Dartifact=fi.pyramus:pyramus:${-> pyramusVersion}:war",
      "-Ddest=" + jboss_path + '/standalone/deployments/ROOT.war'
    );
    pb.redirectError(Redirect.INHERIT)
    Process mvnProc = pb.start()
    mvnProc.in.eachLine {println it}
    mvnProc.waitFor()
    
    println "### Installation complete ###"
    println "Next you need to start you JBoss server by executing ${-> jboss_path + JBOSS_STANDALONE_EXECUTABLE} and navigate"
    println "Into https://${-> hostname}:8443 with your web browser and follow the instructions on the screen"
  }

  println "Done."
  
} catch (SystemNotSupportedException ex) {
  println "Your system is not supported."
}
