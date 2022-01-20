package ui;

import data.Conflict;
import table_cells.NumberByBarCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class ShowConflicts {
  /**
   * The set of conflicts to be shown
   */
  public ArrayList<Conflict> conflicts=null;
  /**
   * Other types of events (separated from conflicts)
   */
  public ArrayList<Conflict> otherEvents=null;
  
  public ConflictTableModel cTableModel=null;
  public JTable cTable=null;
  
  public ArrayList<Conflict> getConflicts() {
    return conflicts;
  }
  
  public void setConflicts(ArrayList<Conflict> conflicts) {
    this.conflicts = conflicts;
    if (otherEvents!=null)
      otherEvents.clear();
    if (conflicts==null || conflicts.isEmpty())
      return;
    /**
    for (int i=conflicts.size()-1; i>=0; i--)
      if (conflicts.get(i).typeNum!=Conflict.type_Conflict) {
        if (otherEvents==null)
          otherEvents=new ArrayList<Conflict>(conflicts.size()/2);
        otherEvents.add(conflicts.get(i));
        conflicts.remove(i);
      }
     */
    
    if (cTableModel==null)
      cTableModel=new ConflictTableModel();
    cTableModel.setConflicts(conflicts);
    if (cTable==null) {
      cTable = new JTable(cTableModel);
      for (int i=0; i<cTableModel.getColumnCount(); i++) {
        String cName=cTableModel.getColumnName(i);
        if (cName.startsWith("Hor") || cName.startsWith("Vert")) {
          NumberByBarCellRenderer bRend= new NumberByBarCellRenderer(0,
              (cName.startsWith("Hor"))?Conflict.getMaxHorDistance(conflicts):
                                        Conflict.getMaxVertDistance(conflicts));
          bRend.setPrecision(0);
          cTable.getColumnModel().getColumn(i).setCellRenderer(bRend);
        }
        int w=cTableModel.getPreferredColumnWidth(i);
        if (w>0)
          cTable.getColumnModel().getColumn(i).setPreferredWidth(w);
      }
      
      Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
  
      cTable.setPreferredScrollableViewportSize(new Dimension(Math.round(size.width * 0.7f), Math.round(size.height * 0.6f)));
      cTable.setFillsViewportHeight(true);
      cTable.setAutoCreateRowSorter(true);
      cTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      cTable.setRowSelectionAllowed(true);
      cTable.setColumnSelectionAllowed(false);
      JScrollPane scrollPane = new JScrollPane(cTable);
      JFrame fr = new JFrame("Conflicts "+conflicts.get(0).detectionTime);
      fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      fr.getContentPane().add(scrollPane, BorderLayout.CENTER);
      //Display the window.
      fr.pack();
      fr.setLocation(30, 30);
      fr.setVisible(true);
      fr.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          if (JOptionPane.showConfirmDialog(FocusManager.getCurrentManager().getActiveWindow(),
              "Sure to exitt?",
              "Sure to exit?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)
              ==JOptionPane.YES_OPTION)
            System.exit(0);
        }
      });
    }
  }
  
}
