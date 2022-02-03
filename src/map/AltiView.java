package map;

import data.Conflict;
import data.ConflictPoint;
import data.FlightPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    double tt3=conflict.flights[0].last.timeTo, ttLast=tt3;
    for (int i=0; i<conflict.flights.length; i++) {
      FlightPoint pp[]=conflict.flights[i].pp;
      if (pp != null && pp[pp.length - 1].pointTimeUnix - conflict.detectionTimeUnix > ttLast)
        ttLast = pp[pp.length - 1].pointTimeUnix - conflict.detectionTimeUnix;
    }
    if (ttLast>tt3*2) ttLast=tt3*2;
    maxTime=minTime+Math.round(ttLast);

    minAlt=Math.min(conflict.flights[0].altitude,conflict.flights[1].altitude);
    maxAlt=Math.max(conflict.flights[0].altitude,conflict.flights[1].altitude);
    for (int i=0; i<conflict.flights.length; i++) {
      for (int j = 0; j < 3; j++) {
        ConflictPoint cp = (j == 0) ? conflict.flights[i].first :
                               (j == 1) ? conflict.flights[i].closest : conflict.flights[i].last;
        if (minAlt > cp.altitude)
          minAlt = cp.altitude;
        else
          if (maxAlt < cp.altitude)
            maxAlt = cp.altitude;
      }
      FlightPoint pp[]=conflict.flights[i].pp;
      if (pp!=null)
        for (int j=0; j<pp.length; j++)
          if (minAlt > pp[j].altitude)
            minAlt = pp[j].altitude;
          else
            if (maxAlt < pp[j].altitude)
              maxAlt = pp[j].altitude;
    }
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
    g.drawLine(x1,yTop,x1,yBottom);
    String timeStr=getTimeText(conflict.flights[0].first.time);
    g.drawString(timeStr,x1-fm.stringWidth(timeStr)/2,yBottom+fm.getHeight());
    g.setColor(Color.red);
    g.drawLine(x2,yTop,x2,yBottom);
    timeStr=getTimeText(conflict.flights[0].closest.time);
    int sw=fm.stringWidth(timeStr), tx=x2-sw/2;
    if (tx+sw>w-xMarg)
      tx=w-xMarg-sw;
    g.drawString(timeStr,tx,yMarg+fm.getAscent());
    g.setColor(Color.green.darker());
    g.drawLine(x3,yTop,x3,yBottom);
    timeStr=getTimeText(conflict.flights[0].last.time);
    sw=fm.stringWidth(timeStr); tx=x3-sw/2;
    if (tx+sw>w-xMarg)
      tx=w-xMarg-sw;
    g.drawString(timeStr,tx,yBottom+fm.getHeight());
  
    g.setStroke(origStroke);
    if (minAlt==maxAlt) {
      g.setColor(Color.darkGray);
      g.setStroke(stroke2);
      g.drawLine(x0,yTop,xEnd,yTop);
  
      int xPrev=x3;
      for (int i=0; i<conflict.flights.length; i++) {
        FlightPoint pp[]=conflict.flights[i].pp;
        if (pp != null) {
          g.setColor((i==0)?paleColorF1:paleColorF2);
          for (int j = 0; j < pp.length; j++) {
            int x=timeToXPos(pp[j].pointTimeUnix);
            g.setStroke(origStroke);
            g.drawRect(x-3,yTop-3,6,6);
            if (x>xPrev) {
              g.setStroke(smallDashStroke);
              if (xPrev<x3 && x>x3)
                xPrev=x3;
              g.drawLine(xPrev,yTop,x,yTop);
              xPrev=x;
            }
          }
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
      int y11=yMin-Math.round((1.0f*conflict.flights[0].altitude-minAlt)/altDiff*yDiff),
          y21=yMin-Math.round((1.0f*conflict.flights[1].altitude-minAlt)/altDiff*yDiff);
      g.setStroke(stroke2);
      int xx1=x0;
      for (int i=0; i<3; i++) {
        ConflictPoint cp1 = (i==0)?conflict.flights[0].first:(i==1)?conflict.flights[0].closest:conflict.flights[0].last,
            cp2 = (i==0)?conflict.flights[1].first:(i==1)?conflict.flights[1].closest:conflict.flights[1].last;
        int y12=yMin-Math.round((1.0f*cp1.altitude-minAlt)/altDiff*yDiff),
            y22=yMin-Math.round((1.0f*cp2.altitude-minAlt)/altDiff*yDiff);
        int xx2=(i==0)?x1:(i==1)?x2:x3;
        g.setColor(colorF1);
        g.drawLine(xx1,y11,xx2,y12);
        g.setColor(colorF2);
        g.drawLine(xx1,y21,xx2,y22);
        if (y12!=y22 && (y12!=y11 || y22!=y21)) {
          altStr=String.format("%d",Math.abs(cp1.altitude-cp2.altitude));
          g.setColor((i==0)?Color.orange.darker():(i==1)?Color.red:Color.green.darker());
          g.drawString(altStr,xx2-fm.stringWidth(altStr),Math.max(y12,y22)+fm.getAscent()+5);
        }
        xx1=xx2;
        y11=y12; y21=y22;
      }
  
      for (int i=0; i<conflict.flights.length; i++) {
        FlightPoint pp[]=conflict.flights[i].pp;
        if (pp != null) {
          g.setColor((i==0)?paleColorF1:paleColorF2);
          int xPrev=xx1, yPrev=(i==0)?y11:y21;;
          for (int j = 0; j < pp.length; j++) {
            int x=timeToXPos(pp[j].pointTimeUnix);
            int y=yMin-Math.round((1.0f*pp[j].altitude-minAlt)/altDiff*yDiff);
            g.setStroke(origStroke);
            g.drawRect(x-3,y-3,6,6);
            if (x>xPrev) {
              g.setStroke(smallDashStroke);
              g.drawLine(xPrev,yPrev,x,y);
              xPrev=x; yPrev=y;
            }
          }
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
    if (tTrans.timeUnix==t)
      return;
    redraw();
    tTrans.timeUnix=t;
    tTrans.notifyChange();
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
