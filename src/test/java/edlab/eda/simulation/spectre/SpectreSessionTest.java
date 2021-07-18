package edlab.eda.simulation.spectre;

import java.io.File;
import java.util.List;

import edlab.eda.reader.nutmeg.NutmegPlot;

public class SpectreSessionTest {

  public static void main(String[] args) {

    SpectreFactory factory = SpectreFactory
        .getSpectreFactory(new File("/home/sim/schweikardt"));

    SpectreSession session = SpectreSession.getSession(factory);
    session.setNetlist(
        SpectreFactory.readFile(new File("./src/test/resources/input.scs")));

    List<NutmegPlot> plots;
    session.start();

    for (int i = 0; i < 12; i++) {
      session.setValueAttribute("VI", i);
      plots = session.simulate();
      System.out.println(i + " - " + plots.size());
    }
    
    session.stop();
  }
}
