package data;

/**
 * Describes a 3D+T point of a flight trajectory, in particular, a projection point
 */

public class FlightPoint {
  /**
   * points_of_projection.csv: projection_ID -
   * This ID has the following form: TimePoint_RTkey_resolutionActionType_ResolutionActionValue
   */
  public String projId=null;
  /**
   * points_of_projection.csv: TimePoint - Time when the projection was made
   */
  public long projTimeUnix =0;
  /**
   * points_of_projection.csv: sequence_number - sequential number of the point in a sequence
   */
  public int sequenceN=-1;
  /**
   * points_of_projection.csv: timestamp - Time stamp of the point
   */
  public long pointTimeUnix=0;
  /**
   * points_of_projection.csv: lon,lat
   * Longitude and latitude of the point, in degrees
   */
  public double lon=Double.NaN, lat=Double.NaN;
  /**
   * points_of_projection.csv: alt
   * point altitude, in feet
   */
  public int altitude=0;
}
