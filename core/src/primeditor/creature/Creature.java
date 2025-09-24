package primeditor.creature;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.Fill;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import primeditor.*;
import primeditor.creature.CellTypes.*;
import primeditor.graphics.*;
import primeditor.io.*;

import java.util.*;

import static primeditor.EditorMain.canvasRes;
import static primeditor.EditorMain.hRes;

public class Creature{
    public final EditorFrameBuffer cellCanvas, updateBuffer;
    private static EditorFrameBuffer tmp2;

    int version = 0;
    public byte orientation = 0;
    int comboCount = 0;
    //public Cell[] cells;
    public Seq<Cell> cells2;
    public CellChunk[] chunks = new CellChunk[EditorMain.chunks * EditorMain.chunks];
    public Cell[] cellGrid = new Cell[canvasRes * canvasRes];
    public Seq<Cell> changed = new Seq<>(true, 100, Cell.class);

    public Seq<ComboCellType> combos = new Seq<>();
    public IntMap<ComboCellType> comboMap = new IntMap<>();
    //Seq<Cell> tmpCells = new Seq<>();
    private static final Seq<CellChunk> tmpChunks = new Seq<>(true, 4, CellChunk.class);
    private static final OrderedSet<ComboCellType> comboTmp = new OrderedSet<>();
    private static final ObjectIntMap<CellType> comboSave = new ObjectIntMap<>();

    public Creature(){
        cellCanvas = new EditorFrameBuffer(canvasRes, canvasRes);
        updateBuffer = new EditorFrameBuffer(canvasRes, canvasRes);
        if(tmp2 == null){
            tmp2 = new EditorFrameBuffer(canvasRes, canvasRes);
        }
        for(int cx = 0; cx < EditorMain.chunks; cx++){
            for(int cy = 0; cy < EditorMain.chunks; cy++){
                chunks[cx + cy * EditorMain.chunks] = new CellChunk(cx, cy);
            }
        }
        CellTypes.reset();
        EditorMain.ui.selector.reset();
        comboTmp.clear();
        comboSave.clear();
    }
    public CellChunk getChunk(Cell cell){
        int cx = Mathf.clamp((cell.x + hRes) / (canvasRes / EditorMain.chunks), 0, EditorMain.chunks - 1);
        int cy = Mathf.clamp((cell.y + hRes) / (canvasRes / EditorMain.chunks), 0, EditorMain.chunks - 1);
        return chunks[cx + cy * EditorMain.chunks];
    }
    public Seq<CellChunk> getChunks(Cell cell){
        tmpChunks.clear();
        int cx = Mathf.clamp((cell.x + hRes) / (canvasRes / EditorMain.chunks), 0, EditorMain.chunks - 1);
        int cy = Mathf.clamp((cell.y + hRes) / (canvasRes / EditorMain.chunks), 0, EditorMain.chunks - 1);
        final int res = canvasRes / EditorMain.chunks;
        for(int ix = -1; ix <= 1; ix++){
            for(int iy = -1; iy <= 1; iy++){
                int icx = cx + ix;
                int icy = cy + iy;
                int dx = (cell.x + hRes) - (icx * res);
                int dy = (cell.y + hRes) - (icy * res);
                if(icx >= 0 && icx < EditorMain.chunks && icy >= 0 && icy < EditorMain.chunks && dx >= -1 && dy >= -1 && dx < res + 1 && dy < res + 1){
                    var chk = chunks[icx + icy * EditorMain.chunks];
                    //int dx = cell.x - (chk.x - hRes);
                    //int dy = cell.y - (chk.y - hRes);
                    tmpChunks.add(chk);
                }
            }
        }
        
        return tmpChunks;
    }

    public void draw(){
        if(!changed.isEmpty()){
            updateCells();
        }
        for(CellChunk ch : chunks){
            ch.update();
        }

        /*
        var s = Shaders.render;
        s.color = cellCanvas.getTextureAttachments().get(0);
        s.type = cellCanvas.getTextureAttachments().get(1);
        Draw.blit(s);
        */
        //Shaders.render2.render(chunks);
        for(CellChunk c : chunks){
            c.draw();
        }
    }
    public void drawTest(){
        Draw.blit(cellCanvas.getTextureAttachments().get(0), Shaders.test);
    }

    public void save(EditorWrites writes){
        //writes.i(version);

        if(EditorMain.saveVersion == 5){
            Version5.save(this, writes);
            return;
        }

        writes.i(4);

        int cc = 0;
        for(Cell c : cells2){
            if(c != null){
                cc++;
                if(c.type instanceof ComboCellType com){
                    com.addSet(comboTmp);
                }
            }
        }

        writes.i(cc);
        writes.i(orientation);

        int comboCount = 0;
        for(ComboCellType com : combos){
            if(comboTmp.contains(com)){
                //comboSave.put(0x80000000 + comboCount, com);
                comboSave.put(com, 0x80000000 + comboCount);
                comboCount++;
            }
        }

        //combo count
        writes.i(comboCount);
        for(ComboCellType com : combos){
            if(comboTmp.contains(com)){
                //writes.i(comboSave.containsKey());
                writes.i(comboSave.get(com.a, com.a.type));
                writes.i(comboSave.get(com.b, com.b.type));
            }
        }
        //writes.i(combos.size);

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
        comboTmp.clear();
        comboSave.clear();
    }

