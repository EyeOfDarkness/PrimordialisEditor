package primeditor.ui;

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

    public CellSelector(){
        super(EditorMain.ui.main);

        group = new ButtonGroup<>();
        //group.setMaxCheckCount(1);
        add(cont = new Table());
        setup();
    }

    void setup(){
        int i = 0;
        for(CellType ct : CellTypes.types){
            if(ct == CellTypes.unknown) break;
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
            cont.add(b).height(32f).width(32f);
            group.add(b);
            buttonMap.put(ct, b);
            i++;
            if(i >= 4){
                cont.row();
                i = 0;
            }
        }
        //Control.currentCell = CellTypes.types.get(0);
        Control.setCell(CellTypes.types.get(0));
    }
}
