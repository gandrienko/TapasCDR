package table_cells;

import ui.ConflictTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class NumberByBarCellRenderer extends JLabel implements TableCellRenderer {
  public double min=0, max=0, value =0;
  public double lowLimit=Double.NaN;
  public boolean toShowDistanceToLowLimit=true;
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
  
  public void setUnit(String unit) {
    this.unit = unit;
  }
  
  public void paintComponent (Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0,0,getWidth(),getHeight());
    if (min<max && !Double.isNaN(value)) {
      if (!toShowDistanceToLowLimit || Double.isNaN(lowLimit) || value>=lowLimit || lowLimit<=min) {
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
        double ratio=1.0-(value-min)/(lowLimit-min);
        g.setColor(new Color((int)Math.round(ratio*255),0,0,25+(int)Math.round(ratio*125)));
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
      if (!Double.isNaN(lowLimit) && this.value<lowLimit && conflictTableModel!=null)
        toShowDistanceToLowLimit=conflictTableModel.isDistanceToLowLimitImportant(row,column);
    }
    if (isSelected)
      setBackground(table.getSelectionBackground());
    else
      setBackground(table.getBackground());
    return this;
  }
}
