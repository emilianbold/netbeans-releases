/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.event;

import java.beans.PropertyChangeListener;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface TreeEventModel {
    
    /** Add a PropertyChangeListener to the listener list.
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener (PropertyChangeListener listener);
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param listener The listener to remove.
     */
    public void removePropertyChangeListener (PropertyChangeListener listener);
    
    /** Add a PropertyChangeListener for a specific property to the listener list.
     * @param propertyname Name of the property to listen on.
     * @param listener The listener to add.
     */
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener);
    
    /** Removes a PropertyChangeListener for a specific property from the listener list.
     * @param propertyname Name of the property that was listened on.
     * @param listener The listener to remove.
     */
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener listener);
    
    /**
     * Check if there are any listeners for a specific property.
     *
     * @param propertyName  the property name.
     * @return true if there are ore or more listeners for the given property
     */
    public boolean hasPropertyChangeListeners (String propertyName);
    
}
