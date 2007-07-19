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

package org.netbeans.modules.cnd.repository.impl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 * Tests Repository.tryGet()
 * @author Vladimir Kvashin
 */
public class TryGetTest extends BaseTestCase {

    private abstract class BaseKey implements Key {
	
	private String key;
	
	public BaseKey(String key) {
	    this.key = key;
	}
	
	public int getSecondaryAt(int level) {
	    return 0;
	}

	public String getAt(int level) {
	    return key;
	}

	public String getUnit() {
	    return "Repository_Test_Unit";
	}

	public int getSecondaryDepth() {
	    return 0;
	}

	public PersistentFactory getPersistentFactory() {
	    return factory;
	}

	public int getDepth() {
	    return 1;
	}

    }
    
    private class SmallKey extends BaseKey {
	public SmallKey(String key) {
	    super(key);
	}
	public Key.Behavior getBehavior() {
	    return Key.Behavior.Default;
	}		
    }
    
    private class LargeKey extends BaseKey {
	public LargeKey(String key) {
	    super(key);
	}
	public Key.Behavior getBehavior() {
	    return Key.Behavior.LargeAndMutable;
	}	
    }
    
    private static class Value implements Persistent {
	private String value;
	
	public Value(String value) {
	    this.value = value;
	}
	
	public String toString() {
	    return value + " @" + hashCode();
	}
	
	public boolean equals(Object obj) {
	    if( obj instanceof  Value ) {
		return value.equals(((Value) obj).value);
	    }
	    return false;
	}
	
	public int hashCode() {
	    return value.hashCode();
	}
	
    }
        
    private class Factory implements PersistentFactory {
	
	public boolean canWrite(Persistent obj) {
	    return true;
	}
	
	public void write(DataOutput out, Persistent obj) throws IOException {
	    assert obj instanceof Value;
	    out.writeUTF(((Value) obj).value);
	}
	
	public Persistent read(DataInput in) throws IOException {
	    readFlag = true;
	    String value = in.readUTF();
	    return new Value(value);
	}
    }
    
    private PersistentFactory factory;
    private Repository repository;
    private boolean readFlag;
	    
    public TryGetTest(java.lang.String testName) {
	super(testName);
    }
    
    public static void main(java.lang.String[] args) {
	junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
	TestSuite suite = new NbTestSuite(TryGetTest.class);
	return suite;
    }
    
    @Override 
    protected void setUp() throws Exception {
	repository = RepositoryAccessor.getRepository();
	factory = new Factory();
	readFlag = false;
        super.setUp();
    }

    @Override 
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testTryGet() {
	_test(new SmallKey("small_1"), new Value("small_obj_1"));
	_test(new LargeKey("large_1"), new Value("large_obj_1"));
	
    }
   
    private void _test(Key key, Value value) {
	
	repository.put(key, value);
	
	Persistent v2 = repository.get(key);
	assertNotNull(v2);
	assertEquals(value, v2);
	
	readFlag = false;
	
	v2 = _tryGet(key);
	assertNotNull(v2);
	assertEquals(value, v2);

	repository.debugClear();
	
	v2 = _tryGet(key);
	assertNull(v2);
    }
    
    private void sleep(long millis) {
	try {
	    Thread.sleep(millis);
	}
	catch( InterruptedException ex ) {
	}
    }
    
    private Persistent _tryGet(Key key) {
	readFlag = false;
	Persistent p = repository.tryGet(key);
	assertFalse("tryGet shouldn't cause reading object from disk", readFlag);
	return p;
    }
}
