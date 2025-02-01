package primeditor.ui;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.input.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import primeditor.*;
import primeditor.graphics.*;

public class ColorWheel extends Table{
    public float h, s, v, a;
    float lh, ls, lv, la;

    Table cont;
    boolean updatingColor, skipLastValue;
    HSVA out = new HSVA();

    ColorSlider hueS, satS, valS, alpS;
    TextField hueF, satF, valF, alpF;

    public ColorWheel(){
        super(EditorMain.ui.main);
        //top().right();
        //setClip(true);
        setWidth(340f);
        setHeight(120f);
        //setOrigin(Align.topRight);
        //top().right();

        //add(cont = new Table()).top().right().expand().fill();
        add(cont = new Table()).height(130f).expand().fill().top().pad(0f);

        setup();
    }
    public void setup(){
        var main = EditorMain.ui.main;
        //top().right();

        cont.top().table(main, t -> {
            for(int i = 0; i < 4; i++){
                //t.add
                float step = i == 0f ? 0.5f : 0.0005f;
                var slider = new ColorSlider(0f, (i == 0) ? 360f : 1f, step, false, EditorMain.ui.sliderStyle, this);
                slider.colorMode = i;
                slider.moved(v -> {
                    if(!updatingColor){
                        updateColor();
                    }
                });
                slider.addCaptureListener(new InputListener(){
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Element fromActor){
                        if(Control.queueRemoveFocus.contains(slider)){
                            Control.queueRemoveFocus.remove(slider);
                        }
                        Core.scene.setScrollFocus(slider);
                    }
                });
                slider.addListener(new InputListener(){
                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                        if(Core.scene.getScrollFocus() == slider && pointer == -1){
                            // && !Core.input.keyDown(KeyCode.mouseLeft)
                            //Core.scene.setScrollFocus(null);
                            if(!Control.queueRemoveFocus.contains(slider)) Control.queueRemoveFocus.add(slider);
                        }
                    }

                    @Override
                    public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                        float d = Math.abs(amountY);
                        if(d > 0.01f){
                            float sign = amountY < 0 ? 1f : -1f;
                            slider.setValue(slider.getValue() + step * sign);
                        }
                        return true;
                    }
                });
                switch(i){
                    case 0 -> hueS = slider;
                    case 1 -> satS = slider;
                    case 2 -> valS = slider;
                    case 3 -> alpS = slider;
                }
                t.add(slider).width(340f - 70f).height(15f).center();

                var ti = new TextField("0", EditorMain.ui.tfStyle);
                ti.addListener(new InputListener(){
                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Element toActor){
                        if(Core.scene.getKeyboardFocus() == ti && (pointer == -1)){
                            Core.scene.setKeyboardFocus(null);
                            updateColor();
                        }
                    }
                });
                ti.setFilter(TextFieldFilter.floatsOnly);
                ti.setMaxLength(8);
                boolean hue = i == 0;
                fc(ti, str -> {
                    if(!updatingColor){
                        float val;
                        try{
                            val = Float.parseFloat(str);
                        }catch(Exception e){
                            val = 0f;
                        }
                        if(hue){
                            val = Mathf.clamp(val, 0f, 360f);
                        }else{
                            val = Mathf.clamp(val, 0f, 100f);
                        }
                        skipLastValue = true;
                        slider.setValue(val);
                        skipLastValue = false;
                    }
                });
                switch(i){
                    case 0 -> hueF = ti;
                    case 1 -> satF = ti;
                    case 2 -> valF = ti;
                    case 3 -> alpF = ti;
                }
                t.add(ti).width(70f).height(25f).top();

                t.row();
            }
        }).fill().top();
        cont.row();
        //current color
        cont.table(main, t -> {
            t.fill((x, y, width, height) -> {
                var sh = Shaders.colwheel;
                sh.mode = 4;
                sh.h = h;
                sh.s = s;
                sh.l = v;
                sh.a = a;
                Draw.color();
                Draw.flush();
                Draw.shader(sh);
                GUtils.srect(x, y, width, height);
                Draw.flush();
                Draw.shader();
            });
        }).fill().top();
        alpS.setValue(100f);
    }

    void fc(TextField field, Cons<String> cons){
        field.changed(() -> cons.get(field.getText()));
    }

    void updateColor(){
        updatingColor = true;

        h = hueS.getValue();
        s = satS.getValue();
        v = valS.getValue();
        a = alpS.getValue();
        //Log.info("H:" + h + " S:" + s + " V:" + v);

        if(!skipLastValue){
            if(h != lh){
                hueF.setText(Float.toString(h));
            }
            if(s != ls){
                satF.setText(Float.toString(s));
            }
            if(v != lv){
                valF.setText(Float.toString(v));
            }
            if(a != la){
                alpF.setText(Float.toString(a));
            }

            lh = h;
            ls = s;
            lv = v;
            la = a;
        }
        if(EditorMain.ui.palette != null) EditorMain.ui.palette.unselect();

        updatingColor = false;
    }
    
    public HSVA get(){
        out.h = h;
        out.s = s;
        out.v = v;
        out.a = a;
        
        return out;
    }
    public void set(float h, float s, float v, float a){
        updatingColor = true;

        hueS.setValue(this.h = h);
        satS.setValue(this.s = s);
        valS.setValue(this.v = v);
        alpS.setValue(this.a = a);

        if(h != lh){
            hueF.setText(Float.toString(h));
        }
        if(s != ls){
            satF.setText(Float.toString(s));
        }
        if(v != lv){
            valF.setText(Float.toString(v));
        }
        if(a != la){
            alpF.setText(Float.toString(a));
        }

        lh = h;
        ls = s;
        lv = v;
        la = a;

        if(EditorMain.ui.palette != null) EditorMain.ui.palette.unselect();

        updatingColor = false;
    }
    
    public static class HSVA{
        public float h, s, v, a;
    }
}
