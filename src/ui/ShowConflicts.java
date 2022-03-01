package ui;

import data.*;
import data.Action;
import map.AltiView;
import map.MapView;
import table_cells.NumberByBarCellRenderer;
import table_cells.TimeCellRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class ShowConflicts implements ItemListener, ChangeListener, ActionListener {
  public static final String versionText="TAPAS CDR UI version 01/03/2022 18:25";
  public static final int defaultMaxRankShown=3;
  /**
   * For testing: data divided into portions; one portion is shown at each time moment
   */
  //public ArrayList<DataPortion> portions=null;
  /**
   * The data updater from which data portions are received
   */
  public DataUpdater dataUpdater=null;
  /**
   * The set of conflicts to be shown
   */
  public ArrayList<Conflict> conflicts=null;
  /**
   * The events of non-conformance to prescribed conflict resolution actions
   */
  public ArrayList<NCEvent> ncEvents=null;
  
  public boolean isSecondary=false;
  
  public ConflictTableModel cTableModel=null;
  public ActionsTableModel aTableModel =null;
  
  public MapView mapView=null;
  public AltiView altiView=null;
  
  public JTable cTable=null, aTable=null;
  public JFrame mainFrame=null;
  public JPanel controlPanel=null, oneConflictPanel=null;
  public JLabel oneConflictTitle=null, updateLabel=null;
  
  protected JComboBox portionChoice=null;
  protected JButton bAuto=null, bNext=null, bPrevious=null;
  protected JSpinner stepChoice=null;
  protected boolean toAllowActionChoice=true;
  
  protected JSpinner rankChoice=null;
  protected JLabel maxRankLabel=null;
  
  protected NonConfEventTableModel ncTableModel=null;
  protected JTable ncTable=null;
  protected JFrame ncFrame=null;
  
  protected ShowConflicts secondary =null, primary =null;
  /**
   * Will receive messages about selection of conflict resolution actions
   */
  public ActionListener actionListener=null;
  /**
   * The channel for sending action selection events
   */
  public String channelName ="VAtoXAI";

  public boolean offlineMode=true;
  
  public void setOfflineMode(boolean offlineMode) {
    this.offlineMode = offlineMode;
  }
  
  /**
   * Sets a data updater from which data portions will be received
   */
  public void setDataUpdater(DataUpdater dataUpdater) {
    this.dataUpdater = dataUpdater;
    if (dataUpdater!=null) {
      dataUpdater.addChangeListener(this);
      String txt=(dataUpdater.getDataPortionsCount()>0)?
                     "Data portion N " + (dataUpdater.lastIdx + 1):"No conflict data received!";
      updateLabel = new JLabel(txt);
      updateLabel.setForeground(Color.blue.darker());
      addToControlPanel(updateLabel);
    }
  }
  
  /**
   * @param actionListener will receive messages about selection of
   *                       conflict resolution actions
   */
  public void setActionListener(ActionListener actionListener) {
    this.actionListener = actionListener;
  }
  
  public void setChannelName(String channelName) {
    this.channelName = channelName;
  }
  
  protected void addToControlPanel(JComponent component) {
    if (controlPanel==null) {
      controlPanel = new JPanel();
      controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
      controlPanel.add(component);
      if (mainFrame!=null) {
        mainFrame.getContentPane().add(controlPanel, BorderLayout.SOUTH);
        mainFrame.pack();
        mainFrame.repaint();
      }
    }
    else {
      controlPanel.add(component);
      controlPanel.invalidate();
      if (mainFrame!=null) {
        mainFrame.invalidate();
        mainFrame.validate();
      }
      else
        controlPanel.validate();
    }
  }
  
  public void enableDataPortionsSelection() {
    if (dataUpdater==null || dataUpdater.getDataPortionsCount()<1)
      return;
    ArrayList<DataPortion> portions=dataUpdater.portions;
    portionChoice=new JComboBox();
    for (int i=0; i<portions.size(); i++) {
      DataPortion p=portions.get(i);
      LocalDateTime dt=LocalDateTime.ofEpochSecond(p.timeUnix,0, ZoneOffset.UTC);
      portionChoice.addItem(String.format("%d : %02d:%02d:%02d on %02d/%02d/%04d",p.timeUnix,
          dt.getHour(),dt.getMinute(),dt.getSecond(),dt.getDayOfMonth(),dt.getMonthValue(),dt.getYear()));
    }
    portionChoice.setSelectedIndex(dataUpdater.lastIdx);
    portionChoice.addItemListener(this);
    JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
    p.add(new JLabel("   "));
    p.add(portionChoice);
    bNext = new JButton("next");
    bNext.setActionCommand("next_portion");
    bNext.addActionListener(this);
    bNext.setEnabled(portionChoice.getSelectedIndex() < portionChoice.getItemCount() - 1);
    p.add(bNext);
    bPrevious = new JButton("previous");
    bPrevious.setActionCommand("previous_portion");
    bPrevious.addActionListener(this);
    bPrevious.setEnabled(portionChoice.getSelectedIndex() > 0);
    p.add(bPrevious);
    addToControlPanel(p);
  }
  
  public void makeAutoUpdateControls(){
    JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
    p.add(new JLabel("       "));
    bAuto=new JButton("Update automatically");
    bAuto.setActionCommand("auto");
    bAuto.addActionListener(this);
    p.add(bAuto);
    p.add(new JLabel("every"));
    SpinnerModel sm=new SpinnerNumberModel(dataUpdater.timeStep,5,300,5);
    stepChoice=new JSpinner(sm);
    stepChoice.addChangeListener(this);
    p.add(stepChoice);
    p.add(new JLabel("seconds"));
    addToControlPanel(p);
  }
  
  public boolean isAutoUpdating(){
    return dataUpdater!=null && dataUpdater.isRunning();
  }
  
  public void takeDataPortion(int pIdx) {
    if (dataUpdater==null)
      return;
    DataPortion dp=dataUpdater.getDataPortion(pIdx);
    if (dp==null)
      return;
    if (portionChoice!=null &&
            portionChoice.getItemCount()<dataUpdater.getDataPortionsCount()) {
      for (int i=portionChoice.getItemCount(); i<dataUpdater.getDataPortionsCount(); i++) {
        DataPortion p=dataUpdater.getDataPortion(i);
        if (p==null)
          continue;
        LocalDateTime dt=LocalDateTime.ofEpochSecond(p.timeUnix,0, ZoneOffset.UTC);
        portionChoice.addItem(String.format("%d : %02d:%02d:%02d on %02d/%02d/%04d",p.timeUnix,
            dt.getHour(),dt.getMinute(),dt.getSecond(),dt.getDayOfMonth(),dt.getMonthValue(),dt.getYear()));
      }
    }
    if (pIdx<0)
      pIdx=dataUpdater.getLastPortionIdx();
    setData(dp.conflicts,dp.ncEvents,pIdx);
    if (updateLabel!=null) {
      updateLabel.setText("Data portion N " + (pIdx + 1));
      updateLabel.invalidate();
      updateLabel.validate();
    }
    if (!isAutoUpdating()) {
      if (bNext != null)
        bNext.setEnabled(pIdx < portionChoice.getItemCount() - 1);
      if (bPrevious != null)
        bPrevious.setEnabled(pIdx > 0);
    }
  }
  
  public void itemStateChanged(ItemEvent e){
    if (e.getSource().equals(portionChoice)) {
      int pIdx=portionChoice.getSelectedIndex();
      if (offlineMode && dataUpdater!=null &&
              (dataUpdater.getLastPortionIdx()<pIdx))
        dataUpdater.setLastPortionIdx(pIdx);
      takeDataPortion(pIdx);
    }
  }
  
  public void actionPerformed (ActionEvent e) {
    String cmd=e.getActionCommand();
    if (cmd.equals("next_portion") || cmd.equals("previous_portion")) {
      int pIdx=portionChoice.getSelectedIndex();
      if (cmd.equals("next_portion")) ++pIdx; else --pIdx;
      portionChoice.setSelectedIndex(pIdx);
    }
    else
    if (cmd.equals("auto")) {
      int fromIndex=(portionChoice!=null)?portionChoice.getSelectedIndex()+1:dataUpdater.lastIdx+1;
      if (dataUpdater.startAutoUpdating(fromIndex)) {
        if (portionChoice != null) {
          portionChoice.setEnabled(false);
          bNext.setEnabled(false);
          bPrevious.setEnabled(false);
        }
        bAuto.setText("Stop auto updates");
        bAuto.setActionCommand("stop");
        if (stepChoice!=null)
          stepChoice.setEnabled(false);
      }
    }
    else
    if (cmd.equals("stop")) {
      dataUpdater.setToStop(true);
      autoUpdateStopped();
    }
    else
    if (e.getActionCommand().startsWith("do")) {
      int idx=cmd.indexOf('_');
      int aIdx=Integer.parseInt(cmd.substring(idx+1));
      Action a=aTableModel.getAction(aIdx);
      if (JOptionPane.showConfirmDialog(FocusManager.getCurrentManager().getActiveWindow(),
          aTableModel.getActionDescription(a)+"?", "Do the action?",
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
              == JOptionPane.YES_OPTION) {
        System.out.println("Confirmed: "+aTableModel.getActionDescription(a));
        int pIdx=dataUpdater.getLastPortionIdx();
        long pTime=dataUpdater.getDataPortion(pIdx).timeUnix;
        LocalDateTime dt=LocalDateTime.ofEpochSecond(pTime,0,ZoneOffset.UTC);
        String msg=String.valueOf(pTime)+";"+a.actionId+" "+a.flightId+" "+
                       a.actionType+" "+a.actionValue+";"+
                       String.format("%04d-%02d-%02d %02d:%02d:%02d",
                           dt.getYear(),dt.getMonthValue(),dt.getDayOfMonth(),
                           dt.getHour(),dt.getMinute(),dt.getSecond());
        ActionEvent msgEvent=new ActionEvent(channelName,ActionEvent.ACTION_PERFORMED,msg);
        if (actionListener!=null) {
          //send the event to the listener
          actionListener.actionPerformed(msgEvent);
        }
        else
          System.out.println(msgEvent);
      }
      else
        System.out.println("Cancelled: "+aTableModel.getActionDescription(a));
    }
  }
  
  public void stateChanged(ChangeEvent e) {
    if (e.getSource().equals(rankChoice))
      aTableModel.setMaxRankToShow((int)rankChoice.getValue());
    else
      if (e.getSource().equals(dataUpdater)) {
        if (dataUpdater.isRunning() && dataUpdater.hasNextPortion()) {
          DataPortion dp=dataUpdater.getCurrentDataPortion();
          setData(dp.conflicts,dp.ncEvents,dataUpdater.lastIdx);
          if (portionChoice!=null)
            portionChoice.setSelectedIndex(dataUpdater.lastIdx);
          updateLabel.setText("Data portion N "+(dataUpdater.lastIdx+1));
        }
        else
          autoUpdateStopped();
      }
      else
        if (e.getSource().equals(stepChoice)){
          dataUpdater.setTimeStep((int)stepChoice.getValue());
        }
  }
  
  protected void autoUpdateStopped() {
    if (portionChoice != null) {
      portionChoice.setEnabled(true);
      int pIdx=portionChoice.getSelectedIndex();
      if (bNext != null)
        bNext.setEnabled(pIdx < portionChoice.getItemCount() - 1);
      if (bPrevious != null)
        bPrevious.setEnabled(pIdx > 0);
    }
    if (bAuto!=null) {
      bAuto.setText("Update automatically");
      bAuto.setActionCommand("auto");
    }
    if (stepChoice!=null)
      stepChoice.setEnabled(true);
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
  
  public void makeUI() {
    UIManager.put("ToolTip.background", new Color(255,240,200));
    ToolTipManager.sharedInstance().setDismissDelay(60000);
  
    cTableModel=new ConflictTableModel();
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
        Math.max(Math.min(Math.round(size.height * 0.6f),cTable.getPreferredSize().height+50),size.height/12)));
    cTable.setFillsViewportHeight(true);
    cTable.setAutoCreateRowSorter(true);
    cTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    cTable.setRowSelectionAllowed(true);
    cTable.setColumnSelectionAllowed(false);
    JScrollPane scrollPane = new JScrollPane(cTable);
    mainFrame = new JFrame(versionText);
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
    if (controlPanel!=null)
      mainFrame.getContentPane().add(controlPanel,BorderLayout.SOUTH);
    //Display the window.
    mainFrame.pack();
    if (isSecondary)
      mainFrame.setLocation(size.width-mainFrame.getWidth()-30,100);
    else
      mainFrame.setLocation(30, 30);
    mainFrame.setVisible(true);
  }
  
  public void setData(ArrayList<Conflict> conflicts,
                      ArrayList<NCEvent> ncEvents,
                      int portionIdx) {
    this.conflicts = conflicts;
    this.ncEvents=ncEvents;
    toAllowActionChoice=dataUpdater!=null && portionIdx==dataUpdater.getLastPortionIdx();
    
    if (portionChoice!=null) {
      portionChoice.removeItemListener(this);
      portionChoice.setSelectedIndex(portionIdx);
      portionChoice.addItemListener(this);
    }
    
    cTableModel.setConflicts(conflicts);
    cTableModel.fireTableDataChanged();
    
    if (conflicts==null || conflicts.isEmpty()) {
      if (mapView!=null)
        mapView.setConflict(null);
      if (altiView!=null)
        altiView.setConflict(null);
      if (aTableModel!=null) {
        aTableModel.setActions(null,false);
        if (rankChoice!=null)
          rankChoice.setEnabled(false);
      }
      if (oneConflictTitle!=null) {
        oneConflictTitle.setText("The earlier shown information expired!");
        oneConflictTitle.repaint();
      }
    }
    
    String frameTitle=versionText;
    if (conflicts!=null && !conflicts.isEmpty()) {
      LocalDateTime dt = conflicts.get(0).detectionTime;
      frameTitle = String.format("%s; Conflicts detected %02d/%02d/%04d at %02d:%02d:%02d",
          versionText,
          dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(), dt.getHour(), dt.getMinute(), dt.getSecond());
    }
    
    mainFrame.setTitle(frameTitle);
    
    if (mapView!=null) {
      int cIdx=-1;
      if (mapView.conflict!=null) {
        //check if the current set of conflicts contains data about a conflict between the same flights
        //as currently shown in the map view
        Conflict c = mapView.conflict;
        for (int i = 0; i < conflicts.size() && cIdx < 0; i++)
          if (c.sameFlights(conflicts.get(i)))
            cIdx = i;
      }
      if (cIdx<0) {
        if (conflicts==null || conflicts.isEmpty()) {
          oneConflictTitle.setText("The earlier shown information expired!");
          oneConflictTitle.repaint();
          mapView.setConflict(null);
          altiView.setConflict(null);
          if (aTableModel != null) {
            aTableModel.setActions(null,false);
            if (rankChoice != null)
              rankChoice.setEnabled(false);
          }
        }
        else {
          showConflictGeometry(conflicts.get(0));
          cTable.setRowSelectionInterval(0, 0);
        }
      }
      else {
        showConflictGeometry(conflicts.get(cIdx));
        int rIdx=cTable.convertRowIndexToView(cIdx);
        cTable.setRowSelectionInterval(rIdx, rIdx);
      }
    }
    if (!isSecondary)
      showNonConformance();
  }
  
  public void showNonConformance() {
    if (ncEvents==null || ncEvents.isEmpty()) {
      if (ncTableModel!=null) {
        ncTableModel.setNCEvents(null);
        ncTableModel.fireTableDataChanged();
      }
      if (ncFrame!=null)
        ncFrame.setVisible(false);
      return;
    }
    if (ncTableModel!=null) {
      ncTableModel.setNCEvents(ncEvents);
      ncTableModel.fireTableDataChanged();
    }
    else {
      ncTableModel=new NonConfEventTableModel();
      ncTableModel.setNCEvents(ncEvents);
      ncTable=new JTable(ncTableModel);
      DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
      centerRenderer.setHorizontalAlignment(JLabel.CENTER);
      TimeCellRenderer timeRenderer=new TimeCellRenderer();
      for (int i=0; i<ncTableModel.getColumnCount(); i++) {
        if (ncTableModel.getColumnClass(i).equals(String.class))
          ncTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        else
          if (ncTableModel.getColumnClass(i).equals(LocalDateTime.class))
            ncTable.getColumnModel().getColumn(i).setCellRenderer(timeRenderer);
        int w=ncTableModel.getPreferredColumnWidth(i);
        if (w>0)
          ncTable.getColumnModel().getColumn(i).setPreferredWidth(w);
      }
      ncTable.setFillsViewportHeight(true);
      ncTable.setAutoCreateRowSorter(true);
      ncTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      ncTable.setRowSelectionAllowed(false);
      ncTable.setColumnSelectionAllowed(false);
      Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
      ncTable.setPreferredScrollableViewportSize(new Dimension(Math.round(size.width * 0.35f),
          Math.min(Math.round(size.height * 0.25f),cTable.getPreferredSize().height+50)));
    }
    if (ncFrame!=null)
      ncFrame.setVisible(true);
    else {
      ncFrame=new JFrame("Non-conformance events");
      JScrollPane scrollPane = new JScrollPane(ncTable);
      ncFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      ncFrame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          super.windowClosing(e);
          ncFrame.dispose();
          ncFrame=null;
        }
      });
      ncFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
      //Display the window.
      ncFrame.pack();
      Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
      ncFrame.setLocation(size.width-ncFrame.getWidth()-10,(size.height-ncFrame.getHeight())/2);
      ncFrame.setVisible(true);
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
      aTableModel.setActionListener(this);
      aTableModel.setActions(conflict.actions,toAllowActionChoice);
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
      aTableModel.setColumnModel(aTable.getColumnModel());
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
                  secondary.setData(null,null,-1);
              }
              else {
                if (secondary ==null) {
                  secondary =new ShowConflicts();
                  secondary.setIsSecondary(true);
                  secondary.setPrimary(primary);
                  secondary.makeUI();
                }
                secondary.setData(a.conflicts,null,-1);
              }
            }
          }
        }
      });
  
      
      aTableModel.setMaxRankToShow(defaultMaxRankShown);
      int value=aTableModel.maxRankToShow;
      if (aTableModel.maxRank>0 && (value<0 || value>aTableModel.maxRank))
        value=aTableModel.maxRank;
      SpinnerModel rankChoiceModel=new SpinnerNumberModel(value,0,
          Math.max(aTableModel.maxRank,value),1);
      rankChoice=new JSpinner(rankChoiceModel);
      rankChoice.addChangeListener(this);
      maxRankLabel=new JLabel("max = ???");
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
        JPanel rp=new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));
        rp.add(new JLabel("Show actions with ranks up to"));
        rp.add(rankChoice);
        rp.add(maxRankLabel);
        JPanel p=new JPanel(new BorderLayout());
        p.add(rp,BorderLayout.NORTH);
        p.add(scrollPane,BorderLayout.CENTER);
        spl2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spl1, p);
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
      aTableModel.setActions(conflict.actions,toAllowActionChoice);
      if (maxRankLabel!=null)
        maxRankLabel.setText("max = "+aTableModel.maxRank);
      if (rankChoice!=null)
        if (aTableModel.maxRank<1)
          rankChoice.setEnabled(false);
        else {
          int value=Math.min(aTableModel.maxRankToShow,aTableModel.maxRank);
          if (value<0)
            value=aTableModel.maxRank;
          SpinnerModel rankChoiceModel=new SpinnerNumberModel(value,0,
              aTableModel.maxRank,1);
          rankChoice.setModel(rankChoiceModel);
          rankChoice.setEnabled(true);
        }
    }
  }
  
}
