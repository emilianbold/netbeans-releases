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

/**
 *
 * @author Vladimir Kvashin
 */
public class TestSingleFileStorage {
    
    private File storageFile;
    private SingleFileStorage sfs;
    private static final boolean VERBOSE = true;
    
    public void test(List<String> args) throws IOException {
	storageFile = new File("/tmp/sfs.dat");
	System.out.printf("Testing SingleFileStorage. Storage file: %s\n", storageFile.getAbsolutePath());
	sfs = new SingleFileStorage(storageFile);
	simpleTest1();
    }
    
    private void simpleTest1() throws IOException {
	TestObject orig = new TestObject("TestObject1", "1", "22", "333");
	testWriteAndReadImmediately(orig);
    }
    
    private void testWriteAndReadImmediately(TestObject orig) throws IOException {
	put(orig);
	TestObject read = get(orig.key);
	if( ! orig.equals(read) ) {
	    System.err.printf("ERROR:\n");
	    System.err.printf("Wrote: %s\n", orig.toString());
	    System.err.printf("Read:  %s\n", read.toString());
	}
    }

    private void put(TestObject obj) throws IOException {
	if( VERBOSE ) System.out.printf("Putting %s\n        %s\n", obj.key, obj.toString());
	sfs.put(obj.key, obj);
    }
    
    private TestObject get(String key) throws IOException {
	if( VERBOSE ) System.out.printf("Getting %s\n", key);
	TestObject result = new TestObject("");
	sfs.get(key, result);
	if( VERBOSE ) System.out.printf("    got %s\n", result.toString());
	return result;
    }
}

