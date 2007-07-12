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

package org.netbeans.modules.cnd.repository.util;

import java.util.*;
import java.lang.ref.*;
import org.netbeans.modules.cnd.repository.testbench.Stats;

public class CachedSoftHashMap extends AbstractMap {
    private final Map hash = new HashMap();
    private final int hardCacheMaxSize;
    private final LinkedList hardCache = new LinkedList();
    private final ReferenceQueue queue = new ReferenceQueue();
    
    public CachedSoftHashMap() 
    { 
        this(1000); 
    }
    
    public CachedSoftHashMap(int hardSize) 
    { 
        hardCacheMaxSize = hardSize; 
        Stats.log("Hard cache size: " + hardSize); //NOI18N
    }
    
    public Object get(Object key) {
        Object result = null;
        
        SoftReference soft_ref = (SoftReference)hash.get(key);
        if (soft_ref != null) {
            result = soft_ref.get();
            if (result == null) {
                hash.remove(key);
            } else {
                hardCache.addFirst(result);
                if (hardCache.size() > hardCacheMaxSize) {
                    hardCache.removeLast();
                }
            }
        }
        return result;
    }
    
    private static class SoftValue extends SoftReference {
        private final Object key;
        private SoftValue(Object k, Object key, ReferenceQueue q) {
            super(k, q);
            this.key = key;
        }
    }
    
    private void processQueue() {
        SoftValue sv;
        int n = 0;
        while ((sv = (SoftValue)queue.poll()) != null) {
            hash.remove(sv.key); 
            n++;
        }
        if (n>0)
            Stats.log(n + " value(s) was GCed."); //NOI18N
    }
    
    
    public Object put(Object key, Object value) {
        processQueue(); 
        return hash.put(key, new SoftValue(value, key, queue));
    }
    
    public Object remove(Object key) {
        processQueue(); 
        return hash.remove(key);
    }
    
    public void clear() {
        hardCache.clear();
        processQueue(); 
        hash.clear();
    }
    
    public int size() {
        processQueue(); 
        return hash.size();
    }
    
    public Set entrySet() {
        throw new UnsupportedOperationException();
    }
}

// based on Dr. Heinz Kabutz example