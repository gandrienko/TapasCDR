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
    }
    return nOk;
  }
}
