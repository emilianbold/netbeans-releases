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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;

/**
 * cache index support for project cache manager
 * 
 * @author Vladimir Voskresensky
 */
final class ProjectIndex extends AbstractCacheIndex implements Serializable {
    private static final long serialVersionUID = -7790789617759717723L;

    // map contains absolute path of file and entry (cacheName,lastModified)
    
    public Entry getFileEntry(CsmFile file) {
        Entry value = (Entry) super.get(file);
        return value;
    }

    public Entry putFile(CsmFile file) {
        Entry value = (Entry) super.put(file);
        return value;
    }

    public void invalidateFile(String absPath) {
        Entry value = (Entry) super.get(absPath);
        if (value != null) {
            value.setLastModified(0);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // save/load implmentation

    protected void loadData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int version = ois.readInt();
        if (version >= 1) {
            // version 1
            //  - load base data
            super.loadData(ois);
        }
    }

    protected void saveData(ObjectOutputStream oos) throws IOException {
        int version = 1;
        oos.writeInt(version);

        // version 1
        //  - save base data
        super.saveData(oos);
    }    

    ////////////////////////////////////////////////////////////////////////////
    // index map content support

    protected String getIndexKey(Object obj) {
        // use absolute path os the key
        String key;
        if (obj instanceof String) {
            key = (String)obj;
        } else {
            CsmFile file = (CsmFile)obj;
            key = file.getAbsolutePath();
        }
        return FilePathCache.getString(key);
    }  

    protected String getBaseCacheName(Object obj) {
        // use name for the file
        CsmFile file = (CsmFile)obj;
        String base = file.getName();
        return base;
    }   

    protected Object createValue(String cacheName, Object obj2cache) {
        return new Entry(cacheName, 0/*((FileImpl)obj2cache).getBuffer().getFile().lastModified()*/);
    }
    
    protected boolean isEqual(Object value, String checkCacheName) {
        return ((Entry)value).getCacheFileName().equals(checkCacheName);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // value entry    
   final static class Entry implements Serializable {
        private static final long serialVersionUID = -7790789617759717723L;
       
        transient private String cacheFileName;
        transient private long lastModified;
        
        private Entry(String name, long modified) {
            setCacheFileName(name);
            setLastModified(modified);
        }

        public String getCacheFileName() {
            return cacheFileName;
        }

        public void setCacheFileName(String cacheFileName) {
            this.cacheFileName = cacheFileName;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public String toString() {
            String retValue;
            
            retValue = "["+cacheFileName+";"+lastModified+"]"; // NOI18N
            return retValue;
        }
         private void writeObject(java.io.ObjectOutputStream out) throws IOException {
             out.defaultWriteObject();
             out.writeObject(cacheFileName);
             out.writeLong(lastModified);
         }
         private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
             in.defaultReadObject();
             cacheFileName = (String)in.readObject();
             lastModified = in.readLong();
         }
    }    
}