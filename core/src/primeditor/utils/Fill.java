package primeditor.utils;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import primeditor.*;
import primeditor.creature.*;
import primeditor.creature.CellTypes.*;
import primeditor.creature.Creature.*;

public class Fill{
    CellType target;
    Color targetColor = new Color();
    boolean paintMode;
    boolean invalid;

    Color fillColor = new Color();
    CellType fillCell;

    static ShortSeq next2 = new ShortSeq(900 * 2), tmp2 = new ShortSeq(900 * 2);
    static IntSet tmpSet = new IntSet(1600);

    Creature creature;

    public static Fill fill(Creature creature, int x, int y, CellType fillCell, Color fillColor){
        next2.clear();
        tmp2.clear();
        Fill f = new Fill();
        f.creature = creature;
        f.fillCell = fillCell;
        f.fillColor.set(fillColor);
        f.paintMode = fillCell == null;

        //Point2 p = Pools.obtain(Point2.class, Point2::new);
        //p.set(x, y);
        var cell = creature.cellGrid[Control.toGrid(x, y)];
        if(cell != null){
            f.target = f.paintMode ? null : cell.type;
            if(f.paintMode){
                f.targetColor.set(cell.r, cell.g, cell.b, cell.a);
            }
        }else{
            f.target = null;
        }
        //f.next.add(p);
        f.invalid = f.paintMode && (cell == null);

        //f.init();
        return f;
    }
    public void start(int x, int y){
        next2.add((short)x, (short)y);
    }

    public void init(){
        if(invalid) return;
        //int limit = target == null ? 250 : 800;
        int limit = (target == null && !paintMode) ? 250 : 800;
        for(int i = 0; i < limit; i++){
            if(next2.isEmpty()) break;
            int ns = next2.size;
            short[] ni = next2.items;
            for(int j = 0; j < ns; j += 2){
                int px = ni[j], py = ni[j + 1];

                Cell cell = creature.cellGrid[Control.toGrid(px, py)];

                Control.undo.setReference(cell);
                if(cell == null){
                    if(paintMode) continue;
                    cell = new Cell();
                    cell.x = px - 1024;
                    cell.y = py - 1024;
                    cell.added = true;
                    //creature.cellGrid[Control.toGrid(px, py)] = cell;
                }

                cell.r = fillColor.r;
                cell.g = fillColor.g;
                cell.b = fillColor.b;
                cell.a = fillColor.a;
                if(!paintMode){
                    cell.type = fillCell;
                }
                //occupied[p.x + p.y * 2048] = true;
                tmpSet.add(px + py * 2048);

                creature.changed.add(cell);
                Control.undo.registerIndividual(cell);
                Control.undo.active = true;
                for(Point2 p2 : Control.hexagonEdge){
                    int nx = px + p2.x;
                    int ny = py + p2.y;
                    if(nx >= 0 && nx < 2048 && ny >= 0 && ny < 2048){
                        if(!tmpSet.contains(nx + ny * 2048)){
                            tmpSet.add(nx + ny * 2048);
                            Cell nc = creature.cellGrid[Control.toGrid(nx, ny)];
                            if(nc == null){
                                if(target == null){
                                    tmp2.add((short)nx, (short)ny);
                                    //tmpSet.add(nx + ny * 2048);
                                }
                            }else{
                                if(paintMode){
                                    float tol = 0.000002f;
                                    boolean br = Mathf.equal(nc.r, targetColor.r, tol);
                                    boolean bg = Mathf.equal(nc.g, targetColor.g, tol);
                                    boolean bb = Mathf.equal(nc.b, targetColor.b, tol);
                                    boolean ba = Mathf.equal(nc.a, targetColor.a, tol);
                                    if(br && bg && bb && ba){
                                        tmp2.add((short)nx, (short)ny);
                                        //tmpSet.add(nx + ny * 2048);
                                    }
                                }else{
                                    if(nc.type == target){
                                        tmp2.add((short)nx, (short)ny);
                                        //tmpSet.add(nx + ny * 2048);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            next2.clear();
            next2.addAll(tmp2);
            tmp2.clear();
        }
        tmpSet.clear();
        next2.clear();
        tmp2.clear();
    }
}
