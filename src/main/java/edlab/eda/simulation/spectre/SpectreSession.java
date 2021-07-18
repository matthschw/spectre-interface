package edlab.eda.simulation.spectre;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edlab.eda.reader.nutmeg.NutReader;
import edlab.eda.reader.nutmeg.NutmegPlot;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;

public class SpectreSession {

  private static final String NL_FILE_NAME = "input";
  private static final String NL_FILE_NAME_EXTENTION = "scs";
  private static final String LOG_FILENAME = "spectre.out";
  private static final String RAW_FILE_NAME_EXTENTION = "raw";
  private static final String AHDLLIB_DIRNAME = "ahdl";

  private static enum MODE {
    BIT32, BIT64
  };

  private static enum RESULT_FMT {
    NUTBIN, NUTASCII
  };

   static final String CMD_TOOL = "spectre";

  private String netlist;
  private MODE mode;
  private Process process = null;
  private RESULT_FMT resultFmt = RESULT_FMT.NUTBIN;
  private File workingDir;
  private File rawFile;
  private int noOfThreads;

  private Expect expect = null;
  private Map<String, String> parameterMapping = new HashMap<String, String>();
  private Map<String, Object> parameterValues = new HashMap<String, Object>();
  private Date lastActivity = null;
  private SpectreWatchdog watchdog;

  private Set<File> includeDirectories = new HashSet<File>();

  private SpectreSession(File simDir) {

    this.mode = MODE.BIT64;
    this.noOfThreads = 1;

    String username = System.getProperty("user.name");

    try {

      Path path = Files.createTempDirectory(simDir.toPath(),
          "spectre" + "_" + username + "_");

      this.workingDir = path.toFile();

    } catch (IOException e) {
      e.printStackTrace();
    }

    rawFile = new File(workingDir.toString() + "/" + NL_FILE_NAME + "."
        + RAW_FILE_NAME_EXTENTION);
  }

  private boolean writeNetlist() {

    if (netlist != null) {
      try {
        FileWriter writer = new FileWriter(getNetlistPath(), false);
        writer.write(netlist);
        writer.close();
        return true;
      } catch (IOException e) {
        System.err.println("Unable to write netlist:\n" + e.getMessage());
        return false;
      }
    } else {
      return false;
    }
  }

  public void setNetlist(String netlist) {

    this.netlist = netlist;

    if (isRunning()) {
      stop();
      writeNetlist();
      start();
    }
  }

  public void addIncludeDirectory(File includeDiretory) {

    this.includeDirectories.add(includeDiretory);

    if (isRunning()) {
      stop();
      writeNetlist();
      start();
    }
  }

  private String getNetlistPath() {
    return workingDir.getAbsolutePath() + "/" + NL_FILE_NAME + "."
        + NL_FILE_NAME_EXTENTION;
  }

  private String getNetlistName() {
    return NL_FILE_NAME + "." + NL_FILE_NAME_EXTENTION;
  }

  private String getCmd() {

    String cmd = CMD_TOOL;

    if (mode == MODE.BIT64) {
      cmd += " -64";
    } else if (mode == MODE.BIT32) {
      cmd += " -32";
    }

    cmd += " +interactive";

    if (resultFmt == RESULT_FMT.NUTBIN) {
      cmd += " -format nutbin";
    } else if (resultFmt == RESULT_FMT.NUTASCII) {
      cmd += " -format nutascii";
    }

    if (noOfThreads > 1) {
      cmd += " +multithread=" + noOfThreads;
    }

    cmd += " -ahdllibdir ./" + AHDLLIB_DIRNAME;
    cmd += " =log " + LOG_FILENAME;

    for (File file : includeDirectories) {
      cmd += " -I" + file.getAbsolutePath();
    }

    cmd += " " + getNetlistName();

    return cmd;
  }

  Date getLastActivity() {
    return lastActivity;
  }

  private boolean isRunning() {
    if (process == null || !process.isAlive()) {
      return false;
    } else {
      return true;
    }
  }

  public boolean start() {

    writeNetlist();

    if (!isRunning()) {
      try {
        this.process = Runtime.getRuntime().exec(getCmd() + "\n", null,
            workingDir);

      } catch (IOException e) {
        System.err.println(
            "Unable to execute simulator" + " with error:\n" + e.getMessage());
        return false;
      }

      try {
        expect = new ExpectBuilder().withInputs(this.process.getInputStream())
            .withOutput(this.process.getOutputStream()).withExceptionOnFailure()
            .build();

        expect.expect(SpectreInteractiveProtocol.NEXT_COMMAND);

        for (String param : this.parameterValues.keySet()) {
          this.setValueAttribute(param, this.parameterValues.get(param));
        }

        this.lastActivity = new Date();
        this.watchdog = new SpectreWatchdog(this);
        this.watchdog.start();

      } catch (IOException e) {

        System.err.println(
            "Unable to execute expect" + " with error:\n" + e.getMessage());

        this.process.destroy();
        return false;
      }
    }

    return true;
  }

