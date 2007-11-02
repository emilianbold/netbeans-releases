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
package org.netbeans.modules.cnd.repository.translator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.repository.api.RepositoryTranslation;
import org.netbeans.modules.cnd.repository.disk.StorageAllocator;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.IntToStringCache;

/**
 *
 * @author Nickolay Dalmatov
 */
public class RepositoryTranslatorImpl implements RepositoryTranslation{
    
    private static UnitsCache unitNamesCache = new UnitsCache();  
    private static boolean loaded = false;
    private static int DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM = 0;    
    private static int version = DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM;
    
    /** Creates a new instance of RepositoryTranslatorImpl */
    public RepositoryTranslatorImpl() {
        // load master index
    }
    
    public int getFileIdByName(final int unitId, final String fileName) {
        assert fileName != null;
        final IntToStringCache unitFileNames = getUnitFileNames(unitId);
        return unitFileNames.getId(fileName);
    }

    public String getFileNameById(final int unitId, final int fileId) {
	final IntToStringCache fileNames = getUnitFileNames(unitId);
	final String fileName = fileNames.getValueById(fileId);
	return fileName;        
    }

    public String getFileNameByIdSafe(final int unitId, final int fileId) {
	final IntToStringCache fileNames = getUnitFileNames(unitId);
	final String fileName = fileNames.containsId(fileId) ? fileNames.getValueById(fileId) : "?"; // NOI18N
	return fileName;        
    }

    public int getUnitId(String unitName) {
        if (!unitNamesCache.containsValue(unitName)) {
            StorageAllocator.getInstance().deleteUnitFiles(unitName, false);
        }
        return unitNamesCache.getId(unitName);         
    }

    public String getUnitName(int unitId) {
        return unitNamesCache.getValueById(unitId);
    }
    
    private static IntToStringCache getUnitFileNames(int unitId) {
	return unitNamesCache.getFileNames(unitId);
    }    
    
   private static void readUnitsCache(DataInput stream) throws IOException {
        assert stream != null;
        
        unitNamesCache = new UnitsCache(stream);
    }
    
    private static void writeUnitsCache(DataOutput stream) throws IOException {
        assert stream != null;
        
        unitNamesCache.write(stream);
    }
    
    private static boolean readUnitFilesCache(String name, DataInput stream, Set<String> antiLoop) throws IOException {
        assert name != null;
        assert stream != null;
        
        IntToStringCache filesCache = new IntToStringCache(stream);
        if ((filesCache.getVersion() == version) && UnitsCache.validateReqUnits(name, antiLoop)) {
            unitNamesCache.insertUnitFileCache(name, filesCache);        
            return true;
        } else {
            unitNamesCache.insertUnitFileCache(name, new IntToStringCache());
        }

        return false;
    }
    
    private static void writeUnitFilesCache (String unitName, DataOutput stream) throws IOException {
        assert unitName != null;
        assert stream != null;
        
        int unitId = unitNamesCache.getId(unitName);
        IntToStringCache cache = unitNamesCache.getFileNames(unitId);
        cache.write(stream);
    }    
    
    public static void closeUnit(String unitName, Set<String> requiredUnits) {
        UnitsCache.updateReqUnitInfo(unitName, requiredUnits);
        storeUnitIndex(unitName);
        unitNamesCache.removeFileNames(unitName);
    }    

    public static void shutdown() {
        storeMasterIndex();
    }
    
    public static void loadUnitIndex(final String unitName){
        loadUnitIndex(unitName, new HashSet<String>());
    }
    
