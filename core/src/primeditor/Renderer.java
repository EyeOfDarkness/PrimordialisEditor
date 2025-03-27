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

import static primeditor.EditorMain.*;

public class Renderer implements ApplicationListener{
    //public static float cx = 0f, cy = 0f, cr = 0f, cs = 0.01f;
    public static float cx = 0f, cy = 0f, cr = 0f, cs = 50f;
    public static boolean showtype = false;
    public static BlankBatch gridBatch;

    float oscTime = 0f;
    Rect creatureBounds = new Rect();

    private static final IntSet occupied = new IntSet();
    private static final Mat camera = new Mat();
    private static final Rect tmpr = new Rect();

    Renderer(){
        Core.atlas = new TextureAtlas(Core.files.internal("sprites/sprites.aatls"));
        Core.atlas.setErrorRegion("white");
        Core.batch = new SpriteBatch();
        gridBatch = new BlankBatch(4096);
    }

    public Mat getProj(){
        //mat.setOrtho(-width / 2f, -height / 2f, width, height).rotate(rotation).translate(position.x, position.y);
        //camera.setOrtho()
        float w = Core.graphics.getWidth(), h = Core.graphics.getHeight();
        float as = Math.max(w, h);
        float w2 = (w / as) * cs, h2 = (h / as) * cs;
        //camera.setOrtho(-w2 / 2f, -h2 / 2f, w2, h2).rotate(cr).translate(cx, cy);
        //camera.setOrtho(0f, 0f, w2, h2).rotate(cr).translate(cx - w2 / 2f, cy - h2 / 2f);
        //camera.setOrtho(-w2 / 2f + cx, -h2 / 2f + cy, w2, h2);
        camera.idt().setOrtho(-w2 / 2f - cx, -h2 / 2f - cy, w2, h2).rotate(cr);

        return camera;
    }
    Mat screenProj(float x, float y, float width, float height){
        //camera.setOrtho(-width / 2f, -height / 2f, width, height).rotate(cr).translate(x, y);
        //camera.setOrtho(0f, 0f, width, height).rotate(cr).translate(x - width / 2f, y - height / 2f);
        camera.idt().setOrtho(-width / 2f + x, -height / 2f + y, width, height).rotate(cr);

        return camera;
    }
    Mat screenProj2(float x, float y, float width, float height){
        //camera.setOrtho(-width / 2f, -height / 2f, width, height).rotate(cr).translate(x, y);
        //camera.setOrtho(0f, 0f, width, height).rotate(cr).translate(x - width / 2f, y - height / 2f);
        camera.idt().setOrtho(x, y, width, height).rotate(cr);

        return camera;
    }

