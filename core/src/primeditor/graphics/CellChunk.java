package primeditor.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import primeditor.*;
import primeditor.creature.*;
import primeditor.creature.Creature.*;

import static primeditor.EditorMain.hRes;

public class CellChunk{
    public int x, y;
    public int ix, iy;
    public EditorFrameBuffer texture;
    public Seq<Cell> changed = new Seq<>();
    public static int res = EditorMain.canvasRes / EditorMain.chunks;
    static EditorFrameBuffer update, upload;
    final static Vec2 off = new Vec2();

    public CellChunk(int x, int y){
        //cellCanvas = new EditorFrameBuffer(canvasRes, canvasRes);
        texture = new EditorFrameBuffer(res + 2, res + 2);
        if(update == null){
            update = new EditorFrameBuffer(res + 2, res + 2);
            upload = new EditorFrameBuffer(res + 2, res + 2);
        }
        ix = x;
        iy = y;
        this.x = x * res;
        this.y = y * res;
    }

    public void draw(){
        float[] verts = GUtils.verts;

        trans(x - 1, y - 1);
        //trans(x, y);
        verts[0] = off.x;
        verts[1] = off.y;
        verts[2] = Color.whiteFloatBits;
        verts[3] = 0f;
        verts[4] = 0f;
        verts[5] = Color.clearFloatBits;

        trans(x + res + 1, y - 1);
        //trans(x + res + 1, y);
        verts[6] = off.x;
        verts[7] = off.y;
        verts[8] = Color.whiteFloatBits;
        verts[9] = 1f;
        verts[10] = 0f;
        verts[11] = Color.clearFloatBits;

        trans(x + res + 1, y + res + 1);
        //trans(x + res + 1, y + res + 1);
        verts[12] = off.x;
        verts[13] = off.y;
        verts[14] = Color.whiteFloatBits;
        verts[15] = 1f;
        verts[16] = 1f;
        verts[17] = Color.clearFloatBits;

        trans(x - 1, y + res + 1);
        //trans(x, y + res + 1);
        verts[18] = off.x;
        verts[19] = off.y;
        verts[20] = Color.whiteFloatBits;
        verts[21] = 0f;
        verts[22] = 1f;
        verts[23] = Color.clearFloatBits;

        Draw.flush();
        var sh = Shaders.main;
        sh.color = texture.getTextureAttachments().get(0);
        sh.type = texture.getTextureAttachments().get(1);

        Draw.shader(sh);
        Draw.vert(CellTypes.unknown.region.texture, verts, 0, 24);
        Draw.shader();
        Draw.flush();
    }
    void trans(float x, float y){
        float nx = (x - hRes) - 1, ny = (y - hRes) - 1;
        float g = 0.866025403f;
        //off.set(nx, ny);
        off.set(nx + 0.5f * ny, ny * g);
    }

    public void dispose(){
        texture.dispose();
    }

    public void update(){
        if(changed.isEmpty()) return;

        var lb = Core.batch;
        Draw.flush();
        update.begin(Color.clear);
        Core.batch = Renderer.gridBatch;
        //Draw.proj().setOrtho((-hRes + x) - 1, (-hRes + y) - 1, res + 2, res + 2);
        Draw.proj().setOrtho(-1, -1, res + 2, res + 2);
        int rx = -hRes + x;
        int ry = -hRes + y;

        for(Cell c : changed){
            if(c.deleted){
                Renderer.gridBatch.setTypeDelete();
            }else{
                Draw.color(c.r, c.g, c.b, c.a);
                Renderer.gridBatch.setType(c.type.getType());
            }
            Fill.rect((c.x - rx) + 0.5f, (c.y - ry) + 0.5f, 1f, 1f);
        }

        Draw.flush();
        update.end();
        Core.batch = lb;

        var srcCol = texture.getTextureAttachments().get(0);
        var srcTyp = texture.getTextureAttachments().get(1);
        var dstCol = update.getTextureAttachments().get(0);
        var dstTyp = update.getTextureAttachments().get(1);

        upload.begin(Color.clear);

        var upd = Shaders.update;

        upd.color = srcCol;
        upd.type = srcTyp;
        upd.changeColor = dstCol;
        upd.changeType = dstTyp;
        GUtils.blit(upd);

        upload.end();

        var upl = Shaders.upload;
        upl.color = upload.getTextureAttachments().get(0);
        upl.type = upload.getTextureAttachments().get(1);

        texture.begin(Color.clear);
        GUtils.blit(upl);
        texture.end();

        changed.clear();
    }
}
