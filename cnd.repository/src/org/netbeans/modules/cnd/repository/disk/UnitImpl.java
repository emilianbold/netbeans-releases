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

package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.repository.sfs.FileStorage;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.util.Pair;

/**
 * Implements a repository unit
 * @author Nickolay Dalmatov
 * @author Vladimir Kvashin
 */
public final class UnitImpl implements Unit {
    
    private Storage    singleFileStorage;
    private Storage    multyFileStorage;
    private final String unitName;
    private final MemoryCache cache;
    
    public UnitImpl(final String unitName) throws IOException {
       assert unitName != null;
       this.unitName = unitName;
       singleFileStorage = FileStorage.create(new File(StorageAllocator.getInstance().getUnitStorageName(unitName)));
       multyFileStorage = new MultyFileStorage(this.getName());
       cache = new MemoryCache();
    }
    
    private Storage getStorage(Key key) {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        if (key.getBehavior() == Key.Behavior.Default) {
            return singleFileStorage;
        } else {
            return multyFileStorage;
        }
    }

    public Persistent get(Key key) throws IOException {
        assert key != null;
        // I commented a next assertion because it is too expensive.
        // Use another way to control the assertion. For example by flag in unit tests.
        //assert getName().equals(key.getUnit().toString());
        Persistent data = cache.get(key);
        if (data == null) {
            data = getStorage(key).read(key);
            if (data != null) {
                // no syncronization here!!!
                // the only possible collision here is lost of element, which is currently being deleted
                // by processQueue - it will be reread
                cache.put(key, data, false);
            }
        }
        return data;
    }
    
    public void putToCache(Key key, Persistent obj) {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        cache.put(key, obj, true);
    }
    
    public void hang(Key key, Persistent obj) {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        cache.hang(key, obj);
    }
    
    public Persistent tryGet(Key key) {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        return cache.get(key);
    }

    public void removePhysically(Key key) throws IOException {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        getStorage(key).remove(key);
    }
    
    public void removeFromCache(Key key) {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        cache.remove(key);
    }

    public void close() throws IOException {
        Collection<Pair<Key, Persistent>> hung = cache.clearHungObjects();
        for( Pair<Key, Persistent> pair : hung ) {
            putPhysically(pair.first, pair.second);
        }
        singleFileStorage.close();
        multyFileStorage.close();
    }

    public void putPhysically(Key key, Persistent object) throws IOException {
        assert key != null;
        assert getName().equals(key.getUnit().toString());
        assert object != null;
        getStorage(key).write(key, object);
    }

    public boolean maintenance(long timeout) throws IOException {
        return singleFileStorage.defragment(timeout);
    }

    public int getMaintenanceWeight() throws IOException {
        return singleFileStorage.getFragmentationPercentage();
    }

    public String getName() {
        return unitName;
    }

    public void debugClear() {
        cache.clearSoftRefs();
    }
}
