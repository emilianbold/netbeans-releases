/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import junit.framework.TestCase;
import threaddemo.locking.Lock;
import threaddemo.locking.Locks;
import threaddemo.locking.PrivilegedLock;

/**
 * Test the two-way support.
 * @author Jesse Glick
 */
public class TwoWaySupportTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new junit.framework.TestSuite(TwoWaySupportTest.class));
    }
    
    private PrivilegedLock p;
    private SimpleTWS s;
    
    protected void setUp() throws Exception {
        p = new PrivilegedLock();
        Lock l = Locks.readWrite("test", p, 0);
        s = new SimpleTWS(l);
        p.enterWrite();
    }
    protected void tearDown() throws Exception {
        p.exitWrite();
    }
    
    public void testBasicDerivation() throws Exception {
        assertNull(s.getValueNonBlocking());
        assertNull(s.getStaleValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getValueBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getStaleValueNonBlocking());
        s.setString("new value");
        assertNull(s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"initial", "value"}), s.getStaleValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getValueBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getStaleValueNonBlocking());
        s.setString("");
        assertNull(s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getStaleValueNonBlocking());
        try {
            Object v = s.getValueBlocking();
            fail("Should not be computed: " + v.toString());
        } catch (InvocationTargetException e) {
            assertEquals("empty string", e.getTargetException().getMessage());
        }
        assertNull(s.getValueNonBlocking());
        assertEquals(Arrays.asList(new String[] {"new", "value"}), s.getStaleValueNonBlocking());
    }
    
    // XXX to test:
    // - mutation
    // - initiation
    // - asynchronous access
    // - delayed computation
    // - firing of changes
    // - forgetting
    
    /**
     * Underlying model: text string (String)
     * Derived model: space-tokenized string (List<String>)
     * Underlying model deltas: same as underlying model (String)
     * Derived model deltas: same as derived model (List<String>)
     * Broken if underlying model is ""!
     */
    private static final class SimpleTWS extends TwoWaySupport {
        
        private String string = "initial value";
        
        public SimpleTWS(Lock l) {
            super(l);
        }
        
        public String getString() {
            return string;
        }
        
        public void setString(String s) {
            this.string = s;
            invalidate(s);
        }
        
        // Impl TWS:
        
        protected Object composeUnderlyingDeltas(Object underlyingDelta1, Object underlyingDelta2) {
            return underlyingDelta2;
        }
        
        protected DerivationResult doDerive(Object oldValue, Object underlyingDelta) throws Exception {
            String undval = (String)underlyingDelta;
            if (undval == null) {
                undval = getString();
            }
            if (undval.length() == 0) throw new Exception("empty string");
            List v = new ArrayList();
            StringTokenizer tok = new StringTokenizer(undval);
            while (tok.hasMoreTokens()) {
                v.add(tok.nextToken());
            }
            return new DerivationResult(v, oldValue != null ? v : null);
        }
        
        protected Object doRecreate(Object oldValue, Object derivedDelta) throws Exception {
            List l = (List)derivedDelta;
            StringBuffer b = new StringBuffer();
            Iterator i = l.iterator();
            if (i.hasNext()) {
                b.append((String)i.next());
            }
            while (i.hasNext()) {
                b.append(' ');
                b.append((String)i.next());
            }
            return b.toString();
        }
        
    }
    
}
