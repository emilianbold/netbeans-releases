/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.repository.api;

import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 * @author Sergey Grinev
 */
public interface Repository {
    /**
     * store object without option to be persistent, 
     * either to register object for later put
     * or to store temporary inpersistable object 
     * @param key the key
     * @param obj the object to store
     */
    void hang(Key key, Persistent obj); 
    
    /**
     * store object
     * @param key the key
     * @param obj the object to store
     */
    void put(Key key, Persistent obj); 
    
    /**
     * retrieve object
     * @param key the key of object to get
     * @return an object corresponding to key or null if there is no such one
     */
    Persistent get(Key key); 
    
    /**
     * stop storing object
     * @param key the key of object to remove
     */
    void remove(Key key); 
    
    /**
     * store all objects to permanent location 
     * should be called during IDE shutdown 
     */
    void debugClear();     
    
    /**
     * Shuts down repository.
     * Should be called during application shutdown 
     */
    void shutdown();
    
    /**
     * Close Repository Unit, e.g. Project for IDE
     * @param unitName the name of unit
     */
    void closeUnit(String unitName, boolean cleanRepository);
}
