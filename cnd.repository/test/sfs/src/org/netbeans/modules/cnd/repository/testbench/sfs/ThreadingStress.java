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

package org.netbeans.modules.cnd.repository.testbench.sfs;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.sfs.*;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;
/**
 * Threading test for FileStorage
 * @author Vladimir Kvashin
 */
public class ThreadingStress extends BaseTest {
    
    private FileStorage sfs;
    private TestObject[] objects;
    private Object startBarrier = new Object();
    
    private int writeCycles = Integer.getInteger("thread.test.write.cycles", 80); // NOI18N
    private int readCycles = Integer.getInteger("thread.test.read.cycles", 320); // NOI18N
    private int readersCount = Integer.getInteger("thread.test.readers.count", 8); // NOI18N
    
    private boolean compact = Stats.getBoolean("thread.test.compact", true); 
    
    private Writer writer;
    private Collection<Reader> readers;
    
    private boolean writingFinished = false;
    
    private int currReader = 0;
    
    private int errors = 0;

    enum WriterStatus {
	Initial,
	Writing,
	Defragmenting,
	Finished
    }
    
    private class Writer extends Thread {
	
	private int writtenThisCycle = 0;
	private WriterStatus status;
	
	public Writer() {
	    super("Stress Writer"); // NOI18N
	    status = WriterStatus.Initial;
	}
	
	public void run() {
	    
	    Thread.currentThread().setName("Stress Writer"); // NOI18N
	    System.out.printf("%s started. waiting on barier.\n", Thread.currentThread().getName()); // NOI18N
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
	    status = WriterStatus.Finished;
	    System.out.printf("%s finished.\n", Thread.currentThread().getName()); // NOI18N
	}
	
	private void writeCycle(int cycleId) throws IOException {
	    status = WriterStatus.Writing;
	    System.out.printf("%s: starting write cycle %d \n", Thread.currentThread().getName(), cycleId); // NOI18N
	    for (writtenThisCycle = 0; writtenThisCycle < objects.length; writtenThisCycle++) {
		TestObject obj = objects[writtenThisCycle];
		sfs.write(obj.getKey(), obj);
	    }
	    if( compact ) {
		status = WriterStatus.Defragmenting;
		long time = (cycleId % 2 == 0) ? 1000 : 0;
		String kind = (time == 0) ? "Full" : "Partial"; // NOI18N
		System.out.printf("%s compacting (cycle %d size %d)...\n", kind, cycleId, sfs.getSize()); // NOI18N
		sfs.maintenance(time);
		System.out.printf("\t%s compacting (cycle %d size %d) done\n", kind, cycleId, sfs.getSize()); // NOI18N
	    }
	}
	
    }
    
    private class Reader extends Thread {
	
	public Reader() {
	    super("Stress Reader " + currReader++); // NOI18N
	}
	
	public void run() {
	    
	    System.out.printf("%s started. waiting on barier.\n", Thread.currentThread().getName()); // NOI18N
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
	    System.out.printf("%s finished.\n", Thread.currentThread().getName()); // NOI18N
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
    
    public boolean test(List<String> args) throws IOException {
	
	File storageFile = new File("/tmp/stress.dat"); // NOI18N
	System.out.printf("Testing FileStorage threading. Storage file: %s\n", storageFile.getAbsolutePath()); // NOI18N
	sfs = FileStorage.create(storageFile.getAbsolutePath());

	writer = new Writer();
	readers = new ArrayList<Reader>();
	for (int i = 0; i < readersCount; i++) {
	    readers.add(new Reader());
	}
	
	writer.start();
	for( Thread reader : readers ) {
	    reader.start();
	}
	wait(1000);

	System.out.printf("Creating test objects...\n"); // NOI18N
	Collection<TestObject> tmp_objects = new TestObjectCreator().createTestObjects(args);
	objects = tmp_objects.toArray(new TestObject[tmp_objects.size()]);
	System.out.printf("\nCreating objects done. %d objects are created\n", objects.length); // NOI18N
	
//	System.out.printf("\nInitial writing...\n");
//	writeCycle();
	
	System.out.printf("\nReader threads: %d. Read cycles: %d, Write cycles: %d \n", readersCount, readCycles, writeCycles); // NOI18N
	synchronized(startBarrier) {
	    startBarrier.notifyAll();
	}
	
	// waiting for threads to finish
	while( ! threadsDone() ) {
	    try {
		Thread.sleep(1000);
	    }
	    catch( InterruptedException e ) {
		break;
	    }
	}

	sfs.dumpSummary(System.out);
	sfs.close();
	
	if( errors == 0 ) {
	    System.out.printf("\nSUCCESS\n"); // NOI18N
	}
	else {
	    System.out.printf("\nFAULIRE: %d errors\n", errors); // NOI18N
	}
	return errors == 0;
    }
    
    private void wait(int millis) {
	try {
	    Thread.sleep(millis);
	}
	catch( InterruptedException e ) {
	}
    }
    
    private boolean threadsDone() {
	if( writer.getState() != Thread.State.TERMINATED ) {
	    return false;
	}
	for( Thread reader : readers ) {
	    if( reader.getState() != Thread.State.TERMINATED ) {
		return false;
	    }
	}
	return true;
    }

    private void readCycle(int cycleId) throws IOException {
	System.out.printf("%s: starting read cycle %d; writer status is %s\n", // NOI18N
		Thread.currentThread().getName(), cycleId, writer.status); // NOI18N
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
		System.err.printf("ERROR: read null by key %s; writer status is %s\n", key, writer.status);
		incermentErrorCnt();
	    }
	}
	else {
	    if( ! read.equals(orig) ) {
		System.err.printf("ERROR:\n");
		System.err.printf("Wrote: %s\n", orig.toString());
		System.err.printf("Read:  %s\n", read.toString());
		long fileSize = sfs.getSize();
		read = (TestObject) sfs.get(key);
		read = (TestObject) sfs.get(key);
		incermentErrorCnt();
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

