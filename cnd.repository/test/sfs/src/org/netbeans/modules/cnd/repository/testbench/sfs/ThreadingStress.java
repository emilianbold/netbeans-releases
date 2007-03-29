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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.sfs.*;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;
/**
 * Threading test for SingleFileStorage
 * @author Vladimir Kvashin
 */
public class ThreadingStress extends BaseTest {
    
    private SingleFileStorage sfs;
    private TestObject[] objects;
    private Object startBarrier = new Object();
    
    private int writeCycles = Integer.getInteger("thread.test.write.cycles", 80);
    private int readCycles = Integer.getInteger("thread.test.read.cycles", 320);
    private int readersCount = Integer.getInteger("thread.test.readers.count", 8);
    
    private boolean compact = Stats.getBoolean("thread.test.compact", true); 
    
    private Thread writerThread;
    private Collection<Thread> readerThreads;
    
    private boolean writingFinished = false;
    
    private int currReader = 0;
    
    private int errors = 0;
    
    private class Writer implements Runnable {
	
	public void run() {
	    
	    Thread.currentThread().setName("Stress Writer");
	    System.out.printf("%s started. waiting on barier.\n", Thread.currentThread().getName());
	    waitBarrier(startBarrier);

	    try {
		for (int i = 0; i < writeCycles; i++) {
		    writeCycle(i);
		    writingFinished = true;
		}
	    }
	    catch( Exception e ) {
		e.printStackTrace(System.err);
		incermentErrorCnt();
	    }
	    System.out.printf("%s finished.\n", Thread.currentThread().getName());
	}
    }
    
    private class Reader implements Runnable {
	
	public void run() {
	    
	    Thread.currentThread().setName("Stress Reader " + currReader++);
	    System.out.printf("%s started. waiting on barier.\n", Thread.currentThread().getName());
	    waitBarrier(startBarrier);
	    
	    try {
		for (int i = 0; i < readCycles; i++) {
		    readCycle(i);
		}
	    }
	    catch( Exception e ) {
		e.printStackTrace(System.err);
		incermentErrorCnt();
	    }
	    System.out.printf("%s finished.\n", Thread.currentThread().getName());
	}
    }

    private void waitBarrier(Object barrier) {
	synchronized (barrier) {
	    try {
		barrier.wait();
	    } catch (InterruptedException ex) {
		//ex.printStackTrace();
	    }
	}
    }
    
    public void test(List<String> args) throws IOException {
	
	File storageFile = new File("/tmp/stress.dat"); // NOI18N
	System.out.printf("Testing SingleFileStorage threading. Storage file: %s\n", storageFile.getAbsolutePath()); // NOI18N
	sfs = new SingleFileStorage(storageFile);

	writerThread = new Thread(new Writer());
	readerThreads = new ArrayList<Thread>();
	for (int i = 0; i < readersCount; i++) {
	    readerThreads.add(new Thread(new Reader()));
	}
	
	writerThread.start();
	for( Thread reader : readerThreads ) {
	    reader.start();
	}
	wait(1000);

	System.out.printf("Creating test objects...\n");
	Collection<TestObject> tmp_objects = new TestObjectCreator().createTestObjects(args);
	objects = tmp_objects.toArray(new TestObject[tmp_objects.size()]);
	System.out.printf("\nCreating objects done. %d objects are created\n", objects.length);
	
//	System.out.printf("\nInitial writing...\n");
//	writeCycle();
	
	System.out.printf("\nReader threads: %d. Read cycles: %d, Write cycles: %d \n", readersCount, readCycles, writeCycles);
	synchronized(startBarrier) {
	    startBarrier.notifyAll();
	}
	
	// waiting for threads to finish
	while( ! threadsDone() ) {
	    try {
		Thread.currentThread().sleep(1000);
	    }
	    catch( InterruptedException e ) {
		break;
	    }
	}

	sfs.dumpSummary(System.out);
	sfs.close();
	
	if( errors == 0 ) {
	    System.out.printf("\nSUCCESS\n");
	}
	else {
	    System.out.printf("\nFAULIRE: %d errors\n", errors);
	}
	
    }
    
    private void wait(int millis) {
	try {
	    Thread.currentThread().sleep(millis);
	}
	catch( InterruptedException e ) {
	}
    }
    
    private boolean threadsDone() {
	if( writerThread.getState() != Thread.State.TERMINATED ) {
	    return false;
	}
	for( Thread reader : readerThreads ) {
	    if( reader.getState() != Thread.State.TERMINATED ) {
		return false;
	    }
	}
	return true;
    }

    private void writeCycle(int cycleId) throws IOException {
	System.out.printf("%s: starting write cycle %d \n", Thread.currentThread().getName(), cycleId);
	for (int i = 0; i < objects.length; i++) {
	    sfs.put(objects[i].getKey(), objects[i]);
	}
	if( compact ) {
	    long time = (cycleId % 2 == 0) ? 1000 : 0;
	    String kind = (time == 0) ? "Full" : "Partial";
	    System.out.printf("%s compacting (cycle %d size %d)...\n", kind, cycleId, sfs.getFileSize());
	    sfs.compact(time);
	    System.out.printf("\t%s compacting (cycle %d size %d) done\n", kind, cycleId, sfs.getFileSize());
	}
    }
    
    private void readCycle(int cycleId) throws IOException {
	System.out.printf("%s: starting read cycle %d \n", Thread.currentThread().getName(), cycleId);
	for (int i = 0; i < objects.length; i++) {
	    int index = Math.min((int) (Math.random() * objects.length), objects.length-1);
	    testRead(objects[index]);
	}
    }
    
    private void testRead(TestObject orig) throws IOException {
	Key key = orig.getKey();
	TestObject read = (TestObject) sfs.get(key);
	if( read == null ) {
	    if( writingFinished ) {
		incermentErrorCnt();
		System.err.printf("ERROR: read null by key %s\n", key);
	    }
	}
	else {
	    if( ! read.equals(orig) ) {
		incermentErrorCnt();
		System.err.printf("ERROR:\n");
		System.err.printf("Wrote: %s\n", orig.toString());
		System.err.printf("Read:  %s\n", read.toString());
		long fileSize = sfs.getFileSize();
		read = (TestObject) sfs.get(key);
		read = (TestObject) sfs.get(key);
	    }
	}
    }
    
    private void incermentErrorCnt() {
	errors++;
	if( stopOnError ) {
	    System.exit(100);
	}
    }
}

