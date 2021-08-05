package edlab.eda.simulation.spectre;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;

import edlab.eda.reader.nutmeg.NutmegPlot;

import org.junit.jupiter.api.Test;

public class SpectreSessionTest {

  private static final String[] PLOTNAMES = { "DC Analysis `dc1'",
      "DC Analysis `dc2': VI = (0 -> 10)",
      "AC Analysis `ac': freq = (1 Hz -> 1 GHz)",
      "Transient Analysis `tran': time = (0 s -> 5 ns)" };
  private static final int[] NUM_OF_POINTS = { 1, 51, 51, 56 };
  private static final int[] NUM_OF_WAVES = { 5, 5, 5, 5 };

  @Test
  void test() {
    simulate();
  }

  public static void simulate() {

    SpectreFactory factory = SpectreFactory.getSpectreFactory(new File("/tmp"));

    if (factory == null) {
      fail("Cannot call simulator");
    }

    SpectreSession session = SpectreSession.getSession(factory);

    if (session == null) {
      fail("Cannot create session");
    }
    
    
    session.setNetlist(
        SpectreFactory.readFile(new File(session.getResourcePath("input.scs"))));

    List<NutmegPlot> plots;

    if (!session.start()) {
      fail("Cannot start session");
    }

    plots = session.simulate();

    NutmegPlot nutmegPlot;

    for (int i = 0; i < PLOTNAMES.length; i++) {

      nutmegPlot = plots.get(i);

      if (!PLOTNAMES[i].equals(nutmegPlot.getPlotname())) {
        fail("Plotname: " + PLOTNAMES[i] + " mismatch with "
            + nutmegPlot.getPlotname());
      }

      if (NUM_OF_WAVES[i] != nutmegPlot.getNoOfWaves()) {
        fail("Num of waves: " + NUM_OF_WAVES[i] + " mismatch with "
            + nutmegPlot.getNoOfWaves());
      }

      if (NUM_OF_POINTS[i] != nutmegPlot.getNoOfPoints()) {
        fail("Num of points: " + NUM_OF_POINTS[i] + " mismatch with "
            + nutmegPlot.getNoOfPoints());
      }
    }

    if (!session.stop()) {
      fail("Cannot stop session");
    }
  }
}
