package ui;

import data.DataUpdater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TapasCDRuiRunner {
  /**
   * receives messages about selection of conflict resolution actions
   */
  public ActionListener actionListener=null;
  /**
   * The channel for sending action selection events
   */
  public String channelName ="VAtoXAI";
  /**
   * Displays the detected conflicts and proposed resolution actions
   */
  public ShowConflicts ui=null;
  /**
   * Loads and keeps the incoming data portions
   */
  public DataUpdater dataUpdater=null;
  
  public boolean offlineMode=true;
  
  /**
   * @param actionListener will receive messages about selection of
   *                       conflict resolution actions
   */
  public TapasCDRuiRunner(ActionListener actionListener) {
    this.actionListener = actionListener;
  }
  
  /**
   * @param actionListener will receive messages about selection of
   *                       conflict resolution actions
   */
  public void setActionListener(ActionListener actionListener) {
    this.actionListener = actionListener;
    if (ui!=null)
      ui.setActionListener(actionListener);
  }
  
  public void setChannelName(String channelName) {
    this.channelName = channelName;
    if (ui!=null)
      ui.setChannelName(channelName);
  }
  
  public void setOfflineMode(boolean offlineMode) {
    this.offlineMode = offlineMode;
    if (ui!=null)
      ui.setOfflineMode(offlineMode);
  }
  
  /**
   * @param path where to take the data
   * @return true if successful
   */
  public boolean takeData(String path) {
    if (path==null)
      return false;
    if (dataUpdater==null)
      dataUpdater=new DataUpdater();
    int nPrevPortions=dataUpdater.getDataPortionsCount();
    if (!dataUpdater.takeNewData(path))
      return false;
    if (ui==null) {
      ui=new ShowConflicts();
      ui.setDataUpdater(dataUpdater);
      ui.setActionListener(actionListener);
      ui.setChannelName(channelName);
    }
    int nCurrPortions=dataUpdater.getDataPortionsCount();
    if (!offlineMode)
      dataUpdater.toLastPortion();
    ui.setOfflineMode(offlineMode);
    ui.takeDataPortion(-1);
    if (nPrevPortions<=1 && nCurrPortions>1) {
      ui.enableDataPortionsSelection();
      if (offlineMode)
        ui.makeAutoUpdateControls();
    }
    return true;
  }
  
  public void emulateDataUpdating() {
    if (ui==null || dataUpdater==null || dataUpdater.getDataPortionsCount()<2)
      return;
    JPanel p=new JPanel(new BorderLayout());
    JPanel pp=new JPanel(new FlowLayout(FlowLayout.CENTER,20,20));
    pp.add(new JLabel("Press the button for taking the next data portion"));
    p.add(pp,BorderLayout.CENTER);
    JButton b=new JButton("Take new data");
    pp=new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
    pp.add(b);
    p.add(pp,BorderLayout.SOUTH);
    
    JFrame frame=new JFrame("Emulation of online data updates");
    frame.getContentPane().add(p,BorderLayout.CENTER);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        if (dataUpdater.getLastPortionIdx()+1>=dataUpdater.getDataPortionsCount() ||
            JOptionPane.showConfirmDialog(FocusManager.getCurrentManager().getActiveWindow(),
            "Stop emulating online mode?",
            "Go offline?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION) {
          frame.dispose();
          goOffline();
        }
      }
    });
  
    b.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (dataUpdater.getLastPortionIdx()+1>=dataUpdater.getDataPortionsCount())
          dataUpdater.toFirstPortion();
        ui.takeDataPortion(-1); //the UI will take the next portion
        dataUpdater.toNextPortion();
      }
    });
  
    Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
    frame.pack();
    frame.setLocation(size.width-frame.getWidth()-20,20);
    frame.setVisible(true);
  }
  
  public void goOffline(){
    if (offlineMode)
      return;
    offlineMode=true;
    ui.setOfflineMode(true);
    ui.makeAutoUpdateControls();
  }
}
