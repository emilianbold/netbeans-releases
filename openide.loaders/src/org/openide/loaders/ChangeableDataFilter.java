/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import javax.swing.event.ChangeListener;

/** Allows certain data objects to be excluded from being displayed.
* @see RepositoryNodeFactory
* @author Jaroslav Tulach
*/
public interface ChangeableDataFilter extends DataFilter {
    
    /** Adds a ChangeListener to the filter. The ChangeListeners must be notified 
     * when the filtering strategy changes.
     * @param listener The ChangeListener to add
     */
    public void addChangeListener( ChangeListener listener );
    
    /** Removes a ChangeListener from the filter. 
     * @param listener The ChangeListener to remove.
     */
    public void removeChangeListener( ChangeListener listener );
    
}
