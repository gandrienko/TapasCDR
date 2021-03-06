package table_cells;

import ui.ConflictTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class NumberByBarCellRenderer extends JLabel implements TableCellRenderer {
  public double min=0, max=0, value =0;
  public double lowLimit=Double.NaN, upLimit=Double.NaN;
  public boolean toShowDistanceToLimit =true;
  public int precision=-1;
  public String unit="";
  public ConflictTableModel conflictTableModel=null;
  
  public NumberByBarCellRenderer(double min, double max) {
    super("", JLabel.RIGHT);
    this.min=min;
    this.max=max;
    setHorizontalAlignment(SwingConstants.RIGHT);
    setOpaque(false);
  }
  
  public void setPrecision(int precision) {
    this.precision = precision;
  }
  
  public void setLowLimit(double lowLimit) {
    this.lowLimit = lowLimit;
  }
  
  public void setUpLimit(double upLimit) {
    this.upLimit = upLimit;
  }
  
  public void setUnit(String unit) {
    this.unit = unit;
  }
  
  public void setConflictTableModel(ConflictTableModel conflictTableModel) {
    this.conflictTableModel = conflictTableModel;
  }
  
  public void paintComponent (Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0,0,getWidth(),getHeight());
    if (min<max && !Double.isNaN(value)) {
      toShowDistanceToLimit &=!Double.isNaN(lowLimit) || !Double.isNaN(upLimit);
      if (toShowDistanceToLimit) {
        if (!Double.isNaN(lowLimit))
          toShowDistanceToLimit=value<lowLimit;
        else
          if (!Double.isNaN(upLimit))
            toShowDistanceToLimit=value>upLimit;
      }
      if (!toShowDistanceToLimit) {
        g.setColor(Color.lightGray);
        if (min >= 0)
          g.fillRect(0, 2, (int) Math.round(getWidth() * (value - min) / (max - min)), getHeight() - 4);
        else
          if (max <= 0) {
            int dw = (int) Math.round(getWidth() * (max - value) / (max - min));
            g.fillRect(getWidth() - dw, 2, dw, getHeight() - 4);
          }
          else { // min<=0, max>=0
            int xZero = (int) Math.round(getWidth() * (0 - min) / (max - min));
            if (value > 0) {
              g.fillRect(xZero, 2, (int) Math.round((getWidth() - xZero) * (value - 0) / (max - 0)), getHeight() - 4);
            }
            else {
              int dw = (int) Math.round(xZero * (0 - value) / (0 - min));
              g.fillRect(xZero - dw, 2, dw, getHeight() - 4);
            }
          }
      }
      else {
        double ratio=(!Double.isNaN(lowLimit))?1.0-(value-min)/(lowLimit-min):
                         (value-upLimit)/(max-upLimit);
        g.setColor(new Color(100+(int)Math.round(ratio*155),0,0,50+(int)Math.round(ratio*100)));
        g.fillRect(0,2,(int)Math.round(ratio*getWidth()),getHeight()-4);
      }
    }
    super.paintComponent(g);
  }
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column) {
    if (value!=null) {
      if (value instanceof Float)
        this.value=(Float)value;
      else
      if (value instanceof Double)
        this.value=(Double)value;
      else
        this.value=(Integer)value;
    }
    else
      this.value=Double.NaN;
    if (value==null)
      setText("");
    else
    if (this.value<min || this.value>max) {
      if (this.value<min)
        min=this.value;
      else
      if (this.value>max)
        max=this.value;
      table.repaint();
    }
    if (value!=null) {
      if (value instanceof Integer)
        setText(String.format("%d%s",value,unit));
      else
      if (precision<0)
        setText(value.toString()+unit);
      else
        setText(String.format("%."+precision+"f%s",value,unit));
      if (!Double.isNaN(lowLimit) || !Double.isNaN(upLimit)) {
        row=table.convertRowIndexToModel(row);
        column=table.convertColumnIndexToModel(column);
        toShowDistanceToLimit = (conflictTableModel == null) ? true :
                                    conflictTableModel.isDistanceToLimitImportant(row, column);
      }
    }
    if (isSelected)
      setBackground(table.getSelectionBackground());
    else
      setBackground(table.getBackground());
    return this;
  }
}
