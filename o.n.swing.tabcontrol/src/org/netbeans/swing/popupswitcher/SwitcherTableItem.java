/*
 * SwitcherTableItem.java
 *
 * Created on November 12, 2004, 12:41 PM
 */

package org.netbeans.swing.popupswitcher;

import javax.swing.Icon;

public class SwitcherTableItem {
    
    /** Item's description */
    private String name;
    
    /** Item's icon */
    private Icon icon;
    
    /** Indicates whether this item is active or not */
    private boolean active;
    
    /**
     * Object to be activated. This is up to concrete <code>PopupSwitcher</code>
     * implementation.
     */
    private Activable activable;
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name) {
        this(activable, name, null);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name, Icon icon) {
        this(activable, name, icon, false);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activable activable, String name, Icon icon,
            boolean active) {
        this.activable = activable;
        this.icon = icon;
        this.name = name;
        this.active = active;
    }
    
    public void activate() {
        activable.activate();
    }
    
    public String getName() {
        return name;
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public String toString() {
        return super.toString() + "[name=" + name + ", icon=" + icon + "]"; // NOI18N
    }
    
    /**
     * This interface has to be implemented and passed to the
     * <code>SwitcherTableItem</code> constructor.
     */
    public static interface Activable {
        void activate();
    }
}
