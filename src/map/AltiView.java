package map;

import data.Conflict;
import data.ConflictPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * Draws a graph of the altitudes of 2 aircraft depending on time
 */

public class AltiView extends JPanel {
  public static final int xMarg=10, yMarg=5;
  public static final Color colorF1=new Color(128,0,0), colorF2=new Color(0,0,128);
  
  public static final Stroke stroke2=new BasicStroke(2);
  public static final float dash[] = {5.0f,5.0f};
  public static Stroke dashedStroke = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER,3.0f, dash, 0.0f);
  /**
   * Specification of the conflict to be shown
   */
  public Conflict conflict=null;
  
  protected int minAlt=0, maxAlt=0;
  /**
   * Used to speed up redrawing
   */
  protected BufferedImage off_Image=null;
  protected boolean off_Valid=false;
  
  
  public void setConflict(Conflict conflict) {
    this.conflict = conflict;
    off_Valid=false;
    if (conflict==null || conflict.flights==null || conflict.flights.length<2 ||
        conflict.flights[0].last==null) {
      redraw();
      return;
    }
    minAlt=Math.min(conflict.flights[0].altitude,conflict.flights[1].altitude);
    maxAlt=Math.max(conflict.flights[0].altitude,conflict.flights[1].altitude);
    for (int i=0; i<conflict.flights.length; i++)
      for (int j=0; j<3; j++) {
        ConflictPoint cp=(j==0)?conflict.flights[i].first:(j==1)?conflict.flights[i].closest:conflict.flights[i].last;
        if (minAlt>cp.altitude)
          minAlt=cp.altitude;
        else
          if (maxAlt<cp.altitude)
            maxAlt=cp.altitude;
      }
    redraw();
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
    int x0=xMarg+fm.stringWidth(altStr), xEnd=w-2*xMarg-1, xLength=xEnd-x0;
    int yTop=yMarg+fm.getHeight(), yBottom=h-2*(yMarg+fm.getHeight());
    
    g.setColor(Color.gray);
    g.drawLine(x0,yTop,x0,yBottom);
    g.drawLine(x0,yBottom,xEnd,yBottom);
    g.drawString(altStr,xMarg,yTop+fm.getAscent());
    g.drawString(getTimeText(conflict.detectionTime),xMarg,yBottom+fm.getHeight());
    
    double tt1=conflict.flights[0].first.timeTo,
           tt2=conflict.flights[0].closest.timeTo,
           tt3=conflict.flights[0].last.timeTo;
    int x1=x0+(int)Math.round(tt1/tt3*xLength), x2=x0+(int)Math.round(tt2/tt3*xLength);
    
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
    g.drawLine(xEnd,yTop,xEnd,yBottom);
    timeStr=getTimeText(conflict.flights[0].last.time);
    g.drawString(timeStr,w-xMarg-fm.stringWidth(timeStr),yBottom+fm.getHeight());
  
    g.setStroke(origStroke);
    if (minAlt==maxAlt) {
      g.setColor(Color.darkGray);
      g.setStroke(stroke2);
      g.drawLine(x0,yTop,xEnd,yTop);
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
        int xx2=(i==0)?x1:(i==1)?x2:xEnd;
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
}
