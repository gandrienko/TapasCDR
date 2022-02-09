package ui;

import data.Action;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ActionsTableModel extends AbstractTableModel {
  public static final String colNames[]={"Flight","Action","Value","Rank",
      "Added miles","Added time","Added conflicts",
      "H-speed change","V-speed change","Course change",
      "H-shift at exit","V-shift at exit","Bearing",
      "Duration","Why not"
  };
  
  public ArrayList<Action> actions=null, allActions=null;
  
  public int maxRank=-1, maxRankToShow=-1;
  
  public void setActions(ArrayList<Action> actions) {
    this.actions = actions;
    this.allActions=null;
    maxRank=-1;
    if (actions!=null)
      for (Action a:actions)
        if (a.rank>maxRank)
          maxRank=a.rank;
    if (maxRankToShow>=0 && maxRankToShow<maxRank) {
      int max = maxRankToShow;
      maxRankToShow = -1; //to force updating
      setMaxRankToShow(max);
    }
  }
  
  public void setMaxRankToShow(int max) {
    if (maxRankToShow==max)
      return;
    maxRankToShow=max;
    if (maxRankToShow<0 || maxRankToShow>=maxRank) {
      if (allActions!=null && actions.size()<allActions.size()) {
        actions=allActions;
        allActions=null;
        fireTableDataChanged();
        return;
      }
    }
    if (allActions==null) {
      allActions = actions;
      actions=new ArrayList<Action>(allActions.size());
    }
    else
      actions.clear();
    for (int i=0; i<allActions.size(); i++)
      if (allActions.get(i).rank<=maxRankToShow)
        actions.add(allActions.get(i));
    fireTableDataChanged();
  }
  
  public String getColumnName(int col) {
    return colNames[col];
  }
  
  public Class getColumnClass(int c) {
    if (colNames[c].equals("Value") || colNames[c].equals("Rank") ||
            colNames[c].equals("Added time") ||
            colNames[c].equals("Duration") || colNames[c].equals("Added conflicts"))
      return Integer.class;
    if (colNames[c].startsWith("H-") || colNames[c].startsWith("V-") ||
            colNames[c].startsWith("Course") || colNames[c].equals("Bearing") ||
            colNames[c].equals("Added miles"))
      return Double.class;
    return String.class;
  }
  
  public int getRowCount() {
    if (actions==null)
      return 0;
    return actions.size();
  }
  
  public int getColumnCount() {
    return colNames.length;
  }
  
  public Object getValueAt(int row, int col) {
    Action a=actions.get(row);
    String cName=colNames[col];
    if (cName.equals("Flight"))
      return a.flightId;
    if (cName.equals("Action"))
      return a.actionType+": "+Action.getMeaningOfActionType(a.actionType);
    if (cName.equals("Value"))
      return a.actionValue;
    if (cName.equals("Rank"))
      return a.rank;
    if (cName.equals("Added miles"))
      return (Double.isNaN(a.addMiles))?null:a.addMiles;
    if (cName.equals("Added time"))
      return a.addTime;
    if (cName.equals("Duration"))
      return a.duration;
    if (cName.equals("Added conflicts"))
      return (a.conflicts==null)?0:a.conflicts.size();
    if (cName.equals("H-speed change"))
      return a.hSpeedChange;
    if (cName.equals("V-speed change"))
      return a.vSpeedChange;
    if (cName.equals("Course change"))
      return a.courseChange;
    if (cName.equals("H-shift at exit"))
      return a.hShiftExit;
    if (cName.equals("V-shift at exit"))
      return a.vShiftExit;
    if (cName.equals("Bearing"))
      return a.bearing;
    if (cName.equals("Why not"))
      return a.whyNot;
    return null;
  }
  
  public int getPreferredColumnWidth(int col) {
    if (!getColumnClass(col).equals(String.class))
      return 0;
    JLabel label=new JLabel(colNames[col]);
    if (colNames[col].startsWith("Flight"))
      label.setText("000000000");
    else
    if (colNames[col].equals("Action")) {
      String s=Action.type_meanings[0];
      for (int i=1; i<Action.type_meanings.length; i++)
        if (Action.type_meanings[i].length()>s.length())
          s=Action.type_meanings[i];
      label.setText("A4: "+s);
    }
    /*
    else
    if (colNames[col].equals("Why not")) {
      label.setText("Horizontal speed cannot be decreased");
    }
    */
    return label.getPreferredSize().width+10;
  }
  
  public String getDetailedText(int row, int col) {
    String cName=colNames[col].toLowerCase();
    if (colNames[col].toLowerCase().startsWith("why"))
      return actions.get(row).whyNot;
    if (colNames[col].toLowerCase().contains("conflicts"))
      return actions.get(row).getConflictsDescriptionHTML();
    Object v=getValueAt(row,col);
    if (v==null)
      return null;
    return v.toString();
  }
}
