import Util.CsvReader;
import data.*;
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
  
      ShowConflicts showConflicts=null;
      DataUpdater du=null;
  
      csvReader=new CsvReader(path,"resolution_actions_episode_1.csv");
      ArrayList<Action> actions=DataReader.getActions(csvReader);
      if (actions==null)
        System.out.println("Failed to get data about resolution actions!");
      else {
        System.out.println("Got data about "+actions.size()+" resolution actions");
        //nOk=Action.linkActionsToConflicts(actions,conflicts);
        //System.out.println("Linked "+nOk+" actions to the corresponding conflicts");
  
        du=new DataUpdater();
        int nPortions=du.setFullData(conflicts,actions);
        System.out.println("Got "+nPortions+" data portions!");
        
        if (nPortions>=0) {
          showConflicts = new ShowConflicts();
          showConflicts.setDataPortions(du.portions);
          showConflicts.setDataUpdater(du);
        }
      }
      if (showConflicts!=null) {
        csvReader=new CsvReader(path,"points_of_projection.csv");
        ArrayList<FlightPoint> pts=DataReader.getProjectionPoints(csvReader);
        if (pts!=null) {
          System.out.println("Got " + pts.size() + " projection points!");
          nOk=Conflict.attachProjectionPoints(conflicts,pts);
          System.out.println(nOk+" projection points have been attached to conflict descriptions");
        }
        else
          System.out.println("Failed to load projection points!");
        csvReader=new CsvReader(path,"non_conformance_events.csv");
        ArrayList<NCEvent> ncEvents=DataReader.getNonConformanceEvents(csvReader);
        if (ncEvents!=null) {
          System.out.println("Got "+ncEvents.size()+" non-conformance events!");
          nOk=du.setNCEvents(ncEvents);
          System.out.println("For "+nOk+" events the corresponding resolution actions have been found!");
        }
        else
          System.out.println("No non-conformance events found!");
      }
    }
      
  }
}
