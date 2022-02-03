package data;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Updates the data about detected conflicts every X seconds (currently X==30).
 * Imitates data coming every X seconds by taking portions from a single long sequence.
 */

public class DataUpdater {
  /**
   * The data divided into portions
   */
  public ArrayList<DataPortion> portions=null;
  
  /**
   * Receives the full set of data and divides it into portions.
   * @return the number of data portions
   */
  public int setFullData(ArrayList<Conflict> conflicts, ArrayList<Action> actions) {
    if (conflicts==null || conflicts.isEmpty())
      return 0;
    
    if (actions!=null && !actions.isEmpty()) {
      for (Action a:actions)
        for (Conflict c:conflicts)
          if (a.actionId.equals(c.actionId1))
            c.causeAction1=a;
          else
            if (a.actionId.equals(c.actionId2))
              c.causeAction2=a;
    }
    
    portions=new ArrayList<DataPortion>(20);
    
    DataPortion p=new DataPortion();
    p.conflicts=new ArrayList<Conflict>(20);
    p.timeUnix=conflicts.get(0).detectionTimeUnix;
    p.conflicts.add(conflicts.get(0));
    portions.add(p);
    
    for (int i=1; i<conflicts.size(); i++) {
      Conflict c=conflicts.get(i);
      if (c.detectionTimeUnix==p.timeUnix) { //the same portion continues
        int idx=p.getConflictIdx(c.conflictId);
        if (idx>=0) {
          Conflict c0=p.conflicts.get(idx);
          if (c0.actionResults==null)
            c0.actionResults=new ArrayList<Conflict>(30);
          c0.actionResults.add(c);
        }
        else
          p.conflicts.add(c);
      }
      else { //a new portion begins
        p=new DataPortion();
        p.conflicts=new ArrayList<Conflict>(20);
        p.timeUnix=c.detectionTimeUnix;
        p.conflicts.add(c);
        portions.add(p);
      }
    }
    if (actions!=null && !actions.isEmpty())
      for (int i=0; i<actions.size(); i++) {
        Action a=actions.get(i);
        //get time stamp from the action identifier
        int divIdx=a.actionId.indexOf("_");
        if (divIdx<=0)
          continue;
        long timeStamp=Long.parseLong(a.actionId.substring(0,divIdx));
        p=null;
        for (int j=0; j<portions.size() && p==null; j++)
          if (timeStamp==portions.get(j).timeUnix)
            p=portions.get(j);
        if (p==null)
          continue;
        int cIdx=p.getConflictIdx(a.conflictId);
        if (cIdx<0)
          continue;
        Conflict c=p.conflicts.get(cIdx);
        if (c.actions==null)
          c.actions=new ArrayList<Action>(50);
        c.actions.add(a);
        if (c.actionResults!=null)
          for (int j=0; j<c.actionResults.size(); j++) {
            Conflict rc=c.actionResults.get(j);
            if (a.actionId.equals(rc.actionId1) || a.actionId.equals(rc.actionId2)) {
              if (a.conflicts == null)
                a.conflicts = new ArrayList<Conflict>(5);
              a.conflicts.add(rc);
            }
          }
      }
    
    return portions.size();
  }
  
  public boolean hasNextPortion(){
    return portions!=null && lastIdx<portions.size();
  }
  
  public DataPortion getCurrentDataPortion() {
    if (portions==null || portions.isEmpty())
      return null;
    if (lastIdx<0 || lastIdx>=portions.size())
      lastIdx=0;
    return portions.get(lastIdx);
  }
  
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
  
  private boolean isRunning=false, toStop=false;
  /**
   * Time interval, in seconds, between receiving/sending data portions
   */
  public int timeStep=10;
  /**
   * The index of the last conflict sent
   */
  public int lastIdx=-1;
  
  private Timer timer=null;
  
  public void setTimeStep(int timeStep) {
    if (this.timeStep==timeStep)
      return;
    this.timeStep = timeStep;
    if (isRunning && timer!=null) {
      timer.cancel();
      timer=new Timer();
      timer.scheduleAtFixedRate(new DataUpdateTask(this),0,timeStep*1000);
    }
  }
  
  public boolean isRunning() {
    return isRunning;
  }
  
  public boolean startAutoUpdating(int fromIdx) {
    if (portions==null || portions.isEmpty())
      return false;
    if (isRunning || timer!=null)
      return false;
    if (fromIdx<0 || fromIdx>=portions.size())
      fromIdx=0;
    setToStop(false);
    lastIdx=fromIdx-1;
    timer=new Timer();
    timer.scheduleAtFixedRate(new DataUpdateTask(this),0,timeStep*1000);
    return true;
  }
  
  public void setToStop(boolean toStop) {
    this.toStop = toStop;
    stopTimer();
    isRunning=false;
  }
  
  protected void stopTimer() {
    if (timer!=null) {
      timer.cancel();
      timer=null;
    }
  }
  
  public void run() {
    if (toStop) {
      stopTimer();
      isRunning=false;
      toStop=false;
      return;
    }
    isRunning=true;
    ++lastIdx;
    if (lastIdx>=portions.size()) {
      stopTimer();
      isRunning = false;
    }
    //notify about the next portion or the end of the process
    notifyChange();
  }
}
