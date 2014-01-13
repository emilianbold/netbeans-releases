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
package org.netbeans.modules.cnd.repository.disk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.cnd.repository.Logger;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.disk.index.KeysListFile;
import org.netbeans.modules.cnd.repository.impl.spi.LayerDescriptor;
import org.netbeans.modules.cnd.repository.impl.spi.LayerKey;
import org.netbeans.modules.cnd.repository.impl.spi.ReadLayerCapability;
import org.netbeans.modules.cnd.repository.impl.spi.WriteLayerCapability;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.storage.data.UTF;
import org.netbeans.modules.cnd.repository.testbench.BaseStatistics;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 * Implements FilesAccessStrategy
 *
 * @author Nickolay Dalmatov
 * @author Vladimir Kvashin
 */
public final class FilesAccessStrategyImpl implements ReadLayerCapability, WriteLayerCapability {

    private static final boolean TRACE_CONFLICTS = Boolean.getBoolean("cnd.repository.trace.conflicts");
    private static final long PURGE_OLD_UNITS_TIMEOUT = 14 * 24 * 3600 * 1000l; // 14 days
    private final ConcurrentHashMap<Integer, UnitStorage> unitStorageCache = new ConcurrentHashMap<Integer, UnitStorage>();
    private final URI cacheLocationURI;
    private final File cacheLocationFile;
    // Statistics
    private final AtomicInteger readCnt = new AtomicInteger();
    private final AtomicInteger readHitCnt = new AtomicInteger();
    private final AtomicInteger writeCnt = new AtomicInteger();
    private final AtomicInteger writeHitCnt = new AtomicInteger();
    private final BaseStatistics<String> writeStatistics = new BaseStatistics<String>("Writes", BaseStatistics.LEVEL_MEDIUM); // NOI18N
    private final BaseStatistics<String> readStatistics = new BaseStatistics<String>("Reads", BaseStatistics.LEVEL_MEDIUM); // NOI18N
    private final LayerIndex layerIndex;
    private final KeysListFile removedKeysFile;
    private final String removedKeysTable = "removed-files";//NOI18N
    private final boolean isWritable;
    private static final java.util.logging.Logger log = Logger.getInstance();

    public FilesAccessStrategyImpl(LayerIndex layerIndex, URI cacheLocation, 
            LayerDescriptor layerDescriptor) {
        this.layerIndex = layerIndex;
        this.cacheLocationURI = cacheLocation;
        this.cacheLocationFile = new File(cacheLocation.getRawPath());
        this.isWritable = layerDescriptor.isWritable();
        KeysListFile f = null;
        RepositoryDataInputImpl din = null;
        try {
            final File file = new File(cacheLocationFile, removedKeysTable);
            if (file.exists()) {
                din = new RepositoryDataInputImpl(RepositoryImplUtil.getBufferedDataInputStream(file));
                f = new KeysListFile(din);
            } 
        } catch (FileNotFoundException ex) {
            //Exceptions.printStackTrace(ex);
            f = null;
        } catch (IOException ex) {
            f = null;
            //Exceptions.printStackTrace(ex);
        }finally {
            if (din != null) {
                try {
                    din.close();
                } catch (IOException ex) {
                }
            }
        }    
        removedKeysFile = f == null ? new KeysListFile() : f;
        if (Stats.multyFileStatistics) {
            resetStatistics();
        }
    }
    
    /**
     * @param unitId
     * @throws IOException
     */
    @Override
    public void closeUnit(final int unitID, boolean cleanRepository) {
        UnitStorage storage = unitStorageCache.remove(unitID);
        if (storage != null) {
            storage.close();
            if (cleanRepository) {
                storage.cleanUnitDirectory();
            }            
        }
        if (Stats.multyFileStatistics) {
            printStatistics();
            resetStatistics();
        }
    }

