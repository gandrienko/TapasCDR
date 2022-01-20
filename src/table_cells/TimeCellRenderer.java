package table_cells;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;

public class TimeCellRenderer implements TableCellRenderer {
  public JLabel label=null;
  public TimeCellRenderer () {
    label=new JLabel("",JLabel.RIGHT);
  }
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column) {
    label.setText("");
    if (value!=null && (value instanceof LocalDateTime)) {
      LocalDateTime dt=(LocalDateTime)value;
      label.setText(String.format("%02d:%02d:%02d",dt.getHour(),dt.getMinute(),dt.getSecond()));
    }
    if (isSelected)
      label.setBackground(table.getSelectionBackground());
    else
      label.setBackground(table.getBackground());
    return label;
  }
}
