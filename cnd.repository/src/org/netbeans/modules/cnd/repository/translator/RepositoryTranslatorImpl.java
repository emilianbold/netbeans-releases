/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.netbeans.modules.cnd.repository.api.RepositoryTranslation;
import org.netbeans.modules.cnd.repository.disk.StorageAllocator;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.IntToStringCache;
import org.openide.modules.Places;
import org.openide.util.CharSequences;

/**
 * This class is responsible for int <-> String translation for both
 *  1) file names
 *  2) unit names
 * It is also responsible for master index processing
 * and the required units verification.
 * 
 * The required units issue is caused by the following two circumstances:
 * a) Each unit stores its own int to string table that is used for decoding its keys
 * b) A unit can store other units (required units) keys
 * 
 * By required units verification we prevent the following situation
 * (which otherwise causes a huge mess).
 * Consider two projects, App1, App2 and a library Lib  * that is required for both App1 and App2.
 * User performs the following steps:
 * 1) Opens App1 - App1 and Lib persistence is created. Then closes IDE.
 * 2) Now Lib persistence is erased (well, if user makes this by hands, we aren't responsible...
 * but it can happen if user opens Lib and IDE exits abnormally - then upon  Lib reopen
 * its persistence is erased)
 * 3) User opens App2 - Lib persistence is recreated 
 * 4) User opens App1 again.
 * Now App1 contains Lin keys, but Lib now contain int/string tables that are quite different!!!
 * 
 * To prevent this situation, the following algorithm is used:
 * - Each unit's int/string table has a timestamp of its creation.
 * - When a unit closes, it stores all required units timestamps.
 * - When a unit opens, it checks that required units timestamps are the same
 * as they were at closure. Otherwise persistence is invalidated (erased)
 * for main unit and requires units.
 * 
 * @author Nickolay Dalmatov
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.repository.api.RepositoryTranslation.class)
public class RepositoryTranslatorImpl implements RepositoryTranslation {

    /**
     * It is 
     * 1) an int/string table of the unit names
     * 2) a container for int/string table for each unit's file names (a table per unit)
     * (stores units timestamps as well)
     */
    private static UnitsCache unitNamesCache = null;
    private static final Object initLock = new Object();
    private static boolean loaded = false;
    private static final int DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM = 0;
    private static int version = DEFAULT_VERSION_OF_PERSISTENCE_MECHANIZM;
    private final static File MASTER_INDEX_FILE = Places.getCacheSubfile("cnd/model/index"); // NOI18N
    private final static String PROJECT_INDEX_FILE_NAME = "project-index"; //NOI18N

    /** Creates a new instance of RepositoryTranslatorImpl */
    public RepositoryTranslatorImpl() {
        // load master index
    }

    @Override
    public int getFileIdByName(final int unitId, final CharSequence fileName) {
        assert fileName != null;
        final IntToStringCache unitFileNames = getUnitFileNames(unitId);
        return unitFileNames.getId(fileName);
    }

    @Override
    public CharSequence getFileNameById(final int unitId, final int fileId) {
        final IntToStringCache fileNames = getUnitFileNames(unitId);
        final CharSequence fileName = fileNames.getValueById(fileId);
        return fileName;
    }

    @Override
    public CharSequence getFileNameByIdSafe(final int unitId, final int fileId) {
        final IntToStringCache fileNames = getUnitFileNames(unitId);
        final CharSequence fileName = fileNames.containsId(fileId) ? fileNames.getValueById(fileId) : "?"; // NOI18N
        return fileName;
    }

    @Override
    public int getUnitId(CharSequence unitName) {
        if (!unitNamesCache.containsValue(unitName)) {
            // NB: this unit can't be open (since there is no such unit in unitNamesCache)
            // so we are just removing some ocassionally existing in persisntence files
            StorageAllocator.getInstance().deleteUnitFiles(unitName, false);
        }
        return unitNamesCache.getId(unitName);
    }

    @Override
    public CharSequence getUnitName(int unitId) {
        return unitNamesCache.getValueById(unitId);
    }

    @Override
    public CharSequence getUnitNameSafe(int unitId) {
        return unitNamesCache.containsId(unitId) ? unitNamesCache.getValueById(unitId) : "No Index " + unitId + " in " + unitNamesCache; // NOI18N
    }

    private static IntToStringCache getUnitFileNames(int unitId) {
        return unitNamesCache.getFileNames(unitId);
    }

    /** 
     * Reads master index - 
     * a list if 
     *	- units
     *  - their timestamps
     *	- their required units 
     *	- required units  timestamps 
     */
    private static void readMasterIndex(DataInput stream) throws IOException {
        assert stream != null;
        unitNamesCache = new UnitsCache(stream);
    }

    private static void writeUnitsCache(DataOutput stream) throws IOException {
        assert stream != null;

        unitNamesCache.write(stream);
    }

    private static boolean readUnitFilesCache(CharSequence name, DataInput stream, Set<CharSequence> antiLoop) throws IOException {
        assert name != null;
        assert stream != null;

        IntToStringCache filesCache = new IntToStringCache(stream);
        if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
            trace("Read unit files cache for %s ts=%d\n", name, filesCache.getTimestamp()); // NOI18N
        }
        if ((filesCache.getVersion() == version) && UnitsCache.validateReqUnits(name, antiLoop)) {
            unitNamesCache.insertUnitFileCache(name, filesCache);
            return true;
        } else {
            filesCache = new IntToStringCache();
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("Req. units validation failed for %s. Setting ts=%d\n", name, filesCache.getTimestamp()); // NOI18N
            }
            unitNamesCache.insertUnitFileCache(name, filesCache);
        }

        return false;
    }

    private static void writeUnitFilesCache(CharSequence unitName, DataOutput stream) throws IOException {
        assert unitName != null;
        assert stream != null;

        int unitId = unitNamesCache.getId(unitName);
        IntToStringCache filesCache = unitNamesCache.getFileNames(unitId);
        filesCache.write(stream);
    }

    public static void closeUnit(CharSequence unitName, Set<CharSequence> requiredUnits) {
        if (requiredUnits != null) {
            UnitsCache.updateReqUnitInfo(unitName, requiredUnits);
        }
        storeUnitIndex(unitName);
        unitNamesCache.removeFileNames(unitName);
    }

    public static void shutdown() {
        storeMasterIndex();
        StorageAllocator.getInstance().purgeCaches();
    }

    public static void loadUnitIndex(final CharSequence unitName) {
        loadUnitIndex(unitName, new HashSet<CharSequence>());
    }

    private static void loadUnitIndex(final CharSequence unitName, Set<CharSequence> antiLoop) {
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
            // don't produce exceptions when it's clear that the file just doesn't exist
            if (new File(unitIndexFileName).exists()) {
                fis = new FileInputStream(unitIndexFileName);
                bis = new BufferedInputStream(fis);
                dis = new DataInputStream(bis);
                indexLoaded = readUnitFilesCache(unitName, dis, antiLoop);
            }
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                tr.printStackTrace();
            }
        } finally {
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

    private static String getUnitIndexName(final CharSequence unitName) {
        return StorageAllocator.getInstance().getUnitStorageName(unitName) + PROJECT_INDEX_FILE_NAME;
    }

    public static void removeUnit(final CharSequence unitName) {
        File file = new File(getUnitIndexName(unitName));
        file.delete();
    }

    private static void storeUnitIndex(final CharSequence unitName) {
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
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
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

    /**
     * Loads master index and unit/timestamp pairs
     */
    private static void loadMasterIndex() {
        InputStream fis = null;
        InputStream bis = null;
        DataInputStream dis = null;
        try {
            fis = new FileInputStream(MASTER_INDEX_FILE);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            readMasterIndex(dis);
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                tr.printStackTrace();
            }
        } finally {
            if (unitNamesCache == null) {
                unitNamesCache = new UnitsCache();
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void storeMasterIndex() {
        OutputStream fos = null;
        OutputStream bos = null;
        DataOutputStream dos = null;

        try {
            fos = new FileOutputStream(MASTER_INDEX_FILE, false);
            bos = new BufferedOutputStream(fos);
            dos = new DataOutputStream(bos);
            writeUnitsCache(dos);
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace();
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
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
        init();
    }

    private static void init() {
        boolean aLoaded = loaded;
        if (!aLoaded) {
            synchronized (initLock) {
                if (!loaded) {
                    loadMasterIndex();
                    loaded = true;
                }
            }
        }
    }

    public static int getVersion() {
        return version;
    }

////////////////////////////////////////////////////////////////////////////
//  impl details
    /**
     * Just a structure that holds name and timestamps
     * for required unit
     */
    private static class RequiredUnit {

        private CharSequence unitName;
        private long timestamp;

        public RequiredUnit(CharSequence name, long time) {
            unitName = name;
            timestamp = time;
        }

        public RequiredUnit(DataInput stream) throws IOException {
            unitName = CharSequences.create(stream.readUTF());
            timestamp = stream.readLong();
        }

        public void write(DataOutput stream) throws IOException {
            stream.writeUTF(unitName.toString());
            stream.writeLong(timestamp);
        }

        public CharSequence getName() {
            return unitName;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * This class
     * 1) Acts as a int/string table for unit names;
     * 2) Contains int/string tables for file names (a table per unit);
     */
    private static class UnitsCache/* extends IntToStringCache*/ {

        private final List<CharSequence> cache;
        private final long timestamp;
        /**
         * A list of int/string tables for units a table per unit.
         * It is "parallel" to the super.cache array.
         *
         * Since it has an entry *for each unit in the persistence*
         * (not only for open units),
         * the super.cache contains empty instances of IntToStringCache
         * (for units that are not yet open)
         */
        private static ArrayList<IntToStringCache> fileNamesCaches = new ArrayList<IntToStringCache>();
        /**
         * Stores unit timestamps.
         * For open units they (as I understand) coincide with
         * fileNamesCaches[idx].getTimestamp().
         * But for units that are not open, the correct value is in unit2timestamp map.
         */
        private static Map<CharSequence, Long> unit2timestamp = new ConcurrentHashMap<CharSequence, Long>();
        /**
         * Maps a unit to its required units
         */
        private static Map<CharSequence, Collection<RequiredUnit>> unit2requnint =
                new ConcurrentHashMap<CharSequence, Collection<RequiredUnit>>();

        /**
         * Is called before closing a unit.
         * Stores the list of required units and their timestamps.
         */
        // package-local
        private static void updateReqUnitInfo(CharSequence unitName, Set<CharSequence> reqUnits) {
            // update timestamps
            // ???! why do we copy the timestamps from unitNamesCache.cache to unit2timestamp for ALL modules???
            for (int i = 0; i < unitNamesCache.cache.size(); i++) { // unitNamesCache AKA this
                CharSequence uName = unitNamesCache.cache.get(i);
                long uTs = fileNamesCaches.get(i).getTimestamp();
                unit2timestamp.put(uName, uTs);
            }
            // store required units set
            Collection<RequiredUnit> unitReqUnits = new CopyOnWriteArraySet<RequiredUnit>();
            if (reqUnits != null) {
                for (CharSequence rUnitName : reqUnits) {
                    long ts = unit2timestamp.get(rUnitName).longValue();
                    RequiredUnit rU = new RequiredUnit(rUnitName, ts);
                    unitReqUnits.add(rU);
                }
            }
            unit2requnint.put(unitName, unitReqUnits);
        }

        /**
         * Validates required units -
         * checks at least one required unit was re-created off-line.
         * If so, the cache of the unit that was re-created
         * and the cache of the unit that depends on it
         * should be invalidated
         * @param unitName name of the unit top check
         * @param antiLoop  prevents infinite recursion (in the case of cyclic dependencies)
         * @return
         */
        private static boolean validateReqUnits(CharSequence unitName, Set<CharSequence> antiLoop) {
            if (antiLoop.contains(unitName)) {
                return true;
            }
            antiLoop.add(unitName);
            boolean result = true;
            Collection<RequiredUnit> reqUnits = unit2requnint.get(unitName);
            for (RequiredUnit rU : reqUnits) {

                if (!isUnitIndexLoaded(rU.getName())) {
                    RepositoryTranslatorImpl.loadUnitIndex(rU.getName(), antiLoop);
                }

                Long tsL = unit2timestamp.get(rU.getName());
                if (tsL != null) {
                    long ts = tsL.longValue();
                    if (ts != rU.getTimestamp()) {
                        if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                            trace("Req. unit validation FAILED for %s: ts(unit2timestamp)=%d, ts(unit2requnint)=%s \n", unitName, ts, rU.getTimestamp()); // NOI18N
                        }
                        result = false;
                        break;
                    }
                } else {
                    if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                        trace("Req. unit validation FAILED for %s: ts=NULL\n", unitName); // NOI18N
                    }
                    result = false;
                    break;
                }
            }
            return result;
        }

        /**
         * For the given unit,
         * stores the collection of it's required units
         * (instances of RequiredUnit)
         */
        private static void writeRequiredUnits(CharSequence unitName, DataOutput stream) throws IOException {
            assert unitName != null;
            assert stream != null;

            Collection<RequiredUnit> rUnits = unit2requnint.get(unitName);
            assert rUnits != null;

            int size = rUnits.size();
            stream.writeInt(size);
            for (RequiredUnit unit : rUnits) {
                unit.write(stream);
                if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                    trace("\t\treq.unit %s ts=%d\n", unit.getName(), unit.getTimestamp()); //NOI18N
                }
            }
        }

        /**
         * Reads the collection of units (instances of RequiredUnit)
         */
        private static Collection<RequiredUnit> readRequiredUnits(DataInput stream) throws IOException {
            assert stream != null;
            Collection<RequiredUnit> units = new CopyOnWriteArraySet<RequiredUnit>();
            int size = stream.readInt();

            for (int i = 0; i < size; i++) {
                RequiredUnit unit = new RequiredUnit(stream);
                units.add(unit);
                if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                    trace("\t\tRead req. unit %s ts=%d\n", unit.getName(), unit.getTimestamp()); // NOI18N
                }
            }
            return units;
        }

        private void write(DataOutput stream) throws IOException {
            assert cache != null;
            assert stream != null;

            stream.writeInt(version);
            stream.writeLong(timestamp);

            int size = cache.size();
            stream.writeInt(size);

            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("Storing master index; size=%d\n", size); //NOI18N
            }
            for (int i = 0; i < size; i++) {
                CharSequence value = cache.get(i);
                stream.writeUTF(value.toString());
                stream.writeLong(unit2timestamp.get(value).longValue());
                if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                    trace("\tUnit %s ts=%d\n", value, unit2timestamp.get(value)); //NOI18N
                }
                writeRequiredUnits(value, stream);
            }
        }

        /*
         * This is a simple cache that keeps last found index by string.
         * Cache reduces method consuming time in 10 times (on huge projects).
         */
        private static final class Lock {
        }
        private final Object oneItemCacheLock = new Lock();
        private CharSequence oneItemCacheString; // Cached last string
        private int oneItemCacheInt; // Cached last index

        private int getId(CharSequence value) {
            CharSequence prevString = null;
            int prevInt = 0;
            synchronized (oneItemCacheLock) {
                prevString = oneItemCacheString;
                prevInt = oneItemCacheInt;
            }
            if (value.equals(prevString)) {
                return prevInt;
            }

            int id = cache.indexOf(value);
            if (id == -1) {
                synchronized (cache) {
                    id = cache.indexOf(value);
                    if (id == -1) {
                        id = makeId(value);
                    }
                }
            }

            synchronized (oneItemCacheLock) {
                oneItemCacheString = value;
                oneItemCacheInt = id;
            }
            return id;
        }

        private static boolean isUnitIndexLoaded(final CharSequence unitName) {
            if (!unitNamesCache.cache.contains(unitName)) {
                return false;
            }
            int id = unitNamesCache.cache.indexOf(unitName);

            if (fileNamesCaches.get(id).size() != 0) { //incorrect! there might be 0 files!

                return true;
            }
            return false;
        }

        private UnitsCache() {
            this.cache = new ArrayList<CharSequence>();
            //this.version = RepositoryTranslatorImpl.getVersion();
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Reads master index.
         * Fills fileNamesCaches with an empty int/string tables.
         */
        private UnitsCache(DataInput stream) throws IOException {

            assert stream != null;
            this.cache = new ArrayList<CharSequence>();
            this.timestamp = System.currentTimeMillis();

            cache.clear();
            fileNamesCaches.clear();

            stream.readInt();
            stream.readLong();

            int size = stream.readInt();

            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("Reading master index (%d) elements\n", size); // NOI18N
            }
            for (int i = 0; i < size; i++) {

                String v = stream.readUTF();
                CharSequence value = getFileKey(CharSequences.create(v));
                cache.add(value);

                // timestamp from master index -> unit2timestamp
                long ts = stream.readLong();
                unit2timestamp.put(value, Long.valueOf(ts));

                if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                    trace("\tRead %s ts=%d\n", value, ts); // NOI18N
                }
                // req. units list from master index -> unit2requnint
                // (with timestamps from master index)
                unit2requnint.put(value, readRequiredUnits(stream));

                // new dummy int/string cache (with the current (???!) timestamp
                fileNamesCaches.add(new IntToStringCache(ts));
            }
        }

        private void insertUnitFileCache(CharSequence name, IntToStringCache filesCache) {
            int index = cache.indexOf(name);
            if (index == -1) {
                cache.add(name);
                index = cache.indexOf(name);
            }
            fileNamesCaches.set(index, filesCache);
            unit2timestamp.put(name, Long.valueOf(filesCache.getTimestamp()));
            unit2requnint.put(name, new CopyOnWriteArraySet<RequiredUnit>());
        }

        private IntToStringCache removeFileNames(CharSequence unitName) {
            IntToStringCache fileNames = null;
            int index = cache.indexOf(unitName);
            if (index != -1) {
                fileNames = fileNamesCaches.get(index);
                long ts = fileNames.getTimestamp();
                unit2timestamp.put(unitName, ts);
                fileNamesCaches.set(index, new IntToStringCache(ts));
            }
            return fileNames;
        }

        /**
         * synchronization is controlled by calling getId() method
         */
        private int makeId(CharSequence unitName) {
            unitName = getFileKey(unitName);
            int id = cache.indexOf(null);
            IntToStringCache fileCache = new IntToStringCache();

            if (id == -1) {
                cache.add(unitName);
                id = cache.indexOf(unitName);
                fileNamesCaches.add(fileCache);
            } else {
                cache.set(id, unitName);
                fileNamesCaches.set(id, fileCache);
            }

            assert fileNamesCaches.size() == cache.size();

            unit2requnint.put(unitName, new CopyOnWriteArraySet<RequiredUnit>());
            unit2timestamp.put(unitName, fileCache.getTimestamp());

            return id;
        }

        private CharSequence getValueById(int id) {
            return cache.get(id);
        }

        private boolean containsId(int id) {
            return 0 <= id && id < cache.size();
        }

        private boolean containsValue(CharSequence value) {
            return cache.contains(value);
        }

        /**
         * no synchronization is set to speed up processing
         * this call is safe due to add-only way of work with
         * List
         */
        private IntToStringCache getFileNames(int unitId) {
            return fileNamesCaches.get(unitId);
        }

        private void cleanUnitData(CharSequence unitName) {
            IntToStringCache fileCache = new IntToStringCache();
            unit2requnint.put(unitName, new CopyOnWriteArraySet<RequiredUnit>());
            unit2timestamp.put(unitName, fileCache.getTimestamp());
        }

        private CharSequence getFileKey(CharSequence str) {
            // use name shared by filesystem
            // return new File(str).getPath();
            return str;
        }
    }

    private static void trace(String format, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = Long.valueOf(System.currentTimeMillis());
        for (int i = 0; i < args.length; i++) {
            newArgs[i + 1] = args[i];
        }
        System.err.printf("RepositoryTranslator [%d] " + format, newArgs);
    }
}
