package gamemode.editor.card;

import entitysystem.field.Collision;
import gamemode.gui.DialogWindowListener;
import gamemode.gui.Dialogwindow;
import gamemode.gui.EditorWindow;
import gamemode.gui.HexButtonListener;
import gamemode.gui.HexGridWindow;
import gamemode.gui.LayoutWindow;
import java.util.ArrayList;
import java.util.HashMap;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;
import utility.HexCoordinate;

/**
 *
 * @author roah
 */
public class CollisionWindow extends EditorWindow implements DialogWindowListener, HexButtonListener {

    private Collision collision;
    private HashMap<Character, HexGridWindow> hexWindow = new HashMap<Character, HexGridWindow>();
    private Dialogwindow popup;
    private char current = '0';

    public CollisionWindow(Screen screen, Element parent, byte areaRange) {
        super(screen, parent, "Collision Window");
        collision = new Collision();
        collision.addLayer((byte) 0, collision.new CollisionData(areaRange));
        populate();
    }

    public CollisionWindow(Screen screen, Element parent, Collision collision) {
        super(screen, parent, "Collision Window");
        this.collision = collision;
        populate();
    }

    private void populate() {
        ArrayList<String> layers = new ArrayList<String>();
        for (byte b : collision.getLayers()) {
            layers.add(Byte.toString(b));
        }
        layers.add("Edit layer");
        addButtonList("Layers", layers.toArray(new String[layers.size()]), LayoutWindow.HAlign.left);
        showConstrainToParent(VAlign.bottom, HAlign.left);
    }

    @Override
    protected void onButtonTrigger(String label) {
        if (label.equals("Edit layer")) {
            if (popup == null) {
                popup = new Dialogwindow(screen, "Edit layer", this);
                popup.addSpinnerField("Layer", new int[]{0, 7, 1, 2});
                popup.addSpinnerField("Radius", new int[]{2, 11, 1, 2});
                popup.show();
            } else if (!popup.isVisible()) {
                popup.setVisible();
            }
        } else if (hexWindow.containsKey(label.charAt(0))
                && hexWindow.get(label.charAt(0)).getRadius() != collision.getCollisionLayer(Byte.valueOf(label)).getAreaRadius()) {
            hexWindow.get(label.charAt(0)).reload(collision.getCollisionLayer(Byte.valueOf(label)).getAreaRadius(), 
                    collision.getCollisionLayer(Byte.valueOf(label)).getCoord());
            updateCurrent(label);
        } else if (hexWindow.containsKey(label.charAt(0))) {
            if (current != label.charAt(0)) {
                if (hexWindow.get(current) != null && hexWindow.get(current).isVisible()) {
                    hexWindow.get(current).hide();
                }
                hexWindow.get(label.charAt(0)).setVisible();
            } else {
                if (hexWindow.get(current).isVisible()) {
                    hexWindow.get(current).hide();
                } else {
                    hexWindow.get(current).setVisible();
                }
            }
            current = label.charAt(0);
        } else if (!hexWindow.containsKey(label.charAt(0))) {
            if(collision.getCollisionLayer(Byte.valueOf(label)).getCoord().isEmpty()){
                hexWindow.put(label.charAt(0),
                        new HexGridWindow(screen, collision.getCollisionLayer(Byte.valueOf(label)).getAreaRadius(),
                        Byte.valueOf(label), getWindow(), this));
            } else {
                hexWindow.put(label.charAt(0),
                        new HexGridWindow(screen, collision.getCollisionLayer(Byte.valueOf(label)).getAreaRadius(),
                        Byte.valueOf(label), collision.getCollisionLayer(Byte.valueOf(label)).getCoord(), getWindow(), this));
                
            }
            updateCurrent(label);
        }
    }

    private void updateCurrent(String label) {
        if (hexWindow.get(current) != null && current != label.charAt(0)) {
            hexWindow.get(current).hide();
        } else if (hexWindow.get(current) != null) {
            return;
        }
        current = label.charAt(0);
    }

    private void reload() {
        removeFromScreen();
        populate();
        for (HexGridWindow w : hexWindow.values()) {
            w.setParent(getWindow());
        }
    }

    public void onDialogTrigger(String dialogUID, boolean confirmOrCancel) {
        if (confirmOrCancel && dialogUID.equals("Edit layer")) {
            if (collision.getCollisionLayer((byte) popup.getSpinnerInput("Layer")) == null) {
                collision.addLayer((byte) popup.getSpinnerInput("Layer"), collision.new CollisionData((byte) popup.getSpinnerInput("Radius")));
            } else if (collision.getCollisionLayer((byte) popup.getSpinnerInput("Layer")).getAreaRadius() != (byte) popup.getSpinnerInput("Radius")) {
                collision.getCollisionLayer((byte) popup.getSpinnerInput("Layer")).setAreaRadius((byte) popup.getSpinnerInput("Radius"));
            }
            reload();
            onButtonTrigger(Integer.toString(popup.getSpinnerInput("Layer")));
        }
        popup.hide();
    }

    public Collision getCollision() {
        return collision;
    }

    public void onButtonTrigger(HexCoordinate pos, boolean selected) {
        if (selected) {
            collision.getCollisionLayer(Byte.valueOf(String.valueOf(current))).addPosition(pos);
        } else {
            collision.getCollisionLayer(Byte.valueOf(String.valueOf(current))).removePosition(pos);
        }
    }
}