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
