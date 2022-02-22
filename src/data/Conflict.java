package data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
   * Relative horizontal speed, knots per minute
   */
  public double relSpeedH=Double.NaN;
  /**
   * Intervals of the rate of horizontal closure:
   * 0 (NONE)   : relSpeedH <= 0
   * 1 (lOW)    : 0 < relSpeedH <= 85
   * 2 (MEDIUM) : 85 < relSpeedH <= 205
   * 4 (HIGH)   : 205 < relSpeedH <= 700
   * 5 (VERY HIGH) : 700 < relSpeedH
   */
  public int rateOfClosureH=0;
  /**
   * Relative vertical speed, feet per minute
   */
  public double relSpeedV=Double.NaN;
  /**
   * Intervals of the rate of vertical closure:
   * 0 (NONE)   : relSpeedV <= 0
   * 1 (lOW)    : 0 < relSpeedV <= 1000
   * 2 (MEDIUM) : 1000 < relSpeedV <= 2000
   * 4 (HIGH)   : 2000 < relSpeedV <= 4000
   * 5 (VERY HIGH) : 4000 < relSpeedV
   */
  public int rateOfClosureV=0;
  
  /**
   * The Measure Of Compliance (MOC), which is the biggest distance apart (expressed as a %)
   *
   * For example two flights that have 0NM separation (lateral) but 950ft vertical are separated
   * by 950ft which is usually within an acceptable tolerance for most places using 1000ft vertical
   * - the MOC in this case would be 95% (not Zero).
   *
   * Similarly two flights that are only 3NM apart (lateral) with 5NM required and 300 Feet vertical
   * have 60% MOC in the lateral and only 30% in vertical BUT the actual MOC is 60% not 30% since
   * this is the furthest distance apart.
   *
   * Expressed another way - two flights with 0NM separation lateral but 1000ft vertical are
   * not in conflict. Also two flights with 5NM lateral but 0ft vertical are also separated.
   *
   * For a flight involved in a conflict, the MOC is the minimum of the MOCs of the
   * specified conflict points.
   */
  public double measureOfCompliance=Double.NaN;
  /**
   * Severity, computed as a weighted sum of the measure of compliance (MoC) and rate of closure (RoC)
   * Weights: 20 for separation according to the MoC and 10 for RoC
   * Separation intervals according to MoC and their values:
   * 0: 100% <= MoC
   * 1: 75% < MoC < 100%
   * 3: 50% < MoC <= 75%
   * 7: 25% < MoC <=50%
   * 10: MoC <=25%
   */
  public double severity=Double.NaN;
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
   * The action(s) this conflict results from
   */
  public Action causeAction1=null, causeAction2=null;
  /**
   * Main.csv: command_category
   * If the conflict emerged due to a previous action or actions, this is
   * the command category: either "foreseen" or "issued"
   * “foreseen”: the action is in the top-3 resolution actions proposed by the XAI
   * “issued”: the resolution action was issued in the previous timestep
   */
  public String commandCategory=null;
  /**
   * Suggested resolution actions
   */
  public ArrayList<Action> actions=null;
  /**
   * How this conflict will change depending on the resolution actions taken
   */
  public ArrayList<Conflict> actionResults=null;
  
  public static int getTypeNum(String sValue) {
    for (int k=0; k<typeStrings.length; k++)
      if (sValue.equalsIgnoreCase(typeStrings[k]))
        return k+1;
    return 0;
  }
  
  /**
   * Computes the Measure Of Compliance (MOC), which is the biggest distance apart (expressed as a %)
   *
   * For example two flights that have 0NM separation (lateral) but 950ft vertical are separated
   * by 950ft which is usually within an acceptable tolerance for most places using 1000ft vertical
   * - the MOC in this case would be 95% (not Zero).
   *
   * Similarly two flights that are only 3NM apart (lateral) with 5NM required and 300 Feet vertical
   * have 60% MOC in the lateral and only 30% in vertical BUT the actual MOC is 60% not 30% since
   * this is the furthest distance apart.
   *
   * Expressed another way - two flights with 0NM separation lateral but 1000ft vertical are
   * not in conflict. Also two flights with 5NM lateral but 0ft vertical are also separated.
   *
   * @return the Measure Of Compliance (MOC)
   */
  public double getComplianceMeasure () {
    if (!Double.isNaN(measureOfCompliance))
      return measureOfCompliance;
    if (flights==null)
      return Double.NaN;
    for (int i=0; i<flights.length; i++) {
      double c=flights[i].getComplianceMeasure();
      if (!Double.isNaN(c) && (Double.isNaN(measureOfCompliance) || measureOfCompliance>c))
        measureOfCompliance=c;
    }
    return measureOfCompliance;
  }
  /**
   * Computes severity as a sum of the measure of compliance (MoC) and rate of closure (RoC)
   * Separation intervals according to MoC and their values:
   * 0: 100% <= MoC
   * 1: 75% < MoC < 100%
   * 3: 50% < MoC <= 75%
   * 7: 25% < MoC <=50%
   * 10: MoC <=25%
   * Intervals of the rate of horizontal closure:
   * 0 (NONE)   : relSpeedH <= 0
   * 1 (lOW)    : 0 < relSpeedH <= 85
   * 2 (MEDIUM) : 85 < relSpeedH <= 205
   * 4 (HIGH)   : 205 < relSpeedH <= 700
   * 5 (VERY HIGH) : 700 < relSpeedH
   * Intervals of the rate of vertical closure:
   * 0 (NONE)   : relSpeedV <= 0
   * 1 (lOW)    : 0 < relSpeedV <= 1000
   * 2 (MEDIUM) : 1000 < relSpeedV <= 2000
   * 4 (HIGH)   : 2000 < relSpeedV <= 4000
   * 5 (VERY HIGH) : 4000 < relSpeedV
   */
  public double getSeverity() {
    if (!Double.isNaN(severity))
      return severity;
    if (flights==null)
      return Double.NaN;
    if (Double.isNaN(measureOfCompliance)) {
      getComplianceMeasure();
      if (Double.isNaN(measureOfCompliance))
        return Double.NaN;
    }
    if (Double.isNaN(relSpeedH) || Double.isNaN((relSpeedV))) {
      ConflictPoint cp = flights[0].closest, cpPrev = flights[0].first;
      if (cp.pointTimeUnix > cpPrev.pointTimeUnix) {
        relSpeedV=(cpPrev.vDistance-cp.vDistance)/(cp.pointTimeUnix-cpPrev.pointTimeUnix)*60;
        rateOfClosureV=(relSpeedV<=0)?0:(relSpeedV<=1000)?1:(relSpeedV<=2000)?2:(relSpeedV<=4000)?4:5;
        if (flights[0].crossing!=null &&
                flights[0].crossing.pointTimeUnix<cp.pointTimeUnix &&
                flights[0].crossing.pointTimeUnix>cpPrev.pointTimeUnix)
          cp=flights[0].crossing;
        relSpeedH=(cpPrev.hDistance-cp.hDistance)/(cp.pointTimeUnix-cpPrev.pointTimeUnix)*3600;
        rateOfClosureH=(relSpeedH<=0)?0:(relSpeedH<=85)?1:(relSpeedH<=205)?2:(relSpeedH<=700)?4:5;
      }
      int rateOfClosure=Math.max(rateOfClosureH,rateOfClosureV);
      int rateMoC=(measureOfCompliance>=100)?
                      0:(measureOfCompliance>75)?
                            1:(measureOfCompliance>50)?
                                  3:(measureOfCompliance>25)?7:10;
      severity=rateMoC+rateOfClosure;
    }
    return severity;
  }
  
  public double getRelSpeedH() {
    if (Double.isNaN(severity))
      getSeverity();
    if (Double.isNaN(relSpeedH))
      return 0;
    return relSpeedH;
  }
  
  public double getRelSpeedV() {
    if (Double.isNaN(severity))
      getSeverity();
    return relSpeedV;
  }
  
  public String getCause() {
    if (causeAction1==null && causeAction2==null)
      return null;
    String s="";
    for (int i=0; i<2; i++) {
      Action a=(i==0)?causeAction1:causeAction2;
      if (a==null)
        continue;
      if (s.length()>0)
        s+=" + ";
      else {
        int divIdx=a.actionId.indexOf("_");
        if (divIdx>0) {
          long timeStamp = Long.parseLong(a.actionId.substring(0, divIdx));
          LocalDateTime dt=LocalDateTime.ofEpochSecond(timeStamp,0, ZoneOffset.UTC);
          s=String.format("%02d:%02d:%02d : ",dt.getHour(),dt.getMinute(),dt.getSecond());
        }
      }
      s+=a.actionType+((a.actionValue==0)?"":" by "+a.actionValue)+" to "+a.flightId;
    }
    s+="; "+commandCategory;
    return s;
  }
  
  public String getCauseHTML() {
    if (causeAction1==null && causeAction2==null)
      return null;
    String s="";
    for (int i=0; i<2; i++) {
      Action a=(i==0)?causeAction1:causeAction2;
      if (a==null)
        continue;
      if (s.length()>0)
        s+=" and ";
      else {
        int divIdx=a.actionId.indexOf("_");
        if (divIdx>0) {
          long timeStamp = Long.parseLong(a.actionId.substring(0, divIdx));
          LocalDateTime dt=LocalDateTime.ofEpochSecond(timeStamp,0, ZoneOffset.UTC);
          s=String.format("%02d:%02d:%02d %s:",dt.getHour(),dt.getMinute(),dt.getSecond(),commandCategory);
        }
      }
      s+="<p><b>"+a.actionType+"</b> ("+Action.getMeaningOfActionType(a.actionType)+")"+
             ((a.actionValue==0)?"":" by "+a.actionValue)+" applied to <b>"+
             a.flightId+"</b></p>";
    }
    return "<html><body>"+s+"</body></html>";
  }
  
  public String toString() {
    String str="Event id="+conflictId+"; type="+type+"; detection time="+detectionTime+"; isPrimary="+isPrimary;
    if (flights!=null)
      str+="; flights "+flights[0].flightId+FlightInConflict.phaseCodes[flights[0].phaseNum]+
               " + "+flights[1].flightId+FlightInConflict.phaseCodes[flights[1].phaseNum];
    return str;
  }
  
  public String getDescriptionBody () {
    if (flights==null)
      return null;
    String s=String.format("<p align=center>Conflict of <b>%s</b> and <b>%s</b></p>",
        flights[0].flightId,flights[1].flightId);
    if (!Double.isNaN(getSeverity())) {
      s += "<table border=0 cellmargin=3 cellpadding=1 cellspacing=3 align=left>";
      s += String.format("<tr><td>Severity</td><td align=right><b>%.0f</b></td></tr>",
          getSeverity());
      s += String.format("<tr><td>Measure of compliance</td><td align=right><b>%.2f</b></td></tr>",
          getComplianceMeasure());
      if (!Double.isNaN(relSpeedH))
        s += String.format("<tr><td>Hor. relative speed</td><td align=right><b>%.2f</b></td><td>nm/min.</td></tr>", relSpeedH);
      if (!Double.isNaN(relSpeedV))
        s += String.format("<tr><td>Vert. relative speed</td><td align=right><b>%.0f</b></td><td>feet/min.</td></tr>", relSpeedV);
      s += "</table>";
    }
    if (flights[0].closest!=null) {
      s += "<table border=0 cellmargin=3 cellpadding=1 cellspacing=3 align=right>";
      s += "<tr align=right><td></td><td>Time</td><td>Hor. distance</td><td>Vert. distance</td></tr>";
      ConflictPoint cp = flights[0].first;
      if (cp != null)
        s += String.format("<tr align=right><td>Start</td><td>%02d:%02d:%02d</td><td>%.2f</td><td>%d</td></tr>",
            cp.time.getHour(), cp.time.getMinute(), cp.time.getSecond(), cp.hDistance, cp.vDistance);
      cp = flights[0].closest;
      if (cp != null)
        s += String.format("<tr align=right><td>CPA</td><td>%02d:%02d:%02d</td><td>%.2f</td><td>%d</td></tr>",
            cp.time.getHour(), cp.time.getMinute(), cp.time.getSecond(), cp.hDistance, cp.vDistance);
      cp = flights[0].last;
      if (cp != null)
        s += String.format("<tr align=right><td>Last(*)</td><td>%02d:%02d:%02d</td><td>%.2f</td><td>%d</td></tr>",
            cp.time.getHour(), cp.time.getMinute(), cp.time.getSecond(), cp.hDistance, cp.vDistance);
      s += "</table>";
      s+=String.format("<p> * within the time horizon of %d sec.</p>",timeHorizon);
    }
    return s;
  }
  
  public String getDescriptionHTML () {
    String s=getDescriptionBody();
    if (s==null)
      return null;
    return "<html><body>"+s+"</body></html>";
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
  
  /**
   * Whether the given conflict involves the same flights as this conflict
   */
  public boolean sameFlights(Conflict c) {
    if (c==null)
      return false;
    if (flights==null || c.flights==null || this.flights.length!=c.flights.length)
      return false;
    if (flights[0].flightId.equals(c.flights[0].flightId))
      return flights[1].flightId.equals(c.flights[1].flightId);
    if (flights[0].flightId.equals(c.flights[1].flightId))
      return flights[1].flightId.equals(c.flights[0].flightId);
    return false;
  }
  
  public String getSectorId(){
    if (flights==null)
      return null;
    if (flights[0].sectorId==null)
      return flights[1].sectorId;
    return flights[0].sectorId;
  }
  
  /**
   * Extracts relevant projection points from the given list of all projection points
   * @param pts - list of all projection points
   * @param ptIds - previously constructed list of identifiers of all projection points, used for searching
   * @return number of projection points found and attached to the flights
   */
  public int getProjectionPoints(ArrayList<FlightPoint> pts, ArrayList<String> ptIds) {
    if (flights==null || pts==null || pts.isEmpty() || ptIds==null || ptIds.isEmpty())
      return 0;
    int n=0;
    for (int i=0; i<flights.length; i++)
      n+=flights[i].getProjectionPoints(pts,ptIds);
    return n;
  }
  /**
   * @return 4 coordinates: min longitude, min latitude, max longitude, max latitude
   */
  public double[] getConflictGeoBoundaries(){
    if (flights==null || flights[0]==null)
      return null;
    double xMin=flights[0].lon, xMax=xMin, yMin=flights[0].lat, yMax=yMin;
    for (int i=0; i<flights.length; i++) {
      FlightInConflict f=flights[i];
      if (i>0) {
        if (xMin>f.lon) xMin=f.lon; else if (xMax<f.lon) xMax=f.lon;
        if (yMin>f.lat) yMin=f.lat; else if (yMax<f.lat) yMax=f.lat;
      }
      for (int j=0; j<4; j++) {
        ConflictPoint cp=(j==0)?f.first:(j==1)?f.closest:(j==2)?f.last:f.crossing;
        if (cp==null)
          continue;
        if (xMin>cp.lon) xMin=cp.lon; else if (xMax<cp.lon) xMax=cp.lon;
        if (yMin>cp.lat) yMin=cp.lat; else if (yMax<cp.lat) yMax=cp.lat;
      }
    }
    double minmax[]={xMin,yMin,xMax,yMax};
    return minmax;
  }
  
  public double[] getProjectionGeoBoundaries() {
    if (flights==null || flights[0]==null)
      return null;
    double xMin=flights[0].lon, xMax=xMin, yMin=flights[0].lat, yMax=yMin;
    for (int i=0; i<flights.length; i++) {
      FlightInConflict f = flights[i];
      if (flights[i].pp!=null)
        for (int j=0; j<flights[i].pp.length; j++) {
          FlightPoint cp=flights[i].pp[j];
          if (xMin>cp.lon) xMin=cp.lon; else if (xMax<cp.lon) xMax=cp.lon;
          if (yMin>cp.lat) yMin=cp.lat; else if (yMax<cp.lat) yMax=cp.lat;
        }
    }
    if (xMax==xMin && yMax==yMin)
      return null;
    double minmax[]={xMin,yMin,xMax,yMax};
    return minmax;
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
  
  public static int attachProjectionPoints(ArrayList<Conflict> conflicts, ArrayList<FlightPoint> pts) {
    if (conflicts==null || conflicts.isEmpty() || pts==null || pts.isEmpty())
      return 0;
    ArrayList<String> ptIds=new ArrayList<String>(pts.size());
    for (int i=0; i<pts.size(); i++)
      ptIds.add(pts.get(i).projId);
    int n=0;
    for (int i=0; i<conflicts.size(); i++)
      n+=conflicts.get(i).getProjectionPoints(pts,ptIds);
    return n;
  }
}
