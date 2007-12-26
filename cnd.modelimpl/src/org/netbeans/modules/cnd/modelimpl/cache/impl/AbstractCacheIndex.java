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

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * base class for cache indexers
 * @author Vladimir Voskresensky
 */
abstract class AbstractCacheIndex implements Serializable {
    private static final long serialVersionUID = -7790789617759717718L;

    private Object indexLock = new Object();
    private Map<CharSequence, Object> index;

    protected AbstractCacheIndex() {
        index = new HashMap();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // index content support
    
    /** returns string key for caching object */
    protected abstract CharSequence getIndexKey(Object obj2cache);
    
    /** creates value object from cacheName */
    protected abstract Object createValue(CharSequence cacheName, Object obj2cache);
    
    /** get base cache name for the object */
    protected abstract CharSequence getBaseCacheName(Object obj2cache);
    
    /** checks if cacheName the same as defined in current value */
    protected abstract boolean isEqual(Object value, CharSequence checkCacheName);
    
    /** gets relative cache file for object in this indexer */
    protected Object get(Object obj) {
        CharSequence key = getIndexKey(obj);
        assert (key != null);
        synchronized (indexLock) {
            assert (index != null);
            return index.get(key);
        }
    }

    /** put object in indexer and returns relative cache file */
    protected Object put(Object obj) {
        CharSequence key = getIndexKey(obj);
        CharSequence baseValue = getBaseCacheName(obj);
        Object value = createUniqValueImpl(baseValue, obj);
        assert (key != null && key.length() > 0);
        assert (value != null);
        synchronized (indexLock) {
            assert (index != null);
            index.put(key, value);
        }
        return value;
    } 
    
    private Object createUniqValueImpl(CharSequence baseValue, Object obj2cache) {
        assert (baseValue != null);
        assert (baseValue.length() > 0);
        int postfix = 0;
        CharSequence value = baseValue;
        while (true) {
            boolean found = false;
            synchronized (indexLock) {
                for (Iterator it = index.values().iterator(); it.hasNext() && !found; ) {
                    Object elem = it.next();
                    if (isEqual(elem, value)) {
                        found = true;
                    }
                }
            }
            if (!found) {
                return createValue(value, obj2cache);
            }  else {
                value = baseValue.toString() + postfix++;
            }
        }
    }      
    ////////////////////////////////////////////////////////////////////////////
    // save/load support
    
    public boolean load(File file) {
        ObjectInputStream ois = null;
        try {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                in = new BufferedInputStream(in);
                ois = new ObjectInputStream(in);
            } finally {
                if (in != null && ois == null) {
                    in.close();
                }
            }
            if (ois != null) {
                loadData(ois);
                return true;
            }
        } catch (IOException io) {
            // null cache
        } catch (ClassNotFoundException e) {
            // null cache
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return false;
    }
    
    protected void loadData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // version 1:
        // - index map

        int version = ois.readInt();
        if (version >= 1) {
            Map index = (Map) ois.readObject();
            synchronized (indexLock) {
                this.index = index;
            }
        }
        assert (index != null);
    }
        
    public boolean save(File file) {
        ObjectOutputStream oos = null;
        try {
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os = new BufferedOutputStream(os);
                oos = new ObjectOutputStream(os);
            } finally {
                if (os != null && oos == null) {
                    os.close();
                }
            }
            if (oos != null) {
                saveData(oos);
                return true;
            }
        } catch (IOException io) {
            // null cache
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return false;
    }
    
    protected void saveData(ObjectOutputStream oos) throws IOException {
        // version
        int version = 1;
        oos.writeInt(version);
        
        // version 1:
        // - index map
        synchronized (indexLock) {
            oos.writeObject(index);
        }
    }   

    public String toString() {
        synchronized (indexLock) {
            return index.toString();
        }
    }
}
