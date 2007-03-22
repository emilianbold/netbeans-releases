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

import java.io.File;
import java.util.*;
import org.netbeans.modules.cnd.repository.sfs.ChunkInfo;
import org.netbeans.modules.cnd.repository.sfs.ChunkInfoFactory;
import org.netbeans.modules.cnd.repository.util.LongHashMap;

/**
 * Tests LongHashMap
 * @author Vladimir Kvashin
 */
public class TestLongHashMap extends BaseTest {
    
    private int errCnt = 0;
    private int cnt = 0;
    
    private Map lastReference;
    private LongHashMap lastMap;
    private Map lastChunkMap;
    
    private class Unit<K> {
	
	private Map<K, Long> reference;
	private LongHashMap<K> map;
	
	public Unit(Map<K, Long> reference) {
	    this.reference = reference;
	    map = new LongHashMap<K>();
	    lastReference = reference;
	    lastMap = map;
	    
	    lastChunkMap = new HashMap<K, ChunkInfo>();
	    for( Map.Entry<K, Long> e : reference.entrySet() ) {
		lastChunkMap.put(e.getKey(), ChunkInfoFactory.instance().createChunkInfo(e.getValue(), 0));
	    }
	}
	
	public void test() {
	    for( Map.Entry<K, Long> e : reference.entrySet() ) {
		
		K key = e.getKey();
		long value = e.getValue().longValue();
		
		testEmpty(key);
		
		map.put(key, value);
		
		assertTrue(map.containsKey(key), "Map should contain the key " + key + " at this moment");
		long v = map.get(key);
		assertEquals(v, value, key, "");
		
		map.remove(key);
		testEmpty(key);
		map.put(key, value);
		
		cnt++;
	    }
	}
	
	private void testEmpty(K key) {
	    assertFalse(map.containsKey(key), "Map shouldn't contain the key " + key + " at this moment");
	    long v = map.get(key);
	    assertEquals(v, LongHashMap.NO_VALUE, key, "Map doesn't contain the key");
	}
	
	private void assertFalse(boolean condition, String text) {
	    assertTrue(!condition, text);
	}
	
	private void assertTrue(boolean condition, String text) {
	    if( ! condition ) {
		errCnt++;
		System.err.printf("%s\n", text);
	    }
	}
	
	private void assertEquals(long actualValue, long referenceValue, K key, String preface) {
	    if( actualValue != referenceValue) {
		errCnt++;
		System.err.printf("%s key=%s value=%d; should be %d\n", preface, key, actualValue, referenceValue);
	    }
	}
	
    }
    
    public void test(List<String> args) {
	test_a();
	test_files(args);
	report();
    }
    
    private void report() {
	if( errCnt > 0 ) {
	    System.out.printf("FAILURE. Count: %d Errors: %d\n", cnt, errCnt);
	}
	else {
	    System.err.printf("SUCCESS. Count: %d \n", cnt);
	}
    }

    private void test_a() {
	Map<String, Long> reference = new HashMap<String, Long>();
	reference.put("One", 1L);
	reference.put("Two", 2L);
	new Unit(reference).test();
    }
    
    private void test_files(List<String> args) {
	
	if( args.isEmpty() ) {
	    return;
	}
	System.out.printf("Creating test objects...\n"); // NOI18N
	
	Map<String, Long> reference_s = new HashMap<String, Long>();
	Map<File, Long> reference_f = new HashMap<File, Long>();
	
	for( String path : args ) {
	    collectFiles(path, reference_s, reference_f);
	}
	
	new Unit(reference_s).test();
	new Unit(reference_f).test();
    }
    
    private void collectFiles(String path, Map<String, Long> reference_s, Map<File, Long> reference_f) {
	collectFiles(new File(path), reference_s, reference_f);
    }
    
    private void collectFiles(File file, Map<String, Long> reference_s, Map<File, Long> reference_f) {
	if( file.exists() ) {
	    reference_f.put(file, file.length());
	    reference_s.put(file.getAbsolutePath(), file.length());
	    if( file.isDirectory() ) {
		System.out.printf("Creating test objects from %s\n", file.getPath()); // NOI18N
		File[] files = file.listFiles();
		if( files != null ) {
		    for (int i = 0; i < files.length; i++) {
			collectFiles(files[i], reference_s, reference_f);
		    }
		}
	    }
	}   
    }
}

