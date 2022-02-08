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

public class MapView extends JPanel
    implements MouseListener, MouseMotionListener, ChangeListener
{
  public static final Color colorF1=new Color(128,0,0), colorF2=new Color(0,0,128),
      paleColorF1 =new Color(255,0,0,128), paleColorF2 =new Color(0,0,255,128),
      textColorF1=new Color(96,0,0,128), textColorF2=new Color(0,0,96,128);
  public static final Stroke stroke2=new BasicStroke(2);
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
  
    FlightInConflict f1=conflict.flights[0], f2=conflict.flights[1];
    int x01=metrics.scrX(f1.lon), y01=metrics.scrY(f1.lat),
        x02=metrics.scrX(f2.lon), y02=metrics.scrY(f2.lat);
    g.setColor(colorF1);
    g.fillRect(x01-2,y01-2,5,5);
    g.setColor(colorF2);
    g.fillRect(x02-2,y02-2,5,5);
    
    FontMetrics fm=g.getFontMetrics();
    int sw1=fm.stringWidth(f1.flightId), sw2=fm.stringWidth(f2.flightId);
    int tx1=x01-sw1/2, ty1=y01-3;
    if (tx1<1) tx1=1; else if (tx1+sw1>=w) tx1=w-sw1-1;
    if (ty1-fm.getAscent()<1) ty1=1+fm.getAscent(); else if (ty1>=h) ty1=h;
    int tx2=x02-sw2/2, ty2=y02-3;
    if (tx2<1) tx2=1; else if (tx2+sw2>=w) tx2=w-sw2-1;
    if (ty2-fm.getAscent()<1) ty2=1+fm.getAscent(); else if (ty2>=h) ty2=h-1;
    g.setColor(textColorF1);
    g.drawString(f1.flightId,tx1,ty1);
    g.setColor(textColorF2);
    g.drawString(f2.flightId,tx2,ty2);
    
    for (int j=0; j<3; j++) {
      ConflictPoint cp1=(j==0)?f1.first:(j==1)?f1.closest:f1.last,
          cp2=(j==0)?f2.first:(j==1)?f2.closest:f2.last;
      if (cp1==null || cp2==null)
        continue;
      int x1=metrics.scrX(cp1.lon), y1=metrics.scrY(cp1.lat),
          x2=metrics.scrX(cp2.lon), y2=metrics.scrY(cp2.lat);
      g.setStroke(stroke2);
      g.setColor(colorF1);
      g.drawLine(x01,y01,x1,y1);
      g.setColor(colorF2);
      g.drawLine(x02,y02,x2,y2);
      Color color=(j==0)?Color.orange.darker():(j==1)?Color.red:Color.green.darker();
      g.setColor(color);
      g.drawLine(x1-3,y1-3,x1+3,y1+3);
      g.drawLine(x1-3,y1+3,x1+3,y1-3);
      g.drawLine(x2-3,y2-3,x2+3,y2+3);
      g.drawLine(x2-3,y2+3,x2+3,y2-3);
      g.setStroke(dashedStroke);
      g.drawLine(x1,y1,x2,y2);
      x01=x1; y01=y1; x02=x2; y02=y2;
    }
    
    if (f1.pp!=null) {
      g.setColor(paleColorF1);
      int x0=x01,y0=y01;
      for (int i=0; i<f1.pp.length; i++) {
        int x=metrics.scrX(f1.pp[i].lon), y=metrics.scrY(f1.pp[i].lat);
        //g.drawLine(x-3,y-3,x+3,y+3);
        //g.drawLine(x-3,y+3,x+3,y-3);
        g.setStroke(stroke);
        g.drawRect(x-3,y-3,6,6);
        if (f1.pp[i].pointTimeUnix>f1.last.pointTimeUnix) {
          g.setStroke(dashedStroke);
          g.drawLine(x0, y0, x, y);
          x0=x; y0=y;
        }
      }
    }
    /**/
    if (f2.pp!=null) {
      g.setColor(paleColorF2);
      int x0=x02,y0=y02;
      for (int i=0; i<f2.pp.length; i++) {
        int x=metrics.scrX(f2.pp[i].lon), y=metrics.scrY(f2.pp[i].lat);
        //g.drawLine(x-3,y-3,x+3,y+3);
        //g.drawLine(x-3,y+3,x+3,y-3);
        g.setStroke(stroke);
        g.drawRect(x-3,y-3,6,6);
        if (f2.pp[i].pointTimeUnix>f2.last.pointTimeUnix) {
          g.setStroke(dashedStroke);
          g.drawLine(x0, y0, x, y);
          x0=x; y0=y;
        }
      }
    }
    /**/
    g.setStroke(stroke);
    
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
        long t0=conflict.detectionTimeUnix;
        double lon0=f.lon, lat0=f.lat, alt0=f.altitude;
        Point p=null;
        for (int j=0; j<3 && p==null; j++) {
          if (tTrans.timeUnix==t0) {
            p = new Point(metrics.scrX(lon0), metrics.scrY(lat0));
            lon[i]=lon0; lat[i]=lat0; alt[i]=alt0;
          }
          else {
            ConflictPoint cp=(j==0)?f.first:(j==1)?f.closest:f.last;
            if (cp==null)
              continue;
            if (cp.pointTimeUnix>t0 && tTrans.timeUnix<cp.pointTimeUnix) {
              double ratio=1.0*(tTrans.timeUnix-t0)/(cp.pointTimeUnix-t0);
              lon[i]=lon0+(cp.lon-lon0)*ratio;
              lat[i]=lat0+(cp.lat-lat0)*ratio;
              alt[i]=alt0+(cp.altitude-alt0)*ratio;
              p=new Point(metrics.scrX(lon[i]),metrics.scrY(lat[i]));
            }
            else {
              t0=cp.pointTimeUnix;
              lon0=cp.lon;
              lat0=cp.lat;
              alt0=cp.altitude;
            }
          }
        }
        if (p==null && f.pp!=null)
          for (int j=0; j<f.pp.length && p==null; j++) {
            if (tTrans.timeUnix==t0) {
              p = new Point(metrics.scrX(lon0), metrics.scrY(lat0));
              lon[i]=lon0; lat[i]=lat0; alt[i]=alt0;
            }
            else {
              FlightPoint cp = f.pp[j];
              if (cp.pointTimeUnix > t0 && tTrans.timeUnix < cp.pointTimeUnix) {
                double ratio = 1.0 * (tTrans.timeUnix - t0) / (cp.pointTimeUnix - t0);
                lon[i]=lon0+(cp.lon-lon0)*ratio;
                lat[i]=lat0+(cp.lat-lat0)*ratio;
                alt[i]=alt0+(cp.altitude-alt0)*ratio;
                p=new Point(metrics.scrX(lon[i]),metrics.scrY(lat[i]));
              }
              else {
                t0 = cp.pointTimeUnix;
                lon0 = cp.lon;
                lat0 = cp.lat;
                alt0=cp.altitude;
              }
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
