package primeditor.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import primeditor.graphics.*;

public class ColorSlider extends Slider{
    int colorMode;
    ColorWheel parent;

    public ColorSlider(float min, float max, float stepSize, boolean vertical, SliderStyle style, ColorWheel source){
        super(min, max, stepSize, vertical, style);
        parent = source;
    }

    @Override
    public void draw(){
        final Drawable knob = getKnobDrawable();
        float x = this.x;
        float y = this.y;
        float width = getWidth();
        float height = getHeight();
        //float knobHeight = knob == null ? 0 : knob.getMinHeight();
        float knobWidth = knob == null ? 0 : knob.getMinWidth();

        Draw.flush();
        Draw.color();
        //Fill.crect(x + knobWidth / 2f, y, width - knobWidth, height);
        var sh = Shaders.colwheel;
        sh.mode = colorMode;
        sh.h = parent.h;
        sh.s = parent.s;
        sh.l = parent.v;
        sh.a = parent.a;
        sh.rectX = width - knobWidth;
        sh.rectY = height;
        Draw.shader(Shaders.colwheel);
        GUtils.srect(x + knobWidth / 2f, y, width - knobWidth, height);
        Draw.flush();
        Draw.shader();

        super.draw();
    }
}
