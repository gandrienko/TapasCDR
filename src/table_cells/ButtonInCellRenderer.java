package table_cells;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.EventObject;

public class ButtonInCellRenderer implements TableCellRenderer, TableCellEditor {
  protected JButton currButton=null;
  
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column) {
    if (value==null || !(value instanceof JButton))
      return null;
    JButton b=(JButton)value;
    return b;
  }
  public Component getTableCellEditorComponent(JTable table,
                                        Object value,
                                        boolean isSelected,
                                        int row,
                                        int column) {
    currButton=null;
    if (value==null || !(value instanceof JButton))
      return null;
    currButton=(JButton)value;
    return currButton;
  }
  public Object getCellEditorValue() {
    return currButton.getActionCommand();
  }
  public boolean isCellEditable(EventObject anEvent) {
    return true;
  }
  public boolean shouldSelectCell(EventObject anEvent) {
    return true;
  }
  public boolean stopCellEditing() {
    currButton=null;
    return true;
  }
  
  public void cancelCellEditing() {}
  
  public void addCellEditorListener(CellEditorListener l) { }
  public void removeCellEditorListener(CellEditorListener l) { }
}
