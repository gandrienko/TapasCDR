import Util.CsvReader;
import data.Conflict;
import data.DataReader;

import java.util.ArrayList;

public class Main {

  public static void main (String[] args) {
    if (args!=null && args.length==1) {
      String path=args[0];
      System.out.println(path);
      CsvReader csvMain=new CsvReader(path,"main.csv"),
                csvConflicts=new CsvReader(path,"conflicts.csv");
  
      ArrayList<Conflict> conflicts=DataReader.getConflictsFromMain(csvMain);
      if (conflicts!=null) {
        System.out.println("Got data about " + conflicts.size() + " conflicts");
        System.out.println("Primary conflicts:");
        for (int i=0; i<conflicts.size(); i++)
          if (conflicts.get(i).isPrimary)
            System.out.println(conflicts.get(i));
      }
      
      //System.out.println("Main.csv: "+csvMain.getNColumns()+" columns, "+csvMain.getNRows()+" rows");
    }
  }
}
