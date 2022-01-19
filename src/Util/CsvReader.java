package Util;

import java.io.*;
import java.util.ArrayList;

public class CsvReader {
  public String columns[]=null;
  public ArrayList<String[]> rows=null;
  public CsvReader (String path, String fname) {
    try {
      read(path+"\\"+fname);
    } catch (IOException ex) {
      System.out.println(ex);
    }
  }
  protected void read (String fname) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fname))));
    String strLine=br.readLine();
    columns=strLine.split(",");
    rows=new ArrayList<String[]>(200);
    while ((strLine = br.readLine()) != null) {
      String values[]=strLine.split(",");
      rows.add(values);
    }
    br.close();
    System.out.println(fname+": "+getNColumns()+" columns, "+getNRows()+" rows");
  }
  public int getNColumns() { return (columns==null) ? 0 : columns.length; }
  public int getNRows() {return (rows==null) ? 0 : rows.size(); }
  public int getColumnN (String column) {
    if (columns==null)
      return -1;
    for (int i=0; i<getNColumns(); i++)
      if (column.equals(columns[i]))
        return i;
    return -1;
  }
  public String getValue (int row, int columnN) {
    return rows.get(row)[columnN].trim();
  }
  public double getValueAsDouble (int row, int columnN) {
    return Double.valueOf(getValue(row,columnN)).doubleValue();
  }
  public double getValueAsInt (int row, int columnN) {
    return Integer.valueOf(getValue(row,columnN)).intValue();
  }
}
