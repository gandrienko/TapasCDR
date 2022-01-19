package data;

/**
 * Contains information about one flight involved in a conflict
 */

public class FlightInConflict {
  public static final int Climbing=1, Cruising=2, Descending=3;
  public static final String phaseStrings[]={"climbing","cruising","descending"};
  public static final char phaseCodes[]={0,'\u2197','\u2192','\u2198'};
  /**
   * Main.csv: RTkey1, RTkey2
   */
  public String flightId=null;
  /**
   * Main.csv: "fp_projection_flag1","fp_projection_flag2"
   * Whether the projection (prediction of the flight positions into the future)
   * was obtained using some version of the flight plan.
   */
  public boolean projectedByPlan=true;
  /**
   * Main.csv: fp_id1, fp_id2
   * Flight plan Id: there may be multiple versions of the flight plan for one flight.
   * This structure contains data referring to a single version.
   */
  public String planId=null;
  /**
   * Main.csv: course1, course2
   * Aircraft course, in degrees
   */
  public double course=Double.NaN;
  /**
   * Main.csv: speed_h1, speed_h2
   * Horizontal speed, in m/s (meters per second)
   */
  public double speed_h=Double.NaN;
  /**
   * Main.csv: speed_v1, speed_v2
   * Vertical speed, in feet/s (feet per second)
   */
  public double speed_v=0;
  /**
   * Main.csv: lon1, lat1, lon2, lat2
   * Longitude and latitude of the aircraft position, in degrees
   */
  public double lon=Double.NaN, lat=Double.NaN;
  /**
   * Main.csv: alt1, alt2
   * Aircraft's altitude, in feet
   */
  public int altitude=0;
  /**
   * Main.csv: flight_phase_1, flight_phase_2
   * Flight phase : "climbing"/"cruising"/ "descending"
   */
  public String phase=null;
  /**
   * One of the numeric constants Climbing=1, Cruising=2, Descending=3;
   */
  public int phaseNum=0;
  /**
   * conflicts.csv: "sectorID"
   * Sector at which the conflict has been detected
   */
  public String sectorId=null;
  /**
   * conflicts.csv: "projection_ID"
   * This ID has the following form: TimePoint_RTkey_resolutionActionType_ResolutionActionValue
   */
  public String projectionId=null;
  /**
   * Points describing the fragment of the flight trajectory that is in conflict
   */
  public ConflictPoint closest=null, first=null, last=null, crossing=null;
  
  public static int getPhaseNum(String sValue) {
    for (int k=0; k<phaseStrings.length; k++)
      if (sValue.equalsIgnoreCase(phaseStrings[k]))
        return k+1;
    return 0;
  }
}
