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
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.sfs.statistics.FileStatistics;
import org.netbeans.modules.cnd.repository.sfs.statistics.RangeStatistics;
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
    
    private class Worker {
	
	private File file;
	private FileStatistics fileStatistics;
	private FileIndex index;
	private FileRWAccess fileRWAccess;
	// used to accumulate the total currently used chunk suze;
	// is necessary for tracking fragmentation
	private long usedSize;
	
	public Worker(File file) throws IOException {
	    this.file = file;
	    fileRWAccess = createFileRWAccess(file);
	    fileRWAccess.truncate(0);
	    fileStatistics = new FileStatistics();
	    index = new SynchronizedFileIndex(Stats.useCompactIndex ? new CompactFileIndex() : new SimpleFileIndex());
	    usedSize = 0;
	}
	
	private Persistent get(Key key) throws IOException {
	    ChunkInfo chunkInfo = index.get(key);
	    if( chunkInfo != null ) {
		Persistent object = fileRWAccess.read(key.getPersistentFactory(), chunkInfo.getOffset(), chunkInfo.getSize());
		fileStatistics.incrementReadCount(key);
		return object;
	    }
	    return null;
	}
	
	private void put(Key key, Persistent object) throws IOException {
	    long offset = fileRWAccess.size();
	    int size = fileRWAccess.write(key.getPersistentFactory(), object, offset);
	    int oldSize = index.put(key, offset, size);
	    fileStatistics.incrementWriteCount(key, oldSize, size);
	    usedSize += (size - oldSize);
	}	
	
	private void remove(Key key) {
	    int oldSize = index.remove(key);
	    fileStatistics.removeNotify(key);
	    usedSize -= oldSize;
	}
	
	public int getObjectsCount() {
	    return index.size();
	}

	public long getFileSize() throws IOException {
	    return fileRWAccess.size();
	}
	
    
	public void close() throws IOException {
	    if( Stats.dumoFileOnExit ) {
		dump(System.out);
	    } else {
		if( Stats.fileStatisticsLevel > 0 ) {
		    dumpSummary(System.out);
		}
	    }
	    fileRWAccess.close();
	    if (Stats.deleteCacheFiles) {
		file.deleteOnExit();
	    }
	}
	
	public int getFragmentationPercentage() throws IOException {
	    long fileSize = fileRWAccess.size();
	    float delta = fileSize - usedSize;
	    float percentage = delta * 100 / fileSize;
	    return Math.round(percentage);
	}

	public void dump(PrintStream ps) throws IOException {

	    ps.printf("\nDumping %s\n", file.getAbsolutePath()); // NOI18N

	    ps.printf("\nKeys:\n"); // NOI18N
	    for( Key key : index.keySet() ) {
		ChunkInfo chunk = index.get(key);
		ps.printf("\t%s: ", key); // NOI18N
		print(ps, null, chunk, true);
	    }

	    ps.printf("\nChunks:\n"); // NOI18N
	    ChunkInfo[] infos = sortedChunkInfos();
	    for (int i = 0; i < infos.length; i++) {
		print(ps, null, infos[i], true);
	    }
	    
	    dumpSummary(ps, infos);
	}

	public void dumpSummary(PrintStream ps) throws IOException {
	    dumpSummary(ps, null);
	}

	public void dumpSummary(PrintStream ps, ChunkInfo[] sortedInfos) throws IOException {

	    RangeStatistics write = new RangeStatistics("Writes:", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);   // NOI18N
	    RangeStatistics read = new RangeStatistics("Reads: ", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);    // NOI18N
	    RangeStatistics size = new RangeStatistics("Sizes: ", Stats.fileStatisticsLevel, Stats.fileStatisticsRanges);    // NOI18N
	    long usedSize = 0;
	    for( Key key : index.keySet() ) {
		ChunkInfo info = index.get(key);
		usedSize += info.getSize();
		read.consume(fileStatistics.getReadCount(key));
		write.consume(fileStatistics.getWriteCount(key));
		size.consume(info.getSize());
	    }
	    long channelSize = fileRWAccess.size();

	    ps.printf("\n"); // NOI18N
	    ps.printf("Dumping %s\n", file.getAbsolutePath()); // NOI18N
	    ps.printf("Entries count: %d\n", index.size()); // NOI18N
	    ps.printf("\n"); // NOI18N
	    write.print(ps);
	    read.print(ps);
	    size.print(ps);
	    ps.printf("\n"); // NOI18N

	    ps.printf("File size:  %16d\n", channelSize); // NOI18N
	    ps.printf("Used size:  %16d\n", usedSize); // NOI18N
	    ps.printf("Percentage used: %11d%%\n", channelSize == 0 ? 0 : ((100*usedSize)/channelSize)); // NOI18N
	    ps.printf("Fragmentation:   %11d%%\n", getFragmentationPercentage()); // NOI18N

	    if( sortedInfos == null ) {
		sortedInfos = sortedChunkInfos();
	    }
	    long firstExtent = (sortedInfos.length > 0) ? sortedInfos[0].getOffset() : 0;
	    ps.printf("First busy extent: %9d (0x%H)\n\n", firstExtent, firstExtent); // NOI18N
	}

	private void print(PrintStream ps, Key key, ChunkInfo chunk, boolean lf) {
	    long endOffset = chunk.getOffset() + chunk.getSize()-1;
	    ps.printf("%d-%d %d [0x%H-0x%H] read: %d written: %d (%s) %c", // NOI18N
		    chunk.getOffset(), endOffset, chunk.getSize(), chunk.getOffset(), endOffset,
		    fileStatistics.getReadCount(key), fileStatistics.getWriteCount(key), chunk.toString(),
		    lf ? '\n' : ' '); // NOI18N
	}

	private ChunkInfo[] sortedChunkInfos() {
	    ChunkInfo[] infos = new ChunkInfo[index.size()];
	    int pos = 0;
	    for( Key key : index.keySet() ) {
		infos[pos++] = index.get(key);
	    }
	    Arrays.sort(infos);
	    return infos;
	}	
	
	private String getTraceString() throws IOException {
	    Formatter formatter = new Formatter();
	    formatter.format("%s index size %d  file size %d  fragmentation %d%%", // NOI18N
			file.getName(), index.size(), getFileSize(), getFragmentationPercentage());
	    return formatter.toString();
	}
    }
    
    private Map<Key, Persistent> fickleMap = new HashMap<Key, Persistent>();

    private File basePath;
    
    private Worker active;
    private Worker passive;
    
    private Object switchLock = new Object();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private boolean defragmenting = false;
    
    private Worker getActive() {
	return active;
    }
    
    private Worker getPassive() {
	return passive;
    }
    
    public DoubleFileStorage(String basePath) throws IOException {
        this(new File(basePath));
    }
    
    public DoubleFileStorage(File basePath) throws IOException {
        this.basePath = basePath;
	active = new Worker(File.createTempFile("cache-0", null, basePath)); // NOI18N
	passive = new Worker(File.createTempFile("cache-1", null, basePath)); // NOI18N
    }
    
    private FileRWAccess createFileRWAccess(File file) throws IOException {
	FileRWAccess result;
        switch( Stats.fileRWAccess ) {
            case 0:
                result = new BufferedRWAccess(file);
            case 1:
                result = new SimpleRWAccess(file);
            default:
                result = new BufferedRWAccess(file);
        }
	result.truncate(0);
        return result;
    }    

    public void close() throws IOException {
	active.close();
	passive.close();
    }
    
    // NB: does not synchronize!
    private void switchFile() {
	Worker previous = active;
	active = passive;
	passive = previous;
    }
    
    public void defragment() throws IOException {
	defragment(0);
    }
    
    public boolean defragment(long timeout) throws IOException {

	WriteStatistics.instance().update(0);
	
	if( Stats.traceDefragmentation ) {
	    System.out.printf(">>> Defragmenting %s; timeout %d ms total fragmentation %d%%\n", basePath.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
	    System.out.printf("\tActive:  %s\n", active.getTraceString()); // NOI18N
	    System.out.printf("\tPassive: %s\n", passive.getTraceString()); // NOI18N
	} 
	
	if( timeout > 0 ) {
	    if( ! defragmenting ) {
		if( getFragmentationPercentage() < Stats.defragmentationThreashold ) {
		    if( Stats.traceDefragmentation ) System.out.printf("\tFragmentation is too low\n"); // NOI18N
		    return false;
		}
	    }
	}
	
	synchronized (switchLock) {
	    if( ! defragmenting ) {
		defragmenting = true;
		switchFile();
	    }
	}
	
	boolean needMoreTime = _defragment(timeout);
	
	synchronized (switchLock) {
	    if( passive.index.size() == 0 ) {
		lock.writeLock().lock();
		try {
		    defragmenting = false;
		    passive.fileRWAccess.truncate(0);
		} finally {
		    lock.writeLock().unlock();
		}
	    }
	}
	
	if( Stats.traceDefragmentation ) {
	    System.out.printf("<<< Defragmenting %s; timeout %d ms total fragmentation %d%%\n", basePath.getAbsolutePath(), timeout, getFragmentationPercentage()); // NOI18N
	    System.out.printf("\tActive:  %s\n", active.getTraceString()); // NOI18N
	    System.out.printf("\tPassive: %s\n", passive.getTraceString()); // NOI18N
	} 
	
	return needMoreTime;
    }
    
    private boolean _defragment(long timeout) throws IOException {
	
	boolean needMoreTime = false;
	long time = ((timeout > 0) || Stats.traceDefragmentation) ? System.currentTimeMillis() : 0;
	
	int cnt = 0;
	Iterator<Key> it = passive.index.getKeySetIterator();
	while( it.hasNext() ) {
	    Key key = it.next();
	    ChunkInfo chunk = passive.index.get(key);
	    int size = chunk.getSize();
	    if( active.index.get(key) == null ) {
		long newOffset = active.fileRWAccess.size();
		active.fileRWAccess.move(passive.fileRWAccess, chunk.getOffset(), size, newOffset);
		active.index.put(key, newOffset, size);
		active.usedSize += size;
	    }
	    passive.usedSize -= size;
	    lock.writeLock().lock();
	    try {
		it.remove();
	    } finally {
		lock.writeLock().unlock();
	    }
	    cnt++;
	    if( (timeout > 0) && (cnt % 10 == 0) ) {
		if( System.currentTimeMillis()-time >= timeout ) {
		    needMoreTime = true;
		    break;
		}
	    }
	}
	if( Stats.traceDefragmentation ) {
	    String text = it.hasNext() ? " finished by timeout" : " completed"; // NOI18N
	    System.out.printf("\t # defragmentinging %s %s; moved: %d remaining: %d \n", passive.file.getName(), text, cnt, passive.index.size()); // NOI18N
	}
	return needMoreTime;
    }
    
    
    public void dump(PrintStream ps) throws IOException {
	ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", basePath.getAbsolutePath()); // NOI18N
	ps.printf("\nActive file:\n"); // NOI18N
	active.dump(ps);
	ps.printf("\nPassive file:\n"); // NOI18N
	passive.dump(ps);
	ps.printf("\n"); // NOI18N
    }
    
    public void dumpSummary(PrintStream ps) throws IOException {
	ps.printf("\nDumping DoubleFileStorage; baseFile %s\n", basePath.getAbsolutePath()); // NOI18N
	ps.printf("\nActive file:\n"); // NOI18N
	active.dumpSummary(ps);
	ps.printf("\nPassive file:\n"); // NOI18N
	passive.dumpSummary(ps);
	ps.printf("\n"); // NOI18N
    }
    
    public Persistent get(Key key) throws IOException {
	if( Stats.hardFickle && key.getBehavior() == Key.Behavior.LargeAndMutable ) {
	    return fickleMap.get(key);
	}
	Persistent object = null;
	lock.readLock().lock();
	try {
	    object = active.get(key);
	    if( object == null ) {
		//TODO: solve removes elements issue
		object = passive.get(key);
	    }
	} finally {
	    lock.readLock().unlock();
	}
	return object;
    }
    
    public int getFragmentationPercentage() throws IOException {
	long fileSize = active.fileRWAccess.size() + passive.fileRWAccess.size();
	float delta = fileSize - (active.usedSize + passive.usedSize);
	float percentage = delta * 100 / fileSize;
	return Math.round(percentage);
    }
    
    public void put(Key key, Persistent object) throws IOException {
 	WriteStatistics.instance().update(1);
	if( Stats.hardFickle &&  key.getBehavior() == Key.Behavior.LargeAndMutable ) {
	    fickleMap.put(key, object);
	    return;
	}
	active.put(key, object);
    }
    
    public void remove(Key key) {
	if( Stats.hardFickle &&  key.getBehavior() == Key.Behavior.LargeAndMutable ) {
	    fickleMap.remove(key);
	    return;
	}
	//TODO: solve removes elements issue
	active.remove(key);
    }
    
    public long getSize() throws IOException {
	return active.getFileSize() + passive.getFileSize();
    }
    
    public int getObjectsCount() {
	return active.getObjectsCount() + passive.getObjectsCount();
    }
    
}
