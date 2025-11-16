package org.ricey_yam.lynxmind.client.utils.game_ext.interaction;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.option.KeyBinding;

@Getter
@Setter
public class Input {
    public final KeyBinding keyBind;
    public boolean pressed = false;
    public Input(KeyBinding keyBind) {
        this.keyBind = keyBind;
    }
    public void setKeyPressed(boolean isPressed){
        pressed = isPressed;
        keyBind.setPressed(isPressed);
    }
}
