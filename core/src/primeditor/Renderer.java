package primeditor;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import primeditor.Control.*;
import primeditor.creature.*;
import primeditor.creature.Creature.*;
import primeditor.graphics.*;

public class Renderer implements ApplicationListener{
    public static float cx = 0f, cy = 0f, cr = 0f, cs = 0.01f;
    public static boolean showtype = false;
    public static BlankBatch gridBatch;

    float oscTime = 0f;
    Rect creatureBounds = new Rect();

    private static final IntSet occupied = new IntSet();

    Renderer(){
        Core.atlas = new TextureAtlas(Core.files.internal("sprites/sprites.aatls"));
        Core.atlas.setErrorRegion("white");
        Core.batch = new SpriteBatch();
        gridBatch = new BlankBatch(4096);
    }

    public void screenshot(){
        Creature cr = EditorMain.creature;

        float minX = 4000f, minY = 4000f;
        float maxX = -4000f, maxY = -4000f;

        float cos = Mathf.cosDeg(Renderer.cr), sin = Mathf.sinDeg(Renderer.cr);
        //testSeq.clear();

        for(Cell c : cr.cells2){
            //Fill.poly(x + 0.5f + y / 2f, (y + 0.5f) / 1.155501302f, 6, 0.5f, 90f);
            //float bx = c.x * cos - c.y * sin;
            //float by = c.y * cos + c.x * sin;
            float x = c.x + 0.5f + c.y / 2f;
            float y = (c.y + 0.5f) / 1.155501302f;
            //float x = c.x, y = c.y;
            //float x = bx + 0.5f + by / 2f;
            //float y = (by + 0.5f) / 1.155501302f;

            float tx = x * cos - y * sin;
            float ty = y * cos + x * sin;
            minX = Math.min(tx, minX);
            minY = Math.min(ty, minY);
            maxX = Math.max(tx, maxX);
            maxY = Math.max(ty, maxY);
            //testSeq.add(tx, ty);
        }

        creatureBounds.set(minX, minY, maxX - minX, maxY - minY);
        creatureBounds.grow(10f);

        int cwidth = (int)(creatureBounds.width) + 1, cheight = (int)(creatureBounds.height) + 1;
        int imgScl = 4;
        float mx = creatureBounds.x + creatureBounds.width / 2f;
        float my = creatureBounds.y + creatureBounds.height / 2f;

        FrameBuffer buffer = new FrameBuffer(cwidth * imgScl, cheight * imgScl);
        var s = Shaders.render;
        s.color = cr.cellCanvas.getTextureAttachments().get(0);
        s.type = cr.cellCanvas.getTextureAttachments().get(1);
        s.screenshotMode = true;
        s.scWidth = cwidth;
        s.scHeight = cheight;
        s.scX = (-mx / (cwidth));
        s.scY = (-my / (cheight));
        //s.scX = (mx) / (cwidth / 2f);
        //s.scY = (my) / (cheight / 2f);

        buffer.begin(Color.clear);
        Draw.blit(s);
        //byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, w, h, true);
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, cwidth * imgScl, cheight * imgScl, true);
        buffer.end();
        buffer.dispose();

        //Pixmap fullPixmap = new Pixmap(w, h);
        //Buffers.copy(lines, 0, fullPixmap.pixels, lines.length);
        //Fi file = screenshotDirectory.child("screenshot-" + Time.millis() + ".png");
        //PixmapIO.writePng(file, fullPixmap);
        //fullPixmap.dispose();
        //app.post(() -> ui.showInfoFade(Core.bundle.format("screenshot", file.toString())));
        Pixmap full = new Pixmap(cwidth * imgScl, cheight * imgScl);
        Buffers.copy(lines, 0, full.pixels, lines.length);
        //Fi dd = Core.settings.getDataDirectory();
        Fi file = Core.settings.getDataDirectory().child("images/").child("creature-" + Time.millis() + ".png");
        PixmapIO.writePng(file, full);
        full.dispose();

        s.screenshotMode = false;

