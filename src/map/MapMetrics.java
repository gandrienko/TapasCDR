package map;

import java.awt.*;

public class MapMetrics {
  public static final double degInRad=180/Math.PI, radInDeg=Math.PI/180,
      pi_4=Math.PI/4, pi_2=Math.PI/2;
  
  /**
   * Indicates whether the coordinates on the map are geographical,
   * i.e. X is the longitude and Y is the latitude. By default true.
   */
  public boolean isGeographic=true;
  /**
   * Lower-left and upper right corners of the visible territory
   */
  public double tx1 =Double.NaN, ty1 =Double.NaN, tx2 =Double.NaN, ty2 =Double.NaN;
  /**
   * The scaling factor for transformations between real-world and screen
   * coordinates
   */
  public double step=1.0f;
  /**
   * In case of geographic coordinates, the scaling factor may be different
   * for X-coordinate (longitude) and Y-coordinate (latitude). This is
   * a special scaling factor for the latitude. In case of non-geographic
   * coordinates, it is equal to step.
   */
  public double stepY=1.0f;
  /**
   * For Mercator projection: projected minimum and maximum Y (latitude)
   */
  public double prLat1=Double.NaN, prLat2=Double.NaN;
  /**
   * The rectangle within some window on the screen in which the map is drawn
   */
  public Rectangle viewport=null;
  /**
   * Maximum Y coordinare on the screen
   */
  protected int scrMaxY=0;
  
  /**
   * Sets the lower-left and upper right corners of the visible territory
   */
  public void setTerritoryBounds(double tx1, double ty1, double tx2, double ty2) {
    this.tx1 =tx1; this.ty1 =ty1; this.tx2 =tx2; this.ty2 =ty2;
    setup();
  }
  
  public void setTerritoryBounds(double b[]) {
    if (b!=null)
      setTerritoryBounds(b[0],b[1],b[2],b[3]);
    else
      setTerritoryBounds(Double.NaN,Double.NaN,Double.NaN,Double.NaN);
  }
  
  public boolean hasTerritoryBounds(){
    return !Double.isNaN(tx1) && !Double.isNaN(ty1) && !Double.isNaN(tx2) && !Double.isNaN(ty2) &&
               tx2 >= tx1 && ty2 >= ty1 && (tx2 > tx1 || ty2 > ty1);
  }
  
  public double[] getVisibleTerritoryBounds() {
    if (viewport==null || !hasTerritoryBounds())
      return null;
    double b[]={absX(viewport.x),absY(viewport.y+viewport.height),
        absX(viewport.x+viewport.width),absY(viewport.y)};
    return b;
  }
  /**
   * Sets the rectangle within some window on the screen in which the map must be drawn
   */
  public void setViewport(Rectangle viewport) {
    this.viewport = viewport;
    setup();
  }
  /**
   * Depending on the current territory extent and the screen area where the
   * map is drawn sets its internal variables used for coordinate transformation.
   */
  public void setup() {
    if (!hasTerritoryBounds() || viewport==null)
      return;
    double rx1=tx1, rx2=tx2, ry1=ty1, ry2=ty2;
    if (isGeographic) { //transform to Mercator projection
      if (ry1<-85.051) ry1=-85.051;
      else
        if (ry2>85.051) ry1=85.051;
      double dy=ry2-ry1;
      ry1=lat2Y(ry1);
      ry2=lat2Y(ry2);
    }
    prLat1=ry1; prLat2=ry2;
    double stepx=(rx2-rx1)/viewport.width,
        stepy=(ry2-ry1)/viewport.height;
    step=stepY=Math.max(stepx,stepy);
    scrMaxY=(int)Math.ceil((prLat2-prLat1)/stepY);
  }
  /**
   * Returns the screen X coordinate for the given real-world point
   */
  public int scrX(double rx) {
    return viewport.x+(int)Math.round((rx-tx1)/step);
  }
  /**
   * Returns the screen Y coordinate for the given real-world point
   */
  public int scrY(double ry) {
    if (!isGeographic) {
      return viewport.y+scrMaxY-(int)Math.round((ry-ty1)/stepY);
    }
    double yy=lat2Y(ry);
    return viewport.y+scrMaxY-(int)Math.round((yy-prLat1)/stepY);
  }
  /**
   * Returns the real-world X coordinate for the given screen X coordinate
   */
  public double absX(int x) {
    return tx1+step*(x-viewport.x);
  }
  /**
   * Returns the real-world Y coordinate for the given screen Y coordinate
   */
  public double absY(int y) {
    if (!isGeographic) {
      return ty1+stepY*(scrMaxY-(y-viewport.y));
    }
    double yy=prLat1+stepY*(scrMaxY-(y-viewport.y));
    return (float)y2Lat(yy);
  }
  /**
   * Returns the screen rectangle corresponding to the given territory bounds
   */
  public Rectangle getScreenRectangle (double x1, double y1, double x2, double y2) {
    int sx1=scrX(x1), sy1=scrY(y1), sx2=scrX(x2), sy2=scrY(y2);
    return new Rectangle(sx1,sy2,sx2-sx1,sy1-sy2);
  }
  /**
   * Transforms real-world coordinates into screen coordinates
   */
  public Point getScreenPoint(double rx, double ry){
    return new Point(scrX(rx),scrY(ry));
  }
  
  /**
   * Transformation from degrees latitude to the Mercator projection
   */
  public static double lat2Y (double lat) {
    if (lat<-85.051) lat=-85.051;
    else
      if (lat>85.051) lat=85.051;
    return degInRad * Math.log(Math.tan(pi_4+lat*radInDeg/2));
  }
  /**
   * Transformation from the Mercator projection to degrees latitude
   */
  public static double y2Lat (double y) {
    return degInRad * (2 * Math.atan(Math.exp(y*radInDeg)) - pi_2);
  }
  /**
   * The average radius of the Earth, in meters.
   */
  public static final long R_EARTH=6371000;
  /**
   * Computes the metric distance between two points on the Earths specified
   * by their geograogical coordinates (latitudes and longitudes).
   * Note that longitude is X and latitude is Y!
   * Returns the distance in meters.
   */
  public static double geoDist (double lon1, double lat1, double lon2, double lat2) {
    double rlon1=lon1*Math.PI/180, rlon2=lon2*Math.PI/180,
        rlat1=lat1*Math.PI/180, rlat2=lat2*Math.PI/180;
    double dlon=(rlon1-rlon2)/2, dlat=(rlat1-rlat2)/2, lat12=(rlat1+rlat2)/2;
    double sindlat=Math.sin(dlat),
        sindlon=Math.sin(dlon);
    double cosdlon=Math.cos(dlon),
        coslat12=Math.cos(lat12),
        f=sindlat*sindlat*cosdlon*cosdlon+sindlon*sindlon*coslat12*coslat12;
    // alternative formula:
    // double f=sindlat*sindlat+sindlon*sindlon*Math.cos(rlat1)*Math.cos(rlat2);
    f=Math.sqrt(f);
    f=Math.asin(f)*2; //the angle between the points
    f*=R_EARTH;
    return (double)f;
  }
}