  public List<NutmegPlot> simulate() {

    if (!this.isRunning()) {
      this.start();
    }

    if (isRunning()) {

      List<NutmegPlot> plots = new LinkedList<NutmegPlot>();

      if (!setResultDir()) {
        System.err.println("Unable to set result dir");
        return plots;
      }

      String res = communicate(SpectreInteractiveProtocol
          .formatCommand(new String[] { SpectreInteractiveProtocol.CMD_RUN,
              SpectreInteractiveProtocol.formatComString("all") }));

      res = res.trim();

      if (res.equals(SpectreInteractiveProtocol.RTN_TRUE)) {

        if (rawFile.exists()) {

          NutReader reader = null;

          if (this.resultFmt == RESULT_FMT.NUTASCII) {

            reader = NutReader.getNutasciiReader(rawFile.toString());

          } else if (this.resultFmt == RESULT_FMT.NUTBIN) {

            reader = NutReader.getNutbinReader(rawFile.toString());
          }

          plots = reader.read().parse().getPlots();

        } else {
          return simulate();
        }

        this.lastActivity = new Date();

      }

      if (rawFile.exists()) {
        rawFile.delete();
      }

      return plots;

    } else {

      System.err.println("Unable to start session");
      return null;
    }
  }

  public boolean setValueAttribute(String parameter, Object value) {

    if (!this.parameterMapping.containsKey(parameter)) {
      if (!readParameterIdentififer(parameter)) {
        System.out.println(
            "Parameter=" + parameter + " is not defined in the netlist");
        return false;
      }
    }

    String res = communicate(SpectreInteractiveProtocol.formatCommand(
        new String[] { SpectreInteractiveProtocol.CMD_SET_ATTRIBUTE,
            SpectreInteractiveProtocol
                .formatComString(this.parameterMapping.get(parameter)),
            SpectreInteractiveProtocol.formatComString("value"),
            value.toString() }));

    res = res.trim();

    if (res.equals(SpectreInteractiveProtocol.RTN_TRUE)) {
      this.parameterValues.put(parameter, value);
      return true;
    } else {
      return false;
    }
  }

  private boolean setResultDir() {

    String res = communicate(SpectreInteractiveProtocol.formatCommand(
        new String[] { SpectreInteractiveProtocol.CMD_SET_RES_DIR,
            SpectreInteractiveProtocol.formatComString("./") }));
    res = res.trim();

    if (res.equals(SpectreInteractiveProtocol.RTN_TRUE)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean readParameterIdentififer(String parameter) {

    String res = communicate(
        SpectreInteractiveProtocol
            .formatCommand(
                new String[] { SpectreInteractiveProtocol.CMD_GET_PARAMETER,
                    SpectreInteractiveProtocol.formatCommand(new String[] {
                        SpectreInteractiveProtocol.CMD_GET_CIRCUT,
                        SpectreInteractiveProtocol.formatComString("") }),
                    SpectreInteractiveProtocol.formatComString(parameter) }));

    if (res != null) {

      res = SpectreInteractiveProtocol.getComString(res);

      if (res != null) {
        this.parameterMapping.put(parameter.toString(), res);
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean stop() {

    communicate(SpectreInteractiveProtocol
        .formatCommand(new String[] { SpectreInteractiveProtocol.CMD_QUIT }));

    try {
      this.expect.close();
    } catch (IOException e) {
    }

    if (this.process != null) {
      this.process.destroyForcibly();
    }

    this.lastActivity = null;
    this.parameterMapping = new HashMap<String, String>();

    if (this.watchdog instanceof SpectreWatchdog) {
      this.watchdog.kill();
    }

    this.watchdog = null;
    this.process = null;


    return true;
  }

  @SuppressWarnings("unused")
  private int readPid() {
    String res = communicate(SpectreInteractiveProtocol.formatCommand(
        new String[] { SpectreInteractiveProtocol.CMD_GET_PID }));

    if (res == null) {
      return -1;
    } else {
      res = res.trim();
      return Integer.parseInt(res);
    }
  }

  private String communicate(String cmd) {

    String retval = null;

    if (this.watchdog != null) {
      this.watchdog.kill();
    }

    try {

      this.expect.send(cmd + "\n");
      retval = expect.expect(SpectreInteractiveProtocol.NEXT_COMMAND)
          .getBefore();

    } catch (Exception e) {
    }

    this.lastActivity = new Date();
    this.watchdog = new SpectreWatchdog(this);
    this.watchdog.start();

    return retval;
  }

  public static SpectreSession getSession(SpectreFactory factory) {
    return new SpectreSession(factory.getSimDirectory());
  }

  public static SpectreSession getSession(String name, SpectreFactory factory) {
    return new SpectreSession(factory.getSimDirectory());
  }

}