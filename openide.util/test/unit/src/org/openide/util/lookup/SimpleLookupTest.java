/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util.lookup;

import java.util.*;

import org.openide.util.Lookup;

import junit.framework.*;
import org.netbeans.junit.*;

/**
 * Tests for class SimpleLookup.
 * @author David Strupl
 */
public class SimpleLookupTest extends org.netbeans.junit.NbTestCase {
    
    public SimpleLookupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(SimpleLookupTest.class));
    }
    
    /**
     * Simple tests testing singleton lookup.
     */
    public void testSingleton() {
        //
        Object orig = new Object();
        Lookup p1 = Lookups.singleton(orig);
        Object obj = p1.lookup(Object.class);
        assertTrue(obj == orig);
        assertNull(p1.lookup(String.class)); 
        assertTrue(orig == p1.lookup(Object.class)); // 2nd time, still the same?
        //
        Lookup p2 = Lookups.singleton("test");
        assertNotNull(p2.lookup(Object.class));
        assertNotNull(p2.lookup(String.class));
        assertNotNull(p2.lookup(java.io.Serializable.class));
    }
    
    /**
     * Simple tests testing fixed lookup.
     */
    public void testFixed() {
        //
        Object[] orig = new Object[] { new Object(), new Object() };
        Lookup p1 = Lookups.fixed(orig);
        Object obj = p1.lookup(Object.class);
        assertTrue(obj == orig[0] || obj == orig[1]);
        assertNull(p1.lookup(String.class)); 
        //
        String[] s = new String[] { "test1", "test2" };
        Lookup p2 = Lookups.fixed(s);
        Object obj2 = p2.lookup(Object.class);
        assertNotNull(obj2);
        if (obj2 != s[0] && obj2 != s[1]) {
            fail("Returned objects are not the originals");
        }
        assertNotNull(p2.lookup(String.class));
        assertNotNull(p2.lookup(java.io.Serializable.class));
        Lookup.Template t = new Lookup.Template(String.class);
        Lookup.Result r = p2.lookup(t);
        Collection all = r.allInstances();
        assertTrue(all.size() == 2);
        for (Iterator i = all.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o != s[0] && o != s[1]) {
                fail("allIinstances contains wrong objects");
            }
        }
    }
    
    /**
     * Simple tests testing converting lookup.
     */
    public void testConverting() {
        //
        Object[] orig = new Object[] { TestConvertor.TEST1, TestConvertor.TEST2 };
        TestConvertor convertor = new TestConvertor();
        Lookup p1 = Lookups.fixed(orig, convertor);
        assertNull("Converting from String to Integer - it should not find String in result", p1.lookup(String.class));
        assertNotNull(p1.lookup(Integer.class));
        assertNotNull(p1.lookup(Integer.class));
        assertTrue("Convertor should be called only once.", convertor.getNumberOfConvertCalls() == 1); 
        Lookup.Template t = new Lookup.Template(Integer.class);
        Lookup.Result r = p1.lookup(t);
        Collection all = r.allInstances();
        assertTrue(all.size() == 2);
        for (Iterator i = all.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o != TestConvertor.t1 && o != TestConvertor.t2) {
                fail("allIinstances contains wrong objects");
            }
        }
    }
    
    private static class TestConvertor implements InstanceContent.Convertor {
        static final String TEST1 = "test1";
        static final Integer t1 = new Integer(1);
        static final String TEST2 = "test2";
        static final Integer t2 = new Integer(2);
        
        private int numberOfConvertCalls = 0;
        
        public Object convert(Object obj) {
            numberOfConvertCalls++;
            if (obj.equals(TEST1)) {
                return t1;
            }
            if (obj.equals(TEST2)) {
                return t2;
            }
            throw new IllegalArgumentException();
        }
        
        public String displayName(Object obj) {
            return obj.toString();
        }
        
        public String id(Object obj) {
            if (obj.equals(TEST1)) {
                return TEST1;
            }
            if (obj.equals(TEST2)) {
                return TEST2;
            }
            return null;
        }
        
        public Class type(Object obj) {
            return Integer.class;
        }
        
        int getNumberOfConvertCalls() { 
            return numberOfConvertCalls;
        }
    }
    
    public void testLookupItem() {
        SomeInst inst = new SomeInst();
        Lookup.Item item = Lookups.lookupItem(inst, "XYZ");
        
        assertTrue("Wrong instance", item.getInstance() == inst);
        assertTrue("Wrong instance class", item.getType() == inst.getClass());
        assertEquals("Wrong id", "XYZ", item.getId());

        item = Lookups.lookupItem(inst, null);
        assertNotNull("Id must never be null", item.getId());
    }

    public void testLookupItemEquals() {
        SomeInst instA = new SomeInst();
        SomeInst instB = new SomeInst();
        Lookup.Item itemA = Lookups.lookupItem(instA, null);
        Lookup.Item itemB = Lookups.lookupItem(instB, null);
        
        assertTrue("Lookup items shouldn't be equal", !itemA.equals(itemB) && !itemB.equals(itemA));

        itemA = Lookups.lookupItem(instA, null);
        itemB = Lookups.lookupItem(instA, null); // same instance

        assertTrue("Lookup items should be equal", itemA.equals(itemB) && itemB.equals(itemA));
        assertTrue("Lookup items hashcode should be same", itemA.hashCode() == itemB.hashCode());

        itemA = Lookups.lookupItem(new String("VOKURKA"), null);
        itemB = Lookups.lookupItem(new String("VOKURKA"), null);

        assertTrue("Lookup items shouldn't be equal (2)", !itemA.equals(itemB) && !itemB.equals(itemA));
    }
    
    public void testAllClassesIssue42399 () throws Exception {
        Object[] arr = { "Ahoj", new Object () };
        
        Lookup l = Lookups.fixed (arr);
        
        java.util.Set s = l.lookup (new Lookup.Template (Object.class)).allClasses ();
        
        assertEquals ("Two there", 2, s.size ());
        assertTrue ("Contains Object.class", s.contains (Object.class));
        assertTrue ("Contains string", s.contains (String.class));
        
    }

    public void testLookupItemEarlyInitializationProblem() {
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        LI item = new LI();
        ArrayList pairs1 = new ArrayList();
        ArrayList pairs2 = new ArrayList();
        
        assertEquals("Item's instance shouldn't be requested", 0, item.cnt);

        pairs1.add(new ItemPair(Lookups.lookupItem(new SomeInst(), null)));
        pairs1.add(new ItemPair(item));
        pairs1.add(new ItemPair(Lookups.lookupItem(new Object(), null)));

        pairs2.add(new ItemPair(item));
        pairs2.add(new ItemPair(Lookups.lookupItem(new Object(), null)));

        ic.setPairs(pairs1);
        ic.setPairs(pairs2);

        assertEquals("Item's instance shouldn't be requested when added to lookup", 0, item.cnt);
        
        LI item2 = (LI) al.lookup(LI.class);
        assertEquals("Item's instance should be requested", 1, item.cnt);
    }
    
    private static class SomeInst { }
    
    private static class LI extends Lookup.Item {

        public long cnt = 0;
        
        public String getDisplayName() {
            return getId();
        }

        public String getId() {
            return getClass() + "@" + hashCode();
        }

        public Object getInstance() {
            cnt++;
            return this;
        }

        public Class getType() {
            return getClass();
        }
    } // End of LI class

    private static class ItemPair extends AbstractLookup.Pair {
        
        private AbstractLookup.Item item;
        
        public ItemPair (Lookup.Item i) {
            this.item = i;
        }

        protected boolean creatorOf(Object obj) {
            return item.getInstance() == obj;
        }

        public String getDisplayName() {
            return item.getDisplayName ();
        }

        public String getId() {
            return item.getId ();
        }

        public Object getInstance() {
            return item.getInstance ();
        }

        public Class getType() {
            return item.getType ();
        }

        protected boolean instanceOf(Class c) {
            return c.isAssignableFrom (getType ());
        }

        public boolean equals (Object o) {
            if (o instanceof ItemPair) {
                ItemPair p = (ItemPair)o;
                return item.equals (p.item);
            }
            return false;
        }

        public int hashCode () {
            return item.hashCode ();
        }
    } // end of ItemPair
}
