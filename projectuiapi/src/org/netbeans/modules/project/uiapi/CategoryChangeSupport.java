/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Support for listening on changes in a category's properties. Separated from
 * API.
 *
 * @author Martin Krauskopf
 */
public class CategoryChangeSupport {
    
    public static final CategoryChangeSupport NULL_INSTANCE = new CategoryChangeSupport() {
        public void firePropertyChange(String pn, Object o, Object n) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        void addPropertyChangeListener(PropertyChangeListener l) {}
    };
    
    private PropertyChangeSupport changeSupport;
    
    /** Name for the <code>valid</code> property. */
    public static final String VALID_PROPERTY = "isCategoryValid"; // NOI18N
    
    /** Property for an error message of the category. */
    public static final String ERROR_MESSAGE_PROPERTY = "categoryErrorMessage"; // NOI18N
    
    synchronized void addPropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }
    
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener listener) {
        if (listener == null || changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(listener);
    }
    
    
    public void firePropertyChange(String propertyName,
            Object oldValue, Object newValue) {
        if (changeSupport == null ||
                (oldValue != null && newValue != null && oldValue.equals(newValue))) {
            return;
        }
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
}
