package primeditor.creature;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.Fill;
import arc.struct.*;
import arc.util.*;
import primeditor.*;
import primeditor.creature.CellTypes.*;
import primeditor.graphics.*;
import primeditor.io.*;
import primeditor.utils.*;

import java.util.*;

public class Creature{
    public final EditorFrameBuffer cellCanvas, updateBuffer;
    private static EditorFrameBuffer tmp2;

    int version = 0;
    public byte orientation = 0;
    int comboCount = 0;
    //public Cell[] cells;
    public Seq<Cell> cells2;
    public Cell[] cellGrid = new Cell[2048 * 2048];
    public Seq<Cell> changed = new Seq<>(true, 100, Cell.class);
    //Seq<Cell> tmpCells = new Seq<>();

    public Creature(){
        cellCanvas = new EditorFrameBuffer(2048, 2048);
        updateBuffer = new EditorFrameBuffer(2048, 2048);
        if(tmp2 == null){
            tmp2 = new EditorFrameBuffer(2048, 2048);
        }
    }

    public void draw(){
        if(!changed.isEmpty()){
            updateCells();
        }

        var s = Shaders.render;
        s.color = cellCanvas.getTextureAttachments().get(0);
        s.type = cellCanvas.getTextureAttachments().get(1);
        Draw.blit(s);
    }
    public void drawTest(){
        Draw.blit(cellCanvas.getTextureAttachments().get(0), Shaders.test);
    }

    public void save(EditorWrites writes){
        //writes.i(version);
        writes.i(4);

        int cc = 0;
        for(Cell c : cells2){
            if(c != null){
                cc++;
            }
        }

        writes.i(cc);
        writes.i(orientation);
        //combo count
        writes.i(0);

        for(Cell c : cells2){
            if(c != null){
                int type = c.type == CellTypes.unknown ? CellTypes.def.type : c.type.type;
                writes.i(type);

                writes.f(c.r);
                writes.f(c.g);
                writes.f(c.b);
                writes.f(c.a);

                writes.i(c.x);
                writes.i(c.y);
            }
        }
        writes.close();
    }

    public void load(EditorReads reads){
        version = reads.i();
        int cellCount = reads.i();
        //cells = new Cell[cellCount];
        //cells2 = new Seq<>(false, (int)(cellCount * 1.25f), Cell.class);
        cells2 = new Seq<>(false, (int)(cellCount * 1.25f), Cell.class);

        orientation = (byte)(reads.i());
        if(version >= 4){
            comboCount = reads.i();
        }
        //Log.info("Cell Count:" + cellCount);
        //Log.info("R1:" + Control.orientation + " R2:" + orientation);
        Control.setOrientation(orientation - 3);

        if(comboCount > 0){
            //combo unsupported for now.
            for(int i = 0; i < comboCount; i++){
                reads.skip(8);
            }
        }
        for(int i = 0; i < cellCount; i++){
            //var c = new Cell();
            var type = CellTypes.get(reads.i());
            //c.type = CellTypes.get(reads.i());
            //c.arrayIdx = i;
            //c.arrayIdx = cells2.size;

            float r = reads.f();
            float g = reads.f();
            float b = reads.f();
            float a = reads.f();
            //c.r = r;
            //c.g = g;
            //c.b = b;
            //c.a = a;
            //c.rgba = Tmp.c1.set(r, g, b, a).toFloatBits();
            //Log.info(c.type.name + " r:" + c.r + " g:" + c.g + " b:" + c.b);

            //c.x = reads.i();
            //c.y = reads.i();
            int cx = reads.i();
            int cy = reads.i();
            if(version <= 2) reads.i();//????
            int ix = cx + 1024, iy = cy + 1024;
            if(ix < 0 || ix >= 2048 || iy < 0 || iy >= 2048){
                continue;
            }
            var c = new Cell();
            c.type = type;
            c.arrayIdx = cells2.size;
            c.x = cx;
            c.y = cy;
            c.r = r;
            c.g = g;
            c.b = b;
            c.a = a;
            //cells[i] = c;
            cells2.add(c);
            cellGrid[c.getGPos()] = c;
        }
        reads.close();

        loadRender();
    }

