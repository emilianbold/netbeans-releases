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
