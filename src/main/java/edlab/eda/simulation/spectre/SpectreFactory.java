package edlab.eda.simulation.spectre;

import java.io.File;

public class SpectreFactory {

  private File simDirectory;

  private SpectreFactory(File simDirectory) {
    this.simDirectory = simDirectory;
  }

  public static SpectreFactory getSpectreFactory(File simDirectory) {

    if (simDirectory.isDirectory() && simDirectory.canRead()
        && simDirectory.canWrite()) {

      return new SpectreFactory(simDirectory);

    }
    return null;
  }

  public File getSimDirectory() {
    return simDirectory;
  }
}