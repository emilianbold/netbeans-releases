/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/** This is the base test for new and old enumerations. It contains
 * factory methods for various kinds of enumerations and set of tests
 * that use them. Factory methods are overriden in OldEnumerationsTest
 *
 * @author Jaroslav Tulach
 */
public class EnumerationsTest extends NbTestCase {
    
    /** Creates a new instance of EnumerationsTest */
    public EnumerationsTest(String testName) {
        super(testName);
    }
    
    //
    // Factory methods
    //
    
    protected Enumeration singleton(Object obj) {
        return Enumerations.singleton(obj);
    }
    protected Enumeration concat(Enumeration en1, Enumeration en2) {
        return Enumerations.concat(en1, en2);
    }
    protected Enumeration concat(Enumeration enumOfEnums) {
        return Enumerations.concat(enumOfEnums);
    }
    protected Enumeration removeDuplicates(Enumeration en) {
        return Enumerations.removeDuplicates(en);
    }
    protected Enumeration empty() {
        return Enumerations.empty();
    }
    protected Enumeration array(Object[] arr) {
        return Enumerations.array(arr);
    }
    protected Enumeration convert(Enumeration en, final Map map) {
        class P implements Enumerations.Processor {
            public Object process(Object obj, Collection nothing) {
                return map.get(obj);
            }
        }
        
        
        return Enumerations.convert(en, new P());
    }
    protected Enumeration removeNulls(Enumeration en) {
        return Enumerations.removeNulls(en);
    }
    protected Enumeration filter(Enumeration en, final Set filter) {
        class P implements Enumerations.Processor {
            public Object process(Object obj, Collection nothing) {
                return filter.contains(obj) ? obj : null;
            }
        }
        
        return Enumerations.filter(en, new P());
    }
    
    protected Enumeration filter(Enumeration en, final QueueProcess filter) {
        class P implements Enumerations.Processor {
            public Object process(Object obj, Collection nothing) {
                return filter.process(obj, nothing);
            }
        }
        
        return Enumerations.filter(en, new P());
    }
    
    /**
     * @param filter the set.contains (...) is called before each object is produced
     * @return Enumeration
     */
    protected Enumeration queue(Collection initContent, final QueueProcess process) {
        class C implements Enumerations.Processor {
            public Object process(Object object, Collection toAdd) {
                return process.process(object, toAdd);
            }
        }
        return Enumerations.queue(
                Collections.enumeration(initContent),
                new C()
                );
    }
    
    /** Processor interface.
     */
    public static interface QueueProcess {
        public Object process(Object object, Collection toAdd);
    }
    
    //
    // The tests
    //
    
    public void testEmptyIsEmpty() {
        Enumeration e = empty();
        assertFalse(e.hasMoreElements());
        try {
            e.nextElement();
            fail("No elements");
        } catch (NoSuchElementException ex) {
            // ok
        }
    }
    
    public void testSingleIsSingle() {
        Enumeration e = singleton(this);
        assertTrue(e.hasMoreElements());
        assertEquals("Returns me", this, e.nextElement());
        assertFalse("Now it is empty", e.hasMoreElements());
        try {
            e.nextElement();
            fail("No elements");
        } catch (NoSuchElementException ex) {
            // ok
        }
    }
    
    public void testConcatTwoAndArray() {
        Object[] one = { new Integer(1), new Integer(2), new Integer(3) };
        Object[] two = { "1", "2", "3" };
        
        ArrayList list = new ArrayList(Arrays.asList(one));
        list.addAll(Arrays.asList(two));
        
        assertEnums(
                concat(array(one), array(two)),
                Collections.enumeration(list)
                );
    }
    
    public void testConcatTwoAndArrayAndTakeOnlyStrings() {
        Object[] one = { new Integer(1), new Integer(2), new Integer(3) };
        Object[] two = { "1", "2", "3" };
        Object[] three = { new Long(1) };
        Object[] four = { "Kuk" };
        
        ArrayList list = new ArrayList(Arrays.asList(two));
        list.addAll(Arrays.asList(four));
        
        Enumeration[] alls = {
            array(one), array(two), array(three), array(four)
        };
        
        assertEnums(
                filter(concat(array(alls)), new OnlyStrings()),
                Collections.enumeration(list)
                );
    }
    
    public void testRemoveDuplicates() {
        Object[] one = { new Integer(1), new Integer(2), new Integer(3) };
        Object[] two = { "1", "2", "3" };
        Object[] three = { new Integer(1) };
        Object[] four = { "2", "3", "4" };
        
        Enumeration[] alls = {
            array(one), array(two), array(three), array(four)
        };
        
        assertEnums(
                removeDuplicates(concat(array(alls))),
                array(new Object[] { new Integer(1), new Integer(2), new Integer(3), "1", "2", "3", "4" })
                );
        
    }
    
    public void testRemoveDuplicatesAndGCWorks() {
        
        /*** Return { i1, "", "", "", i2 } */
        class WeakEnum implements Enumeration {
            public Object i1 = new Integer(1);
            public Object i2 = new Integer(1);
            
            private int state;
            
            public boolean hasMoreElements() {
                return state < 5;
            }
            
            public Object nextElement() {
                switch (state++) {
                    case 0: return i1;
                    case 1: case 2: case 3: return "";
                    default: return i2;
                }
            }
        }
        
        WeakEnum weak = new WeakEnum();
        Enumeration en = removeDuplicates(weak);
        
        assertTrue("Has some elements", en.hasMoreElements());
        assertEquals("And the first one is get", weak.i1, en.nextElement());
        
        try {
            WeakReference ref = new WeakReference(weak.i1);
            weak.i1 = null;
            assertGC("Try hard to GC the first integer", ref);
            // does not matter whether it GCs or not
        } catch (Throwable tw) {
            // not GCed, but does not matter
        }
        assertTrue("Next object will be string", en.hasMoreElements());
        assertEquals("is empty string", "", en.nextElement());
        
        assertFalse("The second integer is however equal to the original i1 and thus" +
                " the enum should not be there", en.hasMoreElements());
    }
    
