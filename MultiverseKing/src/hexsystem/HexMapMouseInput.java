package hexsystem;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import hexsystem.events.HexMapInputEvent;
import hexsystem.events.HexMapInputListener;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import hexsystem.events.HexMapRayListener;
import hexsystem.events.TileChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import kingofmultiverse.MultiverseMain;
import utility.HexCoordinate;
import utility.MouseRay;

/**
 *
 * @author Eike Foede, roah
 */
public class HexMapMouseInput extends AbstractAppState {

    private final MouseRay mouseRay = new MouseRay();    //@see utility/MouseRay.
    private final float cursorOffset = -0.15f;         //Got an offset issue with hex_void_anim.png this will solve it temporary
    private MultiverseMain app;
    private ArrayList<HexMapInputListener> hexMapListeners = new ArrayList<HexMapInputListener>();
    private ArrayList<HexMapRayListener> rayListeners = new ArrayList<HexMapRayListener>(3);
    private Spatial cursor;
    private Spatial rayDebug;
    private int listenerPulseIndex = -1;
    private HexCoordinate lastHexPos;
    private Vector2f lastScreenMousePos = new Vector2f(0, 0);
    private MapData mapData;

    public Spatial getRayDebug() {
        return rayDebug;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.app = (MultiverseMain) app;
        mapData = stateManager.getState(HexSystemAppState.class).getMapData();
        initMarkDebug();
        initInput();
    }

    /**
     * Register a listener to respond to Tile Input.
     *
     * @param listener to register.
     */
    public void registerTileInputListener(HexMapInputListener listener) {
        hexMapListeners.add(listener);
    }

    /**
     * Remove a listener to respond to Tile Input.
     *
     * @param listener to register.
     */
    public void removeTileInputListener(HexMapInputListener listener) {
        hexMapListeners.remove(listener);
    }

    /**
     * Add a listener for the mouse Raycasting.
     *
     * @param listener
     */
    public void registerRayInputListener(HexMapRayListener listener) {
        rayListeners.add(listener);
    }

    /**
     * Remove a listener from the mouse Raycasting.
     *
     * @param listener
     */
    public void removeRayInputListener(HexMapRayListener listener) {
        rayListeners.remove(listener);
    }

