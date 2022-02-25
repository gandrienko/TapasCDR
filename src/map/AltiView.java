package map;

import data.Conflict;
import data.ConflictPoint;
import data.FlightInConflict;
import data.FlightPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

/**
 * Draws a graph of the altitudes of 2 aircraft depending on time
 */

public class AltiView extends JPanel implements MouseMotionListener {
  public static final int xMarg=10, yMarg=5;
  public static final Color colorF1=new Color(128,0,0), colorF2=new Color(0,0,128),
           paleColorF1 =new Color(255,0,0,128), paleColorF2 =new Color(0,0,255,128);
  
  public static final Stroke stroke2=new BasicStroke(2);
  public static final float dash[] = {5.0f,5.0f};
  public static Stroke dashedStroke = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,3.0f, dash, 0.0f);
  public static final float smallDash[] = {2.0f,2.0f};
  public static Stroke smallDashStroke = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,3.0f, smallDash, 0.0f);
  /**
   * Specification of the conflict to be shown
   */
  public Conflict conflict=null;
  
  protected int minAlt=0, maxAlt=0;
  /**
   * Time extent of the x-axis, UNIX timestamps
   */
  protected long minTime=0, maxTime=0;
  protected int x0=xMarg, xLength=0, yTop=yMarg, yBottom=0;
  
  public TimeTransmitter tTrans=new TimeTransmitter();
  /**
   * Used to speed up redrawing
   */
  protected BufferedImage off_Image=null;
  protected boolean off_Valid=false;
  
  public AltiView(){
    super();
    addMouseMotionListener(this);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        if (tTrans.timeUnix>0) {
          tTrans.timeUnix=0;
          tTrans.notifyChange();
        }
      }
    });
  }
  
  public void setConflict(Conflict conflict) {
    if (tTrans.timeUnix>0) {
      tTrans.timeUnix = 0;
      tTrans.notifyChange();
    }
    this.conflict = conflict;
    off_Valid=false;
    if (conflict==null || conflict.flights==null || conflict.flights.length<2 ||
        conflict.flights[0].last==null) {
      redraw();
      return;
    }
    minTime=conflict.detectionTimeUnix;
    long tLast=minTime;
    maxTime=minTime;
    minAlt=Math.min(conflict.flights[0].altitude,conflict.flights[1].altitude);
    maxAlt=Math.max(conflict.flights[0].altitude,conflict.flights[1].altitude);
    for (int i=0; i<conflict.flights.length; i++) {
      if (conflict.flights[i].last!=null && conflict.flights[i].last.pointTimeUnix>tLast)
        tLast=conflict.flights[i].last.pointTimeUnix;
      ArrayList<FlightPoint> path=conflict.flights[i].getPath(conflict.detectionTimeUnix);
      if (path!=null) {
        FlightPoint fp=path.get(path.size()-1);
        if (fp.pointTimeUnix>maxTime)
          maxTime=fp.pointTimeUnix;
        for (int j=0; j<path.size(); j++) {
          fp=path.get(j);
          if (fp.altitude<minAlt)
            minAlt=fp.altitude;
          else
            if (fp.altitude>maxAlt)
              maxAlt=fp.altitude;
        }
      }
    }
    if (tLast>minTime && maxTime-tLast>3*(tLast-minTime))
    maxTime=tLast+3*(tLast-minTime);

    redraw();
  }
  
  public TimeTransmitter getTimeTransmitter() {
    return tTrans;
  }
  
  public int timeToXPos(long timeUnix) {
    if (xLength<=0 || minTime>=maxTime)
      return 0;
    return x0+(int)Math.round(1.0*(timeUnix-minTime)/(maxTime-minTime)*xLength);
  }
  
  public long xPosToTime(int xPos) {
    if (xLength<=0 || minTime>=maxTime)
      return 0;
    if (xPos<x0 || xPos>x0+xLength)
      return 0;
    return minTime+Math.round(1.0*(xPos-x0)/xLength*(maxTime-minTime));
  }
  
  public void paintComponent(Graphics gr) {
    if (gr == null)
      return;
    int w = getWidth(), h = getHeight();
    if (w < 1 || h < 1)
      return;
    if (off_Image != null && off_Valid) {
      if (off_Image.getWidth() != w || off_Image.getHeight() != h) {
        off_Image = null;
        off_Valid = false;
      }
      else {
        gr.drawImage(off_Image, 0, 0, null);
        return;
      }
    }
  
    if (tTrans.timeUnix>0) {
      tTrans.timeUnix = 0;
      tTrans.notifyChange();
    }

    if (conflict==null || conflict.flights==null || conflict.flights.length<2 ||
            conflict.flights[0].last==null) {
      gr.setColor(getBackground());
      gr.fillRect(0, 0, w + 1, h + 1);
      return;
    }
  
    if (off_Image == null || off_Image.getWidth() != w || off_Image.getHeight() != h)
      off_Image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = off_Image.createGraphics();
    RenderingHints rh = new RenderingHints(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHints(rh);
  
    g.setColor(getBackground());
    g.fillRect(0, 0, w + 1, h + 1);
    
    FontMetrics fm=g.getFontMetrics();
    String altStr=String.format("%d",maxAlt);
    
    int xEnd=w-2*xMarg-1;
    x0=xMarg+fm.stringWidth(altStr);
    xLength=xEnd-x0;
    yTop=yMarg+fm.getHeight();
    yBottom=h-2*(yMarg+fm.getHeight());
    
    g.setColor(Color.gray);
    g.drawLine(x0,yTop,x0,yBottom);
    g.drawLine(x0,yBottom,xEnd,yBottom);
    g.drawString(altStr,xMarg,yTop+fm.getAscent());
    g.drawString(getTimeText(conflict.detectionTime),xMarg,yBottom+fm.getHeight());
    
    int x1=timeToXPos(conflict.flights[0].first.pointTimeUnix),
        x2=timeToXPos(conflict.flights[0].closest.pointTimeUnix),
        x3=timeToXPos(conflict.flights[0].last.pointTimeUnix);
    
    Stroke origStroke=g.getStroke();
    g.setStroke(dashedStroke);
    g.setColor(Color.orange.darker());
    if (x1<x2)
      g.drawLine(x1, yTop, x1, yBottom);
    String timeStr = getTimeText(conflict.flights[0].first.time);
    g.drawString(timeStr, x1 - fm.stringWidth(timeStr) / 2, yBottom + fm.getHeight());
    
    g.setColor(Color.red);
    g.drawLine(x2,yTop,x2,yBottom);
    timeStr=getTimeText(conflict.flights[0].closest.time);
    int sw=fm.stringWidth(timeStr), tx=x2-sw/2;
    if (tx+sw>w-xMarg)
      tx=w-xMarg-sw;
    g.drawString(timeStr,tx,yMarg+fm.getAscent());
    if (x3>x1) {
      g.setColor(Color.green.darker());
      if (x3>x2)
        g.drawLine(x3, yTop, x3, yBottom);
      timeStr = getTimeText(conflict.flights[0].last.time);
      sw = fm.stringWidth(timeStr);
      tx = x3 - sw / 2;
      if (tx + sw > w - xMarg)
        tx = w - xMarg - sw;
      g.drawString(timeStr, tx, yBottom + fm.getHeight());
    }
  
    g.setStroke(origStroke);
    
    if (minAlt==maxAlt) {
      int xPrev=x0;
      for (int i=0; i<conflict.flights.length; i++) {
        FlightInConflict f=conflict.flights[i];
        ArrayList<FlightPoint> path=f.getPath(conflict.detectionTimeUnix);
        if (path==null)
          continue;
        for (int j=0; j<path.size(); j++) {
          FlightPoint fp=path.get(j);
          int x=timeToXPos(fp.pointTimeUnix);
          if (fp.pointTimeUnix<=f.last.pointTimeUnix)
            g.setColor((i==0)?colorF1:colorF2);
          else
            g.setColor((i==0)?paleColorF1:paleColorF2);
          if (j>0) {
            Stroke str=(fp.pointTimeUnix<=f.first.pointTimeUnix)?origStroke:
                           (fp.pointTimeUnix<=f.last.pointTimeUnix)?stroke2:dashedStroke;
            g.setStroke(str);
            g.drawLine(xPrev, yTop, x, yTop);
          }
          if (fp instanceof ConflictPoint) {
            Color color=(fp.equals(f.first))?Color.orange.darker():
                            (fp.equals(f.closest))?Color.red:Color.green.darker();
            g.setColor(color);
            g.setStroke(stroke2);
            g.drawLine(x-3,yTop-3,x+3,yTop+3);
            g.drawLine(x-3,yTop+3,x+3,yTop-3);
          }
          else {
            g.setStroke(origStroke);
            g.drawRect(x - 3, yTop - 3, 6, 6);
            if (j==0)
              g.fillRect(x - 3, yTop - 3, 6, 6);
          }
          xPrev=x;
        }
      }
    }
    else {
      int altDiff=maxAlt-minAlt, yMin=yBottom-(yBottom-yTop)/3,
          yDiff=yMin-yTop;
      g.setColor(Color.gray);
      g.drawLine(x0-3,yMin,x0+3,yMin);
      altStr=String.format("%d",minAlt);
      g.drawString(altStr,xMarg,yMin);
      int xPrev=x0, yPrev=-1;
      for (int i=0; i<conflict.flights.length; i++) {
        FlightInConflict f=conflict.flights[i];
        ArrayList<FlightPoint> path=f.getPath(conflict.detectionTimeUnix);
        if (path==null)
          continue;
        for (int j=0; j<path.size(); j++) {
          FlightPoint fp=path.get(j);
          int x=timeToXPos(fp.pointTimeUnix);
          int y=yMin-Math.round((1.0f*fp.altitude-minAlt)/altDiff*yDiff);
          if (fp.pointTimeUnix<=f.last.pointTimeUnix)
            g.setColor((i==0)?colorF1:colorF2);
          else
            g.setColor((i==0)?paleColorF1:paleColorF2);
          if (j>0) {
            Stroke str=(fp.pointTimeUnix<=f.first.pointTimeUnix)?origStroke:
                           (fp.pointTimeUnix<=f.last.pointTimeUnix)?stroke2:dashedStroke;
            g.setStroke(str);
            g.drawLine(xPrev, yPrev, x, y);
            g.setStroke(origStroke);
          }
          if (fp instanceof ConflictPoint) {
            Color color=(fp.equals(f.first))?Color.orange.darker():
                            (fp.equals(f.closest))?Color.red:Color.green.darker();
            g.setColor(color);
            g.setStroke(stroke2);
            g.drawLine(x-3,y-3,x+3,y+3);
            g.drawLine(x-3,y+3,x+3,y-3);
            if (i==0) {
              FlightInConflict f2=conflict.flights[i+1];
              FlightPoint fp2 = (fp.equals(f.first)) ? f2.first :
                                    (fp.equals(f.closest)) ? f2.closest : f2.last;
              if (fp2.altitude!=fp.altitude) {
                g.setStroke(smallDashStroke);
                g.setColor((i==0)?Color.orange.darker():(i==1)?Color.red:Color.green.darker());
                int y2=yMin-Math.round((1.0f*fp2.altitude-minAlt)/altDiff*yDiff);
                g.drawLine(x,y,x,y2);
                altStr=String.format("%d",Math.abs(fp.altitude-fp2.altitude));
                g.drawString(altStr,x-fm.stringWidth(altStr),Math.max(y,y2)+fm.getAscent()+5);
              }
            }
          }
          else {
            g.setStroke(origStroke);
            g.drawRect(x - 3, y - 3, 6, 6);
            if (j==0)
              g.fillRect(x - 3, y - 3, 6, 6);
          }
          
          xPrev=x; yPrev=y;
        }
      }
    }
  
    g.setStroke(origStroke);
    
    gr.drawImage(off_Image,0,0,null);
    off_Valid=true;
  }
  
  public String getTimeText(LocalDateTime dt) {
    return String.format("%02d:%02d:%02d",dt.getHour(),dt.getMinute(),dt.getSecond());
  }
  
  public void redraw(){
    if (isShowing())
      paintComponent(getGraphics());
  }
  public void mouseMoved(MouseEvent e) {
    int x=e.getX();
    if (x>0 && x<getWidth()-1) {
      if (x<x0) x=x0; else if (x>x0+xLength) x=x0+xLength;
    }
    long t=xPosToTime(x);
    redraw();
    if (tTrans.timeUnix!=t) {
      tTrans.timeUnix = t;
      tTrans.notifyChange();
    }
    if (t<=0)
      return;
    Graphics g=getGraphics();
    g.setColor(Color.magenta);
    g.drawLine(x,yTop,x,yBottom);
    String txt=getTimeText(LocalDateTime.ofEpochSecond(t,0, ZoneOffset.UTC));
    int sw=g.getFontMetrics().stringWidth(txt);
    g.drawString(txt,(x+5+sw<x0+xLength)?x+5:x-5-sw,yBottom-5);
  }
  public void mouseDragged(MouseEvent e) {}
}
