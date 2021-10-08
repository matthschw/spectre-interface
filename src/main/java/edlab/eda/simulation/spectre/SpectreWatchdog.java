package edlab.eda.simulation.spectre;

import java.util.Date;

public class SpectreWatchdog extends Thread {

  private SpectreSession session;

  private static final long WATCHDOG_DEFAULT_CHECK_TIME = 100;
  static final long WATCHDOG_DEFAULT_WAIT_TIME = 300000;

  private long watchdogWaitTime;
  private boolean killed = false;

  public SpectreWatchdog(SpectreSession session, SpectreFactory factory) {
    this.session = session;
    this.watchdogWaitTime = factory.getWatchdogWaitTime();
  }

  @Override
  public void run() {
    Date now, lastActivity;

    boolean contineWatching = true;

    while (contineWatching) {

      try {
        Thread.sleep(SpectreWatchdog.WATCHDOG_DEFAULT_CHECK_TIME);
      } catch (InterruptedException e) {
      }

      if (killed) {
        contineWatching = false;
      }

      now = new Date();
      lastActivity = this.session.getLastActivity();

      if (contineWatching && (lastActivity == null
          || now.getTime() - lastActivity.getTime() > this.watchdogWaitTime)) {

        if (!killed) {
          this.session.stop();
          contineWatching = false;
        }
      }
    }
  }

  public void kill() {
    this.killed = true;
  }
}
