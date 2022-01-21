package map;

import data.Conflict;
import data.ConflictPoint;
import data.FlightInConflict;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MapView extends JPanel {
  public static final Color textColor=new Color(0,0,0,90);
  public static final Stroke stroke2=new BasicStroke(2);
  public static final float dash[] = {2.0f,2.0f};
  public static Stroke dashedStroke = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,3.0f, dash, 0.0f);
  /**
   * Contains variables necessary for transformation of coordinates
   */
  public MapMetrics metrics=new MapMetrics();
  /**
   * Specification of the conflict to be shown
   */
  public Conflict conflict=null;
  /**
   * Used to speed up redrawing
   */
  protected BufferedImage off_Image=null;
  protected boolean off_Valid=false;
  
  public void setConflict(Conflict conflict) {
    this.conflict = conflict;
    if (conflict!=null && conflict.flights!=null && conflict.flights.length>=2) {
      double b[]=conflict.getGeoBoundaries();
      if (b==null)
        metrics.setTerritoryBounds(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
      else {
        double dx=(b[2]-b[0])/3, dy=(b[3]-b[1])/3;
        metrics.setTerritoryBounds(b[0]-dx, b[1]-dy, b[2]+dx, b[3]+dy);
      }
    }
    else
      metrics.setTerritoryBounds(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
    off_Valid=false;
    redraw();
  }
  
  public void paintComponent(Graphics gr) {
    if (gr==null)
      return;
    int w=getWidth(), h=getHeight();
    if (w<1 || h<1)
      return;
    if (off_Image!=null && off_Valid) {
      if (off_Image.getWidth()!=w || off_Image.getHeight()!=h) {
        off_Image = null; off_Valid=false;
      }
      else {
        gr.drawImage(off_Image,0,0,null);
        return;
      }
    }
  
    if (off_Image==null || off_Image.getWidth()!=w || off_Image.getHeight()!=h)
      off_Image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = off_Image.createGraphics();
  
    g.setColor(getBackground());
    g.fillRect(0,0,w+1,h+1);
    
    if (conflict==null || !metrics.hasTerritoryBounds())
      return;
    
    if (metrics.viewport==null || metrics.viewport.width!=w || metrics.viewport.height!=h)
      metrics.setViewport(new Rectangle(0,0,w,h));
  
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHints(rh);

    Stroke stroke=g.getStroke();
  
    FlightInConflict f1=conflict.flights[0], f2=conflict.flights[1];
    int x01=metrics.scrX(f1.lon), y01=metrics.scrY(f1.lat),
        x02=metrics.scrX(f2.lon), y02=metrics.scrY(f2.lat);
    g.setColor(Color.darkGray);
    g.fillRect(x01-2,y01-2,5,5);
    g.fillRect(x02-2,y02-2,5,5);
    
    FontMetrics fm=g.getFontMetrics();
    g.setColor(textColor);
    int sw1=fm.stringWidth(f1.flightId), sw2=fm.stringWidth(f2.flightId);
    int tx1=x01-sw1/2, ty1=y01-3;
    if (tx1<1) tx1=1; else if (tx1+sw1>=w) tx1=w-sw1-1;
    if (ty1-fm.getAscent()<1) ty1=1+fm.getAscent(); else if (ty1>=h) ty1=h;
    int tx2=x02-sw2/2, ty2=y02-3;
    if (tx2<1) tx2=1; else if (tx2+sw2>=w) tx2=w-sw2-1;
    if (ty2-fm.getAscent()<1) ty2=1+fm.getAscent(); else if (ty2>=h) ty2=h-1;
    g.drawString(f1.flightId,tx1,ty1);
    g.drawString(f2.flightId,tx2,ty2);
    
    for (int j=0; j<3; j++) {
      ConflictPoint cp1=(j==0)?f1.first:(j==1)?f1.closest:f1.last,
          cp2=(j==0)?f2.first:(j==1)?f2.closest:f2.last;
      if (cp1==null || cp2==null)
        continue;
      int x1=metrics.scrX(cp1.lon), y1=metrics.scrY(cp1.lat),
          x2=metrics.scrX(cp2.lon), y2=metrics.scrY(cp2.lat);
      g.setStroke(stroke2);
      g.setColor(Color.darkGray);
      g.drawLine(x01,y01,x1,y1);
      g.drawLine(x02,y02,x2,y2);
      Color color=(j==0)?Color.orange:(j==1)?Color.red:Color.green.darker();
      g.setColor(color);
      g.drawLine(x1-3,y1-3,x1+3,y1+3);
      g.drawLine(x1-3,y1+3,x1+3,y1-3);
      g.drawLine(x2-3,y2-3,x2+3,y2+3);
      g.drawLine(x2-3,y2+3,x2+3,y2-3);
      g.setStroke(dashedStroke);
      g.drawLine(x1,y1,x2,y2);
      x01=x1; y01=y1; x02=x2; y02=y2;
    }
    g.setStroke(stroke);
    
    gr.drawImage(off_Image,0,0,null);
    off_Valid=true;
  }
  
  public void redraw(){
    if (isShowing())
      paintComponent(getGraphics());
  }
}
