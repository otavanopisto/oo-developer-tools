package fi.otavanopisto.devtools.muikkuinstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class InstallerContext {

  public static final String BASEDIR = "basedir";
  public static final String ECLIPSE_FOLDER = "eclipse.folder";
  public static final String ECLIPSE_WORKSPACE_FOLDER = "eclipse.workspace-folder";
  public static final String JBOSS_FOLDER = "jboss.folder";
  public static final String SOURCE_FOLDER = "source.folder";
  public static final String MYSQL_ADMIN_USERNAME = "mysql.admin.username";
  public static final String MYSQL_ADMIN_PASSWORD = "mysql.admin.password";
  public static final String MYSQL_USER = "mysql.user.username";
  public static final String MYSQL_PASSWORD = "mysql.user.password";
  public static final String MYSQL_DATABASE = "mysql.user.database";

  public InstallerContext() {
    this.options = new HashMap<String, String>();
  }
  
  public void setOption(String name, String value) {
    this.options.put(name, value);
  }

  public String getOption(String name, String question, String defaultValue, boolean enquireMissing) {
    while (enquireMissing && !isOptionSet(name)) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      
      try {
        if (StringUtils.isNotBlank(defaultValue)) {
          question += " (" + defaultValue + ")";
        }
        
        System.out.println(question);
        
        String line = reader.readLine();
        if (StringUtils.isNotBlank(line)) {
          setOption(name, line);
        } else if (StringUtils.isNotBlank(defaultValue)) {
          setOption(name, defaultValue);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    return this.options.get(name);
  }

  public String getPasswordOption(String name, String question1, String question2) {
    while (!isOptionSet(name)) {
      String password1 = null;
      String password2 = null;
      
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      
      try {
        if (StringUtils.isBlank(password1)) {
          System.out.println(question1);
          password1 = reader.readLine();
        }

        if (StringUtils.isBlank(password2)) {
          System.out.println(question2);
          password2 = reader.readLine();
        }

        if (StringUtils.isNotBlank(password1) && StringUtils.isNotBlank(password2)) {
          if (password1.endsWith(password2)) {
            setOption(name, password1);
          } else {
            password1 = null;
            password2 = null;
            System.out.println("The passwords didn't match");
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    return this.options.get(name);
  }
  
  public String getOption(String name) {
    return getOption(name, null, null, false);
  }
  
  public Boolean getBooleanOption(String name) {
    return Boolean.valueOf(name);
  }

  public File getFileOption(String name) {
    return getFileOption(name, null, null, false);
  }
  
  public File getFileOption(String name, String question, String defaultValue, boolean enquireMissing) {
    return new File(getOption(name, question, defaultValue, enquireMissing));
  }

  public boolean isOptionSet(String name) {
    return this.options.containsKey(name);
  }

  private Map<String, String> options;
}
