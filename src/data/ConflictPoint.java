package data;

import java.time.LocalDateTime;

/**
 * Describes a point from the (predicted) trajectory of a flight that is in conflict
 * with another flight.
 */

public class ConflictPoint extends FlightPoint {
  /**
   * Minimal required horizontal (in nm) and vertical (in feet) separation
   */
  public static final double HD_MIN=5, VD_MIN=1000;
  /**
   * Kind of point: CPA (closest point of approach), first point (where the conflict begins),
   * last point (where the conflict ends), or crossing point
   */
  public static final String kinds_of_points[]={"CPA","first","last","crossing"};
  public static final int kindCPA=0, kindFirst=1, kindLast=2, kindCross=3;
  /**
   * conflicts.csv: RTkey
   * Radar Track key (flight identifier)
   */
  public String flightId=null;
  /**
   * Kind of point: CPA (closest point of approach) = 0, first point (where the conflict begins) = 1,
   * last point (where the conflict ends) = 2, or crossing point =3
   * See kinds_of_points
   */
  public int kind=0;
  /**
   * conflicts.csv: "time_to_conflict",
   *       "time_to_first_conflict",
   *       "time_to_last_conflict",
   *       "t_to_crossing_point"
   * Time to this point from the moment of conflict detection
   */
  public double timeTo=Double.NaN;
  /**
   * Unix time of this point; results from adding timeToPoint to the time of conflict detection
   */
  //public long pointTimeUnix=0;
  /**
   * Time of the point; results from converting pointTimeUnix to LocalDateTime
   */
  public LocalDateTime time=null;
  /**
   * conflicts.csv: "conflict_lon","conflict_lat","first_conflict_lon","first_conflict_lat",
   *            "last_conflict_lon","last_conflict_lat","crossing_point_lon","crossing_point_lat",
   * Longitude and latitude of the point, in degrees
   */
  //public double lon=Double.NaN, lat=Double.NaN;
  /**
   * conflicts.csv: "conflict_alt","first_conflict_alt","last_conflict_alt",
   * point altitude, in feet
   */
  //public int altitude=0;
  /**
   * conflicts.csv: "h_distance_at_conflict",
   *       "h_distance_at_first_conflict",
   *       "h_distance_at_last_conflict",
   *       "d_h_cp"
   * Horizontal distance, in nautical miles and in metres
   */
  public double hDistance=Double.NaN, hDistMetr=Double.NaN;
  /**
   * conflicts.csv: "v_distance_at_conflict",
   *       "v_distance_at_first_conflict",
   *       "v_distance_at_last_conflict",
   *       "d_v_cp"
   *  Vertical distance, in feet
   */
  public int vDistance=0;
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
   */
  public double measureOfCompliance=Double.NaN;
  /**
   * Whether the MOC is determined by the horizontal distance ('H'), vertical distance ('V'),
   * both ('B'), or none of them ('N').
   */
  public char mocDueTo='N';
  
  public ConflictPoint() {}
  
  public ConflictPoint(String flightId, int kind) {
    this.flightId=flightId;
    this.kind=kind;
  }
  
  /**
   * Computes the Measure Of Compliance (MOC), which is the biggest distance apart (expressed as a %)
   * @return the Measure Of Compliance (MOC)
   */
  public double getComplianceMeasure () {
    if (!Double.isNaN(measureOfCompliance))
      return measureOfCompliance;
    mocDueTo='N';
    if (Double.isNaN(hDistance) || Double.isNaN(vDistance))
      return Double.NaN;
    double hc=100, vc=100;
    if (hDistance<HD_MIN)
      hc=hDistance/HD_MIN*100;
    if (vDistance<VD_MIN)
      vc=vDistance/VD_MIN*100;
    measureOfCompliance=Math.max(hc,vc);
    if (measureOfCompliance<100)
      mocDueTo=(hc>vc)?'H':(hc==vc)?'B':'V';
    return measureOfCompliance;
  }
  
  /**
   * Informs whether the MOC is determined by the horizontal distance ('H'), vertical distance ('V'),
   * both ('B'), or none of them ('N').
   * @return 'H', 'V', 'B', or 'N'
   */
  public char getMOC_DueTo() {
    if (Double.isNaN(measureOfCompliance))
      getComplianceMeasure();
    return mocDueTo;
  }
}
