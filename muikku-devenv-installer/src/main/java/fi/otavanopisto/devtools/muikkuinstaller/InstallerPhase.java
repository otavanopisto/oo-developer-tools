package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class InstallerPhase {

  public abstract void execute(InstallerContext context) throws Exception;
  
  protected File getBaseFolder(InstallerContext context) {
    return new File(context.getOption(InstallerContext.BASEDIR));
  }

  protected File getTempFolder() {
    File tempFolder = new File(System.getProperty("java.io.tmpdir"), "muikku-installer-temp");
    if (!tempFolder.exists()) {
      tempFolder.mkdir();
    }

    return tempFolder;
  }

  protected void enquireForOption(String option) {

  }

  protected void enquireForFolderOption(String option) {
    enquireForOption(option);
  }

  protected void download(OutputStream outputStream, String address) throws IOException {
    URL url = new URL(address);
    InputStream inputStream = url.openStream();
    try {
      IOUtils.copy(inputStream, outputStream);
    } finally {
      inputStream.close();
    }
  }

  protected void download(File targetFile, String address) throws IOException {
    if (!targetFile.exists()) {
      targetFile.createNewFile();
    }

    FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
    try {
      download(fileOutputStream, address);
    } finally {
      fileOutputStream.flush();
      fileOutputStream.close();
    }
  }

  protected void download(String fileName, String address) throws IOException {
    download(new File(fileName), address);
  }

  protected void unzip(File zipFile, File destFolder) throws IOException, ArchiveException {
    if (!destFolder.exists()) {
      destFolder.mkdirs();
    }
    
    if (zipFile.getName().endsWith(".gz")) {
      FileInputStream zipFileInputStream = new FileInputStream(zipFile);
      try {
        GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(zipFileInputStream);
        try {
          unzipStream(destFolder, gzipInputStream);
        } finally {
          gzipInputStream.close();
        }
      } finally {
        zipFileInputStream.close();
      }
    } else {
      FileInputStream zipFileInputStream = new FileInputStream(zipFile);
      try {
        unzipStream(destFolder, zipFileInputStream);
      } finally {
        zipFileInputStream.close();
      }
    }
  }

  private void unzipStream(File destFolder, InputStream compressedInputStream) throws ArchiveException, IOException, FileNotFoundException {
    BufferedInputStream bufferedInputStream = new BufferedInputStream(compressedInputStream);
    try {
      ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
        .createArchiveInputStream(bufferedInputStream);
 
      ArchiveEntry entry;
      while ((entry = archiveInputStream.getNextEntry()) != null) {
        TarArchiveEntry tarArchiveEntry = entry instanceof TarArchiveEntry ? (TarArchiveEntry) entry : null;
        
        File file = new File(destFolder, entry.getName());
        if (entry.isDirectory()) {
          file.mkdirs();
        } else if (tarArchiveEntry != null && tarArchiveEntry.isSymbolicLink()) {
          Path link = FileSystems.getDefault().getPath(tarArchiveEntry.getLinkName());
          Files.createSymbolicLink(file.toPath(), link);
        } else {
          if (file.createNewFile()) {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
              IOUtils.copy(archiveInputStream, fileOutputStream);
            } finally {
              fileOutputStream.flush();
              fileOutputStream.close();
            }
          } else {
            System.err.println("Could not create new file '" + file.getAbsolutePath() + "'");
          }
        }
        
        if (tarArchiveEntry != null && !tarArchiveEntry.isSymbolicLink()) {
          int mode = tarArchiveEntry.getMode();
          Set<PosixFilePermission> permissions = unixModeToPosixPermissions(mode);
          FileAttribute<Set<PosixFilePermission>> fileAttribute = PosixFilePermissions.asFileAttribute(permissions);
          Files.setPosixFilePermissions(file.toPath(), fileAttribute.value());      
        }
      }
    } finally {
      bufferedInputStream.close();
    }
  }
  
  protected static Set<PosixFilePermission> unixModeToPosixPermissions(int mode) {
    // This method is uses slightly modified code from (Apache License 2.0)
    // https://github.com/ksclarke/cmake-maven-project/blob/master/cmake-binaries-plugin/src/main/java/com/googlecode/cmakemavenproject/GetBinariesMojo.java
    StringBuilder result = new StringBuilder();
    
    for (int i = 2; i >= 0; i--) {
      mode %= Math.pow(8, i);
      int digit = (int) (mode / Math.pow(8, i - 1));
      result.append((digit & 4) != 0 ? 'r' : '-');
      result.append((digit & 2) != 0 ? 'w' : '-');
      result.append((digit & 1) != 0 ? 'x' : '-');
    }
    
    return PosixFilePermissions.fromString(result.toString());
  }

  protected void runCommand(File workDirectory, String... argv) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(argv);
    processBuilder.redirectInput(Redirect.INHERIT);
    processBuilder.redirectOutput(Redirect.INHERIT);
    processBuilder.redirectError(Redirect.INHERIT);
    processBuilder.directory(workDirectory);

    System.out.println("Running command: " + StringUtils.join(argv, ' '));

    Process process = processBuilder.start();
    process.waitFor();
  }

  protected void runCommand(File workDirectory, File binary, String... argv) throws IOException, InterruptedException {
    runCommand(workDirectory, binary, Arrays.asList(argv));
  }

  protected void runCommand(File workDirectory, File binary, List<String> argv) throws IOException, InterruptedException {
    List<String> args = new ArrayList<String>();
    args.add(binary.getAbsolutePath());
    args.addAll(argv);
    runCommand(workDirectory, args.toArray(new String[0]));
  }

  protected void copyResourceToFile(String resource, File file) throws IOException {
    InputStream resourceStream = this.getClass().getResourceAsStream("/resources/" + resource);
    try {
      if (!file.exists()) {
        file.createNewFile();
      }

      FileOutputStream fileOutputStream = new FileOutputStream(file);
      try {
        IOUtils.copy(resourceStream, fileOutputStream);
      } finally {
        fileOutputStream.flush();
        fileOutputStream.close();
      }
    } finally {
      resourceStream.close();
    }
  }

}
