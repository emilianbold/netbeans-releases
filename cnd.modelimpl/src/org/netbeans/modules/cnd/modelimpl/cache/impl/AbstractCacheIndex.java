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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.Collections;
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
    private Map/*<String, Object>*/ index;

    protected AbstractCacheIndex() {
        index = new HashMap();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // index content support
    
    /** returns string key for caching object */
    protected abstract String getIndexKey(Object obj2cache);
    
    /** creates value object from cacheName */
    protected abstract Object createValue(String cacheName, Object obj2cache);
    
    /** get base cache name for the object */
    protected abstract String getBaseCacheName(Object obj2cache);
    
    /** checks if cacheName the same as defined in current value */
    protected abstract boolean isEqual(Object value, String checkCacheName);
    
    /** gets relative cache file for object in this indexer */
    protected Object get(Object obj) {
        String key = getIndexKey(obj);
        assert (key != null);
        synchronized (indexLock) {
            assert (index != null);
            return index.get(key);
        }
    }

    /** put object in indexer and returns relative cache file */
    protected Object put(Object obj) {
        String key = getIndexKey(obj);
        String baseValue = getBaseCacheName(obj);
        Object value = createUniqValueImpl(baseValue, obj);
        assert (key != null && key.length() > 0);
        assert (value != null);
        synchronized (indexLock) {
            assert (index != null);
            index.put(key, value);
        }
        return value;
    } 
    
    private Object createUniqValueImpl(String baseValue, Object obj2cache) {
        assert (baseValue != null);
        assert (baseValue.length() > 0);
        int postfix = 0;
        String value = baseValue;
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
                value = baseValue + postfix++;
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
