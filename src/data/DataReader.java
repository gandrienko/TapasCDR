package data;

import Util.CsvReader;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class DataReader {
  /**
   * Here the order of the column names differs from the order in the files
   */
  public static String colNames_Main[]={
      "conflict_ID",  //0
      "event_type",   //1
      "TimePoint",    //2
      "RTkey1","RTkey2",                    //3,4
      "due_to_flight_1","due_to_flight_2",  //5,6
      "command_category",         //7
      "projection_time_horizon",  //8
      "fp_projection_flag1","fp_projection_flag2",  //9,10
      "fp_id1","fp_id2",      //11,12
      "course1","course2",    //13,14
      "speed_h1","speed_h2",  //15,16
      "speed_v1","speed_v2",  //17,18
      "lon1","lon2",          //19,20
      "lat1","lat2",          //21,22
      "alt1","alt2",          //23,24
      "flight_phase_1","flight_phase_2"};     //25,26
  public static String colNames_Conflicts[]={
      "conflict_ID",
      "due_to_flight_1","due_to_flight_2","command_category",
      "RTkey",
      "time_to_conflict",
      "time_to_first_conflict",
      "time_to_last_conflict",
      "t_to_crossing_point",
      "conflict_lon","conflict_lat",
      "first_conflict_lon","first_conflict_lat",
      "last_conflict_lon","last_conflict_lat",
      "crossing_point_lon","crossing_point_lat",
      "conflict_alt",
      "first_conflict_alt",
      "last_conflict_alt",
      "h_distance_at_conflict",
      "h_distance_at_first_conflict",
      "h_distance_at_last_conflict",
      "d_h_cp",
      "v_distance_at_conflict",
      "v_distance_at_first_conflict",
      "v_distance_at_last_conflict",
      "d_v_cp",
      "sectorID",
      "projection_ID"
  };
  
  public static ArrayList<Conflict> getConflictsFromMain(CsvReader data) {
    if (data==null || data.columns==null || data.rows==null || data.rows.isEmpty())
      return null;
    ArrayList<Conflict> conflicts=new ArrayList<Conflict>(data.getNRows());
    for (int i=0; i<data.getNRows(); i++) {
      Conflict c=new Conflict();
      c.flights=new FlightInConflict[2];
      c.flights[0]=new FlightInConflict();
      c.flights[1]=new FlightInConflict();
      
      for (int j=0; j<data.getNColumns(); j++) {
        String sValue=data.getValue(i,j);
        if (sValue==null)
          continue;
        sValue=sValue.trim();
        if (sValue.length()<1)
          continue;
        
        int cIdx=-1;
        for (int k=0; k<colNames_Main.length && cIdx<0; k++)
          if (data.columns[j].equalsIgnoreCase(colNames_Main[k]))
            cIdx=k;
        if (cIdx<0)
          continue;
        
        FlightInConflict f=(cIdx%2==1)?c.flights[0]:c.flights[1];
        
        switch (cIdx) {
          case 0: c.conflictId=sValue; break;
          case 1:
            c.type=sValue;
            c.typeNum=Conflict.getTypeNum(sValue);
            break;
          case 2:
            c.detectionTimeUnix=Long.parseLong(sValue);
            c.detectionTime= LocalDateTime.ofEpochSecond(c.detectionTimeUnix,0, ZoneOffset.UTC);
            break;
          case 3: case 4:
            f.flightId=sValue; break;
          case 5: c.actionId1=sValue; break;
          case 6: c.actionId2=sValue; break;
          case 7: c.commandCategory=sValue; break;
          case 8: c.timeHorizon=Math.round(Float.parseFloat(sValue)); break;
          case 9: case 10:
            f.projectedByPlan=Boolean.parseBoolean(sValue); break;
          case 11: case 12:
            f.planId=sValue; break;
          case 13: case 14:
            f.course=Double.parseDouble(sValue); break;
          case 15: case 16:
            f.speed_h=Double.parseDouble(sValue); break;
          case 17: case 18:
            f.speed_v=Double.parseDouble(sValue); break;
          case 19: case 20:
            f.lon=Double.parseDouble(sValue); break;
          case 21: case 22:
            f.lat=Double.parseDouble(sValue); break;
          case 23: case 24:
            f.altitude=Math.round(Float.parseFloat(sValue)); break;
          case 25: case 26:
            f.phase=sValue;
            f.phaseNum=FlightInConflict.getPhaseNum(sValue);
            break;
        }
      }
      if (c.conflictId==null || c.detectionTime==null ||
              c.flights[0].flightId==null || c.flights[1].flightId==null)
        continue;
      c.isPrimary=c.actionId1==null && c.actionId2==null;
      conflicts.add(c);
    }
    if (conflicts.isEmpty())
      return null;
    return conflicts;
  }
  
  /**
   * @return the number of records that were successfully processed
   */
  public static int getMoreConflictDataFromConflicts(CsvReader data, ArrayList<Conflict> conflicts){
    if (data==null || data.columns==null || data.rows==null || data.rows.isEmpty())
      return 0;
    if (conflicts==null || conflicts.isEmpty())
      return 0;
    int cNums[]=new int[colNames_Conflicts.length];
    for (int i=0; i<cNums.length; i++) {
      cNums[i] = -1;
      for (int j = 0; j < data.columns.length && cNums[i]<0; j++)
        if (data.columns[j].equalsIgnoreCase(colNames_Conflicts[i]))
          cNums[i]=j;
      if (cNums[i]<0 && i<6)
        return 0; //necessary fields are missing
    }
    
    int nOk=0;
    
    for (int i=0; i<data.getNRows(); i++) {
      String conflictId=data.getValue(i,cNums[0]),
          actionId1=data.getValue(i,cNums[1]), actionId2=data.getValue(i,cNums[2]),
          commandCategory=data.getValue(i,cNums[3]), flightId=data.getValue(i,cNums[4]);
      Conflict c=null;
      for (int j=0; j<conflicts.size() && c==null; j++)
        if (conflicts.get(j).sameConflict(conflictId,actionId1,actionId2,commandCategory))
          c=conflicts.get(j);
      if (c==null || c.flights==null)
        continue;
      FlightInConflict f=null;
      for (int j=0; j<c.flights.length && f==null; j++)
        if (c.flights[j].flightId.equals(flightId))
          f=c.flights[j];
      if (f==null)
        continue;
      ++nOk;
      for (int j=5; j<cNums.length; j++) {
        String sValue=data.getValue(i,cNums[j]);
        if (sValue==null)
          continue;
        
        String colName=data.columns[cNums[j]].toLowerCase();
        if (colName.equals("sectorid"))
          f.sectorId=sValue;
        else
        if (colName.equals("projection_id"))
          f.projectionId=sValue;
        else {
          ConflictPoint cp=null;
          if (colName.contains("first")) {
            if (f.first==null)
              f.first=new ConflictPoint(flightId,ConflictPoint.kindFirst);
            cp=f.first;
          }
          else
          if (colName.contains("last")) {
            if (f.last==null)
              f.last=new ConflictPoint(flightId,ConflictPoint.kindLast);
            cp=f.last;
          }
          else
          if (colName.contains("crossing") || colName.endsWith("_cp"))  {
            if (f.crossing==null)
              f.crossing=new ConflictPoint(flightId,ConflictPoint.kindCross);
            cp=f.crossing;
          }
          else
          if (colName.contains("conflict")) {
            if (f.closest==null)
              f.closest=new ConflictPoint(flightId,ConflictPoint.kindCPA);
            cp=f.closest;
          }
          if (cp==null) //uninterpreted field name
            continue;
          
          if (colName.startsWith("time_to_") || colName.startsWith("t_to_")) {
            cp.timeTo=Double.parseDouble(sValue);
            long timeUnix=c.detectionTimeUnix+Math.round(cp.timeTo);
            cp.time=LocalDateTime.ofEpochSecond(timeUnix,0, ZoneOffset.UTC);
          }
          else
          if (colName.endsWith("_lon"))
            cp.lon=Double.parseDouble(sValue);
          else
          if (colName.endsWith("_lat"))
            cp.lat=Double.parseDouble(sValue);
          else
          if (colName.endsWith("_alt"))
            cp.altitude=Math.round(Float.parseFloat(sValue));
          else
          if (colName.startsWith("h_distance_") || colName.startsWith("d_h_"))
            cp.hDistance=Double.parseDouble(sValue);
          else
          if (colName.startsWith("v_distance_") || colName.startsWith("d_v_"))
            cp.vDistance=Math.round(Float.parseFloat(sValue));
        }
      }
    }
    return nOk;
  }
  
  public static ArrayList<Action> getActions(CsvReader data) {
    if (data==null || data.columns==null || data.rows==null || data.rows.isEmpty())
      return null;
    ArrayList<Action> actions=new ArrayList<Action>(data.getNRows());
    for (int r=0; r<data.getNRows(); r++) {
      Action a=new Action();
      for (int c=0; c<data.getNColumns(); c++) {
        String colName=data.columns[c], sValue=data.getValue(r,c);
        if (sValue==null || sValue.equalsIgnoreCase("null"))
          continue;
        if (colName.equalsIgnoreCase("ResolutionID")) a.actionId=sValue; else
        if (colName.equalsIgnoreCase("ConflictID")) a.conflictId=sValue; else
        if (colName.equalsIgnoreCase("RTkey")) a.flightId=sValue; else
        if (colName.equalsIgnoreCase("ResolutionActionType")) a.actionType=sValue; else
        if (colName.equalsIgnoreCase("ResolutionAction"))
          a.actionValue=Integer.parseInt(sValue);
        else
        if (colName.equalsIgnoreCase("ActionRank"))
          a.rank=Integer.parseInt(sValue);
        else
        if (colName.equalsIgnoreCase("VSpeedChange"))
          a.vSpeedChange=Math.round(Float.parseFloat(sValue));
        else
        if (colName.equalsIgnoreCase("HSpeedChange"))
          a.hSpeedChange=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("CourseChange"))
          a.courseChange=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("HShiftFromExitPoint"))
          a.hShiftExit=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("VShiftFromExitPoint"))
          a.vShiftExit=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("Bearing"))
          a.bearing=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("Q-Value"))
          a.qValue=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("AdditionalNauticalMiles"))
          a.addMiles=Double.parseDouble(sValue);
        else
        if (colName.equalsIgnoreCase("Duration"))
          a.addTime=Math.round(Double.parseDouble(sValue));
        else
        if (colName.equalsIgnoreCase("Prioritization"))
          a.hitMapId=sValue;
        else
        if (colName.equalsIgnoreCase("FilteredOut"))
          a.whyNot=sValue;
      }
      if (a.actionId!=null && a.conflictId!=null && a.flightId!=null && a.actionType!=null)
        actions.add(a);
    }
    if (actions.isEmpty())
      return null;
    return actions;
  }
}
