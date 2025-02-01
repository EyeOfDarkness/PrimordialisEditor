package primeditor;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import primeditor.creature.CellTypes.*;
import primeditor.creature.Creature.*;
import primeditor.ui.*;
import primeditor.utils.*;

import static primeditor.Renderer.*;

public class Control implements ApplicationListener{
    private static final Vec2 ret = new Vec2(), v = new Vec2();

    public static boolean test = false;
    public static int orientation = 0;

    public static Point2[] hexagonEdge = {
            new Point2(0, -1), new Point2(1, -1),
            new Point2(-1, 0), new Point2(1, 0),
            new Point2(-1, 1), new Point2(0, 1)
    };

    public static float[] directions;
    static MirrorF[] mirrors = new MirrorF[12];
    static Point2 tp = new Point2();

    public static CellType currentCell;
    public static boolean paintMode = false;
    public static boolean mirror = false;

    public static ToolType tool = ToolType.brush;
    public static int brushSize = 0;

    public static Undo undo = new Undo();

    public static Seq<Element> queueRemoveFocus = new Seq<>();

    private static final float[] tmpHsv = new float[3];
    int lastX, lastY;
    float lastMX, lastMY;
    static IntSet placedTmp = new IntSet(16);
    static IntSeq brushTmp = new IntSeq();

    static int[][] brushes;
    static int maxBrushSize = 0;

    static{
        directions = new float[12];
        for(int i = 0; i < 12; i++){
            double ang = (i * 30f + 90f) * Mathf.doubleDegRad;
            //float tx = Mathf.cosDeg(ang);
            double tx = Math.cos(ang), ty = Math.sin(ang);
            tx -= ty * 0.5;
            directions[i] = Angles.angle((float)tx, (float)ty);
        }
        mirrors[0] = (x, y, out) -> {
            int offX = -(y / 2);
            int offX2 = (y % 2) == 0 ? 0 : 1;
            out.set((offX - (x - offX)) + 2048 + 1024 - offX2, y);
        };
        mirrors[1] = (x, y, out) -> {
            //int offX = -(y / 2);
            //int offX2 = (y % 2) == 0 ? 0 : 1;
            //out.set((offX - (x - offX)) + 2048 + 1024 - offX2, y);
            out.set(2048 - y, 2048 - x);
        };
        mirrors[2] = (x, y, out) -> {
            int mid = -x / 2 + (2048 - 512);
            int offY2 = (x % 2) == 0 ? 0 : 1;
            out.set(x, -(y - mid) + mid - offY2);
        };
        mirrors[10] = (x, y, out) -> {
            //int offX = -(y / 2);
            //int offX2 = (y % 2) == 0 ? 0 : 1;
            //out.set((offX - (x - offX)) + 2048 + 1024 - offX2, y);
            out.set(y, x);
        };
        mirrors[3] = (x, y, out) -> {
            int cy = y - 1024;
            out.set(x + cy, -cy + 1024);
        };
        mirrors[4] = mirrors[10];
        mirrors[6] = mirrors[0];
        mirrors[7] = mirrors[1];
        mirrors[8] = mirrors[2];
        mirrors[11] = (x, y, out) -> {
            int cx = x - 1024;
            out.set(-cx + 1024, y + cx);
        };
        mirrors[5] = mirrors[11];
        mirrors[9] = mirrors[3];
        //todo 3
        //0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11
    }

    public static void loadBrush(){
        brushes = new int[16][0];
        maxBrushSize = 0;

        Fi list = Core.files.internal("brushsizes.txt");
        String[] str = list.readString().split("\n");

        //brushTmp.clear();

        int idx = 0;
        for(String s : str){
            if(s.length() < 2){
                brushes[idx] = brushTmp.toArray();
                brushTmp.clear();
                idx++;
                continue;
            }
            String[] ps = s.split(",");
            brushTmp.add(Integer.parseInt(ps[0]), Integer.parseInt(ps[1]));
        }
        if(!brushTmp.isEmpty()){
            brushes[idx] = brushTmp.toArray();
            brushTmp.clear();
            idx++;
        }
        maxBrushSize = idx;
    }

    public static void setCell(CellType type){
        currentCell = type;
        ColorWheel w = EditorMain.ui.mainColorWheel;
        Tmp.c2.set(type.defaultColor).toHsv(tmpHsv);

        w.set(tmpHsv[0], tmpHsv[1], tmpHsv[2], 1f);
    }
    public static void setCell(CellType type, Color color){
        currentCell = type;
        ColorWheel w = EditorMain.ui.mainColorWheel;
        color.toHsv(tmpHsv);
        w.set(tmpHsv[0], tmpHsv[1], tmpHsv[2], 1f);
    }