    void sortCells(){
        cells2.sort((c1, c2) -> {
            int p1 = c1.x + c1.y * 2048;
            int p2 = c2.x + c2.y * 2048;
            return Integer.compare(p1, p2);
        });
        int i = 0;
        for(Cell c : cells2){
            c.arrayIdx = i;
            i++;
        }
    }

    void updateCells(){
        boolean dirty = !changed.isEmpty();

        //boolean del = false;
        //boolean add = false;

        //tmpCells.clear();
        for(int i = 0; i < changed.size; i++){
            Cell c = changed.items[i];
            int id = c.arrayIdx;
            if(c.deleted){
                cellGrid[c.getGPos()] = null;
                cells2.remove(id);
                Cell c2 = cells2.items[id];
                if(c2 != null){
                    c2.arrayIdx = id;
                }
                continue;
            }
            if(c.added){
                c.arrayIdx = cells2.size;
                cellGrid[c.getGPos()] = c;
                cells2.add(c);
                c.added = false;
                //add = true;
            }
        }
        /*
        for(Cell c : changed){
            if(c.deleted){
                cells[c.arrayIdx] = null;
                cellGrid[c.getGPos()] = null;
                del = true;
            }
            if(c.added){
                tmpCells.add(c);
                add = true;
                c.added = false;
            }
        }
        if(del){
            int cl = cells.length;
            int i2 = 0;
            int i = 0;
            while(i < cl){
                if(cells[i] != null && !cells[i].deleted){
                    cells[i2] = cells[i];
                    cells[i2].arrayIdx = i2;
                    i2++;
                }
                i++;
            }

            int nl = i2 + tmpCells.size;
            cells = Arrays.copyOf(cells, nl);
            if(add){
                for(Cell c : tmpCells){
                    cells[i2] = c;
                    cellGrid[c.getGPos()] = c;
                    c.arrayIdx = i2;
                    i2++;
                }
                tmpCells.clear();
            }
        }else if(add){
            int nl = cells.length + tmpCells.size;
            int i = cells.length;
            cells = Arrays.copyOf(cells, nl);
            for(Cell c : tmpCells){
                cells[i] = c;
                cellGrid[c.getGPos()] = c;
                c.arrayIdx = i;
                i++;
            }
            tmpCells.clear();
        }
        */

        if(dirty){
            var lb = Core.batch;
            Draw.flush();
            Core.batch = Renderer.gridBatch;
            Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);
            updateBuffer.begin(Color.clear);
            //Gl.clear(depthbufferHandle != 0 ? Gl.colorBufferBit | Gl.depthBufferBit : Gl.colorBufferBit);
            for(Cell c : changed){
                if(c.deleted){
                    Renderer.gridBatch.setTypeDelete();
                }else{
                    Draw.color(c.r, c.g, c.b, c.a);
                    Renderer.gridBatch.setType(c.type.getType());
                }
                Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
            }

            Draw.flush();
            updateBuffer.end();
            Core.batch = lb;

            var srcCol = cellCanvas.getTextureAttachments().get(0);
            var srcTyp = cellCanvas.getTextureAttachments().get(1);
            var dstCol = updateBuffer.getTextureAttachments().get(0);
            var dstTyp = updateBuffer.getTextureAttachments().get(1);

            //doesnt render
            tmp2.begin(Color.clear);

            var upd = Shaders.update;

            upd.color = srcCol;
            upd.type = srcTyp;
            upd.changeColor = dstCol;
            upd.changeType = dstTyp;
            GUtils.blit(upd);

            tmp2.end();

            var upl = Shaders.upload;
            upl.color = tmp2.getTextureAttachments().get(0);
            upl.type = tmp2.getTextureAttachments().get(1);

            cellCanvas.begin(Color.clear);
            GUtils.blit(upl);
            cellCanvas.end();
        }

        changed.clear();