        Log.info("ScreenShot");
    }

    @Override
    public void update(){
        //updateControls();
        oscTime = Mathf.mod(oscTime + 5f, 360f);

        Draw.sort(false);
        Draw.reset();
        Draw.color(0.05f, 0.07f, 0.05f, 1f);
        //Draw.rect();
        Draw.proj().setOrtho(0f, 0f, 1.5f, 1.5f);
        Fill.crect(-1f, -1f, 3f, 3f);
        Draw.reset();

        if(EditorMain.creature != null){
            Draw.flush();
            if(!Control.test){
                EditorMain.creature.draw();
                Draw.flush();
                /*
                Mat pr = Draw.proj();
                float wd = Core.graphics.getWidth(), ht = Core.graphics.getHeight();
                float res = Math.min(wd, ht);
                wd /= res;
                ht /= res;
                wd /= cs;
                ht /= cs;
                pr.setOrtho(-wd / 2f + cx, -ht / 2f + cy, wd, ht);
                pr.rotate(cr);
                Draw.color(Color.white, 0.5f);
                drawCursor();
                */
                drawCursor();
                drawEffects();
                Draw.reset();
            }else{
                EditorMain.creature.drawTest();
                Draw.flush();
                float wd = Core.graphics.getWidth(), ht = Core.graphics.getHeight();
                float res = Math.min(wd, ht);
                wd /= res;
                ht /= res;
                Draw.proj().setOrtho(-wd, -ht, wd * 2f, ht * 2f);
                Draw.color(Color.white);
                Lines.stroke(0.01f);
                Lines.lineAngle(0f, 0f, Control.directions[Mathf.mod(Control.orientation, Control.directions.length)], 1f, false);
            }
        }
        Draw.flush();
        Draw.reset();
    }

    void drawEffects(){
        if(!Control.mirror) return;
        Draw.color(Color.green, Mathf.lerp(0.05f, 0.15f, Mathf.sinDeg(oscTime) * 0.5f + 0.5f));

        hex2(0, 0);
        Lines.stroke(cs * 5f);
        Lines.lineAngleCenter(0.5f, 0.5f / 1.155501302f, -cr + 90f, 9999f, false);
    }

    void drawCursor(){
        //Vec2 v = Control.mouseToCanvas();
        //float size = (1f / 2048f);
        //float size = 1f;
        //Fill.poly(v.x / 2048f, v.y / 2048f, 6, size);
        Mat pr = Draw.proj();
        float wd = Core.graphics.getWidth(), ht = Core.graphics.getHeight();
        float res = Math.min(wd, ht);
        wd /= res;
        ht /= res;
        float trnx = cx * wd * -2048f, trny = cy * ht * -2048f;
        pr.setOrtho((-1024f) * cs * wd + trnx, (-1024f) * cs * ht + trny, 2048f * cs * wd, 2048f * cs * ht);
        pr.rotate(cr);
        Draw.color(Color.white, Mathf.lerp(0.15f, 0.4f, Mathf.sinDeg(oscTime) * 0.5f + 0.5f));

        Vec2 v = Control.mouseToCanvas();
        int ix = (int)(v.x) - 1024, iy = (int)(v.y) - 1024;
        //Fill.poly(vx, vy, 6, 1f, 90f);
        //hex(vx, vy);
        int[] brushSet = Control.brushes[Control.brushSize];
        if(Control.tool == ToolType.fill || Control.tool == ToolType.sampler){
            brushSet = Control.brushes[0];
        }
        for(int i = 0; i < brushSet.length; i += 2){
            hex(ix + brushSet[i], iy + brushSet[i + 1]);
            int px = ix + 1024 + brushSet[i];
            int py = iy + 1024 + brushSet[i + 1];
            occupied.add((px & 0xffff) | ((py & 0xffff) << 16));
        }
        if(Control.mirror && Control.tool != ToolType.sampler && Control.mirrors[Mathf.mod(Control.orientation, Control.mirrors.length)] != null){
            Point2 mir = Control.getMirror(ix + 1024, iy + 1024, Control.orientation);
            for(int i = 0; i < brushSet.length; i += 2){
                int px = mir.x + brushSet[i];
                int py = mir.y + brushSet[i + 1];
                //occupied.add((px & 0xffff) | ((py & 0xffff) << 16));
                if(occupied.contains((px & 0xffff) | ((py & 0xffff) << 16))) continue;
                hex(mir.x - 1024 + brushSet[i], mir.y - 1024 + brushSet[i + 1]);
            }
        }
        occupied.clear();

        //hex(ix, iy);
    }
    void hex(float x, float y){
        //Fill.poly(x + (int)(y - 0.5f) / 2f, y / 1.155501302f, 6, 0.5f, 90f);
        Fill.poly(x + 0.5f + (y / 2f), (y + 0.5f) / 1.155501302f, 6, 0.5f, 90f);
    }
    void hex2(float x, float y){
        //Fill.poly(x + (int)(y - 0.5f) / 2f, y / 1.155501302f, 6, 0.5f, 90f);
        Fill.poly(x + 0.5f + (y / 2f), (y + 0.5f) / 1.155501302f, 6, 0.25f, 90f);
    }
}
