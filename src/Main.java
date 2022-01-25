import Util.CsvReader;
import data.Action;
import data.Conflict;
import data.DataReader;
import data.DataUpdater;
import ui.ShowConflicts;

import java.util.ArrayList;

public class Main {

  public static void main (String[] args) {
    if (args!=null && args.length==1) {
      String path=args[0];
      System.out.println(path);
      CsvReader csvReader=new CsvReader(path,"main.csv");
  
      ArrayList<Conflict> conflicts=DataReader.getConflictsFromMain(csvReader);
      if (conflicts==null) {
        System.out.println("Failed to get conflict data!");
        return;
      }
      System.out.println("Got data about " + conflicts.size() + " conflicts");
      System.out.println("Primary conflicts:");
      for (int i=0; i<conflicts.size(); i++)
        if (conflicts.get(i).isPrimary)
          System.out.println(conflicts.get(i));
        
      csvReader=new CsvReader(path,"conflicts.csv");
      
      int nOk=DataReader.getMoreConflictDataFromConflicts(csvReader,conflicts);
      System.out.println("Successfully identified conflicts and flights for "+nOk+" records");
  
      csvReader=new CsvReader(path,"resolution_actions_episode_1.csv");
      ArrayList<Action> actions=DataReader.getActions(csvReader);
      if (actions==null)
        System.out.println("Failed to get data about resolution actions!");
      else {
        System.out.println("Got data about "+actions.size()+" resolution actions");
        //nOk=Action.linkActionsToConflicts(actions,conflicts);
        //System.out.println("Linked "+nOk+" actions to the corresponding conflicts");
  
        DataUpdater du=new DataUpdater();
        int nPortions=du.setFullData(conflicts,actions);
        System.out.println("Got "+nPortions+" data portions!");
        
        if (nPortions>=0) {
          ShowConflicts showConflicts = new ShowConflicts();
          showConflicts.setDataPortions(du.portions);
        }
      }
    }
      
  }
}
