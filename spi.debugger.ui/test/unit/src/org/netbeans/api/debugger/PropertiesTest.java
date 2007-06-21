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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Test of the Properties class
 * 
 * @author Martin Entlicher
 */
public class PropertiesTest extends TestCase {
    
    public PropertiesTest(String testName) {
        super(testName);
    }
    
    /** Tests just the basic get/set methods. */
    public void testGetSet() throws Exception {
        String prop = "get/set";
        Properties p = Properties.getDefault();
        for (int i = 0; i < 10; i++) {
            int j = 0;
            String app = Integer.toHexString(j);
            testGetSet(p, prop + i + app, true);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (byte) i);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (short) i);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (int) 100*i - 2000);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (long) 1000000000000000L*i - 12345678987654321L);
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (float) (1234.1234*i - 5678));
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, (double) (1234.1234e200*i*Math.sin(i)));
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, Integer.toBinaryString(i*1234));
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i, new double[] { 0.1, 1.2, 2.3, 3.4, 4.5, 5.6, 6.7, 7.8, 8.9, 9.1 });
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" });
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, new String[][] { { "0", "1" }, { "2", "3" }, { "4", "5", "6" }, { "7", "8", "9" } });
            //app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, new Rectangle[] { new Rectangle(), new Rectangle(10, 20), new Rectangle(234, -432) });
            //app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, new ArrayList(Arrays.asList(new Integer[] { new Integer(i) })));
            testGetSet(p, prop + i + app, new ArrayList(Arrays.asList(new String[] { Integer.toString(i) })));
            //testGetSet(p, prop + i + app, Collections.singleton(new Integer(i)));
            app = Integer.toHexString(++j);
            testGetSet(p, prop + i + app, new ArrayList(Arrays.asList(new Object[] { Integer.toHexString(i), "String "+i })));
            //testGetSet(p, prop + i + app, Arrays.asList(new Object[] { Integer.toHexString(i), "String "+i }));
            //testGetSet(p, prop + i + app, Arrays.asList(new Object[] { new Integer(i), "String "+i }));
            app = Integer.toHexString(++j);
            //testGetSet(p, prop + i + app, Collections.singletonMap(new Integer(i), "i = "+i));
            testGetSet(p, prop + i + app, new HashMap(Collections.singletonMap(Integer.toString(i), "i = "+i)));
        }
    }
    
    public void testReader() throws Exception {
        Properties p = Properties.getDefault();
        ((Properties.PropertiesImpl) p).addReader(new TestReader());
        assertNull(p.getObject("rect 1", null));
        assertNull(p.getObject("rect 2", null));
        assertNull(p.getObject("test 1", null));
        assertNull(p.getObject("test 2", null));
        assertNull(p.getObject("test 3", null));
        Rectangle r1 = new Rectangle(9876, 1234);
        Rectangle r2 = new Rectangle(987654321, 123456789);
        p.setObject("rect 1", r1);
        p.setObject("rect 2", r2);
        TestObject t1 = new TestObject(12345678);
        TestObject t2 = new TestObject(999999999965490L);
        TestObject t3 = new TestObject(999999999999999999L);
        p.setObject("test 1", t1);
        p.setObject("test 2", t2);
        p.setObject("test 3", t3);
        
        assertEquals(r1, p.getObject("rect 1", null));
        assertEquals(r2, p.getObject("rect 2", null));
        assertEquals(t1, p.getObject("test 1", null));
        assertEquals(t2, p.getObject("test 2", null));
        assertEquals(t3, p.getObject("test 3", null));
    }
    
    /** Stress test of multi-threaded get/set */
    public void testStressGetSet() throws Exception {
        Properties p = Properties.getDefault();
        ((Properties.PropertiesImpl) p).addReader(new TestReader());
        int n = 5;
        ConcurrentGetSet[] cgs = new ConcurrentGetSet[n];
        Thread[] t = new Thread[n];
        for (int i = 0; i < n; i++) {
            cgs[i] = new ConcurrentGetSet(p, "CGS "+i);
            t[i] = new Thread(cgs[i]);
            t[i].start();
        }
        for (int i = 0; i < n; i++) {
            t[i].join();
            if (cgs[i].getException() != null) {
                throw cgs[i].getException();
            }
        }
    }
    
    private static class ConcurrentGetSet implements Runnable {
        
        private Properties p;
        private String prop;
        private TestObject[] t;
        private Exception ex;
        
        public ConcurrentGetSet(Properties p, String prop) {
            this.p = p;
            this.prop = prop;
            t = new TestObject[3];
            t[0] = new TestObject(12345678);
            t[1] = new TestObject(999999999965490L);
            t[2] = new TestObject(999999999999999999L);
        }
    
        public void run() {
            cycle: for (int i = 0; i < 10000; i++) {
                for (int k = 0; k < t.length; k++) {
                    p.setObject(prop+k, t[k]);
                }
                try {
                    if ((i % 1000) == 0) {
                        Thread.currentThread().sleep(1 + (prop.hashCode() % 10));
                    }
                } catch (InterruptedException iex) {}
                for (int k = 0; k < t.length; k++) {
                    try {
                        assertEquals(t[k], p.getObject(prop+k, null));
                    } catch (Exception ex) {
                        this.ex = ex;
                        break cycle;
                    }
                }
            }
        }
        
        public Exception getException() {
            return ex;
        }
    }
    
    private void testGetSet(Properties p, String name, boolean obj) {
        // suppose that the property is not defined
        assertEquals(true, p.getBoolean(name, true));
        assertEquals(false, p.getBoolean(name, false));
        // check set/get:
        p.setBoolean(name, obj);
        assertEquals(obj, p.getBoolean(name, !obj));
    }
    
    private void testGetSet(Properties p, String name, byte obj) {
        // suppose that the property is not defined
        assertEquals((byte) 10, p.getByte(name, (byte) 10));
        assertEquals((byte) 20, p.getByte(name, (byte) 20));
        // check set/get:
        p.setByte(name, obj);
        assertEquals(obj, p.getByte(name, (byte) 0));
    }
    
    private void testGetSet(Properties p, String name, short obj) {
        // suppose that the property is not defined
        assertEquals((short) 10, p.getShort(name, (short) 10));
        assertEquals((short) 20, p.getShort(name, (short) 20));
        // check set/get:
        p.setShort(name, obj);
        assertEquals(obj, p.getShort(name, (short) 0));
    }
    
    private void testGetSet(Properties p, String name, int obj) {
        // suppose that the property is not defined
        assertEquals((int) 10, p.getInt(name, (int) 10));
        assertEquals((int) 20, p.getInt(name, (int) 20));
        // check set/get:
        p.setInt(name, obj);
        assertEquals(obj, p.getInt(name, (int) 0));
    }
    
    private void testGetSet(Properties p, String name, long obj) {
        // suppose that the property is not defined
        assertEquals((long) 10, p.getLong(name, (long) 10));
        assertEquals((long) 20, p.getLong(name, (long) 20));
        // check set/get:
        p.setLong(name, obj);
        assertEquals(obj, p.getLong(name, (long) 0));
    }
    
    private void testGetSet(Properties p, String name, float obj) {
        // suppose that the property is not defined
        assertEquals((float) 10, p.getFloat(name, (float) 10), 0);
        assertEquals((float) 20, p.getFloat(name, (float) 20), 0);
        // check set/get:
        p.setFloat(name, obj);
        assertEquals(obj, p.getFloat(name, (float) 0), 0);
    }
    
    private void testGetSet(Properties p, String name, double obj) {
        // suppose that the property is not defined
        assertEquals((double) 10, p.getDouble(name, (double) 10), 0);
        assertEquals((double) 20, p.getDouble(name, (double) 20), 0);
        // check set/get:
        p.setDouble(name, obj);
        assertEquals(obj, p.getDouble(name, (double) 0), 0);
    }
    
    private void testGetSet(Properties p, String name, String obj) {
        // suppose that the property is not defined
        assertNull(p.getString(name, null));
        assertEquals("10", p.getString(name, "10"));
        assertEquals("20", p.getString(name, "20"));
        // check set/get:
        p.setString(name, obj);
        assertEquals(obj, p.getString(name, ""));
    }
    
    private void testGetSet(Properties p, String name, Object[] obj) {
        // suppose that the property is not defined
        assertNull(p.getArray(name, null));
        assertTrue(Arrays.deepEquals(new String[] { "10" }, p.getArray(name, new String[] { "10" })));
        assertTrue(Arrays.deepEquals(new String[] { "20" }, p.getArray(name, new String[] { "20" })));
        // check set/get:
        p.setArray(name, obj);
        //assertEquals(obj, p.getArray(name, new Object[]{}));
        Object[] ret = p.getArray(name, new Object[]{});
        assertTrue("Expecting: "+Arrays.asList(obj)+"\nbut got: "+Arrays.asList(ret), Arrays.deepEquals(obj, ret));
    }
    
    private void testGetSet(Properties p, String name, Collection obj) {
        // suppose that the property is not defined
        assertNull(p.getCollection(name, null));
        assertEquals(Collections.singleton("10"), p.getCollection(name, Collections.singleton("10")));
        assertEquals(Collections.singletonList("20"), p.getCollection(name, Collections.singletonList("20")));
        // check set/get:
        p.setCollection(name, obj);
        assertEquals(obj, p.getCollection(name, Collections.emptySet()));
    }
    
    private void testGetSet(Properties p, String name, Map obj) {
        // suppose that the property is not defined
        assertNull(p.getMap(name, null));
        assertEquals(Collections.singletonMap("10", "20"), p.getMap(name, Collections.singletonMap("10", "20")));
        // check set/get:
        p.setMap(name, obj);
        assertEquals(obj, p.getMap(name, Collections.emptyMap()));
    }
    
    private void testGetSet(Properties p, String name, Object obj) {
        // suppose that the property is not defined
        assertNull(p.getObject(name, null));
        assertEquals("10", p.getObject(name, "10"));
        assertEquals("20", p.getObject(name, "20"));
        // check set/get:
        p.setObject(name, obj);
        assertEquals(obj, p.getObject(name, ""));
    }
    
    private static class TestReader implements Properties.Reader {
        
    
        public String[] getSupportedClassNames() {
            return new String[] { "java.awt.Rectangle", "org.netbeans.api.debugger.PropertiesTest$TestObject" };
        }

        public Object read(String className, Properties properties) {
            if (className.equals("java.awt.Rectangle")) {
                int w = properties.getInt("Rectangle.width", 0);
                int h = properties.getInt("Rectangle.height", 0);
                return new Rectangle(w, h);
            }
            if (className.equals("org.netbeans.api.debugger.PropertiesTest$TestObject")) {
                return new TestObject(properties);
            }
            throw new IllegalArgumentException(className);
        }

        public void write(Object object, Properties properties) {
            if (object instanceof Rectangle) {
                properties.setInt("Rectangle.width", ((Rectangle) object).width);
                properties.setInt("Rectangle.height", ((Rectangle) object).height);
                return ;
            }
            if (object instanceof TestObject) {
                ((TestObject) object).write(properties);
                return ;
            }
            throw new IllegalArgumentException(object.toString());
        }
    }
    
    private static class TestObject {
        
        private boolean boo;
        private char c;
        private int l;
        private String[] strings;
        private Rectangle r;
        private String binaryState;
        
        public TestObject(long state) {
            boo = (state & 1) == 1;
            c = (char) (state & 255);
            l = (int) ((state << 8) & 255);
            strings = new String[l];
            for (int i = 0; i < l; i++) {
                int d = i % 64;
                strings[i] = "arr[i] = "+((state << d) & 1);
            }
            r = new Rectangle((int) (state & 65535), (int) ((state << 16) & 65535));
            binaryState = Long.toBinaryString(state);
        }
        
        public TestObject(Properties p) {
            boo = p.getBoolean("boo", false);
            c = p.getChar("char", (char) 0);
            l = p.getInt("length", 0);
            for (int i = 0; i < l; i++) {
                strings[i] = p.getString("string "+i, null);
            }
            r = (Rectangle) p.getObject("rectangle", null);
            binaryState = p.getString("binaryState", null);
        }
        
        public void write(Properties p) {
            p.setBoolean("boo", boo);
            p.setChar("char", c);
            p.setInt("length", l);
            for (int i = 0; i < l; i++) {
                p.setString("string "+i, strings[i]);
            }
            p.setObject("rectangle", r);
            p.setString("binaryState", binaryState);
        }
        
        public boolean equals(Object obj) {
            if (!(obj instanceof TestObject)) {
                return false;
            }
            TestObject t = (TestObject) obj;
            if (boo != t.boo) return false;
            if (c != t.c) return false;
            if (l != t.l) return false;
            for (int i = 0; i < l; i++) {
                if (!strings[i].equals(t.strings[i])) return false;
            }
            if (!r.equals(t.r)) return false;
            if (!binaryState.equals(t.binaryState)) return false;
            return true;
        }
        
        public int hashCode() {
            return 1234+r.width+r.height;
        }
        
        public String toString() {
            return binaryState;
        }
    }
    
}