    public void processCombos(){
        for(ComboCellType c : combos){
            c.a = comboMap.containsKey(c.ia) ? comboMap.get(c.ia) : CellTypes.get(c.ia);
            c.b = comboMap.containsKey(c.ib) ? comboMap.get(c.ib) : CellTypes.get(c.ib);
        }
        for(ComboCellType c : combos){
            c.loadDescription();
            EditorMain.ui.selector.addCombo(c);
            Log.info(c.desc);
        }
    }
    public void addCombo(ComboCellType combo){
        comboMap.put(0x80000000 + combos.size, combo);
        combos.add(combo);
        EditorMain.ui.selector.addCombo(combo);
    }

    public void load(EditorReads reads){
        version = reads.i();

        if(version >= 5){
            cells2 = new Seq<>(false, (int)(200 * 1.25f), Cell.class);

            Version5.load(this, reads);
            reads.close();

            loadRender();
            return;
        }

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
            for(int i = 0; i < comboCount; i++){
                int id = 0x80000000 + i;
                int a = reads.i();
                int b = reads.i();
                /*
                CellType ac = CellTypes.get(a);
                CellType bc = CellTypes.get(b);
                String as = ac != CellTypes.unknown ? ac.name : Integer.toHexString(a);
                String bs = bc != CellTypes.unknown ? bc.name : Integer.toHexString(b);
                //reads.skip(8);
                Log.info("Cell " + Integer.toHexString(id) + ": " + as + ", " + bs);
                */

                ComboCellType cmb = new ComboCellType(id);
                cmb.ia = a;
                cmb.ib = b;
                combos.add(cmb);
                comboMap.put(id, cmb);
            }
        }
        processCombos();

        for(int i = 0; i < cellCount; i++){
            //var c = new Cell();
            int it = reads.i();
            var type = comboCount > 0 && comboMap.containsKey(it) ? comboMap.get(it) : CellTypes.get(it);
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
            int ix = cx + hRes, iy = cy + hRes;
            if(ix < 0 || ix >= canvasRes || iy < 0 || iy >= canvasRes){
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
            int p1 = c1.x + c1.y * canvasRes;
            int p2 = c2.x + c2.y * canvasRes;
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
                var chk = getChunks(c);
                for(CellChunk ch : chk){
                    ch.changed.add(c);
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
            //var ch = getChunk(c);
            //ch.changed.add(c);
            var chk = getChunks(c);
            for(CellChunk ch : chk){
                ch.changed.add(c);
            }
        }
        /*
        for(CellChunk ch : chunks){
            ch.update();
        }
        */
        changed.clear();

        /*
        if(dirty){
            var lb = Core.batch;
            Draw.flush();
            Core.batch = Renderer.gridBatch;
            Draw.proj().setOrtho(-hRes, -hRes, canvasRes, canvasRes);
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
        */

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
        /*
        Draw.flush();
        var lb = Core.batch;
        Core.batch = Renderer.gridBatch;
        Draw.proj().setOrtho(-hRes, -hRes, canvasRes, canvasRes);
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
        */

        for(Cell c : cells2){
            //CellChunk ch = getChunk(c);
            var chk = getChunks(c);
            //ch.changed.add(c);
            for(CellChunk ch : chk){
                ch.changed.add(c);
            }
        }
        Draw.flush();
        var lb2 = Core.batch;
        Core.batch = Renderer.gridBatch;
        Draw.flush();
        for(CellChunk ch : chunks){
            Draw.proj().setOrtho((-hRes + ch.x) - 1, (-hRes + ch.y) - 1, CellChunk.res + 2, CellChunk.res + 2);
            ch.texture.begin(Color.clear);
            for(Cell c : ch.changed){
                Draw.color(c.r, c.g, c.b, c.a);
                Renderer.gridBatch.setType(c.type.getType());
                Fill.rect(c.x + 0.5f, c.y + 0.5f, 1f, 1f);
            }
            ch.texture.end();
            ch.changed.clear();
            Draw.flush();
        }
        Core.batch = lb2;
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
        //cellCanvas.dispose();
        updateBuffer.dispose();
        for(CellChunk ch : chunks){
            ch.dispose();
        }
    }

    public static class Cell{
        public CellType type;

        public float r, g, b, a;

        public int x, y;

        public int arrayIdx;
        public boolean deleted, added;
        boolean changed;

        int getGX(){
            return x + hRes;
        }
        int getGY(){
            return y + hRes;
        }
        public int getGPos(){
            return (getGX() % canvasRes) + ((y * canvasRes) + hRes * canvasRes);
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
