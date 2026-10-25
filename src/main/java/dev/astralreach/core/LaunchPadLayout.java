package dev.astralreach.core;

import java.util.ArrayList;
import java.util.List;

/** Five-by-five joined deck modules, with a console outside the 25-block pad. */
public final class LaunchPadLayout {
 public static final int MODULE_WIDTH=5, MODULES_ACROSS=5, WIDTH=25, CONSOLE_DISTANCE=14;
 private LaunchPadLayout(){}
 public record Cell(int x,int y,int z){public Cell add(int dx,int dy,int dz){return new Cell(Math.addExact(x,dx),Math.addExact(y,dy),Math.addExact(z,dz));}}
 public enum Facing {
  NORTH(0,-1),EAST(1,0),SOUTH(0,1),WEST(-1,0);
  public final int dx,dz;Facing(int dx,int dz){this.dx=dx;this.dz=dz;}
 }
 public static Cell center(Cell console,Facing towardPad){return console.add(towardPad.dx*CONSOLE_DISTANCE,0,towardPad.dz*CONSOLE_DISTANCE);}
 public static List<Cell> modules(Cell center){
  var out=new ArrayList<Cell>(25);
  for(int x=-10;x<=10;x+=5)for(int z=-10;z<=10;z+=5)out.add(center.add(x,0,z));
  return List.copyOf(out);
 }
 public static List<Cell> occupiedCells(Cell center){
  var out=new ArrayList<Cell>(625);
  for(var module:modules(center))for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)out.add(module.add(x,0,z));
  return List.copyOf(out);
 }
 public static boolean contains(Cell center,Cell cell){return center.y==cell.y&&Math.abs((long)center.x-cell.x)<=12&&Math.abs((long)center.z-cell.z)<=12;}
 public static boolean supports(LaunchPlan plan){return plan.serviceMargin()>=2;}
}
