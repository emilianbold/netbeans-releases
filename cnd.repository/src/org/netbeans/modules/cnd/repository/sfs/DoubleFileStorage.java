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

package org.netbeans.modules.cnd.repository.sfs;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * Stores Persistent objects in two files;
 * the main purpose of the two files is managing defragmentation:
 * while one file is active (recent changes are written into it),
 * other one might be defragmented
 *
 * @author Vladimir Kvashin
 */
public class DoubleFileStorage extends FileStorage {
    
    private Map<Key, Persistent> fickleMap = new HashMap<Key, Persistent>();
    private File basePath;
    
    private IndexedStorageFile file1;
    private IndexedStorageFile file2;
    
    private boolean defragmenting = false;
    private boolean swapped = false;
    
    private ReadWriteLock  rwLock;
    
    private IndexedStorageFile getActive() {
        return (swapped ? file2 : file1);
    }
    
    private IndexedStorageFile getPassive() {
        return (swapped ? file1 : file2) ;
    }
    
    private Lock readLock() {
        return rwLock.readLock();
    }
    
    private Lock writeLock() {
        return rwLock.writeLock();
    }
    
    public DoubleFileStorage(final String basePath) throws IOException {
        this(new File(basePath));
    }
    
    public DoubleFileStorage(final File basePath) throws IOException {
        this (basePath, false);
    }
    /**
     * Creates a new <code>DoubleFileStorage</code> instance 
     *
     * @param   basePath  A File representing path to the storage 
     * @param   create    A flag if the storage should be created, not opened
     *
     */
    protected DoubleFileStorage (final File basePath, final boolean create) throws IOException {
        this.basePath = basePath;
        rwLock = new ReentrantReadWriteLock(true);
        file1 = new IndexedStorageFile(basePath, "cache-0", create); // NOI18N
        file2 = new IndexedStorageFile(basePath, "cache-1", create); // NOI18N

        //
        if ((file1.getDataFileUsedSize() == 0 ) &&
            (file2.getDataFileUsedSize() == 0)) {
            swapped = false;
        } else if ((file1.getDataFileUsedSize() != 0 ) &&
                    (file2.getDataFileUsedSize() != 0)) {
            swapped = 
             (file1.getFragmentationPercentage() < file2.getFragmentationPercentage())?false:true;
        } else {
            swapped = (file1.getDataFileUsedSize() == 0)?false:true;
        }
    }
    
    public void close() throws IOException {
        file1.close();
        file2.close();
    }
    
    public Persistent get(final Key key) throws IOException {
        if( Stats.hardFickle && key.getBehavior() == Key.Behavior.LargeAndMutable ) {
            return fickleMap.get(key);
        }
        Persistent object = null;
        try {
            readLock().lock();
            object = getActive().get(key);
            if( object == null ) {
                object = getPassive().get(key);
            }
        } finally {
            readLock().unlock();
        }
        return object;
    }
    
    public void put(final Key key, final Persistent object) throws IOException {
        WriteStatistics.instance().update(1);
        if( Stats.hardFickle &&  key.getBehavior() == Key.Behavior.LargeAndMutable ) {
            fickleMap.put(key, object);
            return;
        }
        
        try {
            writeLock().lock();
            getActive().put(key, object);
            getPassive().remove(key);
        } finally {
            writeLock().unlock();
        }
    }
    
    public void remove(final Key key) throws IOException {
        if( Stats.hardFickle &&  key.getBehavior() == Key.Behavior.LargeAndMutable ) {
            fickleMap.remove(key);
            return;
        }
        try {
            writeLock().lock();
            getActive().remove(key);
            getPassive().remove(key);
        } finally {
            writeLock().unlock();
        }
        
    }
    
    public void defragment() throws IOException {
        defragment(0);
    }
    
