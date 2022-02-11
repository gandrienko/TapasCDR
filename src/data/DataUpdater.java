package data;

import Util.CsvReader;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Timer;

/**
 * Updates the data about detected conflicts every X seconds (currently X==30).
 * Imitates data coming every X seconds by taking portions from a single long sequence.
 */

public class DataUpdater {
  /**
   * The list of all conflicts, including both detected and foreseen (due to some actions)
   */
  public ArrayList<Conflict> conflicts=null;
  /**
   * The list of all proposed actions
   */
  public ArrayList<Action> actions=null;
  /**
   * The data divided into portions
   */
  public ArrayList<DataPortion> portions=null;
  /**
   * The list of detected non-conformance events
   */
  public ArrayList<NCEvent> ncEvents=null;
  
  /**
   * Receives the full set of data and divides it into portions.
   * @return the number of data portions
   */
  public int addData(ArrayList<Conflict> newConflicts, ArrayList<Action> newActions) {
    if (newConflicts==null || newConflicts.isEmpty())
      return 0;
    if (conflicts==null)
      conflicts=newConflicts;
    else
      conflicts.addAll(newConflicts);
    if (actions==null)
      actions=newActions;
    else
      actions.addAll(newActions);
    
    if (actions!=null && !actions.isEmpty()) {
      for (Action a:actions)
        for (Conflict c:newConflicts)
          if (a.actionId.equals(c.actionId1))
            c.causeAction1=a;
          else
            if (a.actionId.equals(c.actionId2))
              c.causeAction2=a;
    }
  
    ArrayList<DataPortion> portions=new ArrayList<DataPortion>(20);
    
    DataPortion p=new DataPortion();
    p.conflicts=new ArrayList<Conflict>(20);
    p.timeUnix=newConflicts.get(0).detectionTimeUnix;
    p.conflicts.add(newConflicts.get(0));
    portions.add(p);
    
    for (int i=1; i<newConflicts.size(); i++) {
      Conflict c=newConflicts.get(i);
      if (c.detectionTimeUnix==p.timeUnix) { //the same portion continues
        int idx=p.getConflictIdx(c.conflictId);
        if (idx>=0) { //a conflict with the same ID already exists
          Conflict c0=p.conflicts.get(idx);
          if (c0.actionResults==null)
            c0.actionResults=new ArrayList<Conflict>(30);
          c0.actionResults.add(c);  //c would result from applying an action to c0
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
    if (newActions!=null && !newActions.isEmpty()) //attach actions to corresponding conflicts
      for (int i=0; i<newActions.size(); i++) {
        Action a=newActions.get(i);
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
        for (FlightInConflict f:c.flights)
          if (f.rtKey.equals(a.rtKey)) {
            a.callSign=f.callSign;
            a.flightId = f.flightId;
          }
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
      
    if (this.portions==null)
      this.portions=portions;
    else
      this.portions.addAll(portions);
    
    return portions.size();
  }
  
  /**
   * @return the number of events for which corresponding actions have been found
   */
  public int addNCEvents(ArrayList<NCEvent> events) {
    if (events==null || events.isEmpty())
      return 0;
    if (ncEvents==null)
      ncEvents=events;
    else
      ncEvents.addAll(events);
    int nActionsFound=0;
    if (actions!=null && !actions.isEmpty()) {
      for (Action a:actions)
        for (NCEvent e:events)
          if (a.actionId.equals(e.actionId)) {
            e.action=a;
            e.callSign=a.callSign;
            e.flightId=a.flightId;
            ++nActionsFound;
          }
    }
    if (portions!=null)
      for (DataPortion p:portions)
        for (NCEvent e:events)
          if (p.timeUnix==e.timeUnix) {
            if (p.ncEvents==null)
              p.ncEvents=new ArrayList<NCEvent>(10);
            p.ncEvents.add(e);
          }
    return nActionsFound;
  }
  
  public int getDataPortionsCount(){
    if (portions==null)
      return 0;
    return portions.size();
  }
  
  public int getLastPortionIdx(){
    return lastIdx;
  }
  
  public DataPortion getDataPortion(int pIdx) {
    if (portions==null || portions.isEmpty())
      return null;
    if (pIdx<0)
      pIdx=lastIdx+1;
    if (pIdx>=portions.size())
      return null;
    lastIdx=pIdx;
    return portions.get(pIdx);
  }
  
  //--------------- getting new data, which are added to the previously loaded data portions ---------
  
  public boolean takeNewData(String path) {
    if (path==null)
      return false;
    CsvReader csvReader=new CsvReader(path,"main.csv");
  
    ArrayList<Conflict> conflicts=DataReader.getConflictsFromMain(csvReader);
    if (conflicts==null) {
      System.out.println("Failed to get conflict data!");
      return false;
    }
    System.out.println("Got data about " + conflicts.size() + " conflicts");
    /*
    System.out.println("Primary conflicts:");
    for (int i=0; i<conflicts.size(); i++)
      if (conflicts.get(i).isPrimary)
        System.out.println(conflicts.get(i));
    */
  
    csvReader=new CsvReader(path,"conflicts.csv");
  
    int nOk=DataReader.getMoreConflictDataFromConflicts(csvReader,conflicts);
    System.out.println("Successfully identified conflicts and flights for "+nOk+" records");
  
    csvReader=new CsvReader(path,"resolution_actions_episode_1.csv");
    ArrayList<Action> actions=DataReader.getActions(csvReader);
    if (actions==null)
      System.out.println("Failed to get data about resolution actions!");
    
    int nPortions=addData(conflicts,actions);
    System.out.println("Got "+nPortions+" data portions!");
    if (nPortions<1)
      return false;
    
    csvReader=new CsvReader(path,"points_of_projection.csv");
    ArrayList<FlightPoint> pts=DataReader.getProjectionPoints(csvReader);
    if (pts!=null) {
      System.out.println("Got " + pts.size() + " projection points!");
      nOk=Conflict.attachProjectionPoints(conflicts,pts);
      System.out.println(nOk+" projection points have been attached to conflict descriptions");
    }
    else
      System.out.println("Failed to load projection points!");
    csvReader=new CsvReader(path,"non_conformance_events.csv");
    ArrayList<NCEvent> ncEvents=DataReader.getNonConformanceEvents(csvReader);
    if (ncEvents!=null) {
      System.out.println("Got "+ncEvents.size()+" non-conformance events!");
      nOk= addNCEvents(ncEvents);
      System.out.println("For "+nOk+" events the corresponding resolution actions have been found!");
    }
    else
      System.out.println("No non-conformance events found!");
    
    return true;
  }
  
  //--------------- used for simulating automatic updates ------------------------
  
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