    public void screenshot(){
        Creature cr = EditorMain.creature;

        float minX = 800000f, minY = 800000f;
        float maxX = -800000f, maxY = -800000f;

        float cos = Mathf.cosDeg(Renderer.cr), sin = Mathf.sinDeg(Renderer.cr);
        //testSeq.clear();

        for(Cell c : cr.cells2){
            //Fill.poly(x + 0.5f + y / 2f, (y + 0.5f) / 1.155501302f, 6, 0.5f, 90f);
            //float bx = c.x * cos - c.y * sin;
            //float by = c.y * cos + c.x * sin;
            float x = c.x + 0.5f + c.y / 2f;
            //float y = (c.y + 0.5f) / 1.155501302f;
            float y = (c.y + 0.5f) * 0.866025403f;
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
        
        //float cos2 = Mathf.cosDeg(-Renderer.cr), sin2 = Mathf.sinDeg(-Renderer.cr);
        float mx = creatureBounds.x + creatureBounds.width / 2f;
        float my = creatureBounds.y + creatureBounds.height / 2f;
        //float ax = mx * cos2 - my * sin2;
        //float ay = my * cos2 + mx * sin2;
        //mx = ax;
        //my = ay;

        int cwidth = (int)(creatureBounds.width) + 1, cheight = (int)(creatureBounds.height) + 1;
        int imgScl = 4;
        int sampler = 512;
        int sampler2 = sampler / imgScl;

        int tw = cwidth * imgScl, th = cheight * imgScl;
        Pixmap full = new Pixmap(tw, th);
        FrameBuffer buffer = new FrameBuffer(sampler, sampler);
        //int gw = (tw / sampler) + 1, gh = (th / sampler) + 1;
        int gw = (tw / sampler) + 1, gh = (th / sampler) + 1;

        Shaders.main.screenshotMode = true;
        xaxis:
        for(int ix = -40; ix <= 40; ix++){
            yaxis:
            for(int iy = -40; iy <= 40; iy++){
                //tmpr
                //int iix = ix - (gw / 2);
                //float bx = ix * sampler2 + mx * ((sampler2 * 2.5f) / (cwidth / 2f)), by = iy * sampler2 + my * ((sampler2 * 2.5f) / (cheight / 2f));
                //float bx = ix * sampler2 + mx * (sampler / ((float)tw)), by = iy * sampler2 + my * (sampler / ((float)th));
                float bx = ix * sampler2 + mx, by = iy * sampler2 + my;
                //float bx = ix * sampler2, by = iy * sampler2;
                tmpr.setCentered(bx, by, sampler2, sampler2);
                if((tmpr.x + tmpr.width) < creatureBounds.x || tmpr.x > (creatureBounds.x + creatureBounds.width)){
                    continue xaxis;
                }
                if((tmpr.y + tmpr.height) < creatureBounds.y || tmpr.y > (creatureBounds.y + creatureBounds.height)){
                    continue yaxis;
                }
                buffer.begin(Color.clear);
                Draw.proj().set(screenProj(bx, by, sampler2, sampler2));
                cr.draw();
                Draw.flush();
                byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, sampler, sampler, false);
                buffer.end();

                for(int jx = 0; jx < sampler; jx++){
                    for(int jy = 0; jy < sampler; jy++){
                        //convert mx, my to pixel space
                        int px = ((ix * sampler + (tw / 2)) + (jx - sampler / 2));
                        //int py = (ix * sampler + tw / 2) + (jx - sampler / 2);
                        //int py = ((gh - 1) - iy) * sampler + ((sampler - 1) - jy);
                        int py = ((-iy * sampler + (th / 2)) - (jy - sampler / 2));

                        if(px < 0 || px >= tw || py < 0 || py >= th){
                            continue;
                        }

                        int idx1 = (jx + jy * sampler) * 4;
                        int idx2 = (px + py * tw) * 4;

                        byte r = lines[idx1], g = lines[idx1 + 1], b = lines[idx1 + 2], a = lines[idx1 + 3];
                        if(a == 0) continue;
                        full.pixels.put(idx2, r);
                        full.pixels.put(idx2 + 1, g);
                        full.pixels.put(idx2 + 2, b);
                        full.pixels.put(idx2 + 3, a);
                    }
                }
            }
        }
        buffer.dispose();
        Shaders.main.screenshotMode = false;

        Fi file = Core.settings.getDataDirectory().child("images/").child("creature-" + Time.millis() + ".png");
        PixmapIO.writePng(file, full);
        full.dispose();

        /*
        float mx = creatureBounds.x + creatureBounds.width / 2f;
        float my = creatureBounds.y + creatureBounds.height / 2f;
        Shaders.main.screenshotMode = true;

        FrameBuffer buffer = new FrameBuffer(cwidth * imgScl, cheight * imgScl);

        buffer.begin(Color.clear);
        Draw.proj().set(screenProj(mx, my, cwidth, cheight));
        cr.draw();
        byte[] lines = ScreenUtils.getFrameBufferPixels(0, 0, cwidth * imgScl, cheight * imgScl, true);
        buffer.end();
        buffer.dispose();
        Shaders.main.screenshotMode = false;

        Pixmap full = new Pixmap(cwidth * imgScl, cheight * imgScl);
        Buffers.copy(lines, 0, full.pixels, lines.length);
        //Fi dd = Core.settings.getDataDirectory();
        Fi file = Core.settings.getDataDirectory().child("images/").child("creature-" + Time.millis() + ".png");
        PixmapIO.writePng(file, full);
        full.dispose();
        */

        //s.screenshotMode = false;

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

        //Core.camera.update();
        if(EditorMain.creature != null){
            Draw.flush();
            Draw.proj().set(getProj());
            if(!Control.test){
                EditorMain.creature.draw();
                Draw.flush();
                drawCursor2();
                drawEffects();
                //Fill.square(0f, 0f, 2f, 0f);
                //drawCursor();
                //drawEffects();
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

    void drawCursor2(){
        //Draw.color(Color.white, 0.125f);
        Vec2 m = Control.mouseToWorld();

        //Fill.poly(m.x, m.y, 12, 0.5f);
        
        //Draw.color(Color.red, 0.125f);
        Vec2 m2 = Control.worldToCanvas(m.x, m.y);
        Point2 ci = Control.canvasToIdx(m2.x, m2.y);
        int cix = ci.x - hRes;
        int ciy = ci.y - hRes;
        /*
        float nx = (x - hRes) - 1, ny = (y - hRes) - 1;
        float g = 0.866025403f;
        //off.set(nx, ny);
        off.set(nx + 0.5f * ny, ny * g);
        */
        
        float g = 0.866025403f;
        //Fill.poly(m2.x + 0.5f * m2.y, m2.y * g, 4, 0.333f);
        
        Draw.color(Color.white, Mathf.lerp(0.15f, 0.4f, Mathf.sinDeg(oscTime) * 0.5f + 0.5f));
        //Fill.poly(cix + 0.5f * ciy, ciy * g, 6, 0.333f, 90f);

        int[] brushSet = Control.brushes[Control.brushSize];
        if(Control.tool == ToolType.fill || Control.tool == ToolType.sampler){
            brushSet = Control.brushes[0];
        }
        //int ix = (int)(v.x) - hRes, iy = (int)(v.y) - hRes;

        for(int i = 0; i < brushSet.length; i += 2){
            //hex(cix + brushSet[i], ciy + brushSet[i + 1]);
            float bx = cix + brushSet[i], by = ciy + brushSet[i + 1];
            Fill.poly(bx + 0.5f * by, by * g, 6, 0.5f, 90f);
            int px = cix + hRes + brushSet[i];
            int py = ciy + hRes + brushSet[i + 1];
            occupied.add((px & 0xffff) | ((py & 0xffff) << 16));
        }
        if(Control.mirror && Control.tool != ToolType.sampler && Control.mirrors[Mathf.mod(Control.orientation, Control.mirrors.length)] != null){
            Point2 mir = Control.getMirror(cix + hRes, ciy + hRes, Control.orientation);
            for(int i = 0; i < brushSet.length; i += 2){
                int px = mir.x + brushSet[i];
                int py = mir.y + brushSet[i + 1];
                //occupied.add((px & 0xffff) | ((py & 0xffff) << 16));
                if(occupied.contains((px & 0xffff) | ((py & 0xffff) << 16))) continue;
                //hex(mir.x - hRes + brushSet[i], mir.y - hRes + brushSet[i + 1]);
                int bx = px - hRes, by = py - hRes;
                Fill.poly(bx + 0.5f * by, by * g, 6, 0.5f, 90f);
            }
        }
        occupied.clear();

        //drawEffects();
        Draw.reset();
    }

    void drawEffects(){        
        if(Control.mirror){
            Draw.color(Color.green, Mathf.lerp(0.05f, 0.15f, Mathf.sinDeg(oscTime) * 0.5f + 0.5f));

            //hex2(0, 0);
            Fill.poly(0f, 0f, 6, 0.333f, 90f);
            Lines.stroke(cs * 0.002f);
            Lines.lineAngleCenter(0f, 0f, -cr + 90f, 9999f, false);
        }
        
        Draw.color(Color.red, Mathf.lerp(0.1f, 0.333f, Mathf.sinDeg(oscTime) * 0.5f + 0.5f));
        //
        Lines.stroke(cs * 0.003f);
        Lines.beginLine();
        Vec2 v = Tmp.v1;
        trans(0f, 0f);
        Lines.linePoint(v.x, v.y);
        
        trans(0f, canvasRes);
        Lines.linePoint(v.x, v.y);
        
        trans(canvasRes, canvasRes);
        Lines.linePoint(v.x, v.y);
        
        trans(canvasRes, 0f);
        Lines.linePoint(v.x, v.y);
        Lines.endLine(true);
    }
    void trans(float x, float y){
        float nx = (x - hRes) - 1, ny = (y - hRes) - 1;
        float g = 0.866025403f;
        //off.set(nx, ny);
        //off.set(nx + 0.5f * ny, ny * g);
        Tmp.v1.set(nx + 0.5f * ny, ny * g);
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
        float trnx = cx * wd * -canvasRes, trny = cy * ht * -canvasRes;
        pr.setOrtho((-hRes) * cs * wd + trnx, (-hRes) * cs * ht + trny, canvasRes * cs * wd, canvasRes * cs * ht);
        pr.rotate(cr);
        Draw.color(Color.white, Mathf.lerp(0.15f, 0.4f, Mathf.sinDeg(oscTime) * 0.5f + 0.5f));

        //Vec2 v = Control.mouseToCanvas();
        Vec2 v = Tmp.v1;
        int ix = (int)(v.x) - hRes, iy = (int)(v.y) - hRes;
        //Fill.poly(vx, vy, 6, 1f, 90f);
        //hex(vx, vy);
        int[] brushSet = Control.brushes[Control.brushSize];
        if(Control.tool == ToolType.fill || Control.tool == ToolType.sampler){
            brushSet = Control.brushes[0];
        }
        for(int i = 0; i < brushSet.length; i += 2){
            hex(ix + brushSet[i], iy + brushSet[i + 1]);
            int px = ix + hRes + brushSet[i];
            int py = iy + hRes + brushSet[i + 1];
            occupied.add((px & 0xffff) | ((py & 0xffff) << 16));
        }
        if(Control.mirror && Control.tool != ToolType.sampler && Control.mirrors[Mathf.mod(Control.orientation, Control.mirrors.length)] != null){
            Point2 mir = Control.getMirror(ix + hRes, iy + hRes, Control.orientation);
            for(int i = 0; i < brushSet.length; i += 2){
                int px = mir.x + brushSet[i];
                int py = mir.y + brushSet[i + 1];
                //occupied.add((px & 0xffff) | ((py & 0xffff) << 16));
                if(occupied.contains((px & 0xffff) | ((py & 0xffff) << 16))) continue;
                hex(mir.x - hRes + brushSet[i], mir.y - hRes + brushSet[i + 1]);
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