    /**
     * Base input, it not depend on the gameMode or other thing if hexMap is
     * instanced that mean Tiles is or will be instanced so this input too.
     */
    private void initInput() {
        app.getInputManager().addListener(tileActionListener, new String[]{"Confirm", "Cancel"});
    }
    private final ActionListener tileActionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Confirm") && isPressed) {
                if (listenerPulseIndex == -1) {
                    castRay("L");
                } else {
                    hexMapListeners.get(listenerPulseIndex).leftMouseActionResult(
                            new HexMapInputEvent(mapData.convertWorldToGridPosition(cursor.getLocalTranslation()), null));
                }
            } else if (name.equals("Cancel") && isPressed) {
                if (listenerPulseIndex == -1) {
                    castRay("R");
                }
            }
        }
    };

    private void initMarkDebug() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        rayDebug = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        rayDebug.setMaterial(mark_mat);
    }

    private void initCursor() {
        cursor = app.getAssetManager().loadModel("Models/utility/animPlane.j3o");
        Material animShader = app.getAssetManager().loadMaterial("Materials/animatedTexture.j3m");
        animShader.setInt("Speed", 16);
        cursor.setMaterial(animShader);
        app.getRootNode().attachChild(cursor);
        //Remove offset and set it to zero if hex_void_anim.png is not used
        float z = mapData.getTile(new HexCoordinate(HexCoordinate.OFFSET, 0, 0)).getHeight() * HexSettings.FLOOR_HEIGHT + 0.01f;
        cursor.setLocalTranslation(new Vector3f(0f, z + 0.01f, cursorOffset));
        System.out.println(HexSettings.GROUND_HEIGHT * HexSettings.FLOOR_HEIGHT + " + " + z + 0.01f);
    }

    @Override
    public void update(float tpf) {
        if (listenerPulseIndex != -1) {
            Vector2f newMousePos = app.getInputManager().getCursorPosition().normalize();
            if (!newMousePos.equals(lastScreenMousePos)) {
                castRay("0");
                lastScreenMousePos = newMousePos;
            }
        }
    }

    /**
     * Activate the cursor on pulse mode, Raycast will follow the mouse, Have to
     * be called by the the same listener to disable. The pulse mode lock other
     * update.
     *
     * @todo Ray listener support
     * @param listener calling for it.
     * @return false if an error happen or if already on pulseMode.
     */
    public boolean setCursorPulseMode(HexMapInputListener listener) {
        if (listenerPulseIndex == -1) {
            //We keep track of the listener locking the input.
            if (!hexMapListeners.contains(listener)) {
                hexMapListeners.add(listener);
            }
            listenerPulseIndex = hexMapListeners.indexOf(listener);
            lastScreenMousePos = app.getInputManager().getCursorPosition();
            return true;
        } else {
            /**
             * We check if the listener calling the pulseMode is the same than
             * the one who activated it. if it is the same we desable the pulse
             * mode.
             */
            if (hexMapListeners.contains(listener) && hexMapListeners.indexOf(listener) == listenerPulseIndex) {
                listenerPulseIndex = -1;
                return true;
            } else if (hexMapListeners.contains(listener) && hexMapListeners.indexOf(listener) != listenerPulseIndex) {
                System.err.println("Pulse already locked by : " + hexMapListeners.get(listenerPulseIndex).getClass().toString()
                        + ". Lock request by : " + listener.toString());
                return false;
            } else {
                System.err.println("listener not registered : " + listener.toString());
                return false;
            }
        }
    }

    private void castRay(String mouseInput) {
        CollisionResults results = new CollisionResults();
        Ray ray = mouseRay.get3DRay(app);
        HexMapInputEvent event = callRayActionListeners(mouseInput, ray);

        if (event == null) {
            app.getRootNode().getChild("mapNode").collideWith(ray, results);
            if (results.size() != 0) {
                if (results.size() > 0) {
                    CollisionResult closest = results.getClosestCollision();

                    rayDebug.setLocalTranslation(closest.getContactPoint());
                    app.getRootNode().attachChild(rayDebug);    //TODO Debug to remove.

                    HexCoordinate newPos = convertMouseCollision(results);
                    if (newPos != null && !newPos.equals(lastHexPos)) {
                        event = new HexMapInputEvent(newPos, ray);
                        moveCursor(newPos);
                        callMouseActionListeners(mouseInput, event);
                    }
                } else if (app.getRootNode().hasChild(rayDebug)) {
                    // No hits? Then remove the red mark.
                    app.getRootNode().detachChild(rayDebug);    //TODO Debug to remove.
                } else {
                    System.out.println("no  mark");
                }
            } else {
                //Error catching.
                System.out.println("null raycast");
            }
        } else {
            if (!event.getEventPosition().equals(lastHexPos)) {
                moveCursor(event.getEventPosition());
                callMouseActionListeners(mouseInput, event);
            }
        }
    }

    /**
     * @param mouseInput L or R listener to call
     * @param event event to pass
     */
    private void callMouseActionListeners(String mouseInput, HexMapInputEvent event) {
        for (HexMapInputListener l : hexMapListeners) {
            if (mouseInput.contains("L")) {
                l.leftMouseActionResult(event);
            } else if ((mouseInput.contains("R"))) {
                l.rightMouseActionResult(event);
            } else {
                return; //in case of...
            }
        }
    }

    /**
     * @todo When multiple ray listeners run on same time, the closest got the
     * event.
     * @param mouseInput
     * @param ray
     */
    private HexMapInputEvent callRayActionListeners(String mouseInput, Ray ray) {
        HexMapInputEvent event = null;
        for (HexMapRayListener l : rayListeners) {
            if (mouseInput.contains("L")) {
                event = l.leftRayInputAction(ray);
            } else if ((mouseInput.contains("R"))) {
                event = l.rightRayInputAction(ray);
            }
        }
        return event;
    }

    public void moveCursor(HexCoordinate tilePos) {
        if (cursor == null) {
            initCursor();
        }
        Vector3f pos = mapData.getTileWorldPosition(tilePos);
        cursor.setLocalTranslation(pos.x, mapData.getTile(tilePos).getHeight() * HexSettings.FLOOR_HEIGHT
                + ((tilePos.getAsOffset().y & 1) == 0 ? 0.01f : 0.02f), pos.z + cursorOffset);
        /**
         * The cursor real position is not updated on pulseMode.
         */
        if (listenerPulseIndex == -1) {
            lastHexPos = tilePos;
        }
    }

    /**
     *
     * @return
     */
    private HexCoordinate convertMouseCollision(CollisionResults rayResults) {
        HexCoordinate tilePos;
        Vector3f pos;
        Iterator<CollisionResult> i = rayResults.iterator();

        do {
            pos = i.next().getContactPoint();
            tilePos = mapData.convertWorldToGridPosition(pos);
            if (mapData.getTile(tilePos) == null) {
                break;
            } else {
                return tilePos;
            }/*else if (mapData.getTile(tilePos).getHeight() 
             * == (byte)FastMath.floor(pos.y/mapData.getHexSettings().getFloorHeight())){
             return tilePos;
             }*/
        } while (i.hasNext());

        return null;
    }

    /**
     *
     * @param event
     */
    public void tileChange(TileChangeEvent event) {
        if (cursor == null) {
            initCursor();
        }
        if (mapData.convertWorldToGridPosition(cursor.getLocalTranslation()).equals(event.getTilePos())) {
            cursor.setLocalTranslation(cursor.getLocalTranslation().x, event.getNewTile().getHeight()
                    * HexSettings.FLOOR_HEIGHT + 0.1f, cursor.getLocalTranslation().z);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
