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
    String values[]=rows.get(row);
    String v=null;
    if (values!=null && columnN<values.length)
      v=values[columnN];
    if (v!=null)
      v=v.trim();
    return v;
  }
  public double getValueAsDouble (int row, int columnN) {
    String v=getValue(row,columnN);
    if (v!=null)
      return Double.parseDouble(v);
    return Double.NaN;
  }
  public double getValueAsInt (int row, int columnN) {
    String v=getValue(row,columnN);
    if (v!=null)
      return Integer.parseInt(v);
    return 0;
  }
  public long getValueAsLong(int row, int columnN) {
    String v=getValue(row,columnN);
    if (v!=null)
      return Long.parseLong(v);
    return 0;
  }
}
