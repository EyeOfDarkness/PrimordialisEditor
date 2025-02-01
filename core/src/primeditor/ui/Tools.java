package primeditor.ui;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import primeditor.*;
import primeditor.Control.*;

public class Tools extends Table{
    Table cont;
    ButtonGroup<ImageButton> tools, mode;
    ImageButton mirror;

    public Tools(){
        super(EditorMain.ui.main);
        //group = new ButtonGroup<>();
        tools = new ButtonGroup<>();
        mode = new ButtonGroup<>();
        add(cont = new Table()).expand().fill().left().top();
        setup();
    }

    void setup(){
        cont.label(() -> "Brush:\n" + (1 + Control.brushSize));
        //cont.row();
        //cont.label(() -> Integer.toString(1 + Control.brushSize));
        cont.row();
        bar();

        mirror = new ImageButton(Core.atlas.drawable("ui-mirror"));
        mirror.setWidth(64f);
        mirror.setHeight(64f);
        mirror.changed(() -> Control.mirror = mirror.isChecked());
        cont.add(mirror).height(64f).width(64f);
        cont.row();
        bar();

        var cellMode = new ImageButton(Core.atlas.drawable("ui-cell-mode"));
        cellMode.setWidth(64f);
        cellMode.setHeight(64f);
        cellMode.changed(() -> Control.paintMode = false);
        cont.add(cellMode).height(64f).width(64f);
        cont.row();
        var colorMode = new ImageButton(Core.atlas.drawable("ui-color-mode"));
        colorMode.setWidth(64f);
        colorMode.setHeight(64f);
        colorMode.changed(() -> Control.paintMode = true);
        cont.add(colorMode).height(64f).width(64f);
        cont.row();
        mode.add(cellMode);
        mode.add(colorMode);
        bar();

        var brush = new ImageButton(Core.atlas.drawable("ui-brush"));
        brush.setWidth(64f);
        brush.setHeight(64f);
        brush.changed(() -> Control.tool = ToolType.brush);
        cont.add(brush).height(64f).width(64f);
        cont.row();

        var erase = new ImageButton(Core.atlas.drawable("ui-eraser"));
        erase.setWidth(64f);
        erase.setHeight(64f);
        erase.changed(() -> Control.tool = ToolType.eraser);
        cont.add(erase).height(64f).width(64f);
        cont.row();

        var fill = new ImageButton(Core.atlas.drawable("ui-fill"));
        fill.setWidth(64f);
        fill.setHeight(64f);
        fill.changed(() -> Control.tool = ToolType.fill);
        cont.add(fill).height(64f).width(64f);
        cont.row();
        
        var sampl = new ImageButton(Core.atlas.drawable("ui-sampler"));
        sampl.setWidth(64f);
        sampl.setHeight(64f);
        sampl.changed(() -> Control.tool = ToolType.sampler);
        cont.add(sampl).height(64f).width(64f);

        tools.add(brush);
        tools.add(erase);
        tools.add(fill);
        tools.add(sampl);
    }

    void bar(){
        cont.table(t -> {
            t.fill((x, y, width, height) -> {
                Draw.color(0.5f, 0.5f, 0.5f, 1f);
                Fill.crect(x, (y + height / 2) - 1f, width, 2f);
                Draw.color();
            });
        }).height(8f).growX();
        cont.row();
    }
}
