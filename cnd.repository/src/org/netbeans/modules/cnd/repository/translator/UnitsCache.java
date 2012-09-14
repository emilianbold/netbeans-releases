/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.JButton;
import org.netbeans.modules.cnd.repository.disk.StorageAllocator;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.IntToStringCache;
import org.netbeans.modules.cnd.repository.util.UnitCodec;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.CharSequences;
import org.openide.util.NbBundle;

/**
 * This class
 * 1) Acts as a int/string table for unit names;
 * 2) Contains int/string tables for file names (a table per unit);
 *
 * @author Nickolay Dalmatov
 */
/*package*/ final class UnitsCache {

    private final static String PROJECT_INDEX_FILE_NAME = "project-index"; //NOI18N

    private final File masterIndexFile;
    private final List<CharSequence> cache = new ArrayList<CharSequence>();
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
    private final ArrayList<IntToStringCache> fileNamesCaches = new ArrayList<IntToStringCache>();
    /**
     * Stores unit timestamps.
     * For open units they (as I understand) coincide with
     * fileNamesCaches[idx].getTimestamp().
     * But for units that are not open, the correct value is in unit2timestamp map.
     */
    private final Map<CharSequence, Long> unit2timestamp = new ConcurrentHashMap<CharSequence, Long>();
    /**
     * Maps a unit to its required units
     */
    private final Map<CharSequence, Collection<RequiredUnit>> unit2requnint = new ConcurrentHashMap<CharSequence, Collection<RequiredUnit>>();
    /*
     * This is a simple cache that keeps last found index by string.
     * Cache reduces method consuming time in 10 times (on huge projects).
     */
    private static final class Lock {
    }
    private final Object oneItemCacheLock = new Lock();
    private CharSequence oneItemCacheString; // Cached last string
    private int oneItemCacheInt; // Cached last index
    private RandomAccessFile randomAccessFile;
    private FileChannel channel;
    private FileLock masterIndexLock;
    private final StorageAllocator storageAllocator;
    private final UnitCodec unitCodec;

    /**
     * Loads master index and unit/timestamp pairs
     * Master index is a list of
     *	- units
     *  - their timestamps
     *	- their required units 
     *	- required units  timestamps 
     */
    /*package*/ UnitsCache(StorageAllocator storageAllocator, UnitCodec unitCodec) {
        //this.version = RepositoryTranslatorImpl.getVersion();
        this.storageAllocator = storageAllocator;
        this.unitCodec = unitCodec;
        masterIndexFile = new File(storageAllocator.getCacheBaseDirectory(), "index"); // NOI18N
        this.timestamp = System.currentTimeMillis();
        boolean inited = false;
        try {
            randomAccessFile = new RandomAccessFile(masterIndexFile, "rw"); // NOI18N
	    channel = randomAccessFile.getChannel();
            masterIndexLock = channel.tryLock();
            if (masterIndexLock == null) {
                String message = NbBundle.getMessage(UnitsCache.class, "IDE_Already_Running"); // NOI18N
                IOException exception = new IOException(message);
                if (!(CndUtils.isStandalone() || CndUtils.isUnitTestMode())) {
                    while (masterIndexLock == null) {
                        JButton reload = new JButton(NbBundle.getMessage(UnitsCache.class, "Yes_Button")); // NOI18N
                        JButton cancel = new JButton(NbBundle.getMessage(UnitsCache.class, "No_Button")); // NOI18N
                        String question = NbBundle.getMessage(UnitsCache.class, "Retry_Load"); // NOI18N
                        NotifyDescriptor nd = new NotifyDescriptor(question,
                                message, NotifyDescriptor.YES_NO_OPTION,
                                NotifyDescriptor.QUESTION_MESSAGE,
                                new Object[] {reload, cancel},
                                reload);
                        Object ret = DialogDisplayer.getDefault().notify(nd);
                        if (ret == reload) {
                            masterIndexLock = channel.tryLock();
                        } else {
                            exception.printStackTrace(System.err);
                            throw exception;
                        }
                    }
               } else {
                    exception.printStackTrace(System.err);
                    throw exception;
                }
            }
            IndexConverter converter = loadMasterIndex(randomAccessFile);
            if (converter != null) {
                convertIfNeed(converter);
            }
            inited = true;
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                tr.printStackTrace(System.err);
            }
        } finally {
            if (!inited) {
                cache.clear();
                fileNamesCaches.clear();
                unit2timestamp.clear();
                unit2requnint.clear();
                fileNamesCaches.clear();
            }
        }
    }
    
    /**
     * Reads master index.
     * Fills fileNamesCaches with an empty int/string tables.
     */
    private IndexConverter loadMasterIndex(DataInput stream) throws IOException {
        IndexConverter converter = null;
        cache.clear();
        fileNamesCaches.clear();
        stream.readInt();
        stream.readLong();
        String oldIndexPath = stream.readUTF();
        if( ! oldIndexPath.equals(masterIndexFile.getAbsolutePath())) {
            converter = new IndexConverter(oldIndexPath, masterIndexFile.getAbsolutePath());
        }
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
        return converter;
    }

    private void convertIfNeed(IndexConverter converter) {
        // called from ctor => no sync needed
        if (!converter.needsConversion()) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < cache.size(); i++) {
            CharSequence value = cache.get(i);
            CharSequence newValue = converter.convert(value);
            if (newValue != value) {
                changed = true;
                cache.set(i, newValue);
                Collection<RequiredUnit> reqUnits = unit2requnint.remove(value);
                unit2requnint.put(newValue, reqUnits);
                if (!storageAllocator.renameUnitDirectory(value, newValue)) {
                    storageAllocator.deleteUnitFiles(newValue, true);
                    storageAllocator.deleteUnitFiles(value, true);
                }
            }
        }
        if (changed) {
            for (CharSequence unitName : cache) {
                String unitIndexFileName = getUnitIndexName(unitName);
                if (new File(unitIndexFileName).exists()) {
                    DataInputStream is = null;
                    DataOutputStream os = null;
                    boolean success = false;
                    try {
                        is = new DataInputStream(new BufferedInputStream(new FileInputStream(unitIndexFileName)));
                        IntToStringCache filesCache = new IntToStringCache(is);
                        filesCache.convert(converter);
                        os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(unitIndexFileName, false)));
                        filesCache.write(os);
                        success = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace(System.err);
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException ex) {
                                ex.printStackTrace(System.err);
                            }
                        }
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException ex) {
                                ex.printStackTrace(System.err);
                            }
                        }
                        if (!success) {
                            storageAllocator.deleteUnitFiles(unitName, true);
                        }
                    }
                }

            }
        }
    }

    void storeMasterIndex() {
        try {
            if (randomAccessFile == null) {
                randomAccessFile = new RandomAccessFile(masterIndexFile, "rw"); // NOI18N
            }
            randomAccessFile.seek(0);
            randomAccessFile.setLength(0);
            write(randomAccessFile);
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                tr.printStackTrace(System.err);
            }
        } finally {
            try {
                if (masterIndexLock != null) {
                    masterIndexLock.release();
                }
                if (channel != null) {
                    channel.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
                
            } catch (IOException e) {
            }
        }
    }
    

    private boolean loadUnitIndex(final CharSequence unitName, String unitIndexFileName, Set<CharSequence> antiLoop) {
        DataInputStream dis = null;
        boolean indexLoaded = false;

        try {
            // don't produce exceptions when it's clear that the file just doesn't exist
            if (new File(unitIndexFileName).exists()) {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(unitIndexFileName)));
                indexLoaded = readUnitFilesCache(unitName, dis, antiLoop);
            }
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                tr.printStackTrace(System.err);
            }
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                    new File(unitIndexFileName).delete();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
        return indexLoaded;
    }
    
    void storeUnitIndex(final CharSequence unitName) {
        DataOutputStream dos = null;
        String unitIndexFileName = getUnitIndexName(unitName);
        boolean indexStored = false;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(unitIndexFileName, false)));
            assert unitName != null;
            int unitId = getId(unitName);
            IntToStringCache filesCache = getFileNames(unitId);
            filesCache.write(dos);
            indexStored = true;
        } catch (FileNotFoundException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (IOException e) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                e.printStackTrace(System.err);
            }
        } catch (Throwable tr) {
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                tr.printStackTrace(System.err);
            }
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        if (!indexStored) {
            storageAllocator.deleteUnitFiles(unitName, false);
//            System.err.println("storeUnitIndex: unit file deleted for " + unitName);            
        }
    }

    void removeUnit(final CharSequence unitName) {
        File file = new File(getUnitIndexName(unitName));
        file.delete();
    }

    private String getUnitIndexName(final CharSequence unitName) {
        return storageAllocator.getUnitStorageName(unitName) + PROJECT_INDEX_FILE_NAME;
    }
    
    private boolean readUnitFilesCache(CharSequence name, DataInput stream, Set<CharSequence> antiLoop) throws IOException {
        assert name != null;
        assert stream != null;

        IntToStringCache filesCache = new IntToStringCache(stream);
        if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
            trace("Read unit files cache for %s ts=%d\n", name, filesCache.getTimestamp()); // NOI18N
        }
        if ((filesCache.getVersion() == RepositoryTranslatorImpl.getVersion()) && validateReqUnits(name, antiLoop)) {
            insertUnitFileCache(name, filesCache);
            return true;
        } else {
            filesCache = new IntToStringCache();
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("Req. units validation failed for %s. Setting ts=%d\n", name, filesCache.getTimestamp()); // NOI18N
            }
            insertUnitFileCache(name, filesCache);
        }

        return false;
    }

    /**
     * Is called before closing a unit.
     * Stores the list of required units and their timestamps.
     */
    // package-local
    void updateReqUnitInfo(CharSequence unitName, Set<CharSequence> reqUnits) {
        // update timestamps
        // ???! why do we copy the timestamps from unitNamesCache.cache to unit2timestamp for ALL modules???
        for (int i = 0; i < cache.size(); i++) {
            // unitNamesCache AKA this
            CharSequence uName = cache.get(i);
            long uTs = fileNamesCaches.get(i).getTimestamp();
            unit2timestamp.put(uName, uTs);
        }
        // store required units set
        Collection<RequiredUnit> unitReqUnits = new CopyOnWriteArraySet<RequiredUnit>();
        if (reqUnits != null) {
            for (CharSequence rUnitName : reqUnits) {
                long ts = unit2timestamp.get(rUnitName).longValue();
                RequiredUnit rU = new RequiredUnit(getId(rUnitName), ts, unitCodec);
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
    private boolean validateReqUnits(CharSequence unitName, Set<CharSequence> antiLoop) {
        if (antiLoop.contains(unitName)) {
            return true;
        }
        antiLoop.add(unitName);
        boolean result = true;
        Collection<RequiredUnit> reqUnits = unit2requnint.get(unitName);
        if (reqUnits == null) {
            return false;
        }
        for (RequiredUnit rU : reqUnits) {
            if (!isUnitIndexLoaded(rU.getName())) {
                loadUnitIndex(rU.getName(), antiLoop);
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
    
    void loadUnitIndex(final CharSequence unitName, Set<CharSequence> antiLoop) {
        // check if the index is already loaded
        if (isUnitIndexLoaded(unitName)) {
            return;
        }
        String unitIndexFileName = getUnitIndexName(unitName);
        boolean indexLoaded = loadUnitIndex(unitName, unitIndexFileName, antiLoop);
        if (!indexLoaded) {
            storageAllocator.deleteUnitFiles(unitName, false);
            cleanUnitData(unitName);
//            System.err.println("loadUnitIndex: unit file deleted for " + unitName);
        }
    }

    /**
     * For the given unit,
     * stores the collection of it's required units
     * (instances of RequiredUnit)
     */
    private void writeRequiredUnits(CharSequence unitName, DataOutput stream) throws IOException {
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
    private Collection<RequiredUnit> readRequiredUnits(DataInput stream) throws IOException {
        assert stream != null;
        Collection<RequiredUnit> units = new CopyOnWriteArraySet<RequiredUnit>();
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            RequiredUnit unit = new RequiredUnit(stream, unitCodec);
            units.add(unit);
            if (Stats.TRACE_REPOSITORY_TRANSLATOR) {
                trace("\t\tRead req. unit %d %s ts=%d\n", unit.getUnitId(), unit.getName(), unit.getTimestamp()); // NOI18N
            }
        }
        return units;
    }

    private void write(DataOutput stream) throws IOException {
        assert cache != null;
        assert stream != null;
        stream.writeInt(RepositoryTranslatorImpl.getVersion());
        stream.writeLong(timestamp);
        stream.writeUTF(masterIndexFile.getAbsolutePath());
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

    int getId(CharSequence value) {
        CharSequence prevString;
        int prevInt;
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

    private boolean isUnitIndexLoaded(final CharSequence unitName) {
        if (!cache.contains(unitName)) {
            return false;
        }
        int id = cache.indexOf(unitName);
        if (fileNamesCaches.get(id).size() != 0) {
            //incorrect! there might be 0 files!
            return true;
        }
        return false;
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

    IntToStringCache removeFileNames(CharSequence unitName) {
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

    CharSequence getValueById(int id) {
        return cache.get(id);
    }

    boolean containsId(int id) {
        return 0 <= id && id < cache.size();
    }

    boolean containsValue(CharSequence value) {
        return cache.contains(value);
    }

    /**
     * no synchronization is set to speed up processing
     * this call is safe due to add-only way of work with
     * List
     */
    IntToStringCache getFileNames(int unitId) {
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

////////////////////////////////////////////////////////////////////////////
//  impl details
    private void trace(String format, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = Long.valueOf(System.currentTimeMillis());
        System.arraycopy(args, 0, newArgs, 1, args.length);
        System.err.printf("RepositoryTranslator [%d] " + format, newArgs);
    }

    /**
     * Just a structure that holds name and timestamps
     * for required unit
     */
    private final class RequiredUnit {

        private final int unitId;
        private final long timestamp;
        private final UnitCodec unitCodec;

        public RequiredUnit(int unitId, long time, UnitCodec unitCodec) {
            this.unitCodec = unitCodec;
            this.unitId = unitId;
            this.timestamp = time;
        }

        public RequiredUnit(DataInput stream, UnitCodec unitCodec) throws IOException {
            this.unitCodec = unitCodec;
            unitId = unitCodec.addRepositoryID(stream.readInt());
            timestamp = stream.readLong();
        }

        public void write(DataOutput stream) throws IOException {
            stream.writeInt(unitCodec.removeRepositoryID(unitId));
            stream.writeLong(timestamp);
        }

        public CharSequence getName() {
            return getValueById(unitCodec.removeRepositoryID(unitId));
        }

        public int getUnitId() {
            return unitId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}
