import Util.CsvReader;
import data.Action;
import data.Conflict;
import data.DataReader;
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
  
      ShowConflicts showConflicts=new ShowConflicts();
      showConflicts.setConflicts(conflicts);
  
      csvReader=new CsvReader(path,"resolution_actions_episode_1.csv");
      ArrayList<Action> actions=DataReader.getActions(csvReader);
      if (actions==null)
        System.out.println("Failed to get data about resolution actions!");
      else {
        System.out.println("Got data about "+actions.size()+" resolution actions");
        nOk=Action.linkActionsToConflicts(actions,conflicts);
        System.out.println("Linked "+nOk+" actions to the corresponding conflicts");
      }
    }
      
  }
}
