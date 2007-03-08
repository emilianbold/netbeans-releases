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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.api.Repository;

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

    public void remove(Key key) {
        map.remove(key);
    }

    public void hang(Key key, Persistent obj) {
        put(key, obj);
    }

    public void flush() {
        // do nothing
    }
    
    public void shutdown() {
        // do nothing
    }
    
}
