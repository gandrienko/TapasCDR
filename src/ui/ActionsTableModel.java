package ui;

import data.Action;
import data.FlightInConflict;
import table_cells.ButtonInCellRenderer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ActionsTableModel extends AbstractTableModel {
  public static final String colNames[]={"Flight","Action","Value","Do?","Rank",
      "Added miles","Added seconds","Conflicts foreseen",
      "H-speed change","V-speed change","Course change",
      "H-shift at exit","V-shift at exit","Bearing",
      "Duration","Why not"
  };
  public static int buttonColIdx=3;
  
  public ArrayList<Action> actions=null, allActions=null;
  public boolean toMakeActionButtons=true;
  public ArrayList<JButton> buttons=null;
  
  public TableColumnModel columnModel =null;
  
  public ActionListener actionListener=null;
  
  public int maxRank=-1, maxRankToShow=-1;
  
  public void setColumnModel(TableColumnModel columnModel) {
    this.columnModel = columnModel;
    setTableColumnRenderers();
  }
  
  public void setActions(ArrayList<Action> actions, boolean toMakeActionButtons) {
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
    if (this.toMakeActionButtons != toMakeActionButtons) {
      this.toMakeActionButtons = toMakeActionButtons;
      fireTableStructureChanged();
      setTableColumnRenderers();
    }
    setVisibilityOfButtons();
    fireTableDataChanged();
  }
  
  protected void setTableColumnRenderers() {
    if (columnModel==null)
      return;
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    for (int i=0; i<getColumnCount(); i++) {
      if (getColumnClass(i).equals(String.class))
        columnModel.getColumn(i).setCellRenderer(centerRenderer);
      else
        if (getColumnClass(i).equals(JButton.class)) {
          ButtonInCellRenderer bRend=new ButtonInCellRenderer();
          columnModel.getColumn(i).setCellRenderer(bRend);
          columnModel.getColumn(i).setCellEditor(bRend);
        }
      int w=getPreferredColumnWidth(i);
      if (w>0)
        columnModel.getColumn(i).setPreferredWidth(w);
    }
  }
  
  public Action getAction(int idx) {
    if (actions==null || idx<0 || idx>=actions.size())
      return null;
    return actions.get(idx);
  }
  
  public void setActionListener(ActionListener actionListener) {
    this.actionListener = actionListener;
  }
  
  public void setVisibilityOfButtons() {
    if (buttons!=null)
      if (toMakeActionButtons) {
        for (int i = getRowCount(); i < buttons.size(); i++)
          buttons.get(i).setVisible(false);
        for (int i=0; i<getRowCount() && i<buttons.size(); i++)
          buttons.get(i).setVisible(true);
      }
      else
        for (JButton b:buttons)
          b.setVisible(false);
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
      actions=new ArrayList<Action>(100);
    }
    else
      actions.clear();
    if (allActions!=null && actions!=null)
      for (int i=0; i<allActions.size(); i++)
        if (allActions.get(i).rank<=maxRankToShow)
          actions.add(allActions.get(i));
    setVisibilityOfButtons();
    fireTableDataChanged();
  }
  
  public String getColumnName(int col) {
    if (!toMakeActionButtons && col>=buttonColIdx)
      ++col;
    return colNames[col];
  }
  
  public Class getColumnClass(int c) {
    if (!toMakeActionButtons && c>=buttonColIdx)
      ++c;
    String cName=colNames[c].toLowerCase();
    if (cName.equals("do?"))
      return JButton.class;
    if (cName.equals("rank") ||
            cName.equals("added seconds") ||
            cName.equals("duration") || cName.contains("conflicts"))
      return Integer.class;
    if (cName.equals("value") || cName.startsWith("h-") || cName.startsWith("v-") ||
            cName.startsWith("course") || cName.equals("bearing") ||
            cName.equals("added miles"))
      return Double.class;
    return String.class;
  }
  
  public int getRowCount() {
    if (actions==null)
      return 0;
    return actions.size();
  }
  
  public int getColumnCount() {
    return (toMakeActionButtons)?colNames.length:colNames.length-1;
  }
  
  public boolean isCellEditable(int row, int col) {
    return getColumnClass(col).equals(JButton.class);
  }
  
  public Object getValueAt(int row, int col) {
    if (!toMakeActionButtons && col>=buttonColIdx)
      ++col;
    Action a=actions.get(row);
    if (a==null)
      return null;
    String cName=colNames[col].toLowerCase();
    if (cName.equals("do?")) {
      if (buttons==null)
        buttons=new ArrayList<JButton>(100);
      for (int i=buttons.size(); i<=row; i++) {
        JButton b=new JButton("Do");
        b.setActionCommand("do_"+i);
        if (actionListener!=null)
          b.addActionListener(actionListener);
        buttons.add(b);
      }
      JButton b=buttons.get(row);
      b.setToolTipText(getActionDescription(a));
      return b;
    }
    if (cName.equals("flight"))
      return a.flightId;
    if (cName.equals("action"))
      return a.actionType+": "+Action.getMeaningOfActionType(a.actionType);
    if (cName.equals("value")) {
      if (a.actionValue!=0 && a.actionType.equals("A2"))
        return FlightInConflict.transformKnotsToMachNumber(a.actionValue);
      return a.actionValue;
    }
    if (cName.equals("rank"))
      return a.rank;
    if (cName.equals("added miles"))
      return (Double.isNaN(a.addMiles))?null:a.addMiles;
    if (cName.equals("added seconds"))
      return a.addTime;
    if (cName.equals("duration"))
      return a.duration;
    if (cName.contains("conflicts"))
      return (a.conflicts==null)?0:a.conflicts.size();
    if (cName.equals("h-speed change"))
      return a.hSpeedChangeMach*100;
    if (cName.equals("v-speed change"))
      return a.vSpeedChange;
    if (cName.equals("course change"))
      return a.courseChange;
    if (cName.equals("h-shift at exit"))
      return a.hShiftExit;
    if (cName.equals("v-shift at exit"))
      return a.vShiftExit;
    if (cName.equals("bearing"))
      return a.bearing;
    if (cName.equals("why not"))
      return a.whyNot;
    return null;
  }
  
  public int getPreferredColumnWidth(int col) {
    if (!toMakeActionButtons && col>=buttonColIdx)
      ++col;
    JLabel label=new JLabel(colNames[col]);
    if (colNames[col].startsWith("Flight"))
      label.setText("000000000");
    else
    if (colNames[col].equals("Do?"))
      label.setText("___Do___");
    else
    if (colNames[col].equals("Action")) {
      String s=Action.type_meanings[0];
      for (int i=1; i<Action.type_meanings.length; i++)
        if (Action.type_meanings[i].length()>s.length())
          s=Action.type_meanings[i];
      label.setText("RFP: "+s);
    }
    return label.getPreferredSize().width+10;
  }
  
  public String getDetailedText(int row, int col) {
    if (!toMakeActionButtons && col>=buttonColIdx)
      ++col;
    String cName=colNames[col].toLowerCase();
    if (cName.startsWith("why"))
      return actions.get(row).whyNot;
    if (cName.contains("conflicts"))
      return actions.get(row).getConflictsDescriptionHTML();
    if (cName.equals("do?"))
      return getActionDescription(actions.get(row));
    Object v=getValueAt(row,col);
    if (v==null)
      return null;
    String txt=v.toString();
    if (v instanceof Double) {
      int pIdx=txt.indexOf('.');
      if (pIdx>0 && pIdx<txt.length()-4)
        txt=String.format("%.4f",v);
    }
    if (cName.equals("value")) {
      Action a=actions.get(row);
      if (a.actionType.equals("A1"))
        txt+=(a.actionValue>0)?" (up)":" (down)";
      else
      if (a.actionType.equals("A2"))
        txt+=" Mach";
      else
      if (a.actionType.equals("A3"))
        txt+=" (waypoint number)";
      else
      if (a.actionType.equals("S2"))
        txt+=" degrees";
    }
    else
    if (cName.equals("added miles"))
      txt+=" nm";
    else
    if (cName.equals("added seconds") || cName.equals("duration"))
      txt+=" seconds";
    else
    if (cName.equals("h-speed change"))
      txt+=" Mach * 100";
    else
    if (cName.equals("v-speed change"))
      txt+=" feet/minute";
    else
    if (cName.equals("course change") || cName.equals("bearing"))
      txt+=" degrees";
    else
    if (cName.equals("h-shift at exit"))
      txt+=" meters";
    else
    if (cName.equals("v-shift at exit"))
      txt+="feet";
    return txt;
  }
  
  public String getActionDescription(Action a) {
    String txt="Apply action "+a.actionType;
    if (a.actionValue!=0)
      txt+=":"+a.actionValue;
    String meaning=Action.getMeaningOfActionType(a.actionType);
    txt+=" ("+meaning;
    if (a.actionValue!=0)
      txt+=((meaning.contains("change"))?" by ":" ")+a.actionValue;
    txt+=") to flight "+a.flightId;
    return txt;
  }
}
