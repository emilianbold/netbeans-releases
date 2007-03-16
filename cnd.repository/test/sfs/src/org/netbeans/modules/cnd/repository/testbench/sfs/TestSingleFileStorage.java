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
import java.nio.*;
import org.netbeans.modules.cnd.repository.sfs.*;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 *
 * @author Vladimir Kvashin
 */
public class TestSingleFileStorage {
    
    private File storageFile;
    private SingleFileStorage sfs;
    
    private boolean verbose = false;
    private boolean dump = false;
    
    private int errCnt = 0;
    
    public void test(List<String> args) throws IOException {
	
	args = processOptions(args);
	
	storageFile = new File("/tmp/sfs.dat"); // NOI18N
	System.out.printf("Testing SingleFileStorage. Storage file: %s RWAccess: %d\n", storageFile.getAbsolutePath(), Stats.fileRWAccess); // NOI18N
	sfs = new SingleFileStorage(storageFile);
	sfs.collectStatistics(Stats.fileStatisticsLevel);
	
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
	testWriteAndReadImmediately(orig);
    }
    
    private void simpleTest2() throws IOException {
	String key = "TestObject1"; // NOI18N
	testWriteAndReadImmediately(new TestObject(key, "1", "22", "333")); // NOI18N
	testWriteAndReadImmediately(new TestObject(key, "aaa", "bb", "c")); // NOI18N
    }
    
    private void filesTest(List<String> args) throws IOException {
	
	if( args.isEmpty() ) {
	    return;
	}
	
	System.out.printf("Creating test objects...\n"); // NOI18N
	Collection<TestObject> objects = createTestObjects(args);
	System.out.printf("\t%d objects created\n", objects.size()); // NOI18N
	
	long t;
	
	System.out.printf("\nPutting objects into storage\n"); // NOI18N
	t = System.currentTimeMillis();
	filesTestW(objects);
	System.out.printf("Done putting. Object count: %d  File size: %d   Time: %d seconds\n", // NOI18N
		sfs.getObjectsCount(), sfs.getFileSize(), (System.currentTimeMillis()-t)/1000);
	
	for( int i = 1; i <= 3; i++ ) {
	    System.out.printf("\nReading objects once (take %d)\n", i); // NOI18N
	    t = System.currentTimeMillis();
	    filesTestR(objects);
	    System.out.printf("Reading took %d seconds\n", (System.currentTimeMillis()-t)/1000); // NOI18N
	}

	System.out.printf("\nPutting objects again into storage, twice each!\n"); // NOI18N
	t = System.currentTimeMillis();
	filesTestW2(objects);
	System.out.printf("Done putting. Object count: %d  File size: %d   Time: %d seconds\n", // NOI18N
		sfs.getObjectsCount(), sfs.getFileSize(), (System.currentTimeMillis()-t)/1000);
	
	for( int i = 1; i <= 3; i++ ) {
	    System.out.printf("\nReading objects once (take %d)\n", i); // NOI18N
	    t = System.currentTimeMillis();
	    filesTestR(objects);
	    System.out.printf("Reading took %d seconds\n", (System.currentTimeMillis()-t)/1000); // NOI18N
	}
	
	
//	System.out.printf("\nReading objects randomly\n");
//	t = System.currentTimeMillis();
//	filesTestR(objects);
//	System.out.printf("Reading took %d seconds\n", (System.currentTimeMillis()-t)/1000);
	
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
    
    private Collection<TestObject> createTestObjects(List<String> args) {
	Collection<TestObject> objects = new ArrayList<TestObject>();
	for( String path : args ) {
	    createTestObjects(new File(path), objects);
	}
	return objects;
    }
    
    private void createTestObjects(File file, Collection<TestObject> objects) {
	if( file.exists() ) {
	    TestObject  obj = new TestObject(file.getAbsolutePath());
	    obj.lData = file.length();
	    objects.add(obj);
	    if( file.isDirectory() ) {
		obj.sData = file.list();
		 File[] children = file.listFiles();
		 if( children != null ) {
		     for (int i = 0; i < children.length; i++) {
			 createTestObjects(children[i], objects);
		     }
		 }
	    }
	}
    }
    
    private void testGet(TestObject orig) throws IOException {
	TestObject read = get(orig.key);
	assertEqual(orig, read);
    }
    
    private void testWriteAndReadImmediately(TestObject orig) throws IOException {
	put(orig);
	TestObject read = get(orig.key);
	assertEqual(orig, read);
    }
    
    private void assertEqual(TestObject orig, TestObject read) {
	if( ! orig.equals(read) ) {
	    System.err.printf("ERROR:\n");
	    System.err.printf("Wrote: %s\n", orig.toString());
	    System.err.printf("Read:  %s\n", read.toString());
	    errCnt++;
	}
    }

    private void put(TestObject obj) throws IOException {
	if( verbose ) System.out.printf("Putting %s\n        %s\n", obj.key, obj.toString()); // NOI18N
	sfs.put(obj.key, obj);
    }
    
    private TestObject get(String key) throws IOException {
	if( verbose ) System.out.printf("Getting %s\n", key); // NOI18N
	TestObject result = new TestObject("");
	sfs.get(key, result);
	if( verbose ) System.out.printf("    got %s\n", result.toString()); // NOI18N
	return result;
    }
}

