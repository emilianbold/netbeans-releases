package org.netbeans.spi.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * PanelController creates visual representation of one Options Dialog
 * category, and manages communication between Options Dialog and this
 * panel.
 */
public abstract class OptionsPanelController {

    /**
     * Property name constant.
     */
    public static final String PROP_VALID = "valid";

    /**
     * Property name constant.
     */
    public static final String PROP_CHANGED = "changed";

    /**
     * Property name constant.
     */
    public static final String PROP_HELP_CTX = "helpCtx";

    /**
     * Component should load its data here. You should not do any 
     * time-consuming operations inside the constructor, because it 
     * blocks initialization of OptionsDialog. Initialization 
     * should be implemented in update method.
     */
    public abstract void update ();

    /**
     * This method is called when Options Dialog "OK" button is pressed.
     * This method can be called even before update () method is called.
     */
    public abstract void applyChanges ();

    /**
     * This method is called when Options Dialog "Cancel" button is pressed.
     * This method can be called even before update () method is called.
     */
    public abstract void cancel ();

    /**
     * Should returns <code>true</code> if some option value in this 
     * category is valid.
     * 
     * 
     * @return <code>true</code> if some option value in this 
     * category is valid
     */
    public abstract boolean isValid ();

    /**
     * Should returns <code>true</code> if some option value in this 
     * category has been changed.
     * 
     * 
     * @return <code>true</code> if some option value in this 
     * category has been changed
     */
    public abstract boolean isChanged ();

    /**
     * Each option category can provide some lookup. Options Dialog master
     * lookup is composed from these individual lookups. Master lookup
     * can be obtained from {@link #getComponent} call. This lookup is design
     * to support communication anong individual panels in one Options
     * Dialog.
     * 
     * 
     * @return lookup provided by this Options Dialog panel
     */
    public Lookup getLookup () {
        return Lookup.EMPTY;
    }

    /**
     * Returns visual component representing this options category.
     * 
     * 
     * @return visual component representing this options category
     */
    public abstract JComponent getComponent (Lookup masterLookup);

    /**
     * 
     * Get current help context asociated with this panel.
     * 
     * 
     * @return current help context
     */
    public abstract HelpCtx getHelpCtx ();

    /**
     * Registers new listener.
     * 
     * 
     * @param l a new listener
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Unregisters given listener.
     * 
     * 
     * @param l a listener to be removed
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}
