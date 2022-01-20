package ui;

import data.Conflict;
import table_cells.NumberByBarCellRenderer;
import table_cells.TimeCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ShowConflicts {
  /**
   * The set of conflicts to be shown
   */
  public ArrayList<Conflict> conflicts=null;
  /**
   * The conflicts are separated into primary and secondary
   */
  public ArrayList<Conflict> conflicts1=null, conflicts2;
  
  public ConflictTableModel cTableModel=null;
  public JTable cTable=null;
  public JFrame mainFrame=null;
  
  public ArrayList<Conflict> getConflicts() {
    return conflicts;
  }
  
  public void setConflicts(ArrayList<Conflict> conflicts) {
    this.conflicts = conflicts;
    if (conflicts1!=null)
      conflicts1.clear();
    if (conflicts2!=null)
      conflicts2.clear();
    if (conflicts==null || conflicts.isEmpty())
      return;
    if (conflicts1==null)
      conflicts1=new ArrayList<Conflict>(20);
    conflicts1.add(conflicts.get(0));
    for (int i=1; i<conflicts.size(); i++) {
      boolean found=false;
      Conflict c=conflicts.get(i);
      for (Conflict c1:conflicts1) {
        found=c1.typeNum==c.typeNum && c1.sameFlights(c);
        if (found)
          break;
      }
      if (found) {
        if (conflicts2==null)
          conflicts2=new ArrayList<Conflict>(conflicts.size()-1);
        conflicts2.add(c);
      }
      else
        conflicts1.add(c);
    }
    
    if (cTableModel==null)
      cTableModel=new ConflictTableModel();
    cTableModel.setConflicts(conflicts1);
    
    LocalDateTime dt=conflicts.get(0).detectionTime;
    String frameTitle=String.format("Conflicts detected %02d/%02d/%04d at %02d:%02d:%02d",
        dt.getDayOfMonth(),dt.getMonthValue(),dt.getYear(),dt.getHour(),dt.getMinute(),dt.getSecond());
    
    if (cTable==null) {
      cTable = new JTable(cTableModel);
      DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
      centerRenderer.setHorizontalAlignment(JLabel.CENTER);
      TimeCellRenderer timeRenderer=new TimeCellRenderer();
      for (int i=0; i<cTableModel.getColumnCount(); i++) {
        String cName=cTableModel.getColumnName(i);
        if (cName.startsWith("Hor") || cName.startsWith("Vert")) {
          NumberByBarCellRenderer bRend= new NumberByBarCellRenderer(0,
              (cName.startsWith("Hor"))?Conflict.getMaxHorDistance(conflicts):
                                        Conflict.getMaxVertDistance(conflicts));
          bRend.setPrecision(0);
          cTable.getColumnModel().getColumn(i).setCellRenderer(bRend);
        }
        else
        if (cTableModel.getColumnClass(i).equals(String.class))
          cTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        else
        if (cTableModel.getColumnClass(i).equals(LocalDateTime.class))
          cTable.getColumnModel().getColumn(i).setCellRenderer(timeRenderer);
        int w=cTableModel.getPreferredColumnWidth(i);
        if (w>0)
          cTable.getColumnModel().getColumn(i).setPreferredWidth(w);
      }
      
      Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
  
      cTable.setPreferredScrollableViewportSize(new Dimension(Math.round(size.width * 0.6f),
          Math.min(Math.round(size.height * 0.6f),cTable.getPreferredSize().height+10)));
      cTable.setFillsViewportHeight(true);
      cTable.setAutoCreateRowSorter(true);
      cTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      cTable.setRowSelectionAllowed(true);
      cTable.setColumnSelectionAllowed(false);
      JScrollPane scrollPane = new JScrollPane(cTable);
      mainFrame = new JFrame(frameTitle);
      mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
      //Display the window.
      mainFrame.pack();
      mainFrame.setLocation(30, 30);
      mainFrame.setVisible(true);
      mainFrame.addWindowListener(new WindowAdapter() {
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
