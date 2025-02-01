package primeditor.ui;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import primeditor.*;
import primeditor.graphics.*;
import primeditor.ui.ColorWheel.*;

public class Palette extends Table{
    Table cont, main;
    ButtonGroup<Button> group;
    Button selected = null;

    Seq<Button> palettes = new Seq<>();
    int buttonRows = 0;

    private boolean ev = false;

    ScrollPane scroll;

    public Palette(){
        super(EditorMain.ui.main);
        group = new ButtonGroup<>();
        main = new Table();
        add(cont = new Table()).width(300f).height(180f).expand().right().top();
        setup();
    }
    public void unselect(){
        if(ev) return;
        group.uncheckAll();
        selected = null;
    }

    void updatePalette(){
        main.clear();
        int i = 0;
        for(Button p : palettes){
            main.add(p).right().width(35f).height(35f);
            i++;
            if(i > 6){
                main.row();
                i = 0;
            }
        }
    }
    void setColor(HSVA pal){
        ColorWheel w = EditorMain.ui.mainColorWheel;
        w.set(pal.h, pal.s, pal.v, pal.a);
    }
    void addPalette(){
        ColorWheel w = EditorMain.ui.mainColorWheel;
        addPalette(w.get());
    }
    void addPalette(HSVA pal){
        HSVA copy = new HSVA();
        copy.h = pal.h;
        copy.s = pal.s;
        copy.v = pal.v;
        copy.a = pal.a;
        Button palette = new Button(EditorMain.ui.buttonChecked);
        palette.clicked(() -> {
            if(ev) return;
            ev = true;
            setColor(copy);
            selected = palette;
            ev = false;
        });
        palette.fill((x, y, w, h) -> {
            var sh = Shaders.colwheel;
            sh.mode = 4;
            sh.h = copy.h;
            sh.s = copy.s;
            sh.l = copy.v;
            sh.a = copy.a;
            sh.rectX = w;
            sh.rectY = h;
            Draw.shader(sh);
            float pad = 10f;
            GUtils.srect(x + pad / 2f, y + pad / 2f, w - pad, h - pad);
            Draw.flush();
            Draw.shader();
        });
        palette.setHeight(30f);
        palette.setWidth(30f);
        group.add(palette);
        if(palettes.size <= 0){
            group.uncheckAll();
        }
        palettes.add(palette);
        updatePalette();
    }
    void removePalette(){
        if(selected == null || palettes.isEmpty()) return;
        palettes.remove(selected);
        group.remove(selected);
        selected = null;
        updatePalette();
    }

    void setup(){
        //cont.setWidth(320);
        //cont.setHeight(140f);

        cont.table(EditorMain.ui.main, t -> {
            TextButton add = new TextButton("Add", EditorMain.ui.button);
            add.clicked(this::addPalette);
            add.setWidth(130f);
            add.setHeight(30f);
            t.add(add).right().top().width(140f).height(30f);

            TextButton remove = new TextButton("Remove", EditorMain.ui.button);
            remove.clicked(this::removePalette);
            remove.setWidth(130f);
            remove.setHeight(30f);
            t.add(remove).right().top().width(140f).height(30f);
        }).right().top().fill();
        cont.row();
        scroll = new ScrollPane(main, EditorMain.ui.scrollStyle);
        scroll.addListener(new InputListener(){
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                if(Core.scene.getScrollFocus() == scroll && pointer == -1){
                    Core.scene.setScrollFocus(null);
                }
            }
        });
        cont.table(EditorMain.ui.main, 2, t -> {
            t.add(scroll).fillY().fillX();
        }).fillX().height(120f).bottom().right();
        cont.row();
        cont.table(t -> {}).fillX().height(20f).bottom();

        /*
        main.setWidth(300f);
        main.setHeight(140f);
        scroll = new ScrollPane(main, EditorMain.ui.scrollStyle);
        //scroll.setForceScroll(false, true);
        scroll.setWidth(300f);
        scroll.setHeight(90f);
        cont.add(scroll).bottom().right().fillY();
        */
        //group.uncheckAll();
    }

    static class PaletteDrawable extends BaseDrawable{
        HSVA col;
        PaletteDrawable(HSVA col){
            this.col = col;
        }

        @Override
        public void draw(float x, float y, float width, float height){
            var sh = Shaders.colwheel;
            sh.mode = 4;
            sh.h = col.h;
            sh.s = col.s;
            sh.l = col.v;
            sh.a = col.a;
            sh.rectX = width;
            sh.rectY = height;
            Draw.shader(sh);
            GUtils.srect(x, y, width, height);
            Draw.flush();
            Draw.shader();
        }

        @Override
        public void draw(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation){
            draw(x, y, width * scaleX, height * scaleY);
        }
    }
}
