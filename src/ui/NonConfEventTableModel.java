package ui;

import data.Action;
import data.FlightInConflict;
import data.NCEvent;

import javax.swing.*;
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
    if (cName.equals("Action value")) {
      if (a.actionValue!=0 && a.actionType.equals("A2"))
        return FlightInConflict.transformKnotsToMachNumber(a.actionValue);
      return a.actionValue;
    }
    if (cName.equals("Action time"))
      return LocalDateTime.ofEpochSecond(e.actionTimeUnix,0, ZoneOffset.UTC);
    if (cName.equals("Violated"))
      return e.type;
    if (cName.equals("Desired value"))
      return NCEvent.getValueInRightUnits(e.type,e.desiredValue);
    if (cName.equals("Actual value"))
      return NCEvent.getValueInRightUnits(e.type,e.actualValue);
    return null;
  }
  
  public int getPreferredColumnWidth(int col) {
    JLabel label=new JLabel(colNames[col]);
    if (colNames[col].startsWith("Flight"))
      label.setText("000000000");
    else
    if (colNames[col].equals("Action")) {
      String s=Action.type_meanings[0];
      for (int i=1; i<Action.type_meanings.length; i++)
        if (Action.type_meanings[i].length()>s.length())
          s=Action.type_meanings[i];
      label.setText("RFP: "+s);
    }
    else
    if (colNames[col].equals("Action time")) {
      label.setText("00:00:00");
    }
    else
    if (colNames[col].equals("Violated")) {
      label.setText("Horizontal speed");
    }
    return label.getPreferredSize().width+10;
  }
}
