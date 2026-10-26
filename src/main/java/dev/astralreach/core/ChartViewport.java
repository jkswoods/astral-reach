package dev.astralreach.core;

/** Per-open-page chart view. Never changes a journey, inventory or camera. */
public final class ChartViewport {
 public static final int WIDTH=640,HEIGHT=614;
 private int level;
 private double panX,panY;
 public double scale(){return 1+level*.25;}
 public int percent(){return 100+level*25;}
 public boolean canZoomIn(){return level<5;}
 public boolean canZoomOut(){return level>0;}
 public boolean moved(){return level!=0||panX!=0||panY!=0;}
 public int left(){return (int)Math.round((WIDTH-WIDTH*scale())/2+panX);}
 public int top(){return (int)Math.round((HEIGHT-HEIGHT*scale())/2+panY);}
 public int scaled(double value){return (int)Math.round(value*scale());}
 public boolean action(String action){
  switch(action){
   case "ZoomIn" -> level=Math.min(5,level+1);
   case "ZoomOut" -> level=Math.max(0,level-1);
   case "PanLeft" -> panX+=64;
   case "PanRight" -> panX-=64;
   case "PanUp" -> panY+=64;
   case "PanDown" -> panY-=64;
   case "Fit" -> {level=0;panX=panY=0;}
   default -> {return false;}
  }
  double x=(WIDTH*scale()-WIDTH)/2,y=(HEIGHT*scale()-HEIGHT)/2;
  panX=Math.clamp(panX,-x,x);panY=Math.clamp(panY,-y,y);
  return true;
 }
}
