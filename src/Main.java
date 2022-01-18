import Util.CsvReader;

public class Main {

  public static void main (String[] args) {
    if (args!=null && args.length==1) {
      String path=args[0];
      System.out.println(path);
      CsvReader csvMain=new CsvReader(path,"main.csv");
      //System.out.println("Main.csv: "+csvMain.getNColumns()+" columns, "+csvMain.getNRows()+" rows");
    }
  }
}
