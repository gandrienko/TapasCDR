package data;

import java.util.ArrayList;

/**
 * Contains a portion of data received in one time step (e.g., every 30 seconds)
 */

public class DataPortion {
  /**
   * The time stamp of this data portion
   */
  public long timeUnix=0;
  /**
   * The set of detected conflicts
   */
  public ArrayList<Conflict> conflicts=null;
  /**
   * The events of non-conformance to prescribed conflict resolution actions
   */
  public ArrayList<NCEvent> ncEvents=null;
  
  public int getConflictIdx(String conflictId) {
    if (conflicts==null || conflictId==null)
      return -1;
    for (int i=0; i<conflicts.size(); i++)
      if (conflictId.equals(conflicts.get(i).conflictId))
        return i;
    return -1;
  }
  
}
