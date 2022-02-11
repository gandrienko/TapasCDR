import ui.TapasCDRuiRunner;

public class Main {

  public static void main (String[] args) {
    if (args!=null && args.length==1) {
      String path=args[0];
      System.out.println(path);
      TapasCDRuiRunner uiRunner=new TapasCDRuiRunner(null);
      uiRunner.setOfflineMode(false);
      uiRunner.takeData(path);
      uiRunner.emulateDataUpdating();
    }
    
  }
}
