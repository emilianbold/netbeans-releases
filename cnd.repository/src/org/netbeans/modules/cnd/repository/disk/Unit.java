/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.repository.disk;

import java.io.IOException;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Represents a repository unit.
 * Repository implementation delegates all operation to units.

 * Typically, a unit is backed with physical storage (see Stoage interface)
 * and also has an in-memory cache
 * 
 * @author Sergey Grinev
 * @author Vladimir Kvashin
 */
public interface Unit {
    
    /** 
     * Gets this unit unique name. 
     * This name should be the same as Key.getUnit()
     */
    public String getName();

    /** 
     * Gets the object stored by the given key 
     * Typically, the implementation first looks it up in a cache,
     * then, if not found, reads it from te Storage its's backed with
     */
    public Persistent get(Key key) throws IOException;
    
    /**
     * The writing operations are queued and performed in a separate thread.
     * 
     * This thread communicates with the central repository implementation,
     * which in turn delegates requests to units.
     * 
     * That's why put and remove operations are separated into
     * cache-related and physicall operations.
     * 
     * This design is quite arguable; anyhow I'm not going to change at that point
     * 
     * This method puts the object to in-memory cache;
     * it's the caller's responsibility to enqueue writing this object
     * into physical storage
     */
    public void putToCache(Key key, Persistent obj);
    
    /** 
     * Writes object to physical storage
     * (see comment to putToCache method)
     */
    public void putPhysically(Key key, Persistent object)  throws IOException ;
    
    /** 
     * Removes the object from cache
     * (see comment to putToCache method)
     */
    public void removeFromCache(Key key);
    
    /**
     * Removes the given from physical storage
     * (see comment to putToCache method)
     */
    public void removePhysically(Key key) throws IOException;
    
    
    /** 
     * Places object into hard-ref map
     */
    public void hang(Key key, Persistent obj);

    /** 
     * Tries to get the object from in-memory cache 
     * @return object in the case it resides in cache, otherwise null
     */
    public Persistent tryGet(Key key);
    
    
    /** Closes the unit */
    public void close() throws IOException;
    
    
    /** 
     * Clears in-memory cache. 
     * Used for testing purposes 
     */
    public void debugClear();
    
    /**
     * Show distributions of object in repository
     */
    void debugDistribution();

    /** 
     * Determines the necessity of maintenance.
     * When a maintenancy is to be done, repository
     * sorts all units in accordance with the returned value.
     * So greater is the value, more need in maintenance unit has.
     */
    public int getMaintenanceWeight() throws IOException;
    
    /**
     * Performes necessary maintenance (such as defragmentation) during the given timeout
     * @return true if maintenance was finished by timeout and needs more time to be completed
     */
    public boolean maintenance(long timeout)  throws IOException;
    
    
}
