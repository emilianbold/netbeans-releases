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

import java.io.File;
import java.util.*;
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
//	    lastChunkMap = new HashMap<K, ChunkInfo>();
//	    for( Map.Entry<K, Long> e : reference.entrySet() ) {
//		lastChunkMap.put(e.getKey(), ChunkInfoFactory.instance().createChunkInfo(e.getValue(), 0));
//	    }
	}
        
        private boolean passed;
	
	public boolean test() {
            passed = true;
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
            return passed;
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
                passed = false;
	    }
	}
	
	private void assertEquals(long actualValue, long referenceValue, K key, String preface) {
	    if( actualValue != referenceValue) {
		errCnt++;
		System.err.printf("%s key=%s value=%d; should be %d\n", preface, key, actualValue, referenceValue);
                passed = false;
	    }
	}
	
    }
    
    public boolean test(List<String> args) {
	boolean passed = test_a();
	passed &= test_files(args);
	report();
        return passed;
    }
    
    private void report() {
	if( errCnt > 0 ) {
	    System.out.printf("FAILURE. Count: %d Errors: %d\n", cnt, errCnt); // NOI18N
	}
	else {
	    System.err.printf("SUCCESS. Count: %d \n", cnt);
	}
    }

    private boolean test_a() {
	Map<String, Long> reference = new HashMap<String, Long>();
	reference.put("One", 1L); // NOI18N
	reference.put("Two", 2L); // NOI18N
	return new Unit<String>(reference).test();
    }
    
    private boolean test_files(List<String> args) {
	
	if( args.isEmpty() ) {
	    return true;
	}
	System.out.printf("Creating test objects...\n"); // NOI18N
	
	Map<String, Long> reference_s = new HashMap<String, Long>();
	Map<File, Long> reference_f = new HashMap<File, Long>();
	
	for( String path : args ) {
	    collectFiles(path, reference_s, reference_f);
	}
	
	return new Unit<String>(reference_s).test() && new Unit<File>(reference_f).test();
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

