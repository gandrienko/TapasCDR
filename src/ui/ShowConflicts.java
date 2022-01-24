package ui;

import data.Conflict;
import map.AltiView;
import map.MapView;
import table_cells.NumberByBarCellRenderer;
import table_cells.TimeCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
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
  public ActionsTableModel aTableModel =null;
  
  public MapView mapView=null;
  public AltiView altiView=null;
  
  public JTable cTable=null, aTable=null;
  public JFrame mainFrame=null, mapFrame=null;
  
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
  
      
      cTable.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          super.mousePressed(e);
          if (e.getButton()==MouseEvent.BUTTON1) {
            int rowIndex=cTable.rowAtPoint(e.getPoint());
            if (rowIndex<0)
              return;
            int realRowIndex = cTable.convertRowIndexToModel(rowIndex);
            showConflictGeometry(conflicts1.get(realRowIndex));
          }
        }
      });
      
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
  
  public void showConflictGeometry(Conflict conflict) {
    Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
    if (mapView==null) {
      mapView=new MapView();
      mapView.setPreferredSize(new Dimension(size.height/3,size.height/3));
    }
    if (altiView==null) {
      altiView=new AltiView();
      altiView.setPreferredSize(new Dimension(size.width/3,size.height/4));
    }
    if (aTable==null) {
      aTableModel =new ActionsTableModel();
      aTableModel.setActions(conflict.actions);
      aTable = new JTable(aTableModel);
      DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
      centerRenderer.setHorizontalAlignment(JLabel.CENTER);
      TimeCellRenderer timeRenderer=new TimeCellRenderer();
      for (int i=0; i<aTableModel.getColumnCount(); i++) {
        if (aTableModel.getColumnClass(i).equals(String.class))
          aTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
      }
      aTable.setPreferredScrollableViewportSize(new Dimension(Math.round(size.width * 0.6f),
          Math.min(Math.round(size.height * 0.6f),aTable.getPreferredSize().height+10)));
      aTable.setFillsViewportHeight(true);
      aTable.setAutoCreateRowSorter(true);
      aTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      aTable.setRowSelectionAllowed(true);
      aTable.setColumnSelectionAllowed(false);
    }
    if (mapFrame==null) {
      JSplitPane spl1=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,mapView,altiView);
      spl1.setDividerLocation(mapView.getPreferredSize().width);
      JScrollPane scrollPane = new JScrollPane(aTable);
      JSplitPane spl2=new JSplitPane(JSplitPane.VERTICAL_SPLIT,spl1,scrollPane);
      spl2.setDividerLocation(mapView.getPreferredSize().height);

      mapFrame = new JFrame("Conflict geometry");
      mapFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      mapFrame.getContentPane().add(spl2, BorderLayout.CENTER);
      //Display the window.
      mapFrame.pack();
      mapFrame.setSize(Math.min(mainFrame.getWidth(),Math.round(0.8f*size.width)),
          Math.min(mainFrame.getHeight(),Math.round(0.8f*size.height)));
      mapFrame.setLocation(size.width -mapFrame.getWidth()-50, size.height -mapFrame.getHeight()-50);
      mapFrame.setVisible(true);
    }
    mapFrame.setTitle("Conflict of flights " + conflict.flights[0].flightId + " and " + conflict.flights[1].flightId);
    mapView.setConflict(conflict);
    altiView.setConflict(conflict);
    aTableModel.setActions(conflict.actions);
    aTable.repaint();
  }
  
}
