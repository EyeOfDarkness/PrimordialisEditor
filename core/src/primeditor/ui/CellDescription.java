package primeditor.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import primeditor.*;

public class CellDescription extends Table{
    Label text;
    boolean focusing;
    public Element active;

    private final static GlyphLayout sizeTest = new GlyphLayout();

    public CellDescription(){
        super(EditorMain.ui.main);
        text = new Label("");
        text.setAlignment(Align.center);
        touchable = Touchable.disabled;
        //add(text).grow().center();
        add(text).grow();
        //setTransform(true);
        //text.fill();
        visibility = () -> focusing;
        setOrigin(Align.topRight);
        //setWidth(150f);
        //fill();
    }

    public void focus(Element e, String desc, float x, float y){
        if(e != active){
            toFront();
            active = e;
            focusing = true;

            //sizeTest.setText(EditorMain.ui.def, ct.desc, 0, ct.desc.length(), Color.white, 100f, Align.left, false, null);

            text.setWrap(true);
            text.setText(desc);

            sizeTest.setText(EditorMain.ui.def, desc, 0, desc.length(), Color.white, 10f, Align.center, false, null);

            //desc2.setWidth(Math.min(150f, sizeTest.width + 4f));
            //desc2.setHeight(desc.getPrefHeight());
            //desc2.setPosition(Core.input.mouseX(), Core.input.mouseY(), Align.topRight);
            setWidth(Math.min(180f, sizeTest.width + 8f));
            setHeight(text.getPrefHeight() + 4f);
            setPosition(x, y, Align.topRight);
            //
            text.layout();
        }
    }
    public void unfocus(Element e){
        if(e == active){
            active = null;
            focusing = false;
        }
    }
}
