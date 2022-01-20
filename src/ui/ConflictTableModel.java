package ui;

import data.Conflict;
import data.ConflictPoint;
import data.FlightInConflict;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ConflictTableModel  extends AbstractTableModel {
  public static final String colNames[]={"N","Type","Flight 1","Flight 2",
      "Start time","CPA time","End time",
      "HorD at start","HorD at CPA","HorD at end",
      "VertD at start","VertD at CPA","VertD at end",
      "Is primary?"
  };
  /**
   * The set of conflicts to be shown
   */
  public ArrayList<Conflict> conflicts=null;
  
  public void setConflicts(ArrayList<Conflict> conflicts) {
    this.conflicts = conflicts;
  }
  public String getColumnName(int col) {
    return colNames[col];
  }
  public Class getColumnClass(int c) {
    if (c==0 || colNames[c].contains("Vert"))
      return Integer.class;
    if (colNames[c].contains("time"))
      return LocalDateTime.class;
    if (colNames[c].contains("Hor"))
      return Double.class;
    if (colNames[c].contains("primary"))
      return Boolean.class;
    return String.class;
  }
  public int getRowCount() {
    if (conflicts==null)
      return 0;
    return conflicts.size();
  }
  public int getColumnCount() {
    return colNames.length;
  }
  public Object getValueAt(int row, int col) {
    if (col==0)
      return row+1;
    Conflict c=conflicts.get(row);
    if (colNames[col].equals("Type"))
      return c.type;
    if (colNames[col].equals("Is primary?"))
      return c.isPrimary;
    if (colNames[col].equals("Flight 1"))
      return c.flights[0].flightId+" "+FlightInConflict.phaseCodes[c.flights[0].phaseNum];
    if (colNames[col].equals("Flight 2"))
      return c.flights[1].flightId+" "+FlightInConflict.phaseCodes[c.flights[1].phaseNum];
    FlightInConflict f=c.flights[0];
    ConflictPoint cp=(colNames[col].contains("CPA"))?f.closest:
                         (colNames[col].toLowerCase().contains("start"))?f.first:
                             (colNames[col].toLowerCase().contains("end"))?f.last:null;
    if (cp==null)
      return null;
    if (colNames[col].contains("time"))
      return cp.time;
    if (colNames[col].startsWith("Hor"))
      return  cp.hDistance;
    if (colNames[col].startsWith("Vert"))
      return cp.vDistance;
    return null;
  }
  
  public int getPreferredColumnWidth(int col) {
    if (colNames[col].equals("N"))
      return 20;
    if (colNames[col].equals("Type"))
      return 30;
    if (colNames[col].equals("Is primary?"))
      return 25;
    if (colNames[col].startsWith("Flight"))
      return 50;
    return 0;
  }
  
}
