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

package org.netbeans.modules.cnd.repository.impl;

import java.util.Set;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.disk.DiskRepositoryManager;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.RepositoryListenersManager;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class DelegateRepository implements Repository {
    
    private final Repository delegate;
    
    public DelegateRepository() {
        if (Stats.validateKeys) {
            Stats.log("Testing keys using KeyValidatorRepository."); // NOI18N
            delegate = new KeyValidatorRepository();
        } else if (Stats.useHardRefRepository) {
            Stats.log("Using HashMapRepository."); // NOI18N
            delegate = new HashMapRepository ();
        } else {
            Stats.log("by default using HybridRepository."); // NOI18N
            delegate = new DiskRepositoryManager();
        }        
    }

    public void hang(Key key, Persistent obj) {
        delegate.hang(key, obj);
    }

    public void put(Key key, Persistent obj) {
        delegate.put(key, obj);
    }

    public Persistent get(Key key) {
        Persistent result = delegate.get(key);
	if( result == null && Stats.useNullWorkaround ) {
	    String keyClassName = key.getClass().getName();
	    // repository is often asked for projects when theis persistence just does not exist
	    if( ! keyClassName.endsWith(".ProjectKey") && ! keyClassName.endsWith(".OffsetableDeclarationKey") ) { // NOI18N
		System.err.printf("NULL returned for key %s on attempt 1\n", key);
		result = delegate.get(key);
		System.err.printf("%s value returned for key %s on attempt 2\n", (result == null) ? "NULL" : "NON-NULL", key);
	    }
	}
	return result;
    }
    
    public Persistent tryGet(Key key) {
	return delegate.tryGet(key);
    }

    public void remove(Key key) {
        delegate.remove(key);
    }

    public void debugClear() {
        delegate.debugClear();
    }

    public void shutdown() {
        delegate.shutdown();
    }

    
    public void openUnit(int unitId, String unitName) {
        delegate.openUnit(unitId, unitName);
    }
    
    public void closeUnit(String unitName, boolean cleanRepository, Set<String> requiredUnits) {
        delegate.closeUnit(unitName, cleanRepository, requiredUnits);
    }
    
    public void removeUnit(String unitName) {
        delegate.removeUnit(unitName);
    }

    public void cleanCaches() {
        delegate.cleanCaches();
    }

    public void registerRepositoryListener(RepositoryListener aListener) {
        RepositoryListenersManager.getInstance().registerListener(aListener);
    }

    public void unregisterRepositoryListener(RepositoryListener aListener) {
        RepositoryListenersManager.getInstance().unregisterListener(aListener);
    }

    public void startup(int persistMechanismVersion) {
        delegate.startup(persistMechanismVersion);
    }
}
