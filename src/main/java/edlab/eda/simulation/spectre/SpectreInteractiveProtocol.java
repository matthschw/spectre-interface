package edlab.eda.simulation.spectre;

import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.Matchers;

public interface SpectreInteractiveProtocol {

  static final String CMD_GET_PARAMETER = "sclGetParameter";
  static final String CMD_LIST_PARAMETER = "sclListParameter";
  static final String CMD_GET_ATTRIBUTE = "sclGetAttribute";
  static final String CMD_SET_ATTRIBUTE = "sclSetAttribute";
  static final String CMD_LIST_ATTRIBUTE = "sclListAttribute";
  static final String CMD_GET_ANALYSIS = "sclGetAnalysis";
  static final String CMD_GET_CIRCUT = "sclGetCircuit";
  static final String CMD_GET_INSTANCE = "sclGetInstance";
  static final String CMD_GET_MODEL = "sclGetModel";
  static final String CMD_GET_PRIMITIVE = "sclGetPrimitive";
  static final String CMD_LIST_ANALYSIS = "sclListAnalysis";
  static final String CMD_LIST_CIRCUIT = "sclListCircuit";
  static final String CMD_LIST_INSTANCE = "sclListInstance";
  static final String CMD_LIST_MODEL = "sclListModel";
  static final String CMD_LIST_NET = "sclListNet";
  static final String CMD_LIST_PRIMITIVE = "sclListPrimitive";
  static final String CMD_CREATE_ANALYSIS = "sclCreateAnalysis";
  static final String CMD_RELEASE_OBJ = "sclReleaseObject";
  static final String CMD_RUN = "sclRun";
  static final String CMD_RUN_ANALYSIS = "sclRunAnalysis";
  static final String CMD_GET_ERROR = "sclGetError";
  static final String CMD_GET_RES_DIR = "sclGetResultDir";
  static final String CMD_SET_RES_DIR = "sclSetResultDir";
  static final String CMD_GET_PID = "sclGetPid";
  static final String CMD_HELP = "sclHelp";
  static final String CMD_QUIT = "sclQuit";
  static final String CMD_REG_MEAS = "mdlRegMeasurement";
  static final String CMD_LIST_ALIAS_MEAS = "mdlListAliasMeasurement";
  static final String CMD_MDL_RUN = "mdlRun";
  static final String CMD_DEL_MEAS = "mdlDelMeasurement";

  static final String RTN_TRUE = "t";
  static final Matcher<Result> NEXT_COMMAND = Matchers.regexp("\n>");

  public static String formatComString(String val) {
    return "\"" + val + "\"";
  }

  public static boolean isComString(String val) {

    val = val.trim();

    if (val instanceof String && val.length() > 1) {

      return val.substring(0, 1).equals("\"")
          && val.substring(val.length() - 1, val.length()).equals("\"");
    } else {
      return false;
    }
  }

  public static String getComString(String val) {

    val = val.trim();

    if (isComString(val)) {
      return val.substring(1, val.length() - 1);
    } else {
      return null;
    }
  }

  public static String formatCommand(Object[] cmd) {

    String res = "(";

    for (int i = 0; i < cmd.length; i++) {
      if (i > 0) {
        res += " ";
      }
      res += cmd[i];
    }

    res += ")";

    return res;
  }
}