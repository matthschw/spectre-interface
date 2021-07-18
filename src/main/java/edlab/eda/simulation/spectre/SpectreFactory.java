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

  public static void main(String[] args) throws IOException {

    Runtime rt = Runtime.getRuntime();
    String[] commands = { "spectre", "-W" };
    Process proc = rt.exec(commands);

    BufferedReader stdError = new BufferedReader(
        new InputStreamReader(proc.getErrorStream()));

    String s;

    s = stdError.readLine();

    Pattern pattern = Pattern
        .compile("sub-version[  ]+[0-9]+.[0-9]+.[0-9]+.[0-9]+.isr[0-9]+");

    System.out.println(s);

    Matcher matcher = pattern.matcher(s);
    boolean matchFound = matcher.find();

    System.out.println(matchFound);

  }

  private File simDirectory;

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
          .compile("sub-version[  ]+[0-9]+.[0-9]+.[0-9]+.[0-9]+.isr[0-9]+");

      Matcher matcher = pattern.matcher(retval);

      boolean matchFound = matcher.find();

      if (matchFound) {
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
}