/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.model.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;

/**
 * map-based implementation for CsmCacheManager.CsmCacheEntry.
 * @author Vladimir Voskresensky
 */
public class CsmCacheMap implements CsmCacheManager.CsmClientCache {
    private final long initTime;
    private final Map<Object, Object> values;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public CsmCacheMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public CsmCacheMap(int initialCapacity) {
        this.values = new HashMap<Object, Object>(initialCapacity);
        this.initTime = System.currentTimeMillis();
    }

    public final Object get(@NonNull Object key) {
        if (key == null) {
            throw new NullPointerException();
        }        
        return values.get(key);
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    public final Object put(@NonNull Object key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        return values.put(key, value);
    }

    @Override
    public void cleanup() {
        values.clear();
    }

    protected final Map<Object, Object> values() {
        return Collections.unmodifiableMap(values);
    }
    
    protected final long timeSinceInitialization() {
        return System.currentTimeMillis() - initTime;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<Object, Object> entry : values.entrySet()) {
            out.append(entry.getKey()).append("=").append(entry.getValue()).append("\n"); // NOI18N
        }
        return out.toString();
    }
    
}
