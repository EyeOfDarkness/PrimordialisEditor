package primeditor;

import arc.*;
import arc.files.*;
import arc.freetype.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.Button.*;
import arc.scene.ui.Dialog.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.ProgressBar.*;
import arc.scene.ui.ScrollPane.*;
import arc.scene.ui.Slider.*;
import arc.scene.ui.TextButton.*;
import arc.scene.ui.TextField.*;
import arc.util.*;
import primeditor.ui.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.io.*;

public class UI implements ApplicationListener{
    public Drawable main = Core.atlas.drawable("button");
    public Drawable mainOver = Core.atlas.drawable("button-over");
    public Drawable mainDown = Core.atlas.drawable("button-down");
    //public Drawable bdown = ((TextureRegionDrawable)Core.atlas.drawable("white")).tint(0.4f, 0.4f, 0.4f, 0.4f);
    public Drawable clear = Core.atlas.drawable("clear");
    public Drawable white = Core.atlas.drawable("white");
    public Font def;

    public ColorWheel mainColorWheel;
    public CellSelector selector;
    public Tools tools;
    public Palette palette;

    public ComboDialog comboDialog;

    public TextButtonStyle button;
    public ButtonStyle buttonChecked;
    public SliderStyle sliderStyle;
    public TextFieldStyle tfStyle;
    public ScrollPaneStyle scrollStyle;
    public DialogStyle dialogStyle;

    public CellDescription desc;

    @Override
    public void init(){
        Core.scene = new Scene();
        Core.input.addProcessor(Core.scene);

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Core.files.internal("fonts/font.ttf"));
        def = gen.generateFont(new FreeTypeFontParameter(){{
            size = 13;
            incremental = true;
        }});
        Core.scene.addStyle(LabelStyle.class, new LabelStyle(){{
            font = def;
            fontColor = Color.white;
        }});
        Core.scene.addStyle(ButtonStyle.class, new ButtonStyle(){{
            //font = def;
            //fontColor = Color.white;
            up = main;
            down = mainDown;
            over = mainOver;
        }});
        buttonChecked = new ButtonStyle(){{
            //font = def;
            //fontColor = Color.white;
            up = main;
            checked = down = mainDown;
            over = mainOver;
        }};
        Core.scene.addStyle(ImageButtonStyle.class, new ImageButtonStyle(){{
            up = main;
            checked = down = mainDown;
            over = mainOver;
        }});
        Core.scene.addStyle(TextButtonStyle.class, button = new TextButtonStyle(){{
            font = def;
            fontColor = Color.white;
            up = main;
            down = mainDown;
            over = mainOver;
        }});
        Core.scene.addStyle(ProgressBarStyle.class, sliderStyle = new SliderStyle(){{
            background = disabledBackground = clear;
            knob = Core.atlas.drawable("color-knob");
        }});
        Core.scene.addStyle(TextFieldStyle.class, tfStyle = new TextFieldStyle(){{
            font = def;
            fontColor = new Color(0.6f, 0.6f, 0.6f, 1f);
            background = main;
            focusedBackground = mainOver;
            selection = Core.atlas.drawable("white");
        }});
        Core.scene.addStyle(ScrollPaneStyle.class, scrollStyle = new ScrollPaneStyle(){{
            //background = main;
            vScroll = main;
            vScrollKnob = main;
        }});
        Core.scene.addStyle(DialogStyle.class, dialogStyle = new DialogStyle(){{
            background = main;
            titleFont = def;
        }});

        setup();
    }

    @Override
    public void resize(int width, int height){
        Core.scene.resize(width, height);
    }

    @Override
    public void update(){
        Core.scene.act();
        Core.scene.draw();
    }

    void setup(){
        Core.scene.table(t -> {
            t.top().left().table(main, c -> {
                c.table(c2 -> {
                    c2.button("open", button, () -> {
                        JFileChooser fc = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("Primordialis Creature", "bod");
                        fc.setFileFilter(filter);
                        fc.setMultiSelectionEnabled(false);
                        int ret = fc.showOpenDialog(null);
                        if(ret == JFileChooser.APPROVE_OPTION){
                            File file = fc.getSelectedFile();
                            //Log.info();
                            Log.info(file.toString());
                            EditorMain.loadCreature(file);
                        }
                    }).height(40f).width(120f).expandX().left();
                    c2.button("save", button, () -> {
                        if(EditorMain.creature == null) return;
                        Fi dd = Core.settings.getDataDirectory();
                        Fi bodd = dd.child("creatures/");
                        String name = "CreatureT" + (Time.millis() / 1000);
                        //result = customMapDirectory.child(name + (index == 0 ? "" : "_" + index) + "." + mapExtension);
                        Fi out = bodd.child(name + ".bod");
                        //out.writes()
                        EditorMain.saveCreature(out);

                    }).height(40f).width(120f).expandX().left();
                }).width(248f).height(40f).expandX().left();
                //c.table(main, c2 -> {}).width(120f).height(40f).growX().left();
            }).height(50f).width(500f).top().left().margin(3f);
        });
        //Core.scene.add(mainColorWheel = new ColorWheel());
        comboDialog = new ComboDialog();
        desc = new CellDescription();
        mainColorWheel = new ColorWheel();
        selector = new CellSelector();
        tools = new Tools();
        palette = new Palette();
        Core.scene.table(t -> {
            t.top().right().add(mainColorWheel).top().right();
        });
        Core.scene.table(t -> {
            t.right().add(selector).right();
        });
        //Core.scene.add(selector.desc2);
        Core.scene.table(t -> {
            t.left().add(tools).left();
        });
        Core.scene.table(t -> {
            t.right().bottom().add(palette).right().bottom();
        });
        Core.scene.add(desc);
        //desc.toFront();
    }
}
