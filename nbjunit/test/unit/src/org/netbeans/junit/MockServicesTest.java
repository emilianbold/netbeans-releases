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

package org.netbeans.junit;

import java.lang.reflect.Method;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import org.openide.util.Lookup;

public abstract class MockServicesTest extends TestCase {
    
    protected MockServicesTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTestSuite(JreTest.class);
        s.addTestSuite(LookupTest.class);
        return s;
    }
    
    public interface Choice {
        String value();
    }
    
    protected abstract <T> Iterator<? extends T> lookup(Class<T> clazz);
    
    private String getChoice() {
        Iterator<? extends Choice> it = lookup(Choice.class);
        if (it.hasNext()) {
            Choice c = it.next();
            if (it.hasNext()) {
                throw new IllegalStateException("have >1 instance available: " + c + " vs. " + it.next());
            }
            return c.value();
        } else {
            return "default";
        }
    }
    
    public void testGetChoice() {
        MockServices.setServices();
        assertEquals("initial value", "default", getChoice());
        MockServices.setServices(MockChoice1.class);
        assertEquals("registered value", "mock1", getChoice());
        MockServices.setServices(MockChoice2.class);
        assertEquals("registered value", "mock2", getChoice());
        MockServices.setServices(MockChoice1.class, MockChoice2.class);
        try {
            getChoice();
            fail("Should not work on >1 choice");
        } catch (IllegalStateException x) {}
    }
    
    public static final class MockChoice1 implements Choice {
        public MockChoice1() {}
        public String value() {
            return "mock1";
        }
    }
    
    public static final class MockChoice2 implements Choice {
        public MockChoice2() {}
        public String value() {
            return "mock2";
        }
    }
    
    public void testBackgroundServicesStillAvailable() {
        MockServices.setServices();
        Iterator<? extends DummyService> i = lookup(DummyService.class);
        assertTrue("statically registered service available", i.hasNext());
        assertEquals("of correct type", DummyServiceImpl.class, i.next().getClass());
        assertFalse("but no more", i.hasNext());
        MockServices.setServices(DummyServiceImpl2.class);
        i = lookup(DummyService.class);
        assertTrue("custom service registered", i.hasNext());
        assertEquals("before static service", DummyServiceImpl2.class, i.next().getClass());
        assertTrue("then static service", i.hasNext());
        assertEquals("of static type", DummyServiceImpl.class, i.next().getClass());
        assertFalse("and that is all", i.hasNext());
    }
    
    public static final class DummyServiceImpl2 implements DummyService {}
    
    public void testModifierRestrictions() {
        try {
            MockServices.setServices(MockChoice3.class);
            fail("Should not permit nonpublic class to be registered");
        } catch (IllegalArgumentException x) {/* right */}
        try {
            MockServices.setServices(MockChoice4.class);
            fail("Should not permit class w/o public constructor to be registered");
        } catch (IllegalArgumentException x) {/* right */}
        try {
            MockServices.setServices(MockChoice5.class);
            fail("Should not permit class w/o no-arg constructor to be registered");
        } catch (IllegalArgumentException x) {/* right */}
    }
    
    private static final class MockChoice3 implements Choice {
        public MockChoice3() {}
        public String value() {
            return "mock3";
        }
    }
    
    public static final class MockChoice4 implements Choice {
        MockChoice4() {}
        public String value() {
            return "mock4";
        }
    }
    
    public static final class MockChoice5 implements Choice {
        public MockChoice5(String v) {}
        public String value() {
            return "mock5";
        }
    }
    
    public void testInstancesFromDerivativeClassLoaders() {
        // XXX try passing class objects not loadable by MockServices's class loader
        // currently will throw assertion errors
        // but should try to load them
    }
    
    public static class JreTest extends MockServicesTest {
        
        public JreTest(String s) {
            super(s);
        }
        
        @SuppressWarnings("unchecked") // using reflection
        protected <T> Iterator<? extends T> lookup(Class<T> clazz) {
            try {
                Class serviceLoader = Class.forName("java.util.ServiceLoader");
                Method load = serviceLoader.getMethod("load", Class.class);
                Object loader = load.invoke(null, clazz);
                return ((Iterable) loader).iterator();
            } catch (Exception x1) {
                try {
                    Class service = Class.forName("sun.misc.Service");
                    Method providers = service.getMethod("providers", Class.class);
                    return (Iterator) providers.invoke(null, clazz);
                } catch (Exception x2) {
                    throw (AssertionError) new AssertionError("Neither java.util.ServiceLoader nor sun.misc.Service available").initCause(x1.initCause(x2));
                }
            }
        }
        
    }
    
    public static class LookupTest extends MockServicesTest {
        
        public LookupTest(String s) {
            super(s);
        }
        
        protected <T> Iterator<? extends T> lookup(Class<T> clazz) {
            return Lookup.getDefault().lookupAll(clazz).iterator();
        }
        
    }
    
}
