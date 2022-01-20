package data;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Describes a single conflict event between two aircraft flights.
 * A conflict event is described by one record in the file Main.csv.
 * Each conflict is identified by the fields:
 * Conflict_ID,
 * due_to_flight_1,
 * due_to_flight_2, and
 * command_category.
 */

public class Conflict {
  public static final int type_Conflict=1, type_Alert=2, type_LoS=3;
  public static final String typeStrings[]={"Conflict", "Alert", "loss"};
  /**
   * Main.csv: conflict_ID
   * This ID has the following form: TimePoint_FlightID1_FlightID2
   * Where FlightIDx is a flight in conflict/loss.
   * An event can be a conflict(C), loss of separation (LoS).
   * Note: the conflictId is not a unique identifier of an event.
   */
  public String conflictId=null;
  /**
   * Main.csv: event_type
   * Type of event;	This can  be Conflict, Alert, LoS
   */
  public String type=null;
  /**
   * One of the constants type_Conflict=1, type_Alert=2, type_LoS=3
   */
  public int typeNum=0;
  /**
   * Main.csv: TimePoint (UNIX time; needs to be transformed)
   * Time of event detection
   */
  public LocalDateTime detectionTime=null;
  /**
   * Main.csv: TimePoint
   * The original Unix time of event detection
   */
  public long detectionTimeUnix=0;
  /**
   * Main.csv: projection_time_horizon
   * Time horizon (length of the time interval, in seconds) that was used for the
   * projection of the aircraft positions towards the future based on which
   * the event was detected.
   */
  public int timeHorizon=0;
  /**
   * Information about the flights involved in the conflict.
   * Includes two elements
   */
  public FlightInConflict flights[]=null;
  /**
   * Whether the conflict is primary (= true) or
   * it has emerged due to a previous conflict resolution action (= false).
   */
  public boolean isPrimary=true;
  /**
   * Main.csv: due_to_flight_1, due_to_flight_2
   * If the conflict emerged due to a previous action or actions applied to
   * one or both of the flights, these are the identifiers of the actions
   */
  public String actionId1=null, actionId2=null;
  /**
   * Main.csv: command_category
   * If the conflict emerged due to a previous action or actions, this is
   * the command category: either "foreseen" or "issued"
   * “foreseen”: the action is in the top-3 resolution actions proposed by the XAI
   * “issued”: the resolution action was issued in the previous timestep
   */
  public String commandCategory=null;
  
  public static int getTypeNum(String sValue) {
    for (int k=0; k<typeStrings.length; k++)
      if (sValue.equalsIgnoreCase(typeStrings[k]))
        return k+1;
    return 0;
  }
  
  public String toString() {
    String str="Event id="+conflictId+"; type="+type+"; detection time="+detectionTime+"; isPrimary="+isPrimary;
    if (flights!=null)
      str+="; flights "+flights[0].flightId+FlightInConflict.phaseCodes[flights[0].phaseNum]+
               " + "+flights[1].flightId+FlightInConflict.phaseCodes[flights[1].phaseNum];
    return str;
  }
  
  /**
   * A detected conflict is uniquely identified by these four fields
   */
  public boolean sameConflict(String conflictId,String actionId1,String actionId2,String commandCategory) {
    if (!conflictId.equals(this.conflictId))
      return false;
    if (this.actionId1==null)
      if (actionId1!=null)
        return false;
      else;
    else
      if (!this.actionId1.equals(actionId1))
        return false;
    if (this.actionId2==null)
      return actionId2==null;
    if (!this.actionId2.equals(actionId2))
      return false;
    if (this.commandCategory==null)
      return commandCategory==null;
    return this.commandCategory.equals(commandCategory);
  }
  
  public static double getMaxHorDistance(ArrayList<Conflict> conflicts) {
    if (conflicts==null || conflicts.isEmpty())
      return 0;
    double maxD=0;
    for (Conflict c:conflicts) 
      if (c.flights!=null)
        for (FlightInConflict f:c.flights) {
          if (f.closest != null && f.closest.hDistance > maxD)
            maxD = f.closest.hDistance;
          if (f.first != null && f.first.hDistance > maxD)
            maxD = f.first.hDistance;
          if (f.last != null && f.last.hDistance > maxD)
            maxD = f.last.hDistance;
          if (f.crossing != null && f.crossing.hDistance > maxD)
            maxD = f.crossing.hDistance;
        }
    return maxD;    
  }
  
  public static int getMaxVertDistance(ArrayList<Conflict> conflicts) {
    if (conflicts==null || conflicts.isEmpty())
      return 0;
    int maxD=0;
    for (Conflict c:conflicts)
      if (c.flights!=null)
        for (FlightInConflict f:c.flights) {
          if (f.closest != null && f.closest.vDistance > maxD)
            maxD = f.closest.vDistance;
          if (f.first != null && f.first.vDistance > maxD)
            maxD = f.first.vDistance;
          if (f.last != null && f.last.vDistance > maxD)
            maxD = f.last.vDistance;
          if (f.crossing != null && f.crossing.vDistance > maxD)
            maxD = f.crossing.vDistance;
        }
    return maxD;
  }
}
