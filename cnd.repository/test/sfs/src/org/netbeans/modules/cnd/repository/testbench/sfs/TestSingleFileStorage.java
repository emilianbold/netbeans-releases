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
import org.netbeans.modules.cnd.repository.sfs.*;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Vladimir Kvashin
 */
public class TestSingleFileStorage extends BaseTest {
    
    private File storageFile;
    private SingleFileStorage sfs;
    
    private boolean verbose = false;
    private boolean dump = false;
    
    private int errCnt = 0;
    
    public void test(List<String> args) throws IOException {
	
	args = processOptions(args);
	
	storageFile = new File("/tmp/sfs.dat"); // NOI18N
	System.out.printf("Testing SingleFileStorage. Storage file: %s RWAccess: %d\n", storageFile.getAbsolutePath(), -1 /*Stats.fileRWAccess*/); // NOI18N
	sfs = new SingleFileStorage(storageFile);
	
	simpleTest1();
	simpleTest2();
	
	filesTest(args);
	
	if( dump ) {
	    sfs.dump(System.out);
	}
	else {
	    sfs.dumpSummary(System.out);
	}
	
	System.out.printf("Compacting...\n"); // NOI18N
	long t = System.currentTimeMillis();
	sfs.compact();
	t = System.currentTimeMillis() - t;
	System.out.printf("Compacting took %d seconds\n", t/1000); // NOI18N
	
	if( dump ) {
	    sfs.dump(System.out);
	}
	else {
	    sfs.dumpSummary(System.out);
	}
	
	System.out.printf("%8d objects tested\n", sfs.getObjectsCount()); // NOI18N
	System.out.printf("%8d errors\n", errCnt); // NOI18N
    }
    
    private List<String> processOptions(List<String> args) {
	List<String> newOptions = new ArrayList<String>();
	for( String arg : args ) {
	    if( arg.equals("-v") ) { // NOI18N
		verbose = true;
	    }
	    if( arg.equals("-d") ) { // NOI18N
		dump = true;
	    }
	    else {
		newOptions.add(arg);
	    }
	}
	return newOptions;
    }
    
    private void simpleTest1() throws IOException {
	TestObject orig = new TestObject("TestObject1", "1", "22", "333"); // NOI18N
	testWriteAndReadImmediately(orig);
	testRead(orig);
	testWriteAndReadImmediately(orig);
	testRead(orig);
    }
    
    private void simpleTest2() throws IOException {
	TestObject obj1 = new TestObject("TestObject2a", "1", "22", "333");
	testWriteAndReadImmediately(obj1); // NOI18N
	TestObject obj2 = new TestObject("TestObject2b", "aaa", "bb", "c");
	testWriteAndReadImmediately(obj2); // NOI18N
	sfs.dump(System.out);
	sfs.compact();
	sfs.dump(System.out);
	testRead(obj1);
	testRead(obj2);
    }
    
    private void filesTest(List<String> args) throws IOException {
	
	if( args.isEmpty() ) {
	    return;
	}
	
	System.out.printf("Creating test objects...\n"); // NOI18N
	Collection<TestObject> objects = new TestObjectCreator().createTestObjects(args);
	System.out.printf("\t%d objects created\n", objects.size()); // NOI18N
	
	long t;
	
	System.out.printf("\nPutting objects into storage\n"); // NOI18N
	t = System.currentTimeMillis();
	filesTestW(objects);
	System.out.printf("Done putting. Object count: %d  File size: %d   Time: %d seconds\n", // NOI18N
		sfs.getObjectsCount(), sfs.getFileSize(), (System.currentTimeMillis()-t)/1000);
	
	readCycle(objects);

	System.out.printf("\nPutting objects again into storage, twice each!\n"); // NOI18N
	t = System.currentTimeMillis();
	filesTestW2(objects);
	System.out.printf("Done putting. Object count: %d  File size: %d   Time: %d seconds\n", // NOI18N
		sfs.getObjectsCount(), sfs.getFileSize(), (System.currentTimeMillis()-t)/1000);
	
	readCycle(objects);
	
	System.out.printf("Compacting (size %d)...\n", sfs.getFileSize());
	sfs.compact();
	System.out.printf("\tCompacting (size %d) done\n", sfs.getFileSize());
	
	readCycle(objects);
	
//	System.out.printf("\nReading objects randomly\n");
//	t = System.currentTimeMillis();
//	filesTestR(objects);
//	System.out.printf("Reading took %d seconds\n", (System.currentTimeMillis()-t)/1000);
	
    }
    
    private void readCycle(Collection<TestObject> objects) throws IOException {
	for( int i = 1; i <= 3; i++ ) {
	    System.out.printf("\nReading objects once (take %d)\n", i); // NOI18N
	    long t = System.currentTimeMillis();
	    filesTestR(objects);
	    System.out.printf("Reading took %d seconds\n", (System.currentTimeMillis()-t)/1000); // NOI18N
	}
    }
    
    // unused
    private void filesTestWRimmediate(Collection<TestObject> objects) throws IOException {
	// writing each object and then reading it immediately
	for( TestObject obj : objects ) {
	    testWriteAndReadImmediately(obj);
	}
    }

    private void filesTestW(Collection<TestObject> objects) throws IOException {
	// reading all objects 
	for( TestObject obj : objects ) {
	    put(obj);
	}
    }
    
    private void filesTestW2(Collection<TestObject> objects) throws IOException {
	// reading all objects 
	for( TestObject obj : objects ) {
	    put(obj);
	    put(obj);
	}
    }
    
    private void filesTestR(Collection<TestObject> objects) throws IOException {
	// reading all objects 
	for( TestObject obj : objects ) {
	    testGet(obj);
	}
    }
    

    
    private void testGet(TestObject orig) throws IOException {
	TestObject read = get(orig.getKey());
	assertEqual(orig, read);
    }
    
    private void testWriteAndReadImmediately(TestObject orig) throws IOException {
	put(orig);
	TestObject read = get(orig.getKey());
	assertEqual(orig, read);
    }
    
    private void testRead(TestObject orig) throws IOException {
	TestObject read = get(orig.getKey());
	assertEqual(orig, read);
    }
    
    private void assertEqual(TestObject orig, TestObject read) {
	if( ! orig.equals(read) ) {
	    System.err.printf("ERROR:\n");
	    System.err.printf("Wrote: %s\n", orig.toString());
	    System.err.printf("Read:  %s\n", read.toString());
	    errCnt++;
	    if( stopOnError ) {
		System.exit(100);
	    }
	}
    }

    private void put(TestObject obj) throws IOException {
	if( verbose ) System.out.printf("Putting %s\n        %s\n", obj.key, obj.toString()); // NOI18N
	sfs.put(obj.key, obj);
    }
    
    private TestObject get(Key key) throws IOException {
	if( verbose ) System.out.printf("Getting %s\n", key); // NOI18N
	TestObject result = (TestObject)sfs.get(key);
	if( verbose ) System.out.printf("    got %s\n", result.toString()); // NOI18N
	return result;
    }
}

