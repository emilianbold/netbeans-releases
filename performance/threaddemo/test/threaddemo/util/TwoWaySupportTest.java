/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import junit.framework.TestCase;
import threaddemo.locking.RWLock;
import threaddemo.locking.Locks;
import threaddemo.locking.PrivilegedLock;
import threaddemo.util.TwoWaySupport;
import threaddemo.util.TwoWaySupport.DerivationResult;

/**
 * Test the two-way support.
 * @author Jesse Glick
 */
public class TwoWaySupportTest extends TestCase {

    private PrivilegedLock p;
    private SimpleTWS s;
    
    protected void setUp() throws Exception {
        p = new PrivilegedLock();
        RWLock l = Locks.readWrite(p);
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
     * Broken if underlying model is ""!
     */
    private static final class SimpleTWS extends TwoWaySupport<List<String>, String, List<String>> {
        
        private String string = "initial value";
        
        public SimpleTWS(RWLock l) {
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
        
        protected String composeUnderlyingDeltas(String underlyingDelta1, String underlyingDelta2) {
            return underlyingDelta2;
        }
        
        protected DerivationResult<List<String>,List<String>> doDerive(List<String> oldValue, String undval) throws Exception {
            if (undval == null) {
                undval = getString();
            }
            if (undval.length() == 0) throw new Exception("empty string");
            List<String> v = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(undval);
            while (tok.hasMoreTokens()) {
                v.add(tok.nextToken());
            }
            return new DerivationResult<List<String>,List<String>>(v, oldValue != null ? v : null);
        }
        
        protected List<String> doRecreate(List<String> oldValue, List<String> l) throws Exception {
            StringBuffer b = new StringBuffer();
            Iterator<String> i = l.iterator();
            if (i.hasNext()) {
                b.append(i.next());
            }
            while (i.hasNext()) {
                b.append(' ');
                b.append(i.next());
            }
            string = b.toString();
            return l;
        }
        
    }
    
}
