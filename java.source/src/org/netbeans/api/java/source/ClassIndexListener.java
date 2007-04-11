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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import java.util.EventListener;

/**
 * Listener for changes in {@link ClassIndex}.
* <P>
* When attached to a {ClassIndex} it listens for addition,
* removal and modification of declared types.
* <P>
*
* @see ClassIndex#addClassIndexListener
 * @author Tomas Zezula
 */
public interface ClassIndexListener extends EventListener {
    
    /**
     * Called when the new declared types are added
     * into the {@link ClassIndex}
     * @param event specifying the added types
     */
    public void typesAdded (TypesEvent event);
    
    /**
     * Called when declared types are removed
     * from the {@link ClassIndex}
     * @param event specifying the removed types
     */
    public void typesRemoved (TypesEvent event);
        
    /**
     * Called when some declared types are changed.
     * @param event specifying the changed types
     */
    public void typesChanged (TypesEvent event);
    
    /**
     * Called when new roots are added
     * into the {@link ClassPath} for which the {@link ClassIndex}
     * was created.
     * @param event specifying the added roots
     */
    public void rootsAdded (RootsEvent event);
    
    /**
     * Called when root are removed
     * from the {@link ClassPath} for which the {@link ClassIndex}
     * was created.
     * @param event specifying the removed roots
     */
    public void rootsRemoved (RootsEvent event);
    
}