    public static void setOrientation(int i){
        //
        cr = -30f * i;
        orientation = i;
    }

    @Override
    public void update(){
        if(!Core.input.keyDown(KeyCode.mouseLeft)){
            for(Element f : queueRemoveFocus){
                if(Core.scene.getScrollFocus() == f){
                    Core.scene.setScrollFocus(null);
                }
            }
            queueRemoveFocus.clear();
        }

        boolean mouse = Core.scene.hasMouse() || Core.scene.getScrollFocus() != null || Core.scene.getKeyboardFocus() != null;
        float sx = (Core.input.axis(Binding.move_x) + Core.input.axis(Binding.move_x2)) * cs * 0.02f;
        float sy = (Core.input.axis(Binding.move_y) + Core.input.axis(Binding.move_y2)) * cs * 0.02f;

        cx -= sx;
        cy -= sy;
        if(!mouse){
            cs -= Core.input.axis(Binding.zoom) * 0.25f * cs;
        }

        if(Core.input.keyTap(KeyCode.q)){
            cr += 30f;
            orientation--;
            if(EditorMain.creature != null){
                //Log.info("R1:" + orientation + " R2:" + EditorMain.creature.orientation);
                EditorMain.creature.orientation = (byte)(Mathf.mod(orientation, 12) + 3);
            }
        }
        if(Core.input.keyTap(KeyCode.e)){
            cr -= 30f;
            orientation++;
            if(EditorMain.creature != null){
                //Log.info("R1:" + orientation + " R2:" + EditorMain.creature.orientation);
                EditorMain.creature.orientation = (byte)(Mathf.mod(orientation, 12) + 3);
            }
        }
        if(Core.input.keyTap(KeyCode.tab)){
            showtype = !showtype;
        }
        if(Core.input.keyTap(KeyCode.f1)){
            test = !test;
        }
        /*
        if(Core.input.keyTap(KeyCode.f1) && EditorMain.creature != null){
            //Log.info("Positions");
            StringBuilder str = new StringBuilder("Positions: \n");
            for(Cell c : EditorMain.creature.cells){
                if(c != null){
                    String ps = "X:" + c.x + ", Y:" + c.y + "\n";
                    str.append(ps);
                }
            }
            Log.info(str.toString());
        }
        */
        if(Core.input.keyTap(KeyCode.f5) && EditorMain.creature != null){
            EditorMain.creature.loadRender();
        }
        if(Core.input.keyTap(KeyCode.f2) && EditorMain.creature != null){
            EditorMain.renderer.screenshot();
        }

        if(Core.input.keyTap(KeyCode.equals)){
            brushSize = Math.min(brushSize + 1, maxBrushSize - 1);
        }
        if(Core.input.keyTap(KeyCode.minus)){
            brushSize = Math.max(brushSize - 1, 0);
        }
        if(Core.input.keyDown(KeyCode.controlLeft) && Core.input.keyTap(KeyCode.z) && EditorMain.creature != null){
            undo.undo(EditorMain.creature);
        }

        Vec2 m = mouseToCanvas();
        int ix = (int)m.x, iy = (int)m.y;
        float mx = Core.input.mouseX();
        float my = Core.input.mouseY();
        if(!mouse){
            updateMouse();
        }
        lastX = ix;
        lastY = iy;
        lastMX = mx;
        lastMY = my;
    }

    void putCellSized(int x, int y, boolean place){
        int[] pos = brushes[brushSize];
        for(int i = 0; i < pos.length; i += 2){
            int ox = x + pos[i];
            int oy = y + pos[i + 1];
            if(placedTmp.add(ox + oy * 2048)){
                putCell(ox, oy, place);
            }
        }
    }

    void putCell(int x, int y, boolean place){
        if(!(x >= 0 && x < 2048 && y >= 0 && y < 2048)) return;

        ColorWheel w = EditorMain.ui.mainColorWheel;
        var creature = EditorMain.creature;

        var c = creature.cellGrid[toGrid(x, y)];
        undo.setReference(c);
        undo.active = true;

        if(!place){
            if(c != null){
                c.deleted = true;
                creature.changed.add(c);
                undo.registerIndividual(c);
            }
            return;
        }

        var col = w.get();
        Color cl = Tmp.c2.fromHsv(col.h, col.s, col.v).a(col.a);
        if(c != null){
            float tol = 0.000002f;
            boolean br = !Mathf.equal(cl.r, c.r, tol);
            boolean bg = !Mathf.equal(cl.g, c.g, tol);
            boolean bb = !Mathf.equal(cl.b, c.b, tol);
            boolean ba = !Mathf.equal(cl.a, c.a, tol);

            if((c.type != currentCell) || br || bg || bb || ba){
                if(!paintMode) c.type = currentCell;
                c.r = cl.r;
                c.g = cl.g;
                c.b = cl.b;
                c.a = cl.a;
                creature.changed.add(c);
                undo.registerIndividual(c);
            }
        }else if(!paintMode){
            c = new Cell();
            c.x = x - 1024;
            c.y = y - 1024;
            c.type = currentCell;
            c.r = cl.r;
            c.g = cl.g;
            c.b = cl.b;
            c.a = cl.a;
            c.added = true;

            creature.changed.add(c);
            undo.registerIndividual(c);
        }
    }

