package Util;

import java.io.*;

public class CsvReader {
  String columns[]=null;
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
}
