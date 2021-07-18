package edlab.eda.simulation.spectre;

import java.util.Date;

public class SpectreWatchdog extends Thread {

  private SpectreSession session;

  private static final long WATCHDOG_DEFAULT_CHECK_TIME = 30;
  private static final long WATCHDOG_DEFAULT_WAIT_TIME = 30;

  private long watchdogWaitTime;
  private boolean killed = false;

  public SpectreWatchdog(SpectreSession session) {
    this.session = session;
    this.watchdogWaitTime = WATCHDOG_DEFAULT_WAIT_TIME;

    if (this.watchdogWaitTime <= 0) {
      this.watchdogWaitTime = WATCHDOG_DEFAULT_WAIT_TIME;
    }
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

      if (contineWatching && (lastActivity == null || now.getTime()
          - lastActivity.getTime() > 1000 * this.watchdogWaitTime)) {

        if (!killed) {
          this.session.stop();
        }
      }
    }
  }

  public void kill() {
    this.killed = true;
  }
}
