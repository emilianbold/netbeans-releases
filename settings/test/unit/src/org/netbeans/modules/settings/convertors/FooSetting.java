/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import java.util.Properties;

/**
 *
 * @author  Jan Pokorsky
 */
public class FooSetting {
    private final static String PROP_PROPERTY1 = "property1";
    
    /** Holds value of property property1. */
    private String property1;
    
    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
    /** Creates a new instance of FooSetting */
    public FooSetting() {
    }
    public FooSetting(String txt) {
        this.property1 = txt;
    }
    
    /** Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    /** Getter for property property1.
     * @return Value of property property1.
     */
    public String getProperty1() {
        return this.property1;
    }
    
    /** Setter for property property1.
     * @param property1 New value of property property1.
     */
    public void setProperty1(String property1) {
        String oldProperty1 = this.property1;
        this.property1 = property1;
        propertyChangeSupport.firePropertyChange(PROP_PROPERTY1, oldProperty1, property1);
    }
    
    private void readProperties(Properties p) {
        property1 = p.getProperty(PROP_PROPERTY1);
    }
    
    private void writeProperties(Properties p) {
        if (property1 == null) return;
        p.setProperty(PROP_PROPERTY1, property1);
    }
    
    public String toString() {
        return this.getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(this)) +
            '[' + property1 + ']';
    }
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FooSetting)) return false;
        FooSetting foo = (FooSetting) obj;
        if (property1 == null || foo.property1 == null) {
            return property1 == foo.property1;
        }
        return property1.equals(foo.property1);
    }
    
}
