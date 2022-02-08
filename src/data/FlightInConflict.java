package data;

import java.util.ArrayList;
import java.util.Arrays;

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
  public String rtKey=null;
  /**
   * Main.csv: Callsign1, Callsign2
   */
  public String callSign=null;
  /**
   * Either callSign (if specified) or rtKey
   */
  public String flightId=null;
  /**
   * Main.csv: DestinationAirport1, DestinationAirport2
   */
  public String destination=null;
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
   * Projection points taken from points_of_projection.csv
   */
  public FlightPoint pp[]=null;
  /**
   * Points describing the fragment of the flight trajectory that is in conflict
   */
  public ConflictPoint closest=null, first=null, last=null, crossing=null;
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
  
  public static int getPhaseNum(String sValue) {
    for (int k=0; k<phaseStrings.length; k++)
      if (sValue.equalsIgnoreCase(phaseStrings[k]))
        return k+1;
    return 0;
  }
  
  /**
   * @return the smallest Measure Of Compliance (MOC) among the conflict points specified
   */
  public double getComplianceMeasure () {
    if (!Double.isNaN(measureOfCompliance))
      return measureOfCompliance;
    for (int i=0; i<3; i++) {
      ConflictPoint cp=(i==0)?first:(i==1)?closest:last;
      if (cp==null)
        continue;
      double c=cp.getComplianceMeasure();
      if (!Double.isNaN(c) && (Double.isNaN(measureOfCompliance) || measureOfCompliance>c))
        measureOfCompliance=c;
    }
    return measureOfCompliance;
  }
  
  /**
   * Extracts relevant projection points from the given list of all projection points
   * @param pts - list of all projection points
   * @param ptIds - previously constructed list of identifiers of all projection points, used for searching
   * @return number of projection points found and attached to this record
   */
  public int getProjectionPoints(ArrayList<FlightPoint> pts, ArrayList<String> ptIds) {
    if (pp!=null || projectionId==null ||
            pts==null || pts.isEmpty() || ptIds==null || ptIds.isEmpty())
      return 0;
    int i1=ptIds.indexOf(projectionId);
    if (i1<0)
      return 0;
    int i2;
    for (i2=i1+1; i2<ptIds.size() && projectionId.equals(ptIds.get(i2)); i2++);
    pp=new FlightPoint[i2-i1];
    for (int i=i1; i<i2; i++)
      pp[i-i1]=pts.get(i);
    Arrays.sort(pp);
    return pp.length;
  }
  
  public String getDescriptionHTML() {
    String s=String.format("<p align=center>Flight <b>%s</b></p>",flightId);
    s += "<table border=0 cellmargin=3 cellpadding=3 cellspacing=3 align=center>";
    s+=String.format("<tr align=right><td>Destination</td><td>%s</td></tr>",destination);
    s+=String.format("<tr align=right><td>Phase</td><td>%s</td></tr>",phase);
    s+=String.format("<tr align=right><td>Course</td><td>%.1f</td><td>degrees</td></tr>",course);
    s+=String.format("<tr align=right><td>Horizontal speed</td><td>%.2f</td><td>nm/min.</td></tr>",speed_h*60/1852);
    s+=String.format("<tr align=right><td>Vertical speed</td><td>%.0f</td><td>feet/min.</td></tr>",speed_v*60);
    s+=String.format("<tr align=right><td>Altitude</td><td>%d</td><td>feet</td></tr>",altitude);
    s += "</table>";
    return "<html><body>"+s+"</body></html>";
  }
}
