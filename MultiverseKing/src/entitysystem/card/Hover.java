/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entitysystem.card;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import tonegod.gui.controls.windows.Window;
import tonegod.gui.core.ElementManager;

/**
 * @todo generate too much object have to be solved/reduce.
 * @author roah
 */
public class Hover extends Window {

    public Hover(ElementManager screen) {
        super(screen, "hover", new Vector2f(300f, 250f), Vector2f.ZERO, Vector4f.ZERO , "Textures/Cards/cardHover.png");
        this.removeAllChildren();
//        this.removeChild(dragBar);//.getDragBar().setIsVisible(false);
        this.setIgnoreMouse(true);
    }

    /**
     * @todo
     */
    void setProperties(CardPropertiesComponent component, String cardName) {
//        float posY = this.getPosition().y-this.getHeight()-20;
        Window level = new Window(this.screen, Vector2f.ZERO, new Vector2f(11,17), Vector4f.ZERO, "Textures/PlatformerGUIText/Individual/"+component.getLevel()+".png");
        level.removeAllChildren();
        this.addChild(level);
        level.setPosition(new Vector2f(5, 99));
//        level.getDragBar().hide();
        
        Window name = new Window(this.screen, Vector2f.ZERO, new Vector2f(this.getDimensions().x, 15));
        name.removeAllChildren();
        this.addChild(name);
        name.centerToParent();
        name.setPosition(new Vector2f(level.getPosition().x, 20));
        name.setText(cardName);
        name.hideWindow();
        screen.updateZOrder(name);
//        name.getDragBar().hide();
    }    
}