    private static void loadUnitIndex(final String unitName, Set<String> antiLoop){
        // check if the index is already loaded
        if (UnitsCache.isUnitIndexLoaded(unitName)) {
            return;
        }
        InputStream fis = null;
        InputStream bis = null;
        DataInputStream dis = null;
        String unitIndexFileName = getUnitIndexName(unitName);
        boolean indexLoaded = false;
        
        try {
            fis = new FileInputStream(unitIndexFileName);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            indexLoaded = readUnitFilesCache(unitName, dis, antiLoop);
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_FILE_INDEX){
                tr.printStackTrace();
            }
        }   finally {
            if (dis != null) {
                try {
                    dis.close();
                    new File(unitIndexFileName).delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }    
        
        if (!indexLoaded) {
            StorageAllocator.getInstance().deleteUnitFiles(unitName, false);
            unitNamesCache.cleanUnitData(unitName);
//            System.err.println("loadUnitIndex: unit file deleted for " + unitName);
        }
    }
    
    private static String getUnitIndexName(final String unitName) {
	return StorageAllocator.getInstance().getUnitStorageName(unitName) + PROJECT_INDEX_FILE_NAME;
    }
	    
    public static void removeUnit(final String unitName) {
	File file = new File(getUnitIndexName(unitName));
	file.delete();
    }
    
    private static void storeUnitIndex(final String unitName) {
        OutputStream fos = null;
        OutputStream bos = null;
        DataOutputStream dos = null;
        String unitIndexFileName = getUnitIndexName(unitName);
        boolean indexStored = false;
        try {
            fos = new FileOutputStream(unitIndexFileName, false);
            bos = new BufferedOutputStream(fos);
            dos = new DataOutputStream(bos);
            writeUnitFilesCache(unitName, dos);
            indexStored = true;
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_FILE_INDEX){
                tr.printStackTrace();
            }
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }        
       if (!indexStored) {
            StorageAllocator.getInstance().deleteUnitFiles(unitName, false);
//            System.err.println("storeUnitIndex: unit file deleted for " + unitName);            
       } 
    }
    
