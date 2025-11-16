package org.ricey_yam.lynxmind.client.utils.game_ext.interaction;


import net.minecraft.client.option.KeyBinding;
import org.ricey_yam.lynxmind.client.utils.game_ext.ClientUtils;

import java.util.ArrayList;
import java.util.List;

public class InteractionHelper {
    public final static List<Input> toRelease = new ArrayList<>();
    public static void setKeyPress(KeyBinding keyBinding,boolean isPressed){
        var input = new Input(keyBinding);
        input.setPressed(isPressed);
    }
    public static void rightClick(){
        var options = ClientUtils.getOptions();
        setKeyPress(options.rightKey,true);
    }
}
