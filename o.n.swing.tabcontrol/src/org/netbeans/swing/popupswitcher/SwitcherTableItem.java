/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
public class SwitcherTableItem implements Comparable {

    /** Item's name. Base name used by the <code>SwitcherTable</code> */
    private String name;

    /** Item's html-based name. */
    private String htmlName;

    /** Item's description. Text which can be used for arbitrary purpose. */
    private String description;

    /** Item's icon */
    private Icon icon;
    
    /** Indicates whether this item is active or not */
    private boolean active;
    
    /**
     * Object to be activated. This is up to concrete <code>PopupSwitcher</code>
     * implementation.
     */
    private Activatable activatable;
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activatable activatable, String name) {
        this(activatable, name, null);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activatable activatable, String name, Icon icon) {
        this(activatable, name, name, icon, false);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activatable activatable, String name, String htmlName,
            Icon icon, boolean active) {
        this(activatable, name, htmlName, icon, active, null);
    }
    
    /** Creates a new instance of SwitcherTableItem */
    public SwitcherTableItem(Activatable activatable, String name, String htmlName,
            Icon icon, boolean active, String description) {
        this.activatable = activatable;
        this.name = name;
        this.htmlName = htmlName;
        this.icon = icon;
        this.active = active;
        this.description = description;
    }
    
    /**
     * Calls <code>activate()</code> method of <code>Activatable</code> interface
     * which has to be passed in a constructor.
     * 
     * @see SwitcherTableItem.Activatable#activate
     */
    public void activate() {
        activatable.activate();
    }
    
    /** Returns item's name */
    public String getName() {
        return name;
    }
    
    /** Returns item's html name. */
    public String getHtmlName() {
        return htmlName;
    }
    
    /**
     * Return item's description - the text which can be used for arbitrary
     * purpose. E.g. </code>KeyboardPopupSwitcher</code> uses it for statusbar
     * text.
     */
    public String getDescription() {
        return description;
    }
    
    /** Returns item's icon */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * Returns item's activatable object
     */
    public Activatable getActivatable() {
        return activatable;
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
     * Returns true if the <code>name</code> and <code>activatable</code> are the
     * same as passed one.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SwitcherTableItem) {
            SwitcherTableItem item = (SwitcherTableItem) o;
            boolean result = item.getName().equals(name) &&
                    item.getActivatable().equals(activatable);
            return result;
        } else {
            return false;
        }
    }
    
    /**
     * Returns a hash code value for the item.
     *
     * @return int hashcode
     */
    public int hashCode() {
        return (name == null ? 1 : name.hashCode()) * activatable.hashCode();
    }
    
    /**
     * Compares items based on theirs <code>name</code>s. Items which has
     * null-name will be last.
     */
    public int compareTo(Object o) {
        String name1 = getName();
        String name2 = null;
        if (o instanceof SwitcherTableItem) {
            name2 = ((SwitcherTableItem) o).getName();
        }
        if (name2 == null) {
            return (name1 == null ? 0 : -1);
        } else {
            return (name1 == null ? 1 : name1.compareToIgnoreCase(name2));
        }
    }
    
    /**
     * This interface has to be implemented and passed to the
     * <code>SwitcherTableItem</code> constructor.
     */
    public static interface Activatable {
        /**
         * Here should be code witch <em>activate</em> this item. The method
         * <code>SwitcherTableItem.activate()</code> conveniently call this
         * method. So you never need to call this method directly.
         *
         * @see SwitcherTableItem#activate
         */
        void activate();
    }
}
