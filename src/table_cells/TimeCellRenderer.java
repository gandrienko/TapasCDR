package table_cells;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;

public class TimeCellRenderer extends JLabel implements TableCellRenderer {
  public TimeCellRenderer () {
    super("",JLabel.RIGHT);
  }
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column) {
    setText("");
    if (value!=null && (value instanceof LocalDateTime)) {
      LocalDateTime dt=(LocalDateTime)value;
      setText(String.format("%02d:%02d:%02d",dt.getHour(),dt.getMinute(),dt.getSecond()));
    }
    if (isSelected)
      setBackground(table.getSelectionBackground());
    else
      setBackground(table.getBackground());
    return this;
  }
  public void paintComponent (Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    super.paintComponent(g);
  }
}