    public void testQueueEnum() {
        class Pr implements QueueProcess {
            public Object process(Object o, Collection c) {
                Integer i = (Integer)o;
                int plus = i.intValue() + 1;
                if (plus < 10) {
                    c.add(new Integer(plus));
                }
                return i;
            }
        }
        Pr p = new Pr();
        
        Enumeration en = queue(
                Collections.nCopies(1, new Integer(0)), p
                );
        
        for (int i = 0; i < 10; i++) {
            assertTrue("has next", en.hasMoreElements());
            en.nextElement();
        }
        
        assertFalse("No next element", en.hasMoreElements());
    }
    
    public void testFilteringAlsoDoesConvertions() throws Exception {
        class Pr implements QueueProcess {
            public Object process(Object o, Collection ignore) {
                Integer i = (Integer)o;
                int plus = i.intValue() + 1;
                return new Integer(plus);
            }
        }
        Pr p = new Pr();
        
        Enumeration onetwo = array(new Object[] { new Integer(1), new Integer(2) });
        Enumeration twothree = array(new Object[] { new Integer(2), new Integer(3) });
        
        assertEnums(
                filter(onetwo, p), twothree
                );
    }
    
    
    private static void assertEnums(Enumeration e1, Enumeration e2) {
        int indx = 0;
        while (e1.hasMoreElements() && e2.hasMoreElements()) {
            Object i1 = e1.nextElement();
            Object i2 = e2.nextElement();
            assertEquals(indx++ + "th: ", i1, i2);
        }
        
        if (e1.hasMoreElements()) {
            fail("first one contains another element: " + e1.nextElement());
        }
        if (e2.hasMoreElements()) {
            fail("second one contains another element: " + e2.nextElement());
        }
        
        try {
            e1.nextElement();
            fail("First one should throw exception, but nothing happend");
        } catch (NoSuchElementException ex) {
            // ok
        }
        
        try {
            e2.nextElement();
            fail("Second one should throw exception, but nothing happend");
        } catch (NoSuchElementException ex) {
            // ok
        }
    }
    
    public void testConvertIntegersToStringRemoveNulls() {
        Object[] garbage = { new Integer(1), "kuk", "hle", new Integer(5) };
        
        assertEnums(
                removeNulls(convert(array(garbage), new MapIntegers())),
                array(new Object[] { "1", "5" })
                );
    }
    
    public void testQueueEnumerationCanReturnNulls() {
        Object[] nuls = { null, "NULL" };
        
        class P implements QueueProcess {
            public Object process(Object toRet, Collection toAdd) {
                if (toRet == null) return null;
                
                if ("NULL".equals(toRet)) {
                    toAdd.add(null);
                    return null;
                }
                
                return null;
            }
        }
        
        assertEnums(
                array(new Object[] { null, null, null }),
                queue(Arrays.asList(nuls), new P())
                );
    }
    
    /** Filters only strings.
     */
    private static final class OnlyStrings implements Set {
        public boolean add(Object o) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean addAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public void clear() {
            fail("Should not be every called");
        }
        
        public boolean contains(Object o) {
            return o instanceof String;
        }
        
        public boolean containsAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean isEmpty() {
            fail("Should not be every called");
            return false;
        }
        
        public Iterator iterator() {
            fail("Should not be every called");
            return null;
        }
        
        public boolean remove(Object o) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean removeAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean retainAll(Collection c) {
            fail("Should not be every called");
            return false;
        }
        
        public int size() {
            fail("Should not be every called");
            return 1;
        }
        
        public Object[] toArray() {
            fail("Should not be every called");
            return null;
        }
        
        public Object[] toArray(Object[] a) {
            fail("Should not be every called");
            return null;
        }
    }
    
    /** Filters only strings.
     */
    private static final class MapIntegers implements Map {
        public boolean containsKey(Object key) {
            fail("Should not be every called");
            return false;
        }
        
        public boolean containsValue(Object value) {
            fail("Should not be every called");
            return false;
        }
        
        public Set entrySet() {
            fail("Should not be every called");
            return null;
        }
        
        public Object get(Object key) {
            if (key instanceof Integer) {
                return key.toString();
            }
            return null;
        }
        
        public Set keySet() {
            fail("Should not be every called");
            return null;
        }
        
        public Object put(Object key, Object value) {
            fail("Should not be every called");
            return null;
        }
        
        public void putAll(Map t) {
            fail("Should not be every called");
        }
        
        public Collection values() {
            fail("Should not be every called");
            return null;
        }
        
        public void clear() {
            fail("Should not be every called");
        }
        
        public boolean isEmpty() {
            fail("Should not be every called");
            return false;
        }
        
        public Object remove(Object key) {
            fail("Should not be every called");
            return null;
        }
        
        public int size() {
            fail("Should not be every called");
            return 1;
        }
        
    }
}
