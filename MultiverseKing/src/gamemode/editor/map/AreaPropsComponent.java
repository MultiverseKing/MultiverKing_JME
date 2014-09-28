package gamemode.editor.map;

import com.simsilica.es.PersistentComponent;

/**
 * Component used for object on the Room field.
 * @author roah
 */
public class AreaPropsComponent implements PersistentComponent {

    private boolean isTrigger = false;
    private boolean isImmune = false;

    public AreaPropsComponent() {
    }

    /**
     * 
     * @param isTrigger Define if the entity can be activated like a button by a
     * character.
     * @param isImmune Define if the entity respond to undirect effect as when a
     * firebold hit it.
     */
    public AreaPropsComponent(boolean isTrigger, boolean isImmune) {
        this.isTrigger = isTrigger;
        this.isImmune = isImmune;
    }

    public boolean isIsTrigger() {
        return isTrigger;
    }

    public boolean isIsImmune() {
        return isImmune;
    }
}