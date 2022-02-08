package data;

import java.util.ArrayList;

public class Action {
  public static final String action_types[]={"A1","A2","A3","A4","S2"};
  public static final String type_meanings[]={
      "flight level change","speed change","direct to waypoint",
      "no action","course change"};
  /**
   * resolution_actions_episode_1.csv: ResolutionID
   */
  public String actionId=null;
  /**
   * resolution_actions_episode_1.csv: ConflictID
   */
  public String conflictId=null;
  /**
   * resolution_actions_episode_1.csv: RTkey
   */
  public String rtKey=null;

  public String flightId=null;
  /**
   * resolution_actions_episode_1.csv: ResolutionActionType
   */
  public String actionType=null;
  /**
   * resolution_actions_episode_1.csv: ResolutionAction
   * A1: [1,-1] (higher/lower flight level) - binary decision
   * A2: [10, -10] - knots
   * A4: 0
   * S2: [10, -10] - degrees
   */
  public int actionValue=0;
  /**
   * resolution_actions_episode_1.csv: VSpeedChange
   */
  public int vSpeedChange=0;
  /**
   * resolution_actions_episode_1.csv: HSpeedChange
   */
  public double hSpeedChange=0;
  /**
   * resolution_actions_episode_1.csv: CourseChange
   */
  public double courseChange=0;
  /**
   * resolution_actions_episode_1.csv: HShiftFromExitPoint
   * Horizontal shift from exit point
   */
  public double hShiftExit=0;
  /**
   * resolution_actions_episode_1.csv: VShiftFromExitPoint
   * Vertical shift from exit point
   */
  public double vShiftExit=0;
  /**
   * resolution_actions_episode_1.csv: Bearing
   * Αngle of the agent’s course w.r.t. North (χ)
   */
  public double bearing=0;

  /**
   * resolution_actions_episode_1.csv: Q-Value
   */
  public double qValue=0;
  /**
   * resolution_actions_episode_1.csv: ActionRank
   * Ranking of the current resolution action based on q-values.
   * The lowest ActionRank value (which is 0) corresponds to the highest q-value.
   */
  public int rank=0;
  /**
   * resolution_actions_episode_1.csv: AdditionalNauticalMiles
   */
  public double addMiles=Double.NaN;
  /**
   * resolution_actions_episode_1.csv: Duration
   * Additional time, in seconds
   */
  public long addTime=0;
  /**
   * resolution_actions_episode_1.csv: Prioritization
   * AttentionHeads’ Hitmap (???)
   */
  public String hitMapId=null;
  /**
   * resolution_actions_episode_1.csv: FilteredOut
   * Reason why or null
   * Reasons:
   * “Vertical speed cannot be increased because it will exceed 60.0 feet/s”,
   * “Vertical speed cannot be decreased because it will fall below -80.0 feet/s”,
   * “Horizontal speed cannot be increased because it will exceed 291 m/s”,
   * “Horizontal speed cannot be decreased because it will fall below 178.67 m/s”
   *
   * This filtering is applied to the actions proposed by the XAI model in order
   * to deterministically exclude actions that will result in the following
   * conditions not being met:
   *
   * 178.67 <= horizontal_speed <= 291
   * -80 <= vertical_speed <= 60
   *
   * With this deterministic filtering, we facilitate the agents by not letting
   * them learn the above conditions (e.g. by penalization). Instead, we explicitly
   * force them to choose and evaluate the best (based on q-values and only in exploitation)
   * valid action. An action is valid only if it meets the above conditions.
   * During exploration, we force the agents to choose the first random action which is valid.
   */
  public String whyNot=null;
  /**
   * Secondary conflicts that may be caused by this action
   */
  public ArrayList<Conflict> conflicts=null;
  
  public String getConflictsDescriptionHTML(){
    if (conflicts==null)
      return null;
    String s=null;
    for (int i=0; i<conflicts.size(); i++) {
      String d=conflicts.get(i).getDescriptionBody();
      if (d==null)
        continue;
      if (s==null)
        s=d;
      else
        s+="<br>"+d;
    }
    if (s==null)
      return null;
    return "<html><body>"+s+"</body></html>";
  }
  
  /**
   * Attaches suggested actions to conflicts and secondary conflicts to actions
   * @param actions - list of all actions
   * @param conflicts - list of all conflicts
   * @return number of actions linked to corresponding conflicts
   */
  public static int linkActionsToConflicts(ArrayList<Action> actions, ArrayList<Conflict> conflicts) {
    if (actions==null || actions.isEmpty() || conflicts==null || conflicts.isEmpty())
      return 0;
    int n=0;
    for (Action a:actions) {
      for (Conflict c:conflicts) {
        if (a.conflictId.equals(c.conflictId)) {
          if (c.actions==null)
            c.actions=new ArrayList<Action>(20);
          c.actions.add(a);
          for (FlightInConflict f:c.flights)
            if (f.rtKey==a.rtKey)
              a.flightId=f.flightId;
          ++n;
        }
        else
        if (a.actionId.equals(c.actionId1) || a.actionId.equals(c.actionId2))  {
          if (a.conflicts==null)
            a.conflicts=new ArrayList<Conflict>(5);
          a.conflicts.add(c);
          if (a.actionId.equals(c.actionId1)) {
            c.causeAction1 = a;
            a.flightId=c.flights[0].flightId;
          }
          else {
            c.causeAction2 = a;
            a.flightId=c.flights[1].flightId;
          }
        }
      }
    }
    return n;
  }
  
  public static String getMeaningOfActionType(String type) {
    if (type==null)
      return  null;
    type=type.toUpperCase();
    for (int i=0; i<action_types.length; i++)
      if (type.equals(action_types[i]))
        return type_meanings[i];
    return type;
  }
}
