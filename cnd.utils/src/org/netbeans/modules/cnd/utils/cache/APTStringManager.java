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

package org.netbeans.modules.cnd.utils.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * APT string table manager
 * Responsibility:
 *  - only one instance per String object
 *  - based on weak references to allow GC of unused strings
 * 
 * @author Vladimir Voskresensky
 */
public abstract class APTStringManager  {
    
    public abstract CharSequence getString(CharSequence text);
    public abstract void dispose();

    private static final Map<String, APTSingleStringManager> instances = Collections.synchronizedMap(new HashMap<String, APTSingleStringManager>());

    private static final int STRING_MANAGER_DEFAULT_CAPACITY=1024;

    /*package*/ static final String TEXT_MANAGER="Manager of sharable texts"; // NOI18N
    /*package*/ static final int    TEXT_MANAGER_INITIAL_CAPACITY=STRING_MANAGER_DEFAULT_CAPACITY;
    /*package*/ static final String FILE_PATH_MANAGER="Manager of sharable file paths"; // NOI18N
    /*package*/ static final int    FILE_PATH_MANAGER_INITIAL_CAPACITY=STRING_MANAGER_DEFAULT_CAPACITY;
    
    public static APTStringManager instance(String kind) {
        return instance(kind, STRING_MANAGER_DEFAULT_CAPACITY);
    }
    
    /*package*/ static APTSingleStringManager instance(String kind, int initialCapacity) {
        APTSingleStringManager instance = instances.get(kind);
        if (instance == null) {
            instance = new APTSingleStringManager(initialCapacity);
            instances.put(kind, instance);
        }
        return instance;
    }  
        
    /*package*/ static final class APTSingleStringManager extends APTStringManager {
        private final WeakSharedSet<CharSequence> storage;

        /** Creates a new instance of APTStringManager */
        private APTSingleStringManager(int initialCapacity) {
            storage = new WeakSharedSet<CharSequence>(initialCapacity);
        }

        // we need exclusive copy of string => use "new String(String)" constructor
        private final String lock = new String("lock in APTStringManager"); // NOI18N

        /**
         * returns shared string instance equal to input text.
         * 
         * @param test - interested shared string 
         * @return the shared instance of text
         * @exception NullPointerException If the <code>text</code> parameter
         *                                 is <code>null</code>.
         */
        public final CharSequence getString(CharSequence text) {
            if (text == null) {
                throw new NullPointerException("null string is illegal to share"); // NOI18N
            }
            CharSequence outText = null;
            synchronized (lock) {
                outText = storage.addOrGet(text);
            }
            assert (outText != null);
            assert (outText.equals(text));
            return outText;
        }

        public final void dispose() {
            storage.clear();
        }
    }
    
    /*package*/ static final class APTCompoundStringManager extends APTStringManager {
        private final APTSingleStringManager[] instances;
        private final int capacity; // primary number for better distribution
        /*package*/APTCompoundStringManager(int capacity) {
            this.capacity = capacity;
            instances = new APTSingleStringManager[capacity];
            for (int i = 0; i < instances.length; i++) {
                instances[i] = APTStringManager.instance(APTStringManager.TEXT_MANAGER + i, APTStringManager.TEXT_MANAGER_INITIAL_CAPACITY);
            }
        }
        
        private APTStringManager getDelegate(CharSequence text) {
            if (text == null) {
                throw new NullPointerException("null string is illegal to share"); // NOI18N
            }            
            int index = text.hashCode() % capacity;
            if (index < 0) {
                index += capacity;
            }
            return instances[index];
        }
        
        public final CharSequence getString(CharSequence text) {
            return getDelegate(text).getString(text);
        }

        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                instances[i].dispose();
            }            
        }        
    }    
}
