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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;

/**
 * hash map based Repository
 * @author Vladimir Voskresensky
 */
public class HashMapRepository implements Repository {
    private Map<Key,Persistent> map = Collections.synchronizedMap(new HashMap<Key,Persistent>());
    
    /** 
     *  HashMapRepository creates from META-INF/services;
     *  no need for public constructor
     */
    public HashMapRepository() {
    }
    
    public void put(Key key, Persistent obj) {
        assert key != null;
        assert obj != null;
        map.put(key, obj);
    }

    public Persistent get(Key key) {
        return map.get(key);
    }

    public Persistent tryGet(Key key) {
	return map.get(key);
    }
    
    public void remove(Key key) {
        map.remove(key);
    }

    public void hang(Key key, Persistent obj) {
        put(key, obj);
    }

    public void debugClear() {
        // do nothing
    }
    
    public void shutdown() {
        map.clear();
    }

    public void openUnit(String unitName) {
	// do nothing
    }
    
    public void closeUnit(String unitName, boolean cleanRepository, Set<String> requiredUnits) {
        // do nothing
    }
    
    public void removeUnit(String unitName) {
    }

    public void cleanCaches() {
        // do nothing
    }

    public void registerRepositoryListener(RepositoryListener aListener) {
        // do nothing
    }

    public void unregisterRepositoryListener(RepositoryListener aListener) {
        // do nothing
    }

    public void startup(int persistMechanismVersion) {
    }
    
}
