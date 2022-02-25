package map;

import data.Conflict;
import data.ConflictPoint;
import data.FlightInConflict;
import data.FlightPoint;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MapView extends JPanel
    implements MouseListener, MouseMotionListener, ChangeListener
{
  public static final Color colorF1=new Color(128,0,0), colorF2=new Color(0,0,128),
      paleColorF1 =new Color(255,0,0,128), paleColorF2 =new Color(0,0,255,128),
      textColorF1=new Color(96,0,0,128), textColorF2=new Color(0,0,96,128);
  public static final Stroke stroke2=new BasicStroke(3);
  public static final float dash[] = {2.0f,2.0f};
  public static Stroke dashedStroke = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,3.0f, dash, 0.0f);
  /**
   * Contains variables necessary for transformation of coordinates
   */
  public MapMetrics metrics=new MapMetrics();
  /**
   * Original visible territory
   */
  public double visibleTerrBounds[]=null;
  /**
   * Specification of the conflict to be shown
   */
  public Conflict conflict=null;
  /**
   * Used to speed up redrawing
   */
  protected BufferedImage off_Image=null;
  protected boolean off_Valid=false;
  
  public TimeTransmitter tTrans=new TimeTransmitter();
  
  public MapView(){
    super();
    addMouseListener(this);
    addMouseMotionListener(this);
  }
  
  public void setTimeTransmitter(TimeTransmitter tTrans) {
    this.tTrans = tTrans;
    if (tTrans!=null)
      tTrans.addChangeListener(this);
  }
  
  public void setConflict(Conflict conflict) {
    this.conflict = conflict;
    if (conflict!=null && conflict.flights!=null && conflict.flights.length>=2) {
      double b[]=conflict.getConflictGeoBoundaries();
      if (b==null)
        metrics.setTerritoryBounds(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
      else {
        /**/
        double bp[]=conflict.getProjectionGeoBoundaries();
        if (bp!=null) {
          double maxDiff=Math.max(b[2]-b[0],b[3]-b[1])/4;
          if (bp[0]<b[0]) b[0]=Math.max(bp[0],b[0]-maxDiff);
          if (bp[1]<b[1]) b[1]=Math.max(bp[1],b[1]-maxDiff);
          if (bp[2]>b[2]) b[2]=Math.min(bp[2],b[2]+maxDiff);
          if (bp[3]>b[3]) b[3]=Math.min(bp[3],b[3]+maxDiff);
        }
        /**/
        double dx=(b[2]-b[0])/3, dy=(b[3]-b[1])/3;
        metrics.setTerritoryBounds(b[0]-dx, b[1]-dy, b[2]+dx, b[3]+dy);
      }
    }
    else
      metrics.setTerritoryBounds(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
    visibleTerrBounds=null;
    redraw(true);
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
    
    if (conflict==null || !metrics.hasTerritoryBounds()) {
      gr.drawImage(off_Image,0,0,null);
      off_Valid=true;
      return;
    }
    
    if (metrics.viewport==null || metrics.viewport.width!=w || metrics.viewport.height!=h)
      metrics.setViewport(new Rectangle(0,0,w,h));
  
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHints(rh);

    Stroke stroke=g.getStroke();
    FontMetrics fm=g.getFontMetrics();
  
    for (int i=0; i<2; i++) {
      FlightInConflict f=conflict.flights[i];
      ArrayList<FlightPoint> path=f.getPath(conflict.detectionTimeUnix);

      FlightPoint fp=(path==null)?f.getDetectionPoint(conflict.detectionTimeUnix):path.get(0);
      int sx0=metrics.scrX(fp.lon), sy0=metrics.scrY(fp.lat);
      g.setColor((i==0)?colorF1:colorF2);
      g.fillRect(sx0-2,sy0-2,5,5);
      int sw=fm.stringWidth(f.flightId);
      int tx=sx0-sw/2, ty=sy0-3;
      if (tx<1) tx=1; else if (tx+sw>=w) tx=w-sw-1;
      if (ty-fm.getAscent()<1) ty=1+fm.getAscent(); else if (ty>=h) ty=h;
      g.setColor((i==0)?textColorF1:textColorF2);
      g.drawString(f.flightId,tx,ty);
  
      if (path==null)
        continue;

      for (int j=1; j<path.size(); j++) {
        fp=path.get(j);
        int sx=metrics.scrX(fp.lon), sy=metrics.scrY(fp.lat);
        if (fp.pointTimeUnix<=f.last.pointTimeUnix)
          g.setColor((i==0)?colorF1:colorF2);
        else
          g.setColor((i==0)?paleColorF1:paleColorF2);
        Stroke str=(fp.pointTimeUnix<=f.first.pointTimeUnix)?stroke:
                       (fp.pointTimeUnix<=f.last.pointTimeUnix)?stroke2:dashedStroke;
        g.setStroke(str);
        g.drawLine(sx0,sy0,sx,sy);
        if (fp instanceof ConflictPoint) {
          Color color=(fp.equals(f.first))?Color.orange.darker():
                          (fp.equals(f.closest))?Color.red:Color.green.darker();
          g.setColor(color);
          g.setStroke(stroke2);
          g.drawLine(sx-3,sy-3,sx+3,sy+3);
          g.drawLine(sx-3,sy+3,sx+3,sy-3);
          if (i==0) {
            FlightInConflict f2=conflict.flights[i+1];
            FlightPoint fp2 = (fp.equals(f.first)) ? f2.first :
                                  (fp.equals(f.closest)) ? f2.closest : f2.last;
            int x2=metrics.scrX(fp2.lon), y2=metrics.scrY(fp2.lat);
            g.setStroke(dashedStroke);
            g.drawLine(sx,sy,x2,y2);
          }
        }
        else {
          g.setStroke(stroke);
          g.drawRect(sx-3,sy-3,6,6);
        }
        sx0=sx; sy0=sy;
        g.setStroke(stroke);
      }
    }
    
    gr.drawImage(off_Image,0,0,null);
    off_Valid=true;
  }
  
  public void redraw(boolean invalidate){
    if (invalidate)
      off_Valid=false;
    if (isShowing())
      paintComponent(getGraphics());
  }
  
  public void shiftBy(int dx, int dy) {
    if (dx==0 && dy==0)
      return;
    if (metrics==null || !metrics.hasTerritoryBounds())
      return;
    if (visibleTerrBounds==null)
      visibleTerrBounds=metrics.getVisibleTerritoryBounds();
    double ddx=dx*metrics.step, ddy=dy*metrics.stepY;
    double b[]=metrics.getVisibleTerritoryBounds();
    metrics.setTerritoryBounds(b[0]-ddx,b[1]+ddy,b[2]-ddx,b[3]+ddy);
    redraw(true);
  }
  
  
  protected int x0 =-1, y0 =-1;
  boolean isDragging=false;
  
  public void mousePressed(MouseEvent e) {
    if (metrics==null || !metrics.hasTerritoryBounds())
      return;
    if (e.getButton()==MouseEvent.BUTTON1){
      x0 =e.getX(); y0 =e.getY();
    }
  }
  
  public void mouseReleased(MouseEvent e) {
    if (metrics==null || !metrics.hasTerritoryBounds())
      return;
    if (isDragging) {
      int dragX=e.getX(), dragY=e.getY();
      int dx=dragX-x0, dy=dragY-y0;
      if (Math.abs(dx)>0 || Math.abs(dy)>0)
        shiftBy(dx,dy);
    }
    x0 = y0 =-1;
    isDragging=false;
  }
  
  public void mouseClicked(MouseEvent e) {
    if (metrics==null || !metrics.hasTerritoryBounds())
      return;
    if (e.getClickCount() > 1) {
      // restore the original view
      if (visibleTerrBounds!=null) {
        metrics.setTerritoryBounds(visibleTerrBounds);
        redraw(true);
      }
    }
    else
    if (e.getButton()==MouseEvent.BUTTON1){
      if (visibleTerrBounds==null)
        visibleTerrBounds=metrics.getVisibleTerritoryBounds();
      //zoom in
      double b[]=metrics.getVisibleTerritoryBounds();
      double dx=(b[2]-b[0])/10, dy=(b[3]-b[1])/10;
      metrics.setTerritoryBounds(b[0]+dx,b[1]+dy,b[2]-dx,b[3]-dy);
      redraw(true);
    }
    else {
      if (visibleTerrBounds==null)
        visibleTerrBounds=metrics.getVisibleTerritoryBounds();
      //zoom out
      double b[]=metrics.getVisibleTerritoryBounds();
      double dx=(b[2]-b[0])/8, dy=(b[3]-b[1])/8;
      metrics.setTerritoryBounds(b[0]-dx,b[1]-dy,b[2]+dx,b[3]+dy);
      redraw(true);
    }
  }
  public void mouseDragged(MouseEvent e) {
    if (metrics==null || !metrics.hasTerritoryBounds())
      return;
    if (x0 >=0 && y0 >=0){
      isDragging=true;
      int dragX=e.getX(), dragY=e.getY();
      int dx=dragX-x0, dy=dragY-y0;
      if (Math.abs(dx)>=5 || Math.abs(dy)>=5) {
        x0=dragX; y0=dragY;
        shiftBy(dx,dy);
      }
    }
  }
  
  public void mouseEntered(MouseEvent e) {};
  public void mouseExited(MouseEvent e) {};
  public void mouseMoved(MouseEvent e) {}
  
  public void stateChanged(ChangeEvent e) {
    if (conflict==null)
      return;
    if (e.getSource().equals(tTrans)) {
      redraw(false);
      if (tTrans.timeUnix<conflict.detectionTimeUnix)
        return;
      //find the points corresponding to the time
      Point p1=null, p2=null;
      double lon[]={Double.NaN,Double.NaN}, lat[]={Double.NaN,Double.NaN}, alt[]={Double.NaN,Double.NaN};
      
      for (int i=0; i<2; i++) {
        FlightInConflict f=conflict.flights[i];
        ArrayList<FlightPoint> path=f.getPath(conflict.detectionTimeUnix);
        if (path==null || path.isEmpty())
          continue;;
        Point p=null;
        FlightPoint fp0=path.get(0);
        for (int j=1; j<path.size() && p==null; j++) {
          if (tTrans.timeUnix==fp0.pointTimeUnix) {
            p = new Point(metrics.scrX(fp0.lon), metrics.scrY(fp0.lat));
            lon[i]=fp0.lon; lat[i]=fp0.lat; alt[i]=fp0.altitude;
          }
          else {
            FlightPoint fp=path.get(j);
            if (tTrans.timeUnix<fp.pointTimeUnix) {
              double ratio=1.0*(tTrans.timeUnix-fp0.pointTimeUnix)/(fp.pointTimeUnix-fp0.pointTimeUnix);
              lon[i]=fp0.lon+(fp.lon-fp0.lon)*ratio;
              lat[i]=fp0.lat+(fp.lat-fp0.lat)*ratio;
              alt[i]=fp0.altitude+(fp.altitude-fp0.altitude)*ratio;
              p=new Point(metrics.scrX(lon[i]),metrics.scrY(lat[i]));
            }
            else
              fp0=fp;
          }
        }
        if (p==null)
          break;
        else
          if (i==0) p1=p; else p2=p;
      }
      
      if (p1!=null && p2!=null) {
        Graphics g=getGraphics();
        g.setColor(Color.magenta);
        g.drawLine(p1.x,p1.y,p2.x,p2.y);
        double dHor=MapMetrics.geoDist(lon[0],lat[0],lon[1],lat[1])/1852,
            dVert=Math.abs(alt[0]-alt[1]);
        String txt=String.format("Distances: %.2f nm; %.0f feet at %s",dHor,dVert,tTrans.getTimeText());
        int sw=g.getFontMetrics().stringWidth(txt);
        g.drawString(txt,getWidth()/2-sw/2,5+g.getFontMetrics().getAscent());
      }
    }
  }
}