        /*
        if(dirty){
            tmp.begin(Color.clear);

            var ts = Shaders.ts;
            var cs = Shaders.cs;
            ts.t1 = type.getTexture();
            ts.t2 = drawType.getTexture();
            Draw.blit(ts);
            tmp.end();

            type.begin(Color.clear);
            Draw.blit(tmp.getTexture(), Shaders.screen);
            type.end();

            tmp.begin(Color.clear);
            cs.t1 = color.getTexture();
            cs.t2 = drawType.getTexture();
            cs.t3 = drawColor.getTexture();
            Draw.blit(cs);
            tmp.end();

            color.begin(Color.clear);
            Draw.blit(tmp.getTexture(), Shaders.screen);
            color.end();
        }
        */

        /*
        Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);
        drawType.begin(Color.clear);
        for(Cell c : changed){
            if(c.deleted){
                Draw.color(0f, 0f, 0f, 0.5f);
            }else{
                Draw.color(c.type.getType());
            }
            Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
        }
        Draw.flush();
        drawType.end();

        var lb = Core.batch;
        Core.batch = Renderer.gridBatch;
        Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);
        drawColor.begin(Color.clear);
        for(Cell c : changed){
            if(!c.deleted){
                Draw.color(c.r, c.g, c.b, c.a);
            }
            Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
        }
        Draw.flush();
        drawColor.end();
        Core.batch = lb;

        if(dirty){
            tmp.begin(Color.clear);

            var ts = Shaders.ts;
            var cs = Shaders.cs;
            ts.t1 = type.getTexture();
            ts.t2 = drawType.getTexture();
            Draw.blit(ts);
            tmp.end();

            type.begin(Color.clear);
            Draw.blit(tmp.getTexture(), Shaders.screen);
            type.end();

            tmp.begin(Color.clear);
            cs.t1 = color.getTexture();
            cs.t2 = drawType.getTexture();
            cs.t3 = drawColor.getTexture();
            Draw.blit(cs);
            tmp.end();

            color.begin(Color.clear);
            Draw.blit(tmp.getTexture(), Shaders.screen);
            color.end();
        }
        */
    }

    public void loadRender(){
        //Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);
        Draw.flush();
        var lb = Core.batch;
        Core.batch = Renderer.gridBatch;
        Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);
        cellCanvas.begin();
        Core.graphics.clear(Color.clear);

        for(Cell c : cells2){
            Draw.color(c.r, c.g, c.b, c.a);
            Renderer.gridBatch.setType(c.type.getType());
            Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
        }

        Draw.flush();
        cellCanvas.end();
        Core.batch = lb;
        /*
        var lb = Core.batch;
        Core.batch = Renderer.gridBatch;
        Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);
        color.begin();
        Core.graphics.clear(Color.clear);

        for(Cell c : cells){
            Draw.color(c.r, c.g, c.b, c.a);
            Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
        }

        Draw.flush();
        Draw.color();
        color.end();
        Core.batch = lb;
        Draw.proj().setOrtho(-1024f, -1024f, 2048, 2048);

        type.begin();
        Core.graphics.clear(Color.clear);

        for(Cell c : cells){
            Draw.color(c.type.getType());
            Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
        }

        Draw.color();
        type.end();
        */
    }

    public void dispose(){
        /*
        color.dispose();
        type.dispose();
        drawColor.dispose();
        drawType.dispose();
        */
        cellCanvas.dispose();
        updateBuffer.dispose();
    }

    public static class Cell{
        public CellType type;

        public float r, g, b, a;

        public int x, y;

        int arrayIdx;
        public boolean deleted, added;
        boolean changed;

        int getGX(){
            return x + 1024;
        }
        int getGY(){
            return y + 1024;
        }
        public int getGPos(){
            return (getGX() % 2048) + ((y * 2048) + 1024 * 2048);
        }

        @Override
        public boolean equals(Object o){
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            Cell cell = (Cell)o;
            return (cell.r != r) && (cell.g != g) && (cell.b != b) && (cell.a != a) && x == cell.x && y == cell.y && (type != cell.type);
        }

        @Override
        public int hashCode(){
            return Objects.hash(type, r, g, b, a, x, y);
        }
    }
}
