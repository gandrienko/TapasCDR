package map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class TimeTransmitter {
  public long timeUnix=0;
  protected ArrayList<ChangeListener> changeListeners=null;
  
  public void addChangeListener(ChangeListener l) {
    if (changeListeners==null)
      changeListeners=new ArrayList(5);
    if (!changeListeners.contains(l))
      changeListeners.add(l);
  }
  
  public void removeChangeListener(ChangeListener l) {
    if (l!=null && changeListeners!=null)
      changeListeners.remove(l);
  }
  
  public void notifyChange(){
    if (changeListeners==null || changeListeners.isEmpty())
      return;
    ChangeEvent e=new ChangeEvent(this);
    for (ChangeListener l:changeListeners)
      l.stateChanged(e);
  }
  public String getTimeText() {
    if (timeUnix<=0)
      return null;
    LocalDateTime dt=LocalDateTime.ofEpochSecond(timeUnix,0, ZoneOffset.UTC);
    return String.format("%02d:%02d:%02d",dt.getHour(),dt.getMinute(),dt.getSecond());
  }
}
