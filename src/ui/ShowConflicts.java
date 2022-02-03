package ui;

import data.Conflict;
import data.ConflictPoint;
import data.DataPortion;
import map.AltiView;
import map.MapView;
import table_cells.NumberByBarCellRenderer;
import table_cells.TimeCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class ShowConflicts implements ItemListener{
  public static final String versionText="TAPAS CDR UI version 02/02/2022 19:05";
  /**
   * For testing: data divided into portions; one portion is shown at each time moment
   */
  public ArrayList<DataPortion> portions=null;
  /**
   * The set of conflicts to be shown
   */
  public ArrayList<Conflict> conflicts=null;
  
  public boolean isSecondary=false;
  
  public ConflictTableModel cTableModel=null;
  public ActionsTableModel aTableModel =null;
  
  public MapView mapView=null;
  public AltiView altiView=null;
  
  public JTable cTable=null, aTable=null;
  public JFrame mainFrame=null;
  public JPanel oneConflictPanel=null;
  public JLabel oneConflictTitle=null;
  
  protected JComboBox portionChoice=null;
  
  protected ShowConflicts secondary =null, primary =null;
  
  public void setDataPortions(ArrayList<DataPortion> portions) {
    this.portions = portions;
    if (portions==null || portions.isEmpty())
      return;
    setConflicts(portions.get(0).conflicts);
    portionChoice=new JComboBox();
    for (int i=0; i<portions.size(); i++) {
      DataPortion p=portions.get(i);
      LocalDateTime dt=LocalDateTime.ofEpochSecond(p.timeUnix,0, ZoneOffset.UTC);
      portionChoice.addItem(String.format("%d : %02d:%02d:%02d on %02d/%02d/%04d",p.timeUnix,
          dt.getHour(),dt.getMinute(),dt.getSecond(),dt.getDayOfMonth(),dt.getMonthValue(),dt.getYear()));
    }
    portionChoice.setSelectedIndex(0);
    portionChoice.addItemListener(this);
    JPanel p=new JPanel();
    p.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
    p.add(portionChoice);
    mainFrame.getContentPane().add(p,BorderLayout.SOUTH);
    mainFrame.pack();
    mainFrame.repaint();
  }
  
  public void itemStateChanged(ItemEvent e){
    if (e.getSource().equals(portionChoice)) {
      int pIdx=portionChoice.getSelectedIndex();
      setConflicts(portions.get(pIdx).conflicts);
    }
  }
  
  public void setIsSecondary(boolean secondary) {
    isSecondary = secondary;
  }
  
  public void secondaryClosed(){
    secondary =null;
  }
  
  public void setPrimary(ShowConflicts primary) {
    this.primary = primary;
  }
  
  public ArrayList<Conflict> getConflicts() {
    return conflicts;
  }
  
  public void setConflicts(ArrayList<Conflict> conflicts) {
    this.conflicts = conflicts;
    
    if (cTableModel==null)
      cTableModel=new ConflictTableModel();
    cTableModel.setConflicts(conflicts);
    if (cTable!=null)
      cTableModel.fireTableDataChanged();
    
    if (conflicts==null || conflicts.isEmpty()) {
      if (mapView!=null)
        mapView.setConflict(null);
      if (altiView!=null)
        altiView.setConflict(null);
      if (aTableModel!=null) {
        aTableModel.setActions(null);
        aTableModel.fireTableDataChanged();
      }
      return;
    }
    
    LocalDateTime dt=conflicts.get(0).detectionTime;
    String frameTitle=String.format("%s; Conflicts detected %02d/%02d/%04d at %02d:%02d:%02d",
        versionText,
        dt.getDayOfMonth(),dt.getMonthValue(),dt.getYear(),dt.getHour(),dt.getMinute(),dt.getSecond());
    
    if (mainFrame!=null)
      mainFrame.setTitle(frameTitle);
    
    if (cTable==null) {
      UIManager.put("ToolTip.background", new Color(255,240,200));
      ToolTipManager.sharedInstance().setDismissDelay(60000);
      
      cTable = new JTable(cTableModel) {
        public String getToolTipText(MouseEvent e) {
          java.awt.Point p = e.getPoint();
          int colIndex = columnAtPoint(p), rowIndex = rowAtPoint(p);
          if (colIndex >= 0 && rowIndex>=0) {
            int realColIndex = convertColumnIndexToModel(colIndex);
            if (realColIndex >= 0) {
              int realRowIndex = convertRowIndexToModel(rowIndex);
              if (realRowIndex>=0) {
                String text=cTableModel.getDetailedText(realRowIndex,realColIndex);
                if (text!=null && text.length()>5)
                  return text;
              }
            }
          }
          return null;
        }
        
      };
      DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
      centerRenderer.setHorizontalAlignment(JLabel.CENTER);
      TimeCellRenderer timeRenderer=new TimeCellRenderer();
      for (int i=0; i<cTableModel.getColumnCount(); i++) {
        String cName=cTableModel.getColumnName(i).toLowerCase();
        NumberByBarCellRenderer hDRend=new NumberByBarCellRenderer(0,Conflict.getMaxHorDistance(conflicts)),
            vDRend=new NumberByBarCellRenderer(0,Conflict.getMaxVertDistance(conflicts));
        hDRend.setPrecision(2);
        hDRend.setLowLimit(ConflictPoint.HD_MIN);
        hDRend.conflictTableModel=cTableModel;
        vDRend.setPrecision(0);
        vDRend.setLowLimit(ConflictPoint.VD_MIN);
        vDRend.setConflictTableModel(cTableModel);
        if (cName.contains("moc") || cName.contains("compliance")) {
          NumberByBarCellRenderer bRend= new NumberByBarCellRenderer(0,100);
          bRend.setPrecision(2);
          bRend.setLowLimit(100);
          bRend.setUnit("%");
          bRend.setConflictTableModel(cTableModel);
          cTable.getColumnModel().getColumn(i).setCellRenderer(bRend);
        }
        else
        if (cName.contains("severity")) {
          NumberByBarCellRenderer sRend=new NumberByBarCellRenderer(0,15);
          sRend.setPrecision(0);
          sRend.setUpLimit(0);
          sRend.setConflictTableModel(cTableModel);
          cTable.getColumnModel().getColumn(i).setCellRenderer(sRend);
        }
        else
        if (cName.contains("rate")) {
          NumberByBarCellRenderer rend=(cName.startsWith("h"))?
                                           new NumberByBarCellRenderer(-100,750):
                                           new NumberByBarCellRenderer(-1000,4000);
          rend.setPrecision((cName.startsWith("h"))?2:0);
          rend.setUpLimit(0);
          rend.setConflictTableModel(cTableModel);
          cTable.getColumnModel().getColumn(i).setCellRenderer(rend);
        }
        else
        if (cName.startsWith("hor"))
          cTable.getColumnModel().getColumn(i).setCellRenderer(hDRend);
        else
        if (cName.startsWith("vert"))
          cTable.getColumnModel().getColumn(i).setCellRenderer(vDRend);
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
            if (realRowIndex>=0 && realRowIndex<cTableModel.conflicts.size())
              showConflictGeometry(cTableModel.conflicts.get(realRowIndex));
          }
        }
      });
      
      Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
  
      cTable.setPreferredScrollableViewportSize(new Dimension(Math.round(size.width * 0.6f),
          Math.max(Math.min(Math.round(size.height * 0.6f),cTable.getPreferredSize().height+10),size.height/12)));
      cTable.setFillsViewportHeight(true);
      cTable.setAutoCreateRowSorter(true);
      cTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      cTable.setRowSelectionAllowed(true);
      cTable.setColumnSelectionAllowed(false);
      JScrollPane scrollPane = new JScrollPane(cTable);
      mainFrame = new JFrame(frameTitle);
      if (!isSecondary) {
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            if (JOptionPane.showConfirmDialog(FocusManager.getCurrentManager().getActiveWindow(),
                "Sure to exit?",
                "Sure to exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.YES_OPTION)
              System.exit(0);
          }
        });
      }
      else {
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            mainFrame.dispose();
            if (primary !=null)
              primary.secondaryClosed();
          }
        });
      }
      mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
      //Display the window.
      mainFrame.pack();
      if (isSecondary)
        mainFrame.setLocation(size.width-mainFrame.getWidth()-30,100);
      else
        mainFrame.setLocation(30, 30);
      mainFrame.setVisible(true);
    }
    else
    if (mapView!=null && mapView.conflict!=null) {
      //check if the current set of conflicts contains data about a conflict between the same flights
      //as currently shown in the map view
      Conflict c=mapView.conflict;
      int cIdx=-1;
      for (int i=0; i<conflicts.size() && cIdx<0; i++)
        if (c.sameFlights(conflicts.get(i)))
          cIdx=i;
      if (cIdx<0) {
        oneConflictTitle.setText("The earlier shown information expired!");
        oneConflictTitle.repaint();
        mapView.setConflict(null);
        altiView.setConflict(null);
        if (aTableModel!=null) {
          aTableModel.setActions(null);
          aTableModel.fireTableDataChanged();
        }
      }
      else {
        showConflictGeometry(conflicts.get(cIdx));
        int rIdx=cTable.convertRowIndexToView(cIdx);
        cTable.setRowSelectionInterval(rIdx, rIdx);
      }
    }
  }
  
  public void showConflictGeometry(Conflict conflict) {
    Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
    if (mapView==null) {
      mapView=new MapView();
      mapView.setPreferredSize(new Dimension(size.width/4,size.height/4));
    }
    if (altiView==null) {
      altiView=new AltiView();
      altiView.setPreferredSize(new Dimension(size.width/4,size.height/4));
      mapView.setTimeTransmitter(altiView.getTimeTransmitter());
    }
    if (!isSecondary && aTable==null) {
      aTableModel =new ActionsTableModel();
      aTableModel.setActions(conflict.actions);
      aTable = new JTable(aTableModel){
        public String getToolTipText(MouseEvent e) {
          java.awt.Point p = e.getPoint();
          int colIndex = columnAtPoint(p), rowIndex = rowAtPoint(p);
          if (colIndex >= 0 && rowIndex>=0) {
            int realColIndex = convertColumnIndexToModel(colIndex);
            if (realColIndex >= 0) {
              int realRowIndex = convertRowIndexToModel(rowIndex);
              if (realRowIndex>=0) {
                String text=aTableModel.getDetailedText(realRowIndex,realColIndex);
                if (text!=null && text.length()>5)
                  return text;
              }
            }
          }
          return null;
        }
      };
      DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
      centerRenderer.setHorizontalAlignment(JLabel.CENTER);
      TimeCellRenderer timeRenderer=new TimeCellRenderer();
      for (int i=0; i<aTableModel.getColumnCount(); i++) {
        if (aTableModel.getColumnClass(i).equals(String.class))
          aTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        int w=aTableModel.getPreferredColumnWidth(i);
        if (w>0)
          aTable.getColumnModel().getColumn(i).setPreferredWidth(w);
      }
      aTable.setPreferredScrollableViewportSize(new Dimension(Math.round(size.width * 0.6f),
          Math.min(Math.round(size.height * 0.6f),aTable.getPreferredSize().height+10)));
      aTable.setFillsViewportHeight(true);
      aTable.setAutoCreateRowSorter(true);
      aTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      aTable.setRowSelectionAllowed(true);
      aTable.setColumnSelectionAllowed(false);

      ShowConflicts primary=this;
      
      aTable.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          super.mousePressed(e);
          if (e.getButton()==MouseEvent.BUTTON1) {
            int rowIndex=aTable.rowAtPoint(e.getPoint());
            if (rowIndex<0)
              return;
            int realRowIndex = aTable.convertRowIndexToModel(rowIndex);
            if (realRowIndex>=0 && realRowIndex<aTableModel.actions.size()) {
              data.Action a=aTableModel.actions.get(realRowIndex);
              if (a.conflicts==null || a.conflicts.isEmpty()) {
                if (secondary !=null)
                  secondary.setConflicts(null);
              }
              else {
                if (secondary ==null) {
                  secondary =new ShowConflicts();
                  secondary.setIsSecondary(true);
                  secondary.setPrimary(primary);
                }
                secondary.setConflicts(a.conflicts);
              }
            }
          }
        }
      });
    }
    if (oneConflictPanel==null) {
      oneConflictPanel=new JPanel();
      oneConflictPanel.setLayout(new BorderLayout());
      oneConflictTitle=new JLabel("Conflict of flights " +
                                      conflict.flights[0].flightId +
                                      " and " + conflict.flights[1].flightId,JLabel.LEFT);
      oneConflictPanel.add(oneConflictTitle,BorderLayout.NORTH);
      JSplitPane spl1=new JSplitPane(JSplitPane.VERTICAL_SPLIT,mapView,altiView);
      spl1.setDividerLocation(mapView.getPreferredSize().height);
      JSplitPane spl2=null;
      if (aTable!=null) {
        JScrollPane scrollPane = new JScrollPane(aTable);
        spl2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spl1, scrollPane);
        spl2.setDividerLocation(Math.round(0.3f * size.width));
      }
      oneConflictPanel.add((spl2==null)?spl1:spl2,BorderLayout.CENTER);
      Container mainC=mainFrame.getContentPane();
      JSplitPane splAll=new JSplitPane(JSplitPane.VERTICAL_SPLIT,mainC,oneConflictPanel);
      splAll.setDividerLocation(Math.round(0.1f * size.height));
      mainFrame.setContentPane(splAll);
      mainFrame.pack();
      mainFrame.setSize(Math.min(mainFrame.getWidth(),Math.round(0.8f*size.width)),
          Math.min(mainFrame.getHeight(),Math.round(0.8f*size.height)));
    }
    if (conflict.equals(mapView.conflict))
      return;
    oneConflictTitle.setText("Conflict of flights " + conflict.flights[0].flightId +
                                 " and " + conflict.flights[1].flightId);
    oneConflictTitle.repaint();
    mapView.setConflict(conflict);
    altiView.setConflict(conflict);
    if (aTableModel!=null) {
      aTableModel.setActions(conflict.actions);
      aTableModel.fireTableDataChanged();
    }
  }
  
}
