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

package org.netbeans.modules.cnd.repository.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.api.DatabaseTable;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;

/**
 * hash map based Repository
 * @author Vladimir Voskresensky
 */
public class HashMapRepository extends BaseRepository {

    @Override
    public void debugDistribution() {
    }

    @Override
    public DatabaseTable getDatabaseTable(Key unitKey, String tableID) {
        return null;
    }

    /** represents a single unit */
    private static class Unit {
        
        private final Map<Key,Persistent> map = new ConcurrentHashMap<Key,Persistent>();
        private final CharSequence name;
        private final int id;

        public Unit(int id, CharSequence name) {
            this.id = id;
            this.name = name;
        }
        
        public void put(Key key, Persistent obj) {
            assert key.getUnit().equals(name);
            map.put(key, obj);
        }
        
        public Persistent get(Key key) {
            assert key.getUnit().equals(name);
            return map.get(key);
        }
        
        void remove(Key key) {
            assert key.getUnit().equals(name);
            map.remove(key);
        }

        public int getId() {
            return id;
        }        
    }
    
    private final Map<Integer, Unit> units;
    private static final class Lock {}
    private final Object unitsLock = new Lock();
    
    /** 
     *  HashMapRepository creates from META-INF/services;
     *  no need for public constructor
     */
    public HashMapRepository(int id, CacheLocation cacheLocation) {
        super(id, cacheLocation);
        units = new ConcurrentHashMap<Integer, Unit>();
    }

    /** Never returns null */
    private Unit getUnit(Key key) {
        int id = key.getUnitId();
        CharSequence name = key.getUnit();
        assert name != null;
        Unit unit = units.get(id);
        if (unit == null) {
            synchronized (unitsLock) {
                unit = units.get(id);
                if (unit == null) {
                    unit = new Unit(id, name);
                    units.put(id, unit);
                }
            }
        }
        return unit;
    }
    
    @Override
    public void put(Key key, Persistent obj) {
        assert obj != null;
        getUnit(key).put(key, obj);
    }

    @Override
    public Persistent get(Key key) {
        return getUnit(key).get(key);
    }

    @Override
    public Persistent tryGet(Key key) {
	return get(key);
    }
    
    @Override
    public void remove(Key key) {
        getUnit(key).remove(key);
    }

    @Override
    public void hang(Key key, Persistent obj) {
        put(key, obj);
    }

    @Override
    public void debugClear() {
        // do nothing
    }
    
    @Override
    public void shutdown() {
        synchronized( unitsLock ) {
            units.clear();
        }
    }

    @Override
    public void openUnit(int unitId, CharSequence unitName) {
    }
    
    @Override
    public void closeUnit(int unitId, boolean cleanRepository, Set<Integer> requiredUnits) {
        removeUnit(unitId);
    }

    @Override
    public synchronized void removeUnit(int unitId) {
        for( Iterator<Unit> iter = units.values().iterator(); iter.hasNext(); ) {
            Unit unit = iter.next();
            if( unit.getId() == unitId) {
                synchronized( unitsLock ) {
                    iter.remove();
                    return;
                }
            }
        }
    }

    @Override
    public void cleanCaches() {
        // do nothing
    }

    @Override
    public void registerRepositoryListener(RepositoryListener aListener) {
        // do nothing
    }

    @Override
    public void unregisterRepositoryListener(RepositoryListener aListener) {
        // do nothing
    }

    @Override
    public void startup(int persistMechanismVersion) {
    }

}
