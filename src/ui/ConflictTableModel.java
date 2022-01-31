package ui;

import data.Conflict;
import data.ConflictPoint;
import data.FlightInConflict;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ConflictTableModel  extends AbstractTableModel {
  public static final String colNames[]={"N","Type","Flight 1","Flight 2",
      "Detected at","Start time","CPA time","End time",
      "Severity","Compliance (MoC)",
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
    String cName=colNames[c].toLowerCase();
    if (c==0 || cName.contains("vert") || cName.contains("severity"))
      return Integer.class;
    if (cName.contains("time") || cName.startsWith("detect"))
      return LocalDateTime.class;
    if (cName.contains("hor"))
      return Double.class;
    if (cName.contains("primary"))
      return Boolean.class;
    if (cName.contains("moc") || cName.contains("compliance"))
      return Double.class;
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
    String cName=colNames[col].toLowerCase();
    if (cName.contains("moc") || cName.contains("compliance"))
      return c.getComplianceMeasure();
    if (cName.contains("severity"))
      return (int)Math.round(c.getSeverity());
    if (cName.equals("type"))
      return c.type;
    if (cName.equals("is primary?"))
      return c.isPrimary;
    if (cName.startsWith("detect"))
      return c.detectionTime;
    if (cName.equals("flight 1"))
      return c.flights[0].flightId+" "+FlightInConflict.phaseCodes[c.flights[0].phaseNum];
    if (cName.equals("flight 2"))
      return c.flights[1].flightId+" "+FlightInConflict.phaseCodes[c.flights[1].phaseNum];
    FlightInConflict f=c.flights[0];
    ConflictPoint cp=(cName.contains("cpa"))?f.closest:
                         (cName.contains("start"))?f.first:
                             (cName.contains("end"))?f.last:null;
    if (cp==null)
      return null;
    if (cName.contains("time"))
      return cp.time;
    if (cName.startsWith("hor"))
      return  cp.hDistance;
    if (cName.startsWith("vert"))
      return cp.vDistance;
    return null;
  }
  
  public boolean isDistanceToLowLimitImportant(int row, int col) {
    String cName=colNames[col].toLowerCase();
    if (cName.contains("moc") || cName.contains("compliance"))
      return true;
    if (!cName.startsWith("hor") && !cName.startsWith("vert"))
      return false;
    Conflict c=conflicts.get(row);
    FlightInConflict f=c.flights[0];
    ConflictPoint cp=(cName.contains("cpa"))?f.closest:
                         (cName.contains("start"))?f.first:
                             (cName.contains("end"))?f.last:null;
    char dueTo=cp.getMOC_DueTo();
    if (dueTo=='B')
      return true;
    if (cName.startsWith("hor"))
      return dueTo=='H';
    return dueTo=='V';
  }
  
  public int getPreferredColumnWidth(int col) {
    if (col>0 && !colNames[col].contains("time") &&
            !getColumnClass(col).equals(String.class) &&
            !getColumnClass(col).equals(Boolean.class))
      return 0;
    JLabel label=new JLabel(colNames[col]);
    if (colNames[col].equals("N"))
      label.setText("000");
    else
    if (colNames[col].equals("Type"))
      label.setText("conflict");
    else
    if (colNames[col].startsWith("Flight"))
      label.setText("000000000");
    else
      if (colNames[col].contains("time") || colNames[col].startsWith("Detect"))
        label.setText("00:00:00");
    return label.getPreferredSize().width+10;
  }
  
}
