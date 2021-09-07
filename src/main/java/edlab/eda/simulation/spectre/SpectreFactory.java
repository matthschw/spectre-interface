package edlab.eda.simulation.spectre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpectreFactory {

  private File simDirectory;
  private long watchdogWaitTime = SpectreWatchdog.WATCHDOG_DEFAULT_WAIT_TIME;
  private String simPrefix = null;

  private SpectreFactory(File simDirectory) {
    this.simDirectory = simDirectory;
  }

  public static SpectreFactory getSpectreFactory(File simDirectory) {

    Runtime rt = Runtime.getRuntime();

    String[] commands = { SpectreSession.CMD_TOOL, "-W" };
    Process proc = null;

    try {
      proc = rt.exec(commands);
      BufferedReader stdError = new BufferedReader(
          new InputStreamReader(proc.getErrorStream()));

      String retval = stdError.readLine();
      
      Pattern pattern = Pattern
          .compile("sub-version[  ]+[0-9]+.[0-9]+.[0-9]+.[0-9]");

      Matcher matcher = pattern.matcher(retval);
      
      if (matcher.find()) {
        if (simDirectory.isDirectory() && simDirectory.canRead()
            && simDirectory.canWrite()) {

          return new SpectreFactory(simDirectory);

        } else {
          return null;
        }
      } else {
        return null;
      }

    } catch (IOException e) {
      return null;
    }
  }

  public File getSimDirectory() {
    return simDirectory;
  }

  public static String readFile(File file) {

    Scanner scanner;
    String rtn = "";

    try {
      scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        rtn += scanner.nextLine() + "\n";
      }

      scanner.close();

      return rtn;

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public long getWatchdogWaitTime() {
    return watchdogWaitTime;
  }

  public void setWatchdogWaitTime(long watchdogWaitTime) {
    if (watchdogWaitTime >= 1000) {
      this.watchdogWaitTime = watchdogWaitTime;
    }
  }

  public String getSimPrefix() {
    return simPrefix;
  }

  public void setSimPrefix(String simPrefix) {
    this.simPrefix = simPrefix;
  }
}