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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

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

    @Override
    protected void loadData(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        int version = ois.readInt();
        if (version >= 1) {
            // version 1
            //  - load base data
            super.loadData(ois);
        }
    }

    @Override
    protected void saveData(ObjectOutputStream oos) throws IOException {
        int version = 1;
        oos.writeInt(version);

        // version 1
        //  - save base data
        super.saveData(oos);
    }    

    ////////////////////////////////////////////////////////////////////////////
    // index map content support

    protected CharSequence getIndexKey(Object obj) {
        // use absolute path os the key
        CharSequence key;
        if (obj instanceof String) {
            key = (String)obj;
        } else {
            CsmFile file = (CsmFile)obj;
            key = file.getAbsolutePath();
        }
        return FilePathCache.getString(key);
    }  

    protected CharSequence getBaseCacheName(Object obj) {
        // use name for the file
        CsmFile file = (CsmFile)obj;
        CharSequence base = file.getName();
        return base;
    }   

    protected Object createValue(CharSequence cacheName, Object obj2cache) {
        return new Entry(cacheName, 0/*((FileImpl)obj2cache).getBuffer().getFile().lastModified()*/);
    }
    
    protected boolean isEqual(Object value, CharSequence checkCacheName) {
        return ((Entry)value).getCacheFileName().equals(checkCacheName);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // value entry    
   final static class Entry implements Serializable {
        private static final long serialVersionUID = -7790789617759717723L;
       
        transient private CharSequence cacheFileName;
        transient private long lastModified;
        
        private Entry(CharSequence name, long modified) {
            setCacheFileName(name);
            setLastModified(modified);
        }

        public CharSequence getCacheFileName() {
            return cacheFileName;
        }

        public void setCacheFileName(CharSequence cacheFileName) {
            this.cacheFileName = cacheFileName;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        @Override
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
