package primeditor.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import primeditor.*;
import primeditor.creature.*;
import primeditor.creature.CellTypes.*;

public class CellSelector extends Table{
    Table cont;
    public ButtonGroup<ImageButton> group;
    public ObjectMap<CellType, ImageButton> buttonMap = new ObjectMap<>();
    public boolean updating = false;
    public Label desc;
    public Table desc2;
    public boolean focusing = false;

    int row = 0;

    private final static GlyphLayout sizeTest = new GlyphLayout();

    public CellSelector(){
        super(EditorMain.ui.main);

        group = new ButtonGroup<>();
        //group.setMaxCheckCount(1);
        add(cont = new Table());
        setup();
        row();
        button("Combos", EditorMain.ui.button, () -> EditorMain.ui.comboDialog.show()).fillX();
        //add(desc = new Label("")).left();
        desc = new Label("");
        desc.setAlignment(Align.center);
        //desc.setWrap(true);
        //desc = new Dialog("", EditorMain.ui.dialogStyle);
        //desc.title.setWrap(true);
        //desc.setHeight(10f);
        //desc.setWidth(100f);
        //desc.fill();
        //desc.fill();
        desc2 = new Table(EditorMain.ui.main);
        desc2.touchable = Touchable.disabled;
        desc2.add(desc).grow();
        desc2.visibility = () -> focusing;
        //desc2.setTransform(true);
        desc2.setOrigin(Align.topRight);
        //desc2.setWidth(100f);
        //Core.scene.add(desc2);

        addListener(new InputListener(){
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                if(pointer == -1 && focusing){
                    focusing = false;
                }
            }
        });
    }

    public void reset(){
        cont.clear();
        buttonMap.clear();
        group.clear();
        setup();
        Control.setUICell(CellTypes.def);
        focusing = false;
    }

    void setup(){
        row = 0;
        for(CellType ct : CellTypes.types){
            if(ct.hidden) break;
            ImageButton b = new ImageButton(new TextureRegionDrawable(ct.region, 0.5f));
            b.setWidth(32f);
            b.setHeight(32f);
            b.getImage().setScaling(Scaling.none);
            b.changed(() -> {
                //Control.currentCell = ct;
                if(updating) return;
                Control.setCell(ct);
                //Log.info(ct.name);
            });
            b.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                    EditorMain.ui.desc.focus(b, ct.name, Core.input.mouseX(), Core.input.mouseY());
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                    if(pointer == -1){
                        EditorMain.ui.desc.unfocus(b);
                    }
                }
            });

            /*
            b.hovered(() -> {
                desc.setText(ct.name);
                desc.setWrap(false);
                desc2.setWidth(desc.getPrefWidth());
                desc2.setHeight(desc.getPrefHeight());
                desc2.setPosition(Core.input.mouseX(), Core.input.mouseY(), Align.topRight);
                focusing = true;
            });
            */

            cont.add(b).height(32f).width(32f);
            group.add(b);
            buttonMap.put(ct, b);
            row++;
            if(row >= 6){
                cont.row();
                row = 0;
            }
        }
        //Control.currentCell = CellTypes.types.get(0);
        Control.setCell(CellTypes.types.get(0));
    }
    public void addCombo(ComboCellType ct){
        ImageButton b = new ImageButton(new TextureRegionDrawable(ct.region, 0.5f));
        b.setWidth(32f);
        b.setHeight(32f);
        b.getImage().setScaling(Scaling.none);
        b.changed(() -> {
            //Control.currentCell = ct;
            if(updating) return;
            Control.setCell(ct);
            //Log.info(ct.name);
        });

        b.addListener(new InputListener(){
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                EditorMain.ui.desc.focus(b, ct.desc, Core.input.mouseX(), Core.input.mouseY());
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                if(pointer == -1){
                    EditorMain.ui.desc.unfocus(b);
                }
            }
        });

        /*
        b.hovered(() -> {
            //desc.setText(ct instanceof ComboCellType cmb ? cmb.desc : ct.name);
            //desc.setText(ct instanceof ComboCellType cmb ? cmb.desc : ct.name);
            desc.setText(ct.desc);
            desc.setWrap(true);
            //prefSizeLayout.setText(cache.getFont(), text, 0, text.length(), Color.white, width, lineAlign, wrap, ellipsis);
            //desc.getGlyphLayout().width;
            sizeTest.setText(EditorMain.ui.def, ct.desc, 0, ct.desc.length(), Color.white, 100f, Align.left, false, null);
            //desc.
            desc2.setWidth(Math.min(150f, sizeTest.width + 4f));
            desc2.setHeight(desc.getPrefHeight());
            desc2.setPosition(Core.input.mouseX(), Core.input.mouseY(), Align.topRight);
            focusing = true;
        });
        */

        cont.add(b).height(32f).width(32f);
        group.add(b);
        buttonMap.put(ct, b);
        row++;
        if(row >= 6){
            cont.row();
            row = 0;
        }
    }
}