    public void shutdown(boolean writable) {
        maintenance(Long.MAX_VALUE);
        for (Map.Entry<Integer, UnitStorage> entry : unitStorageCache.entrySet()) {
            closeUnit(entry.getKey(), false);
        }        
        if (!writable) {
            return;
        }
        RepositoryDataOutputImpl dos = null;
        try {
            
            final File file = new File(cacheLocationFile, removedKeysTable);
            //delete and create again
            if (file.exists()) {
                file.delete();
            }
            //store removed tables on disk
            dos = new RepositoryDataOutputImpl(RepositoryImplUtil.getBufferedDataOutputStream(file));
            removedKeysFile.write(dos);
        } catch (FileNotFoundException ex) {
            RepositoryExceptions.throwException(this, ex);
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException ex) {
                    RepositoryExceptions.throwException(this, ex);
                }
            }
        }
    }

    @Override
    public void remove(LayerKey key, boolean hasReadOnlyLayersInStorage) {
        //do use now: implement delete from the writable layer and 
        //remove and put to the removed_table
        //when shutdown wtite it on the disk
        //if put with the same key - remove from the table
        //remove phisically
        //add to the removed_table
        //we can use FileIndex for removed objects
        if (hasReadOnlyLayersInStorage) {
            removedKeysFile.put(key);
        }
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());       
        unitStorage.remove(key);
    }

    /*package*/ void testCloseUnit(int unitId) throws IOException {
        closeUnit(unitId, false);
    }

    // package-local - for test purposes
    void printStatistics() {
        System.out.printf("\nFileAccessStrategy statistics: reads %d hits %d (%d%%) writes %d hits %d (%d%%)\n", // NOI18N
                readCnt.get(), readHitCnt.get(), percentage(readHitCnt.get(), readCnt.get()), writeCnt.get(), writeHitCnt.get(), percentage(writeHitCnt.get(), writeCnt.get()));
        if (writeStatistics != null) {
            readStatistics.print(System.out);
        }
        if (writeStatistics != null) {
            writeStatistics.print(System.out);
        }
    }

    private static int percentage(int numerator, int denominator) {
        return (denominator == 0) ? 0 : numerator * 100 / denominator;
    }

    private void resetStatistics() {
        writeStatistics.clear();
        readStatistics.clear();
        readCnt.set(0);
        readHitCnt.set(0);
        writeCnt.set(0);
        writeHitCnt.set(0);
    }

    private static String getBriefClassName(Object o) {
        if (o == null) {
            return "null"; // NOI18N
        } else {
            String name = o.getClass().getName();
            int pos = name.lastIndexOf('.');
            return (pos < 0) ? name : name.substring(pos + 1);
        }
    }

    @Override
    public String toString() {
        return "FilesAccessStrategyImpl: " + cacheLocationURI.toString(); // NOI18N
    }

    @Override
    public boolean knowsKey(LayerKey key) {
        //check if not removed already
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        FileStorage fileStorage = unitStorage.getFileStorage(key, isWritable);
        if (fileStorage == null) {
            return false;
        }
        try {
            return fileStorage.hasKey(key);
        } catch (IOException ex) {
           // Exceptions.printStackTrace(ex);
        }
        return false;        
    }

    @Override
    public ByteBuffer read(LayerKey key) {
        readCnt.incrementAndGet(); // always increment counters
        if (Stats.multyFileStatistics) {
            readStatistics.consume(getBriefClassName(key), 1);
        }
        //check if not removed already
        if (this.removedKeysFile.keySet().contains(key)) {
            log.log(Level.FINE, " the key with unit id:{0} and behaviour: {1} is "
                    + "removed from the layer, will not read from the disk", new Object[]{key.getUnitId(), key.getBehavior()});//NOI18N
            return null;
        }
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        FileStorage fileStorage = unitStorage.getFileStorage(key, isWritable);
         try {
             if (fileStorage != null) {
                log.log(Level.FINE, "Storage is found for the key with unit id:{0} and behaviour: {1} is "
                        , new Object[]{key.getUnitId(), key.getBehavior()});                 
                 return fileStorage.read(key);
             }
         } catch (IOException ex) {
             RepositoryExceptions.throwException(this, key, ex);
         }
         return null;
    }

    @Override
    public void write(LayerKey key, ByteBuffer data) {
        writeCnt.incrementAndGet(); // always increment counters
        if (Stats.multyFileStatistics) {
            writeStatistics.consume(getBriefClassName(key), 1);
        }
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        FileStorage fileStorage = unitStorage.getFileStorage(key, true);
        try {
            if (fileStorage != null) {
                fileStorage.write(key, data);
            }
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, key, ex);
        }

    }

    @Override
    public void removeUnit(int unitIDInLayer) {
        layerIndex.removeUnit(unitIDInLayer);
        UnitStorage unitStorage = getUnitStorage(unitIDInLayer);
        unitStorage.cleanUnitDirectory();
    }

    public void purgeAllDiskStorage() {
        RepositoryImplUtil.deleteDirectory(cacheLocationFile, false);
    }

    /**
     * For test purposes ONLY! - gets read hit count
     */
    // package-local
    int getReadHitCnt() {
        return readHitCnt.get();
    }

    /**
     * For test purposes ONLY! - gets read hit percentage
     */
    // package-local
    int getReadHitPercentage() {
        return percentage(readHitCnt.get(), readCnt.get());
    }

    /**
     * For test purposes ONLY! - gets write hit count
     */
    // package-local
    int getWriteHitCnt() {
        return writeHitCnt.get();
    }

    /**
     * For test purposes ONLY! - gets read hit percentage
     */
    // package-local
    int getWriteHitPercentage() {
        return percentage(writeHitCnt.get(), writeCnt.get());
    }

    public void debugDump(LayerKey key) {
        UnitStorage unitStorage = getUnitStorage(key.getUnitId());
        unitStorage.debugDump(key);
    }

    @Override
    public boolean maintenance(long timeout) {
        long start = System.currentTimeMillis();

        for (UnitStorage storage : unitStorageCache.values()) {
            long rest = timeout - (System.currentTimeMillis() - start);
            if (rest <= 0) {
                return true;
            }
            if (storage.maintenance(rest)) {
                return true;
            }
        }

        return false;
    }

    private UnitStorage getUnitStorage(int unitID) {
        UnitStorage result = unitStorageCache.get(unitID);
        if (result == null) {
            result = new UnitStorage(cacheLocationFile, unitID);
            unitStorageCache.put(unitID, result);
        }
        return result;
    }

    @Override
    public int registerNewUnit(UnitDescriptor unitDescriptor) {
        return layerIndex.registerUnit(unitDescriptor);
    }

    @Override
    public int registerClientFileSystem(FileSystem fileSystem) {
        return layerIndex.registerFileSystem(fileSystem);
    }

    @Override
    public void storeFilesTable(Integer unitIDInLayer, List<CharSequence> filesList) {
        try {
            layerIndex.storeFilesTable(unitIDInLayer, filesList);
        } catch (IOException ex) {
            RepositoryExceptions.throwException(this, ex);
        }
    }

    Collection<LayerKey> removedTableKeySet() {
        return removedKeysFile.keySet();
    }

    @Override
    public int getMaintenanceWeight() throws IOException {
        int weight = 0;
        for (UnitStorage storage : unitStorageCache.values()) {
            weight += storage.dblStorage.getFragmentationPercentage();
        }
        return weight;
    }

    private static class UnitStorage {

        private final DoubleFileStorage dblStorage;
        private final SingleFileStorage singleStorage;
        private final File baseDir;

        private UnitStorage(File cacheLocationFile, int unitID) {
            baseDir = new File(cacheLocationFile, "" + unitID); // NOI18N
            dblStorage = new DoubleFileStorage(baseDir);
            singleStorage = new SingleFileStorage(baseDir);
        }

        private void close() {
            try {
                dblStorage.close();
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
        }

        private FileStorage getFileStorage(LayerKey key, boolean forWriting) {
            FileStorage storage;
            if (Key.Behavior.LargeAndMutable.equals(key.getBehavior())) {
                storage = singleStorage;
            } else {
                storage = dblStorage;
            }

            if (!storage.open(forWriting)) {
                return null;
            }

            return storage;
        }


        
        private void remove(LayerKey key) {
            FileStorage fileStorage = getFileStorage(key, true);
            try {
                if (fileStorage != null) {
                    fileStorage.remove(key);
                }
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
                        
        }
        
        private void debugDump(LayerKey key) {
            // if (Key.Behavior.LargeAndMutable.equals(key.getBehavior())) {
            dblStorage.debugDump(key);
        }

        /**
         * Returns true if more time needed
         * @param timeout
         * @return 
         */
        private boolean maintenance(long timeout) {
            try {
                return dblStorage.maintenance(timeout);
            } catch (IOException ex) {
                RepositoryExceptions.throwException(this, ex);
            }
            return false;
        }

        @Override
        public String toString() {
            return "UnitStorage: " + dblStorage + " & " + singleStorage; // NOI18N
        }

        private void cleanUnitDirectory() {
            RepositoryImplUtil.deleteDirectory(baseDir, true);
        }
    }
    
    private static class RepositoryDataInputImpl extends DataInputStream implements RepositoryDataInput {

        public RepositoryDataInputImpl(InputStream in) {
            super(in);
        }

        @Override
        public CharSequence readCharSequenceUTF() throws IOException {
            return UTF.readUTF(this);
        }

        @Override
        public int readUnitId() throws IOException {
            return readInt();
        }

        @Override
        public FileSystem readFileSystem() throws IOException {
            throw new InternalError();
        }
    }    
    
    private static class RepositoryDataOutputImpl extends DataOutputStream implements RepositoryDataOutput {

        public RepositoryDataOutputImpl(OutputStream in) {
            super(in);
        }

        @Override
        public void writeCharSequenceUTF(CharSequence seq) throws IOException {
            UTF.writeUTF(seq, this);
        }

        @Override
        public void writeUnitId(int unitId) throws IOException {
            writeInt(unitId);
        }

        @Override
        public void writeFileSystem(FileSystem fileSystem) throws IOException {
            writeInt(0);
        }

        @Override
        public void commit() {
            
        }

        
    }        
}
