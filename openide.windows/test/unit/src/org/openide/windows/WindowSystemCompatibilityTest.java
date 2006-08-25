/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.windows;

import junit.framework.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Tests that a window system implementation conforms to the expected
 * behaviour.
 *
 * @author Jaroslav Tulach
 */
public final class WindowSystemCompatibilityTest extends Object {
    /** initialize the lookup for the test */
    public static void init() {
        System.setProperty("org.openide.util.Lookup", WindowSystemCompatibilityTest.class.getName() + "$Lkp");
        
        Object o = Lookup.getDefault();
        if (!(o instanceof Lkp)) {
            Assert.fail("Wrong lookup object: " + o);
        }
    }
    
    private WindowSystemCompatibilityTest(String testName) {
    }

    /** Checks the default implementation.
     */
    public static Test suite() {
        return suite(null);
    }
    
    /** Executes the test for provided window manager.
     */
    public static Test suite(WindowManager wm) {
        init();
        
        Object o = Lookup.getDefault();
        Lkp l = (Lkp)o;
        l.assignWM(wm);
        
        if (wm != null) {
            Assert.assertEquals("Same engine found", wm, WindowManager.getDefault());
        } else {
            o = WindowManager.getDefault();
            Assert.assertNotNull("Engine found", o);
            Assert.assertEquals(DummyWindowManager.class, o.getClass());
        }
        
        TestSuite ts = new TestSuite();
        ts.addTestSuite(WindowManagerHid.class);
        
        return ts;
    }

    /** Default lookup used in the suite.
     */
    public static final class Lkp extends ProxyLookup {
        private InstanceContent ic;
        
        public Lkp() {
            super(new Lookup[0]);
            
            ic = new InstanceContent();
            AbstractLookup al = new AbstractLookup(ic);
            
            setLookups(new Lookup[] {
                al, Lookups.metaInfServices(Lkp.class.getClassLoader())
            });
        }
        
        final void assignWM(WindowManager executionEngine) {
//          ic.setPairs(java.util.Collections.EMPTY_LIST);
            if (executionEngine != null) {
                ic.add(executionEngine);
            }
        }
        
        
    }

}
