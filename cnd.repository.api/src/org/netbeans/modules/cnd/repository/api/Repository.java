/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.repository.api;

import java.util.Set;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;

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
     * retrieve object if it resides in memory cache; 
     * @param key the key of object to get
     * @return an object corresponding to key or null if there is no such object in memory cache
     */
    Persistent tryGet(Key key);
    
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
     * Prepare repositoty and tells the version of the persistent mechanism
     * @param verison 
     */
    void startup(int persistMechanismVersion);
    
    /**
     * Shuts down repository.
     * Should be called during application shutdown 
     */
    void shutdown();
    
    /**
     * Opens repository unit
     * @param unitName the unique identifier of the unit to open
     */

    void openUnit(int unitId, String unitName);
    /**
     * Close Repository Unit, e.g. Project for IDE
     * @param unitName the name of unit
     */
    void closeUnit(String unitName, boolean cleanRepository, Set<String> requiredUnits);
    
    /**
     * Removes repository unit from disk
     */
    void removeUnit(String unitName);
    
    /**
     * clean the disk caches of all repositories
     */
    void cleanCaches();
    
    /**
     * add a listener to the repository
     * @param aListener the listener
     */
    void registerRepositoryListener(final RepositoryListener aListener);
    
    /**
     * remove a listener from the repository
     * @param aListener the listener
     */
    void unregisterRepositoryListener(final RepositoryListener aListener);

}
