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

package org.openide.util;

import junit.framework.*;
import junit.textui.TestRunner;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;
import java.beans.*;
import org.netbeans.junit.*;

/** Test SharedClassObject singletons: esp. initialization semantics.
 * @author Jesse Glick
 */
public class SharedClassObjectTest extends NbTestCase {
    
    public SharedClassObjectTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(SharedClassObjectTest.class));
    }
    
    /*
    protected void setUp() throws Exception {
    }
    protected void tearDown() throws Exception {
    }
     */
    
    public void testSimpleSCO() throws Exception {
        Class c = makeClazz("SimpleSCO");
        assertTrue(c != SimpleSCO.class);
        assertNull("No instance created yet", SharedClassObject.findObject(c, false));
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertNotNull(o);
        assertEquals("org.openide.util.SharedClassObjectTest$SimpleSCO", o.getClass().getName());
        assertEquals(c, o.getClass());
        assertEquals("has not been initialized", 0, o.getClass().getField("initcount").getInt(o));
        assertNull(o.getProperty("foo"));
        assertEquals("has been initialized", 1, o.getClass().getField("initcount").getInt(o));
        assertNull(o.getProperty("bar"));
        assertEquals("has been initialized just once", 1, o.getClass().getField("initcount").getInt(null));
        Class c2 = makeClazz("SimpleSCO");
        assertTrue("Call to makeClazz created a fresh class", c != c2);
        SharedClassObject o2 = SharedClassObject.findObject(c2, true);
        o2.getProperty("baz");
        assertEquals(1, o2.getClass().getField("initcount").getInt(null));
    }
    
    public void testClearSharedData() throws Exception {
        Class c = makeClazz("DontClearSharedDataSCO");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        o.putProperty("inited", Boolean.TRUE);
        assertEquals("DCSD has been initialized", Boolean.TRUE, o.getProperty("inited"));
        o = null;
        System.gc();
        System.runFinalization();
        // XXX should use assertGC here, it is more reliable and helpful...
        assertNull("findObject(Class,false) gives nothing after running GC + finalization #1", SharedClassObject.findObject(c));
        o = SharedClassObject.findObject(c, true);
        assertEquals("has still been initialized", Boolean.TRUE, o.getProperty("inited"));
        c = makeClazz("ClearSharedDataSCO");
        o = SharedClassObject.findObject(c, true);
        o.putProperty("inited", Boolean.TRUE);
        assertEquals("CSD has been initialized", Boolean.TRUE, o.getProperty("inited"));
        o = null;
        System.gc();
        System.runFinalization();
        assertNull("findObject(Class,false) gives nothing after running GC + finalization #2", SharedClassObject.findObject(c));
        o = SharedClassObject.findObject(c, true);
        assertEquals("is no longer initialized", null, o.getProperty("inited"));
        o.putProperty("inited", Boolean.TRUE);
        assertEquals("has now been initialized again", Boolean.TRUE, o.getProperty("inited"));
    }
    
    public void testIllegalState() throws Exception {
        Class c = makeClazz("InitErrorSCO");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertNotNull(o);
        try {
            o.getProperty("foo");
            fail("should not be able to do anything with it");
        } catch (IllegalStateException ise) {
            // Good.
        }
        try {
            o.getProperty("bar");
            fail("should still not be able to do anything with it");
        } catch (IllegalStateException ise) {
            // Good.
        }
    }
    
    public void testPropertyChanges() throws Exception {
        Class c = makeClazz("PropFirerSCO");
        Method putprop = c.getMethod("putprop", new Class[] {Object.class, Boolean.TYPE});
        Method getprop = c.getMethod("getprop", new Class[] {});
        Field count = c.getField("addCount");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertNull(getprop.invoke(o, null));
        assertEquals(0, count.getInt(o));
        class Listener implements PropertyChangeListener {
            public int count = 0;
            public void propertyChange(PropertyChangeEvent ev) {
                if ("key".equals(ev.getPropertyName())) {
                    count++;
                }
            }
        }
        Listener l = new Listener();
        o.addPropertyChangeListener(l);
        assertEquals(1, count.getInt(o));
        Listener l2 = new Listener();
        o.addPropertyChangeListener(l2);
        assertEquals(1, count.getInt(o));
        o.removePropertyChangeListener(l2);
        assertEquals(1, count.getInt(o));
        putprop.invoke(o, new Object[] {"something", Boolean.FALSE});
        assertEquals(0, l.count);
        assertEquals("something", getprop.invoke(o, null));
        putprop.invoke(o, new Object[] {"somethingelse", Boolean.TRUE});
        assertEquals(1, l.count);
        assertEquals("somethingelse", getprop.invoke(o, null));
        // Check that setting the same val does not fire an additional change (cf. #37769):
        putprop.invoke(o, new Object[] {"somethingelse", Boolean.TRUE});
        assertEquals(1, l.count);
        assertEquals("somethingelse", getprop.invoke(o, null));
        // Check equals() as well as ==:
        putprop.invoke(o, new Object[] {new String("somethingelse"), Boolean.TRUE});
        assertEquals(1, l.count);
        assertEquals("somethingelse", getprop.invoke(o, null));
        o.removePropertyChangeListener(l);
        assertEquals(0, count.getInt(o));
        o.addPropertyChangeListener(l);
        assertEquals(1, count.getInt(o));
        o.removePropertyChangeListener(l);
        assertEquals(0, count.getInt(o));
    }
    
    public void testRecursiveInit() throws Exception {
        Class c = makeClazz("RecursiveInitSCO");
        SharedClassObject o = SharedClassObject.findObject(c, true);
        assertEquals(0, c.getField("count").getInt(null));
        o.getProperty("foo");
        assertEquals(1, c.getField("count").getInt(null));
        assertEquals(o, c.getField("INSTANCE").get(null));
    }
    
    public void testAbilityToReadResolveToAnyObject () throws Exception {
        SharedClassObject o = SharedClassObject.findObject (SharedClassObjectWithReadResolve.class, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream ();
        ObjectOutputStream oos = new ObjectOutputStream (os);
        oos.writeObject (o);
        oos.close ();
        
        ObjectInputStream ois = new ObjectInputStream (new ByteArrayInputStream (os.toByteArray()));
        Object result = ois.readObject ();
        ois.close ();
        
        
        assertEquals ("Result should be the string", String.class, result.getClass());
        
    }
    
    /** Create a fresh Class object from one of this test's inner classes.
     * Produces a new classloader so the class is always fresh.
     */
    private Class makeClazz(String name) throws Exception {
        return Class.forName("org.openide.util.SharedClassObjectTest$" + name, false, new MaskingURLClassLoader());
    }
    private static final class MaskingURLClassLoader extends URLClassLoader {
        public MaskingURLClassLoader() {
            super(new URL[] {SharedClassObjectTest.class.getProtectionDomain().getCodeSource().getLocation()},
                  SharedClassObject.class.getClassLoader());
        }
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("org.openide.util.SharedClassObjectTest")) {
                // Do not proxy to parent!
                Class c = findLoadedClass(name);
                if (c != null) return c;
                c = findClass(name);
                if (resolve) resolveClass(c);
                return c;
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }
    
    public static class SimpleSCO extends SharedClassObject {
        public static int initcount = 0;
        private static String firstinit = null;
        protected void initialize() {
            super.initialize();
            initcount++;
            if (initcount > 1) {
                System.err.println("Multiple initializations of SimpleSCO: see http://www.netbeans.org/issues/show_bug.cgi?id=14700");
                System.err.print(firstinit);
                new Throwable("Init #" + initcount + " here").printStackTrace();
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new Throwable("Init #1 here").printStackTrace(new PrintStream(baos));
                firstinit = baos.toString();
                // don't print anything unless there is a problem later
            }
        }
        // Protect against random workings of GC:
        protected boolean clearSharedData() {
            return false;
        }
    }
    
    public static class ClearSharedDataSCO extends SharedClassObject {
        protected boolean clearSharedData() {
            return true;
        }
    }
    
    public static class DontClearSharedDataSCO extends SharedClassObject {
        protected boolean clearSharedData() {
            return false;
        }
    }
    
    // SCO.DataEntry.tryToInitialize in absence of EM will try to print
    // stack trace of unexpected exceptions, so just suppress it
    private static final class QuietException extends NullPointerException {
        public void printStackTrace() {
            // do nothing
        }
    }
    
    public static class InitErrorSCO extends SharedClassObject {
        protected void initialize() {
            throw new QuietException();
        }
    }
    
    public static class PropFirerSCO extends SharedClassObject {
        public int addCount = 0;
        protected void addNotify() {
            super.addNotify();
            addCount++;
        }
        protected void removeNotify() {
            addCount--;
            super.removeNotify();
        }
        public void putprop(Object val, boolean notify) {
            putProperty("key", val, notify);
        }
        public Object getprop() {
            return getProperty("key");
        }
    }
    
    public static class RecursiveInitSCO extends SharedClassObject {
        public static final RecursiveInitSCO INSTANCE = (RecursiveInitSCO)SharedClassObject.findObject(RecursiveInitSCO.class, true);
        public static int count = 0;
        protected void initialize() {
            super.initialize();
            count++;
        }
    }

    public static final class SharedClassObjectWithReadResolve extends SharedClassObject {
        public Object readResolve () throws java.io.ObjectStreamException {
            return "Ahoj";
        }
    }
}
