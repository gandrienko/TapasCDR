package ui;

import data.Action;
import data.NCEvent;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class NonConfEventTableModel extends AbstractTableModel {
  public static final String colNames[]={"Flight",
      "Action","Action value","Action time",
      "Violated","Desired value","Actual value"
  };
  /**
   * The events of non-conformance to prescribed conflict resolution actions
   */
  public ArrayList<NCEvent> ncEvents=null;
  
  public void setNCEvents(ArrayList<NCEvent> ncEvents) {
    this.ncEvents = ncEvents;
  }
  
  public String getColumnName(int col) {
    return colNames[col];
  }
  
  public Class getColumnClass(int c) {
    if (colNames[c].equals("Action value"))
      return Integer.class;
    if (colNames[c].contains("time"))
      return LocalDateTime.class;
    if (colNames[c].equals("Desired value") || colNames[c].equals("Actual value"))
      return Double.class;
    return String.class;
  }
  
  public int getRowCount() {
    if (ncEvents==null)
      return 0;
    return ncEvents.size();
  }
  
  public int getColumnCount() {
    return colNames.length;
  }
  
  public Object getValueAt(int row, int col) {
    NCEvent e=ncEvents.get(row);
    Action a=e.action;
    String cName=colNames[col];
    if (cName.equals("Flight"))
      return e.flightId;
    if (cName.equals("Action"))
      return a.actionType+": "+Action.getMeaningOfActionType(a.actionType);
    if (cName.equals("Action value"))
      return a.actionValue;
    if (cName.equals("Action time"))
      return LocalDateTime.ofEpochSecond(e.actionTimeUnix,0, ZoneOffset.UTC);
    if (cName.equals("Violated"))
      return e.type;
    if (cName.equals("Desired value"))
      return e.desiredValue;
    if (cName.equals("Actual value"))
      return e.actualValue;
    return null;
  }
}
