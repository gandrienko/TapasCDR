package data;

/**
 * Describes a non-conformance event
 */

public class NCEvent {
  /**
   * non_conformance_events.csv: ID
   * This ID has the following form: TimePoint_FlightID1
   * Where FlightIDx is a conflicting flight (RTKey of the flight)
   */
  public String id=null;
  /**
   * RTKey of the flight, extracted from the event id
   */
  public String rtKey=null;
  /**
   * time of this event (UNIX seconds); extracted from the event id
   */
  public long timeUnix=0;
  /**
   * Main.csv: Callsign1, Callsign2
   * Needs to be taken from other data structures
   */
  public String callSign=null;
  /**
   * Either callSign (if specified) or rtKey
   */
  public String flightId=null;
  /**
   * non_conformance_events.csv: ResolutionActionID
   * This ID has the following form: TimePoint_FlightID
   * Where FlightID is a conflicting flight (RTKey).
   */
  public String actionId=null;
  /**
   * The time of the action, extracted from the action id
   */
  public long actionTimeUnix=0;
  /**
   * The descriptor of the action; taken from other data structures
   */
  public Action action=null;
  /**
   * non_conformance_events.csv: NonConformaceType
   * E.g.: "horizontal speed", "vertical speed", "course"
   */
  public String type=null;
  /**
   * non_conformance_events.csv: DesiredValue
   */
  public double desiredValue=Double.NaN;
  /**
   * non_conformance_events.csv: ActualValue
   */
  public double actualValue=Double.NaN;
}