    public boolean defragment(final long timeout) throws IOException {
        
        boolean needMoreTime = false;
        
        WriteStatistics.instance().update(0);
        try {
            writeLock().lock();
            
            if( Stats.traceDefragmentation ) {
                System.out.printf(">>> Defragmenting %s; timeout %d ms total fragmentation %d%%\n", basePath.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
                System.out.printf("\tActive:  %s\n", getActive().getTraceString()); // NOI18N
                System.out.printf("\tPassive: %s\n", getPassive().getTraceString()); // NOI18N
            }
            
            if( timeout > 0 ) {
                if( ! defragmenting ) {
                    if( getFragmentationPercentage() < Stats.defragmentationThreashold ) {
                        if( Stats.traceDefragmentation ) System.out.printf("\tFragmentation is too low\n"); // NOI18N
                        return needMoreTime;
                    }
                }
            }
            
            if( ! defragmenting ) {
                defragmenting = true;
                swapped = !swapped;
            }
            
            
            needMoreTime = _defragment(timeout);
            
            
            if( getPassive().getObjectsCount() == 0 ) {
                defragmenting = false;
            }

            if( Stats.traceDefragmentation ) {
                System.out.printf("<<< Defragmenting %s; timeout %d ms total fragmentation %d%%\n", basePath.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
                System.out.printf("\tActive:  %s\n", getActive().getTraceString()); // NOI18N
                System.out.printf("\tPassive: %s\n", getPassive().getTraceString()); // NOI18N
            }
            
        } finally {
            writeLock().unlock();
        }
        
        return needMoreTime;
    }
    
    private boolean _defragment(final long timeout) throws IOException {
        
        boolean needMoreTime = false;
        final long time = ((timeout > 0) || Stats.traceDefragmentation) ? System.currentTimeMillis() : 0;
        
        int cnt = 0;
        Iterator<Key> it = getPassive().getKeySetIterator();
        while( it.hasNext() ) {
            try {
                writeLock().lock();
                Key key = it.next();
                ChunkInfo chunk = getPassive().getChunkInfo(key);
                int size = chunk.getSize();
                long newOffset = getActive().getSize();
                getActive().moveDataFromOtherFile(getPassive().getDataFile(), chunk.getOffset(), size, newOffset, key);
                it.remove();
                cnt++;
            } finally {
                writeLock().unlock();
            }
            
            if( (timeout > 0) && (cnt % 10 == 0) ) {
                if( System.currentTimeMillis()-time >= timeout ) {
                    needMoreTime = true;
                    break;
                }
            }
        }
        if( Stats.traceDefragmentation ) {
            String text = it.hasNext() ? " finished by timeout" : " completed"; // NOI18N
            System.out.printf("\t # defragmentinging %s %s; moved: %d remaining: %d \n", getPassive().getDataFileName(), text, cnt, getPassive().getObjectsCount()); // NOI18N
        }
        return needMoreTime;
    }
    
    public void dump(PrintStream ps) throws IOException {
        ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", basePath.getAbsolutePath()); // NOI18N
        ps.printf("\nActive file:\n"); // NOI18N
        
        try {
            readLock().lock();
            getActive().dump(ps);
            ps.printf("\nPassive file:\n"); // NOI18N
            getPassive().dump(ps);
            
        } finally {
            readLock().unlock();
        }
        ps.printf("\n"); // NOI18N
    }
    
    public void dumpSummary(PrintStream ps) throws IOException {
        ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", basePath.getAbsolutePath()); // NOI18N
        ps.printf("\nActive file:\n"); // NOI18N
        
        try {
            readLock().lock();
            getActive().dumpSummary(ps);
            ps.printf("\nPassive file:\n"); // NOI18N
            getPassive().dumpSummary(ps);
            
        } finally {
            readLock().unlock();
        }
        ps.printf("\n"); // NOI18N
    }
    
    public int getFragmentationPercentage() throws IOException {
        final long fileSize;
        final float delta;
        
        try {
            readLock().lock();
            fileSize = getActive().getSize() + getPassive().getSize();
            delta = fileSize - (getActive().getDataFileUsedSize() + getPassive().getDataFileUsedSize());
        } finally {
            readLock().unlock();
        }
        final float percentage = delta * 100 / fileSize;
        return Math.round(percentage);
    }
    
    public long getSize() throws IOException {
        try {
            readLock().lock();
            return getActive().getSize() + getPassive().getSize();
        } finally {
            readLock().unlock();
        }
    }
    
    public int getObjectsCount() {
        try {
            readLock().lock();
            return getActive().getObjectsCount() + getPassive().getObjectsCount();
        } finally {
            readLock().unlock();
        }
    }
}