    public void updateMouse(){
        Vec2 m = mouseToCanvas();
        boolean ldown = Core.input.keyDown(KeyCode.mouseLeft);
        boolean lup = Core.input.keyRelease(KeyCode.mouseLeft);

        boolean rdown = Core.input.keyDown(KeyCode.mouseRight);
        boolean rup = Core.input.keyRelease(KeyCode.mouseRight);

        boolean lclick = Core.input.keyTap(KeyCode.mouseLeft);

        ColorWheel w = EditorMain.ui.mainColorWheel;

        int ix = (int)m.x, iy = (int)m.y;
        float mx2 = Core.input.mouseX();
        float my2 = Core.input.mouseY();

        if(ix >= 0 && ix < 2048 && iy >= 0 && iy < 2048 && EditorMain.creature != null){
            var creature = EditorMain.creature;
            /*
            if(lclick){
                Log.info(ix + ":" + iy);
            }
            */
            switch(tool){
                case brush -> {
                    int range = (int)(Mathf.dst(ix, iy, lastX, lastY) * 1.75f) + 1;
                    for(int i = 0; i < range; i++){
                        float fin = i / (float)range;
                        float fx = Mathf.lerp(mx2, lastMX, fin);
                        float fy = Mathf.lerp(my2, lastMY, fin);
                        Vec2 bm = mouseToCanvas(fx, fy);
                        //int bx = (int)Mathf.lerp(ix, lastX, fin);
                        //int by = (int)Mathf.lerp(iy, lastY, fin);
                        int bx = (int)bm.x;
                        int by = (int)bm.y;

                        if(ldown){
                            putCellSized(bx, by, true);
                            //Log.info((mirrors[Mathf.mod(orientation, mirrors.length)] != null) + ":" + orientation + ":" + mirror);
                            if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                                var mir = getMirror(bx, by, orientation);
                                int mx = mir.x, my = mir.y;
                                if(mx != bx || my != by){
                                    putCellSized(mx, my, true);
                                }
                            }
                        }else if(rdown && !paintMode){
                            putCellSized(bx, by, false);
                            if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                                var mir = getMirror(bx, by, orientation);
                                int mx = mir.x, my = mir.y;
                                if(mx != bx || my != by){
                                    putCellSized(mx, my, false);
                                }
                            }
                        }
                    }
                    if((lup || (rup && !paintMode)) && undo.active){
                        undo.flush();
                        undo.active = false;
                        //Log.info("undoTest");
                    }
                }
                case fill -> {
                    if(lclick){
                        Color cl = Tmp.c2.fromHsv(w.h, w.s, w.v).a(w.a);
                        Fill f = Fill.fill(creature, ix, iy, paintMode ? null : currentCell, cl);
                        f.start(ix, iy);
                        if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                            var mir = getMirror(ix, iy, orientation);
                            int mx = mir.x, my = mir.y;
                            if(mx != ix || my != iy){
                                f.start(mx, my);
                            }
                        }
                        f.init();
                    }
                    if(lup && undo.active){
                        undo.flush();
                        undo.active = false;
                    }
                }
                case eraser -> {
                    int range = (int)(Mathf.dst(ix, iy, lastX, lastY) * 1.75f) + 1;
                    for(int i = 0; i < range; i++){
                        float fin = i / (float)range;
                        float fx = Mathf.lerp(mx2, lastMX, fin);
                        float fy = Mathf.lerp(my2, lastMY, fin);
                        Vec2 bm = mouseToCanvas(fx, fy);
                        //int bx = (int)Mathf.lerp(ix, lastX, fin);
                        //int by = (int)Mathf.lerp(iy, lastY, fin);
                        int bx = (int)bm.x;
                        int by = (int)bm.y;
                        if(ldown && !paintMode){
                            putCellSized(bx, by, false);
                            if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                                var mir = getMirror(bx, by, orientation);
                                int mx = mir.x, my = mir.y;
                                if(mx != bx || my != by){
                                    putCellSized(mx, my, false);
                                }
                            }
                        }
                    }
                    if(lup && !paintMode && undo.active){
                        undo.flush();
                        undo.active = false;
                    }
                }
                case sampler -> {
                    if(lclick){
                        var c = creature.cellGrid[toGrid(ix, iy)];
                        if(c != null){
                            if(!paintMode){
                                currentCell = c.type;
                                CellSelector sel = EditorMain.ui.selector;
                                sel.updating = true;
                                sel.group.uncheckAll();
                                ImageButton but = sel.buttonMap.get(c.type);
                                if(but != null){
                                    but.setChecked(true);
                                }
                                sel.updating = false;
                            }
                            Tmp.c2.set(c.r, c.g, c.b, 1f);
                            Tmp.c2.toHsv(tmpHsv);
                            w.set(tmpHsv[0], tmpHsv[1], tmpHsv[2], c.a);
                        }
                    }
                }
            }
        }
        placedTmp.clear();
    }

    public static int toGrid(int x, int y){
        return Mathf.clamp(x, 0, 2047) + Mathf.clamp(y, 0, 2047) * 2048;
    }
    
    public static Vec2 mouseToCanvas(float mx, float my){
        float sw = Core.graphics.getWidth(), sh = Core.graphics.getHeight();
        //float mx = Core.input.mouseX() / sw;
        //float my = Core.input.mouseY() / sh;
        mx /= sw;
        my /= sh;

        float fs = Math.min(sw, sh);
        float aw = sw / fs, ah = sh / fs;

        //return ret.trns(cr, wx, wy / 1.155501302f).scl(1f / aw, 1f / ah).add(cx, cy).scl(1f / cs);
        ret.set(mx, my).sub(0.5f, 0.5f).scl(cs).sub(cx, cy).scl(aw, ah).rotate(-cr).scl(1f, 1.155501302f).add(0.5f, 0.5f);
        //(floor(v_texCoords.y * 2048.0) - 1024.0) / (2048.0 * 2.0);
        float offX = ((int)(ret.y * 2048f) - 1024f) / 4096f;
        float wx = ret.x * 2048f, wy = ret.y * 2048f;
        ret.x -= offX;
        ret.scl(2048f);
        v.set(ret).sub((int)ret.x, (int)ret.y).sub(0.5f, 0.5f);
        
        float lx = 0.5f - Math.max(0f, Math.abs(v.x) - 0.25f) * 0.577350f * 1.155501302f;
        float ly = Math.abs(v.y);

        if(ly > lx){
            if(v.y > 0f){
                float ox = (((int)(wy + 1f)) - 1024f) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy + 1f;
            }else{
                float ox = (((int)(wy - 1f)) - 1024f) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy - 1f;
            }
        }
        ret.x = (int)ret.x + 0.5f;
        ret.y = (int)ret.y + 0.5f;

        return ret;
    }

    public static Vec2 mouseToCanvas(){
        float sw = Core.graphics.getWidth(), sh = Core.graphics.getHeight();
        float mx = Core.input.mouseX() / sw;
        float my = Core.input.mouseY() / sh;

        float fs = Math.min(sw, sh);
        float aw = sw / fs, ah = sh / fs;

        //return ret.trns(cr, wx, wy / 1.155501302f).scl(1f / aw, 1f / ah).add(cx, cy).scl(1f / cs);
        ret.set(mx, my).sub(0.5f, 0.5f).scl(cs).sub(cx, cy).scl(aw, ah).rotate(-cr).scl(1f, 1.155501302f).add(0.5f, 0.5f);
        //(floor(v_texCoords.y * 2048.0) - 1024.0) / (2048.0 * 2.0);
        float offX = ((int)(ret.y * 2048f) - 1024f) / 4096f;
        float wx = ret.x * 2048f, wy = ret.y * 2048f;
        ret.x -= offX;
        ret.scl(2048f);
        v.set(ret).sub((int)ret.x, (int)ret.y).sub(0.5f, 0.5f);
        
        float lx = 0.5f - Math.max(0f, Math.abs(v.x) - 0.25f) * 0.577350f * 1.155501302f;
        float ly = Math.abs(v.y);

        if(ly > lx){
            if(v.y > 0f){
                float ox = (((int)(wy + 1f)) - 1024f) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy + 1f;
            }else{
                float ox = (((int)(wy - 1f)) - 1024f) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy - 1f;
            }
        }
        ret.x = (int)ret.x + 0.5f;
        ret.y = (int)ret.y + 0.5f;

        return ret;
    }

    public static Point2 getMirror(int x, int y, int orientation){
        //
        tp.set(0, 0);
        MirrorF mr = mirrors[Mathf.mod(orientation, mirrors.length)];
        if(mr != null){
            mr.get(x, y, tp);
        }

        return tp;
    }

    interface MirrorF{
        void get(int x, int y, Point2 out);
    }

    private static class Brush{
        int[] pos;
    }

    public enum ToolType{
        brush,
        eraser,
        fill,
        sampler
    }
}
