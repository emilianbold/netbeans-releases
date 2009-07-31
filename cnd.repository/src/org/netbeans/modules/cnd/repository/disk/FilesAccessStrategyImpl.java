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

package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.sfs.BufferedRWAccess;
import org.netbeans.modules.cnd.repository.sfs.statistics.BaseStatistics;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.testbench.Stats;
import org.netbeans.modules.cnd.repository.util.Filter;

/**
 * Implements FilesAccessStrategy
 * @author Nickolay Dalmatov
 * @author Vladimir Kvashin
 */
public class FilesAccessStrategyImpl implements FilesAccessStrategy {
    
    private static class ConcurrentFileRWAccess extends BufferedRWAccess {
       
        public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        public final String unit;

        public ConcurrentFileRWAccess(File file, String unit) throws IOException {
            super(file);
            this.unit = unit;
        }
    }

    private static final class Lock {}
    private final Object cacheLock = new Lock();
    private RepositoryCacheMap<String, ConcurrentFileRWAccess> nameToFileCache;
    
    private static final int OPEN_FILES_LIMIT = Integer.getInteger("cnd.repository.files.cache", 20); // NOI18N
    
    private static final FilesAccessStrategyImpl instance = new FilesAccessStrategyImpl();
    
    private static final boolean TRACE_CONFLICTS = Boolean.getBoolean("cnd.repository.trace.conflicts");
    
    // Statistics
    private int readCnt = 0;
    private int readHitCnt = 0;
    private int writeCnt = 0;
    private int writeHitCnt = 0;
    BaseStatistics<String> writeStatistics;
    BaseStatistics<String> readStatistics;
    
    private FilesAccessStrategyImpl() {
        nameToFileCache = new RepositoryCacheMap<String, ConcurrentFileRWAccess>(OPEN_FILES_LIMIT);
        if( Stats.multyFileStatistics ) {
            resetStatistics();
        }
    }
    
    public static final FilesAccessStrategy getInstance() {
        return instance;
    }

    public Persistent read(Key key) throws IOException {
        readCnt++; // always increment counters
        if( Stats.multyFileStatistics ) {
            readStatistics.consume(getBriefClassName(key), 1);
        }
        ConcurrentFileRWAccess fis = null;
        try {
            fis = getFile(key, true);
            if( fis != null ) {
                final PersistentFactory factory = key.getPersistentFactory();
                assert factory != null;
                long size = fis.size();
                return fis.read(factory, 0, (int)size);
            }
        } finally {
            if (fis != null) {
                fis.lock.readLock().unlock();
            }
        }
        return null;
    }

    public void write(Key key, Persistent object) throws IOException {
        writeCnt++; // always increment counters
        if( Stats.multyFileStatistics ) {
            writeStatistics.consume(getBriefClassName(key), 1);
        }
        ConcurrentFileRWAccess fos = null;
        try {
            fos = getFile(key, false);
            assert fos != null;
            if (fos != null) {
                final PersistentFactory factory = key.getPersistentFactory();
                assert factory != null;
                int size = fos.write(factory, object, 0);
                fos.truncate(size);
            } 
        } finally {
            if (fos != null) {
                fos.lock.writeLock().unlock();
            }
        }
        
    }
    
    private ConcurrentFileRWAccess getFile(Key id, final boolean readOnly) throws IOException {
        
        assert id != null;
        
        String fileName = resolveFileName(id);
        assert fileName != null;
        
        ConcurrentFileRWAccess   aFile = null;
        boolean keepLocked = false;
        
        do {
            synchronized (cacheLock) {
                aFile = nameToFileCache.get(fileName);
                if (aFile == null) {
                    File fileToCreate = new File(fileName);
                    String unit = id.getUnit().toString();
                    if (fileToCreate.exists()) {
                        aFile = new ConcurrentFileRWAccess(fileToCreate, unit); //NOI18N
                        putFile(fileName, aFile);
                    } else if (! readOnly) {
                        String aDirName = fileToCreate.getParent();
                        File aDir = new File(aDirName);
                        if (aDir.exists() || aDir.mkdirs()) {
                            aFile = new ConcurrentFileRWAccess(fileToCreate, unit); //NOI18N
                            putFile(fileName, aFile);
                        }
                    }
                } else {
                    if( readOnly ) {
                        readHitCnt++;
                    } else {
                        writeHitCnt++;
                    }
                }
            }
            
            if (aFile == null) {
                break;
            }
            
            try {
                if (readOnly) {
                    aFile.lock.readLock().lock();
                } else {
                    aFile.lock.writeLock().lock();
                }
                if (aFile.getFD().valid()) {
                    keepLocked = true;
                    break;
                } else if( TRACE_CONFLICTS ) {
                    System.out.printf("invalid file descriptir when %s %s\n", readOnly ? "reading" : "writing", fileName); // NOI18N
                }
            }  finally {
                if (!keepLocked) {
                    if (readOnly) {
                        aFile.lock.readLock().unlock();
                    } else {
                        aFile.lock.writeLock().unlock();
                    }
                }
            }
            
        } while (true);
        
        return aFile;
    }
    
