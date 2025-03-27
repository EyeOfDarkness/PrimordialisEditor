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
import primeditor.creature.*;
import primeditor.creature.CellTypes.*;
import primeditor.creature.Creature.*;
import primeditor.io.*;
import primeditor.ui.*;
import primeditor.utils.*;

import static primeditor.EditorMain.*;
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
    static Point2 tp = new Point2(), tp2 = new Point2();

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
        int full = canvasRes;
        int half = full / 2;
        int quar = half / 2;

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
            out.set((offX - (x - offX)) + full + half - offX2, y);
        };
        mirrors[1] = (x, y, out) -> {
            //int offX = -(y / 2);
            //int offX2 = (y % 2) == 0 ? 0 : 1;
            //out.set((offX - (x - offX)) + 2048 + 1024 - offX2, y);
            out.set(full - y, full - x);
        };
        mirrors[2] = (x, y, out) -> {
            int mid = -x / 2 + (full - quar);
            int offY2 = (x % 2) == 0 ? 0 : 1;
            out.set(x, -(y - mid) + mid - offY2);
        };
        mirrors[10] = (x, y, out) -> {
            //int offX = -(y / 2);
            //int offX2 = (y % 2) == 0 ? 0 : 1;
            //out.set((offX - (x - offX)) + 2048 + 1024 - offX2, y);
            //noinspection SuspiciousNameCombination
            out.set(y, x);
        };
        mirrors[3] = (x, y, out) -> {
            int cy = y - half;
            out.set(x + cy, -cy + half);
        };
        mirrors[4] = mirrors[10];
        mirrors[6] = mirrors[0];
        mirrors[7] = mirrors[1];
        mirrors[8] = mirrors[2];
        mirrors[11] = (x, y, out) -> {
            int cx = x - half;
            out.set(-cx + half, y + cx);
        };
        mirrors[5] = mirrors[11];
        mirrors[9] = mirrors[3];
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

    boolean testf = true;
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
        //log mouse positions
        if(Core.input.keyTap(KeyCode.f3) && EditorMain.creature != null){
            //EditorMain.renderer.screenshot();
            var mw = mouseToWorld();
            //float mx = mw.x, my = mw.y;
            var mc = worldToCanvas(mw.x, mw.y);
            float cx = mc.x, cy = mc.y;
            var iv2 = canvasToIdx(cx, cy);
            var cell = EditorMain.creature.cellGrid[toGrid(iv2.x, iv2.y)];

            //int bx = iv2.x;
            //int by = iv2.y;
            //Log.info("X: " + iv2.x + " Y: " + iv2.y);
            if(cell != null){
                Log.info("Cell: " + cell.type.name + ", X: " + cell.x + ", Y: " + cell.y);
            }
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

        /*
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
        */
        var mw = mouseToWorld();
        //float mx = mw.x, my = mw.y;
        var mc = worldToCanvas(mw.x, mw.y);
        float cx = mc.x, cy = mc.y;
        //var mi = canvasToIdx(mc.x, mc.y);
        //int mx = mi.x, my = mi.y;

        if(!mouse){
            updateMouse2(cx, cy);
        }

        lastMX = cx;
        lastMY = cy;
        //lastX = mx;
        //lastY = my;
    }

    void putCellSized(int x, int y, boolean place){
        int[] pos = brushes[brushSize];
        for(int i = 0; i < pos.length; i += 2){
            int ox = x + pos[i];
            int oy = y + pos[i + 1];
            if(placedTmp.add(ox + oy * canvasRes)){
                putCell(ox, oy, place);
            }
        }
    }

    void putCell(int x, int y, boolean place){
        if(!(x >= 0 && x < canvasRes && y >= 0 && y < canvasRes)) return;

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
            c.x = x - hRes;
            c.y = y - hRes;
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

    public void updateMouse2(float mx, float my){
        boolean ldown = Core.input.keyDown(KeyCode.mouseLeft);
        boolean lup = Core.input.keyRelease(KeyCode.mouseLeft);

        boolean rdown = Core.input.keyDown(KeyCode.mouseRight);
        boolean rup = Core.input.keyRelease(KeyCode.mouseRight);

        boolean lclick = Core.input.keyTap(KeyCode.mouseLeft);

        ColorWheel w = EditorMain.ui.mainColorWheel;

        var iv = canvasToIdx(mx, my);
        int ix = iv.x, iy = iv.y;
        if(creature != null && ix >= 0 && ix < canvasRes && iy >= 0 && iy < canvasRes){
            var creature = EditorMain.creature;
            switch(tool){
                case brush -> {
                    int range = (int)(Mathf.dst(mx, my, lastMX, lastMY) * 2f) + 1;
                    for(int i = 0; i < range; i++){
                        float fin = i / (float)range;
                        float fx = Mathf.lerp(mx, lastMX, fin);
                        float fy = Mathf.lerp(my, lastMY, fin);
                        var iv2 = canvasToIdx(fx, fy);

                        int bx = iv2.x;
                        int by = iv2.y;

                        if(ldown){
                            putCellSized(bx, by, true);
                            if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                                var mir = getMirror(bx, by, orientation);
                                int mix = mir.x, miy = mir.y;
                                if(mix != bx || miy != by){
                                    putCellSized(mix, miy, true);
                                }
                            }
                        }else if(rdown && !paintMode){
                            putCellSized(bx, by, false);
                            if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                                var mir = getMirror(bx, by, orientation);
                                int mix = mir.x, miy = mir.y;
                                if(mix != bx || miy != by){
                                    putCellSized(mix, miy, false);
                                }
                            }
                        }
                    }
                    if((lup || (rup && !paintMode)) && undo.active){
                        undo.flush();
                        undo.active = false;
                    }
                }
                case fill -> {
                    if(lclick){
                        Color cl = Tmp.c2.fromHsv(w.h, w.s, w.v).a(w.a);
                        Fill f = Fill.fill(creature, ix, iy, paintMode ? null : currentCell, cl);
                        f.start(ix, iy);
                        if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                            var mir = getMirror(ix, iy, orientation);
                            int mix = mir.x, miy = mir.y;
                            if(mix != ix || miy != iy){
                                f.start(mix, miy);
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
                    int range = (int)(Mathf.dst(mx, my, lastMX, lastMY) * 2f) + 1;
                    for(int i = 0; i < range; i++){
                        float fin = i / (float)range;
                        float fx = Mathf.lerp(mx, lastMX, fin);
                        float fy = Mathf.lerp(my, lastMY, fin);
                        var iv2 = canvasToIdx(fx, fy);

                        int bx = iv2.x;
                        int by = iv2.y;
                        if(ldown && !paintMode){
                            putCellSized(bx, by, false);
                            if(mirror && mirrors[Mathf.mod(orientation, mirrors.length)] != null){
                                var mir = getMirror(bx, by, orientation);
                                int mix = mir.x, miy = mir.y;
                                if(mix != bx || miy != by){
                                    putCellSized(mix, miy, false);
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
        return Mathf.clamp(x, 0, canvasRes - 1) + Mathf.clamp(y, 0, canvasRes - 1) * canvasRes;
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
        float offX = ((int)(ret.y * canvasRes) - hRes) / (canvasRes * 2f);
        float wx = ret.x * canvasRes, wy = ret.y * canvasRes;
        ret.x -= offX;
        ret.scl(canvasRes);
        v.set(ret).sub((int)ret.x, (int)ret.y).sub(0.5f, 0.5f);
        
        float lx = 0.5f - Math.max(0f, Math.abs(v.x) - 0.25f) * 0.577350f * 1.155501302f;
        float ly = Math.abs(v.y);

        if(ly > lx){
            if(v.y > 0f){
                float ox = (((int)(wy + 1f)) - hRes) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy + 1f;
            }else{
                float ox = (((int)(wy - 1f)) - hRes) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy - 1f;
            }
        }
        ret.x = (int)ret.x + 0.5f;
        ret.y = (int)ret.y + 0.5f;

        return ret;
    }

    public static Vec2 mouseToWorld(){
        float sw = Core.graphics.getWidth(), sh = Core.graphics.getHeight();
        float mx = ((Core.input.mouseX() / sw) * 2f) - 1f;
        float my = ((Core.input.mouseY() / sh) * 2f) - 1f;

        ret.set(mx, my);
        var proj = renderer.getProj().inv();
        ret.mul(proj);
        //ret.x -= (sw / 2f);
        //ret.y -= (sh / 2f);

        return ret;
    }
    
    public static Vec2 worldToCanvas(float x, float y){
        /*
        float nx = (x - hRes) - 1, ny = (y - hRes) - 1;
        float g = 0.866025403f;
        //off.set(nx, ny);
        off.set(nx + 0.5f * ny, ny * g);
        */
        ret.x = x - (y * 0.5f) / 0.866025403f;
        ret.y = y / 0.866025403f;
        return ret;
    }
    public static Point2 canvasToIdx(float x, float y){
        int ix = (int)(x + hRes);
        int iy = (int)(y + hRes);
        int cx = ix, cy = iy;
        float near = 999999f;
        //vec2 loc = dir[i] + icor;
        //vec2 dif = v_texCoords * u_resolution - loc;
        //vec2 ndif = vec2(dif.x + dif.y * 0.5, dif.y * 0.866025403);
        for(int dx = 0; dx < 2; dx++){
            for(int dy = 0; dy < 2; dy++){
                float gx = (x + hRes) - (ix + dx);
                float gy = (y + hRes) - (iy + dy);
                //float nx = gx + gx * 0.5f;
                //float ny = gy * 0.866025403f;
                float nx = gx + gy * 0.5f;
                float ny = gy * 0.866025403f;
                float len = (nx * nx) + (ny * ny);
                if(len < near){
                    cx = (ix + dx);
                    cy = (iy + dy);
                    near = len;
                }
            }
        }
        tp2.x = cx;
        tp2.y = cy;
        return tp2;
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
        float offX = ((int)(ret.y * canvasRes) - hRes) / (canvasRes * 2f);
        float wx = ret.x * canvasRes, wy = ret.y * canvasRes;
        ret.x -= offX;
        ret.scl(canvasRes);
        v.set(ret).sub((int)ret.x, (int)ret.y).sub(0.5f, 0.5f);
        
        float lx = 0.5f - Math.max(0f, Math.abs(v.x) - 0.25f) * 0.577350f * 1.155501302f;
        float ly = Math.abs(v.y);

        if(ly > lx){
            if(v.y > 0f){
                float ox = (((int)(wy + 1f)) - hRes) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy + 1f;
            }else{
                float ox = (((int)(wy - 1f)) - hRes) * 0.5f;
                ret.x = wx - ox;
                ret.y = wy - 1f;
            }
        }
        ret.x = (int)ret.x + 0.5f;
        ret.y = (int)ret.y + 0.5f;

        return ret;
    }

    public static Point2 getMirror(int x, int y, int orientation){
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

    public enum ToolType{
        brush,
        eraser,
        fill,
        sampler
    }
}
