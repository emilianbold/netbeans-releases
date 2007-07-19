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

package org.netbeans.modules.cnd.repository.impl;

import java.util.Set;
import org.netbeans.modules.cnd.repository.api.Repository;
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
            delegate = new HybridRepository();
        }        
    }

    public void hang(Key key, Persistent obj) {
        delegate.hang(key, obj);
    }

    public void put(Key key, Persistent obj) {
        delegate.put(key, obj);
    }

    public Persistent get(Key key) {
        return delegate.get(key);
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
