/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.popupswitcher;

import javax.swing.Icon;

/**
 * Represents one item in <code>SwitcherTable</class>.
 *
 * @see SwitcherTable
 *
 * @author mkrauskopf
 */
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
    
    /**
     * Calls <code>activate()</code> method of <code>Activable</code> interface
     * which has to be passed in a constructor.
     */
    public void activate() {
        activable.activate();
    }
    
    /** Returns item's name */
    public String getName() {
        return name;
    }
    
    /** Returns item's icon */
    public Icon getIcon() {
        return icon;
    }
    
    /** Returns whether this item is active or not. */
    public boolean isActive() {
        return active;
    }
    
    /** Returns human readable description of this item */
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
