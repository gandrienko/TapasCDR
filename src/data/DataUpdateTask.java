package data;

import java.util.TimerTask;

public class DataUpdateTask extends TimerTask {
  public DataUpdater dUp=null;
  
  public DataUpdateTask(DataUpdater dUp) {
    super();
    this.dUp=dUp;
  }
  
  public void run() {
    dUp.run();
  }
}
