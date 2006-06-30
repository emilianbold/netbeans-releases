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

package org.netbeans.spi.queries;

import org.openide.filesystems.FileObject;

import javax.swing.event.ChangeListener;

/**
 * Determine whether files should be hidden in views presented to the user.
 * <p>
 * Global lookup is used to find all instances of VisibilityQueryImplementation.
 * </p>
 * <p>
 * Threading note: implementors should avoid acquiring locks that might be held
 * by other threads. Generally treat this interface similarly to SPIs in
 * {@link org.openide.filesystems} with respect to threading semantics.
 * </p>
 * @see org.netbeans.api.queries.VisibilityQuery
 * @author Radek Matous 
 */ 
public interface VisibilityQueryImplementation {
    /**
     * Check whether a file is recommended to be visible.
     * @param file a file to considered
     * @return true if it is recommended to display this file
     */ 
    boolean isVisible(FileObject file);

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
