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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 * help methods to create repository keys
 * @author Vladimir Voskresensky
 */
public class KeyUtilities {
    
    /** Creates a new instance of KeyUtils */
    private KeyUtilities() {
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // key generators
    
    public static Key createFileKey(CsmFile file) {
        return new FileKey(file);
    }
    
    public static Key createNamespaceKey(CsmNamespace ns) {
        return new NamespaceKey(ns);
    }
    
    public static Key createProjectKey(CsmProject project) {
        return new ProjectKey(project);
    }
    
    public static Key createProjectKey(String projectQualifiedName) {
        return new ProjectKey(projectQualifiedName);
    }
    
    public static Key createOffsetableDeclarationKey(OffsetableDeclarationBase obj) {
        assert obj != null;
        return new OffsetableDeclarationKey(obj);
    }
    
    public static Key createMacroKey(CsmMacro macro) {
        assert macro != null;
        return new MacroKey(macro);
    }
    
    public static Key createIncludeKey(CsmInclude incl) {
        assert incl != null;
        return new IncludeKey(incl);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    private static class UnitsCache extends IntToStringCache {
        private static ArrayList<IntToStringCache> fileNamesCaches = new ArrayList<IntToStringCache>();
        
        public void read(DataInput stream) throws IOException {
            assert stream != null;
            assert cache != null;
            
            cache.clear();
            fileNamesCaches.clear();
            
            int size = stream.readInt();
            
            for (int i = 0; i < size; i++) {
                String value = stream.readUTF();
                if (value.equals("")) {
                    cache.add(null);
                } else {
                    cache.add(value);
                }
                fileNamesCaches.add(new IntToStringCache());
            }
        }
        
        public void insertUnitFileCache (String name, IntToStringCache filesCache) {
            int index = cache.indexOf(name);
            if (index == -1) {
                index = super.makeId(name);
            }
            fileNamesCaches.set(index, filesCache);
        }
        
        
        public IntToStringCache removeFileNames(String unitName) {
            synchronized (cache) {
                IntToStringCache fileNames = null;
                int index = cache.indexOf(unitName);
                if (index != -1) {
                    fileNames = fileNamesCaches.get(index);
                    fileNamesCaches.set(index, new IntToStringCache());
                }
                return fileNames;
            }
        }
    
        public int remove(String value) {
            synchronized (cache) {
                int index = cache.indexOf(value);
                if (index != -1) {
                    cache.set(index, null);
                    fileNamesCaches.set(index, null);
                }
                return index;
            }
        }
        
        /**
         * synchronization is controlled by calling getId() method
         */
        protected int makeId(String value) {
            int id = cache.indexOf(null);
            if (id == -1) {
                id = super.makeId(value);
                //fileNamesCaches.ensureCapacity(id+1);
                //fileNamesCaches.set(id, new IntToStringCache());
                fileNamesCaches.add(new IntToStringCache());
            } else {
                cache.set(id, value);
                fileNamesCaches.set(id, new IntToStringCache());
            }
            return id;
        }
        
        /**
         * no synchronization is set to speed up processing
         * this call is safe due to add-only way of work with
         * List
         */
        public IntToStringCache getFileNames(int unitId) {
            return fileNamesCaches.get(unitId);
        }
    }
    
    private static UnitsCache unitNamesCache = new UnitsCache();
    
    public static IntToStringCache getUnitFileNames(int unitId) {
	return unitNamesCache.getFileNames(unitId);
    }
    
    public static int getUnitId(String unitName) {
	return unitNamesCache.getId(unitName);
    }
    
    public static String getUnitName(int unitIndex) {
	return unitNamesCache.getValueById(unitIndex);
    }
    
    public static void readUnitsCache(DataInput stream) throws IOException {
        assert stream != null;
        
        unitNamesCache.read(stream);
    }
    
    public static void writeUnitsCache(DataOutput stream) throws IOException {
        assert stream != null;
        
        unitNamesCache.write(stream);
    }
    
    public static void readUnitFilesCache(String name, DataInput stream) throws IOException {
        assert name != null;
        assert stream != null;
        
        IntToStringCache filesCache = new IntToStringCache(stream);
        unitNamesCache.insertUnitFileCache(name, filesCache);
    }
    
    public static void writeUnitFilesCache (String unitName, DataOutput stream) throws IOException {
        assert unitName != null;
        assert stream != null;
        
        int unitId = unitNamesCache.getId(unitName);
        IntToStringCache cache = unitNamesCache.getFileNames(unitId);
        cache.write(stream);
    }
    
    public static void closeUnit(String unitName) {
        unitNamesCache.removeFileNames(unitName);
    }
    
    // have to be public or UID factory does not work

}