    private void putFile(String fileName, ConcurrentFileRWAccess aFile) throws IOException {
        ConcurrentFileRWAccess removedFile = null;
        synchronized (cacheLock) {
            removedFile = nameToFileCache.put(fileName, aFile);
        }
        if (removedFile != null) {
            try {
                removedFile.lock.writeLock().lock();
                if (removedFile.getFD().valid()) {
                    removedFile.close();
                }
                
            } finally {
                removedFile.lock.writeLock().unlock();
            }
        }
    }    
    
    public void remove(Key id) throws IOException{
        
        String fileName = resolveFileName(id);
        assert fileName != null;
        
        ConcurrentFileRWAccess removedFile = null;
        synchronized (cacheLock) {
            removedFile = nameToFileCache.remove(fileName);
        }
        if (removedFile != null) {
            try {
                removedFile.lock.writeLock().lock();
                
                if (removedFile.getFD().valid() ) {
                    removedFile.close();
                }
                
            }  finally {
                removedFile.lock.writeLock().unlock();
            }
        }
        
        File toDelete = new File(fileName);
        toDelete.delete();
        
    }
    
    public void closeUnit(final String unitName) throws IOException {
        Filter<ConcurrentFileRWAccess> filter = new Filter<ConcurrentFileRWAccess>() {
            public boolean accept(ConcurrentFileRWAccess value) {
                return value.unit.equals(unitName);
            }
        };
        Collection<ConcurrentFileRWAccess> removedFiles;
        synchronized (cacheLock) {
            removedFiles = nameToFileCache.remove(filter);
        }
        if (removedFiles != null) {
            for (ConcurrentFileRWAccess fileToRemove: removedFiles) {
                try {
                    fileToRemove.lock.writeLock().lock();
                    if (fileToRemove.getFD().valid()) {
                        fileToRemove.close();
                    }

                } finally {
                    fileToRemove.lock.writeLock().unlock();
                }
            }
        }
        if( Stats.multyFileStatistics ) {
            printStatistics();
            resetStatistics();
        }
    }
    
    // package-local - for test purposes
    void printStatistics() {
        System.out.printf("\nFileAccessStrategy statistics: reads %d hits %d (%d%%) writes %d hits %d (%d%%)\n",  // NOI18N
                readCnt, readHitCnt, percentage(readHitCnt, readCnt), writeCnt, writeHitCnt, percentage(writeHitCnt, writeCnt));
        if( writeStatistics != null ) {
            readStatistics.print(System.out);
        }
        if( writeStatistics != null ) {
            writeStatistics.print(System.out);
        }
    }
            
    private static int percentage(int numerator, int denominator) {
        return (denominator == 0) ? 0 : numerator*100/denominator;
    }
            
    private void resetStatistics() {
        writeStatistics = new BaseStatistics<String>("Writes", BaseStatistics.LEVEL_MEDIUM); // NOI18N
        readStatistics = new BaseStatistics<String>("Reads", BaseStatistics.LEVEL_MEDIUM); // NOI18N
        readCnt = readHitCnt = writeCnt = writeHitCnt = 0;
    }
    
    private final static char SEPARATOR_CHAR = '-';
    
    private static String resolveFileName(Key id) throws IOException {
        
        assert id != null;
        int size = id.getDepth();
        
        StringBuilder    nameBuffer = new StringBuilder(""); //NOI18N

        if( size == 0 ) {
            nameBuffer.append(id.getUnit());
        }
        else {
            for (int i = 0 ; i < size; ++i) {
                nameBuffer.append(id.getAt(i));
                nameBuffer.append(SEPARATOR_CHAR);
            }
        }

        for (int j = 0 ; j < id.getSecondaryDepth(); ++j) {
            nameBuffer.append(id.getSecondaryAt(j)  + SEPARATOR_CHAR);
        }

        String fileName = nameBuffer.toString();

        fileName = URLEncoder.encode(fileName, Stats.ENCODING);

        fileName = StorageAllocator.getInstance().getUnitStorageName(id.getUnit().toString()) + 
                StorageAllocator.getInstance().reduceString(fileName);

        return fileName;
    }
    
    private static String getBriefClassName(Object o) {
        if( o == null ) {
            return "null"; // NOI18N
        } else {
            String name = o.getClass().getName();
            int pos = name.lastIndexOf('.');
            return (pos < 0) ? name : name.substring(pos + 1);
        }
    }
    
    /** 
     * For test purposes ONLY! 
     * Gets a collection of all cached files names
     */
    // package-local
    Collection<String> testGetCacheFileNames() {
        synchronized( cacheLock ) {
            return nameToFileCache.keys();
        }
    }

    /** For test purposes ONLY! - gets read hit count */
    // package-local
    int getReadHitCnt() {
        return readHitCnt;
    }

    /** For test purposes ONLY! - gets read hit percentage */
    // package-local
    int getReadHitPercentage() {
        return percentage(readHitCnt, readCnt);
    }

    /** For test purposes ONLY! - gets write hit count */
    // package-local
    int getWriteHitCnt() {
        return writeHitCnt;
    }

    /** For test purposes ONLY! - gets read hit percentage */
    // package-local
    int getWriteHitPercentage() {
        return percentage(writeHitCnt, writeCnt);
    }

    /** For test purposes ONLY! - gets cache size */
    // package-local
    int getCacheSize() {
        return nameToFileCache.size();
    }
    
}
