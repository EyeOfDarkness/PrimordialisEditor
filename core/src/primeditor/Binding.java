package primeditor;

import arc.KeyBinds.*;
import arc.input.*;
import arc.input.InputDevice.*;

public enum Binding implements KeyBind{
    move_x(new Axis(KeyCode.a, KeyCode.d)),
    move_y(new Axis(KeyCode.s, KeyCode.w)),

    move_x2(new Axis(KeyCode.left, KeyCode.right)),
    move_y2(new Axis(KeyCode.down, KeyCode.up)),

    zoom(new Axis(KeyCode.scroll));

    private final KeybindValue defaultValue;

    Binding(KeybindValue defaultValue){
        this.defaultValue = defaultValue;
    }

    @Override
    public KeybindValue defaultValue(DeviceType type){
        return defaultValue;
    }

    @Override
    public String category(){
        return null;
    }
}
