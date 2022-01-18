package Util;

import java.io.*;
import java.util.Vector;

public class CsvReader {
  String columns[]=null;
  Vector<String[]> rows=null;
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
    rows=new Vector<>(100,100);
    while ((strLine = br.readLine()) != null) {
      String values[]=strLine.split(",");
      rows.add(values);
    }
    br.close();
  }
  public int getNColumns() { return (columns==null) ? 0 : columns.length; }
  public int getColumnN (String column) {
    if (columns==null)
      return -1;
    for (int i=0; i<getNColumns(); i++)
      if (column.equals(columns[i]))
        return i;
    return -1;
  }
  public String getValue (int row, int columnN) {
    return rows.elementAt(row)[columnN].trim();
  }
  public double getValueAsDouble (int row, int columnN) {
    return Double.valueOf(getValue(row,columnN)).doubleValue();
  }
  public double getValueAsInt (int row, int columnN) {
    return Integer.valueOf(getValue(row,columnN)).intValue();
  }
}
