package org.tron.studio.solc;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemProperties {

  private static Logger logger = LoggerFactory.getLogger("general");

  private String getOS() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
      return "win";
    } else if (osName.contains("linux")) {
      return "linux";
    } else if (osName.contains("mac")) {
      return "mac";
    } else {
      throw new RuntimeException("Can't find solc compiler: unrecognized OS: " + osName);
    }
  }

  public String customSolcPath() {
    File file = new File(getClass().getResource("/bin/" + getOS() + "/solc").getPath());
    return file.getAbsolutePath();
  }

}
