package primeditor.ui;

import arc.*;
import arc.graphics.*;
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

public class ComboDialog extends Dialog{
    Table cont;
    public ButtonGroup<ImageButton> group;
    public ObjectMap<CellType, ImageButton> buttonMap = new ObjectMap<>();
    public boolean updating = false;

    Seq<CellType> selected = new Seq<>();

    public ComboDialog(){
        super("Combos", EditorMain.ui.dialogStyle);

        shown(this::build);

        group = new ButtonGroup<>();
        group.setMaxCheckCount(2);
        group.setMinCheckCount(0);

        //setFillParent(true);
        title.setAlignment(Align.center);
        titleTable.row();
        //titleTable.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        titleTable.image(EditorMain.ui.white, Color.gray).growX().height(3f).pad(4f);
        titleTable.row();
        titleTable.add(cont = new Table()).grow().fill().width(800f);
        titleTable.row();
        //titleTable.image(Tex.whiteui, Pal.accent).growX().height(3f).pad(4f);
        titleTable.image(EditorMain.ui.white, Color.gray).growX().height(3f).pad(4f);
        titleTable.row();

        setup();
    }

    public void build(){
        buttonMap.clear();
        selected.clear();
        group.clear();
        cont.clear();

        int row = 0;
        var all = CellTypes.all();
        for(CellType c : all){
            if(c.hidden) continue;
            ImageButton b = new ImageButton(new TextureRegionDrawable(c.region, 1f));
            b.setWidth(64f);
            b.setHeight(64f);
            b.getImage().setScaling(Scaling.none);
            b.setDisabled(() -> EditorMain.creature == null);
            b.changed(() -> {
                if(updating || EditorMain.creature == null) return;
                selected.add(c);
                if(selected.size >= 2){
                    finished();
                }
            });
            b.addListener(new InputListener(){
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                    //EditorMain.ui.desc.focus(b, ct.desc, Core.input.mouseX(), Core.input.mouseY());
                    EditorMain.ui.desc.focus(b, c instanceof ComboCellType ct ? ct.desc : c.name, Core.input.mouseX(), Core.input.mouseY());
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                    EditorMain.ui.desc.unfocus(b);
                }
            });

            buttonMap.put(c, b);
            group.add(b);
            cont.add(b).height(64f).width(64f);
            row++;
            if(row >= 12){
                cont.row();
                row = 0;
            }
        }

        updating = true;
        group.uncheckAll();
        updating = false;
    }

    void finished(){
        CellType a = selected.get(0), b = selected.get(1);
        //ComboCellType cmb = new ComboCellType(id);
        //cmb.ia = a;
        //cmb.ib = b;
        //combos.add(cmb);
        //comboMap.put(id, cmb);
        Creature cre = EditorMain.creature;
        int id = 0x80000000 | cre.combos.size;
        ComboCellType cmb = new ComboCellType(id);
        cmb.a = a;
        cmb.b = b;
        cmb.loadDescription();
        cre.addCombo(cmb);

        selected.clear();
        hide();
    }

    void setup(){
        build();
        addCloseButton();
    }

    @Override
    public void addCloseButton(){
        closeOnBack();
        //buttons.button("@back", Icon.left, this::hide).size(width, 64f);
        buttons.button("cancel", EditorMain.ui.button, () -> {
            selected.clear();
            hide();
        }).size(80f, 20f);
    }
}
