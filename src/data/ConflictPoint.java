package data;

import java.time.LocalDateTime;

/**
 * Describes a point from the (predicted) trajectory of a flight that is in conflict
 * with another flight.
 */

public class ConflictPoint {
  /**
   * Kind of point: CPA (closest point of approach), first point (where the conflict begins),
   * last point (where the conflict ends), or crossing point
   */
  public static final String kinds_of_points[]={"CPA","first","last","crossing"};
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
   * Time of the point; results from addint timeToPoint to the time of conflict detection
   */
  public LocalDateTime time=null;
  /**
   * conflicts.csv: "conflict_lon","conflict_lat","first_conflict_lon","first_conflict_lat",
   *            "last_conflict_lon","last_conflict_lat","crossing_point_lon","crossing_point_lat",
   * Longitude and latitude of the point, in degrees
   */
  public double lon=Double.NaN, lat=Double.NaN;
  /**
   * conflicts.csv: "conflict_alt","first_conflict_alt","last_conflict_alt",
   * point altitude, in feet
   */
  public int altitude=0;
  
}
