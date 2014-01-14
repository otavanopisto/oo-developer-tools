package fi.otavanopisto.devtools.muikkuinstaller

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

// CONFIGURATION

def ECLIPSE_URL      = "http://ftp.snt.utwente.nl/pub/software/eclipse" +
    "//technology/epp/downloads/release/kepler/SR1/" +
    "eclipse-standard-kepler-SR1-win64-x86_64.zip"
def ECLIPSE_FILENAME = "eclipse.zip"
def ECLIPSE_DIRNAME  = "eclipse"

// UTILITY FUNCTIONS

def download(address, fname) {
  def file = new FileOutputStream(fname)
  def out = new BufferedOutputStream(file)
  out << new URL(address).openStream()
  out.close()
}

def unzip(file, dest) {
  def result = new ZipInputStream(new FileInputStream(file))
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

// MAIN SCRIPT

download(ECLIPSE_URL, ECLIPSE_FILENAME)
unzip(ECLIPSE_FILENAME, ECLIPSE_DIRNAME)
