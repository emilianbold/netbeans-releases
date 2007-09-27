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