    public static void loadMasterIndex(){
        InputStream fis = null;
        InputStream bis = null;
        DataInputStream dis = null;
        try {
            fis = new FileInputStream(MASTER_INDEX_FILE_NAME);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            readUnitsCache(dis);
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_FILE_INDEX){
                tr.printStackTrace();
            }
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    public static void storeMasterIndex(){
        OutputStream fos = null;
        OutputStream bos = null;
        DataOutputStream dos = null;
        
        try {
            fos = new FileOutputStream(MASTER_INDEX_FILE_NAME, false);
            bos = new BufferedOutputStream(fos);
            dos = new DataOutputStream(bos);
            writeUnitsCache(dos);
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (IOException e)     {
            if (Stats.TRACE_FILE_INDEX){
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_FILE_INDEX){
                tr.printStackTrace();
            }
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
            }
        }
    }
    
    public static void startup(int newVersion) {
        version = newVersion;
        if (!loaded) {
            synchronized (unitNamesCache) {
                if (!loaded) {
                    loaded = true;
                    loadMasterIndex();
                }
            }
        }
    }

    public static int getVersion() {
        return version;
    }
    
    

 
////////////////////////////////////////////////////////////////////////////
    // impl details
    
    private static class RequiredUnit {
        private String unitName;
        private long   timestamp;
        
        public RequiredUnit(String name, long time) {
            unitName = name;
            timestamp = time;
        }
        
        public RequiredUnit(DataInput stream) throws IOException {
            unitName = stream.readUTF();
            timestamp = stream.readLong();
        }
        
        public void write (DataOutput stream) throws IOException {
            stream.writeUTF(unitName);
            stream.writeLong(timestamp);
        }
        
        public String getName() {
            return unitName;
        }
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    private static class UnitsCache extends IntToStringCache {
        private static ArrayList<IntToStringCache>              fileNamesCaches = new ArrayList<IntToStringCache>();
        private static Map<String, Long>                        unit2timestamp = new ConcurrentHashMap<String, Long>();
        private static Map<String, Collection<RequiredUnit>>    unit2requnint = 
                new ConcurrentHashMap<String, Collection<RequiredUnit>>();
        
        public static void updateReqUnitInfo(String unitName, Set<String> reqUnits) {
            // update timestamps
            for (int i = 0; i < unitNamesCache.cache.size(); i++) {
                String uName = unitNamesCache.cache.get(i);
                long uTs = fileNamesCaches.get(i).getTimestamp();
                unit2timestamp.put(uName, uTs);
            }
            //
            Collection<RequiredUnit> unitReqUnits = new CopyOnWriteArraySet<RequiredUnit>();
            if (reqUnits != null) {
                for (String rUnitName: reqUnits) {
                    long ts = unit2timestamp.get(rUnitName).longValue();
                    RequiredUnit rU = new RequiredUnit(rUnitName, ts);
                    unitReqUnits.add(rU);
                }
            }
            unit2requnint.put(unitName, unitReqUnits);
        }

        private static boolean validateReqUnits(String unitName, Set<String> antiLoop) {
            if (antiLoop.contains(unitName)) {
                return true;
            }
            antiLoop.add(unitName);
            boolean result = true;
            Collection<RequiredUnit> reqUnits = unit2requnint.get(unitName);
            for (RequiredUnit rU: reqUnits) {
                                
                if (!isUnitIndexLoaded(rU.getName())) {
                    RepositoryTranslatorImpl.loadUnitIndex(rU.getName(), antiLoop);
                }
                
                Long tsL = unit2timestamp.get(rU.getName());
                if (tsL != null) {
                    long ts = tsL.longValue();
                    if (ts != rU.getTimestamp()) {
                        result = false;
                        break;
                    }
                } else {
                    result = false;
                    break;
                }
            }
            return result;
        }
        
        private static void writeRequiredUnits(String unitName, DataOutput stream) throws IOException {
            assert unitName != null;
            assert stream != null;
            
            Collection<RequiredUnit> rUnits = unit2requnint.get(unitName);
            assert rUnits != null;
            
            int size = rUnits.size();
            stream.writeInt(size);
            for (RequiredUnit unit: rUnits) {
                unit.write(stream);
            }
        }
        
        private static Collection<RequiredUnit> readRequiredUnits(DataInput stream) throws IOException {
            assert stream != null;
            Collection<RequiredUnit> units = new CopyOnWriteArraySet<RequiredUnit>();
            int size = stream.readInt();
            
            for (int i = 0; i < size; i++) {
                RequiredUnit unit = new RequiredUnit(stream);
                units.add(unit);
            }
            return units;
        }
        
	@Override
        public void write(DataOutput stream) throws IOException {
            assert cache != null;
            assert stream != null;
            
            stream.writeInt(version);
            stream.writeLong(timestamp);
            
            int size = cache.size();
            stream.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                String value = cache.get(i);
                stream.writeUTF(value);
                stream.writeLong(unit2timestamp.get(value).longValue());
                writeRequiredUnits(value, stream);
            }
        }
        
        private static boolean isUnitIndexLoaded(final String unitName) {
            if (!unitNamesCache.cache.contains(unitName)) {
                return false;
            }
            int id = unitNamesCache.cache.indexOf(unitName);
            
            if (fileNamesCaches.get(id).size() != 0) {
                
                return true;
            }
            return false;
        }
        
        
        public UnitsCache () {
            
        }
        
        public UnitsCache (DataInput stream) throws IOException {
            assert stream != null;
            assert cache != null;
            
            cache.clear();
            fileNamesCaches.clear();
            
            stream.readInt();
            stream.readLong();
            
            int size = stream.readInt();
            
            for (int i = 0; i < size; i++) {
                String value = FilePathCache.getString(stream.readUTF());
                cache.add(value);
                long ts = stream.readLong();
                unit2timestamp.put(value, Long.valueOf(ts));
                unit2requnint.put(value, readRequiredUnits(stream));
                fileNamesCaches.add(new IntToStringCache());
            }
        }
        
        public void insertUnitFileCache (String name, IntToStringCache filesCache) {
            int index = cache.indexOf(name);
            if (index == -1) {
                index = super.makeId(name);
            }
            fileNamesCaches.set(index, filesCache);
            unit2timestamp.put(name, Long.valueOf(filesCache.getTimestamp()));
            unit2requnint.put(name, new CopyOnWriteArraySet<RequiredUnit>());
        }
        
        
        public IntToStringCache removeFileNames(String unitName) {
                IntToStringCache fileNames = null;
                int index = cache.indexOf(unitName);
                if (index != -1) {
                    fileNames = fileNamesCaches.get(index);
                    unit2timestamp.put(unitName, fileNames.getTimestamp());
                    fileNamesCaches.set(index, new IntToStringCache());
                }
                return fileNames;
        }
    
        /**
         * synchronization is controlled by calling getId() method
         */
	@Override
        protected int makeId(String value) {
            int id = cache.indexOf(null);
            IntToStringCache fileCache = new IntToStringCache();
            
            if (id == -1) {
                id = super.makeId(value);
                fileNamesCaches.add(fileCache);
            } else {
                cache.set(id, value);
                fileNamesCaches.set(id, fileCache);
            }

            unit2requnint.put(value, new CopyOnWriteArraySet<RequiredUnit>());
            unit2timestamp.put(value, fileCache.getTimestamp());
            
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

        private void cleanUnitData(String unitName) {
            IntToStringCache fileCache = new IntToStringCache();
            unit2requnint.put(unitName, new CopyOnWriteArraySet<RequiredUnit>());
            unit2timestamp.put(unitName, fileCache.getTimestamp());            
        }
    }    
    
    private final static String MASTER_INDEX_FILE_NAME = System.getProperty("netbeans.user") + //NOI18N
            File.separator + "var" + File.separator + "cache" + File.separator + "cnd-projects-index"; //NOI18N
    private final static String PROJECT_INDEX_FILE_NAME = "project-index"; //NOI18N
        
}
