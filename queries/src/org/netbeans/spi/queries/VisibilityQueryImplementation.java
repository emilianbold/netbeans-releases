/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.queries;

import org.openide.filesystems.FileObject;

import javax.swing.event.ChangeListener;

/**
 * Determine whether files should be hidden in viewes 
 * presented to the user. This query should be considered 
 * only as a recommendation and there isn't necessary to obey it.   
 * 
 * Global lookup is used to find all instances of VisibilityQueryImplementation.  
 *   
 * @see org.netbeans.api.queries.VisibilityQuery
 * @author Radek Matous 
 */ 
public interface VisibilityQueryImplementation {
    /**
     * Check whether an file is recommended to be visible. 
     * @param file a file which should be checked 
     * @return true if there is recommended to show this file 
     */ 
    boolean isVisible (FileObject file);

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
        
    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);    
}
