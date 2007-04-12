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

package org.netbeans.api.debugger.jpda;

import java.util.List;

import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.junit.NbTestCase;

/**
 * The test of heap walking functionality - retrieval of classes, instances and back references.
 * 
 * @author Martin Entlicher
 */
public class HeapWalkingTest extends NbTestCase {
    
    private JPDASupport     support;
    
    /** Creates a new instance of HeapWalkingTest */
    public HeapWalkingTest(String s) {
        super(s);
    }
    
    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
        LineBreakpoint lb = LineBreakpoint.create (
                Utils.getURL(System.getProperty ("test.dir.src")+
                             "org/netbeans/api/debugger/jpda/testapps/HeapWalkApp.java"),
                40
            );
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.HeapWalkApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
    }
    
    public void testClasses () throws Exception {
        List<JPDAClassType> allClasses = support.getDebugger().getAllClasses();
        boolean foundHeapWalkApp = false;
        boolean foundMultiInstanceClass = false;
        System.out.println("All Classes size = "+allClasses.size());
        for (JPDAClassType type : allClasses) {
            //System.out.println("Have class: '"+type.getName()+"'");
            if (type.getName().equals("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp")) {
                foundHeapWalkApp = true;
            }
            if (type.getName().equals("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass")) {
                foundMultiInstanceClass = true;
            }
        }
        assertTrue("The class HeapWalkApp was not found!", foundHeapWalkApp);
        assertTrue("The class MultiInstanceClass was not found!", foundMultiInstanceClass);
        
        List<JPDAClassType> hClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp");
        assertEquals("HeapWalkApp classes bad number: ", 1, hClasses.size());
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        assertEquals("HeapWalkApp classes bad number: ", 1, mClasses.size());
    }

    public void testInstances () throws Exception {
        if (!support.getDebugger().canGetInstanceInfo()) {
            System.out.println("Can not retrieve instance information! Test is skipped.");
            return ; // Nothing to test
        }
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        JPDAClassType mClass = mClasses.get(0);
        assertEquals("Bad instance count: ", mClass.getInstanceCount(), 10);
        List<ObjectVariable> instances = mClass.getInstances(0);
        assertEquals("Bad number of instances: ", instances.size(), 10);
        long[] mClassesInstanceCounts = support.getDebugger().getInstanceCounts(mClasses);
        assertEquals("Bad number of instances: ", mClassesInstanceCounts[0], 10L);
        for (ObjectVariable instance : instances) {
            assertEquals("The class type differs: ", instance.getClassType(), mClass);
        }
    }
    
    public void testBackReferences() throws Exception {
        if (!support.getDebugger().canGetInstanceInfo()) {
            System.out.println("Can not retrieve instance information! Test is skipped.");
            return ; // Nothing to test
        }
        List<JPDAClassType> mClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp$MultiInstanceClass");
        JPDAClassType mClass = mClasses.get(0);
        List<ObjectVariable> instances = mClass.getInstances(0);
        List<ObjectVariable> referrers = instances.get(0).getReferringObjects(0);
        assertEquals("Bad number of referrers: ", referrers.size(), 1);
        
        List<JPDAClassType> hClasses = support.getDebugger().getClassesByName("org.netbeans.api.debugger.jpda.testapps.HeapWalkApp");
        ObjectVariable hInstance = hClasses.get(0).getInstances(0).get(0);
        ObjectVariable var = referrers.get(0);
        while (var.getUniqueID() != hInstance.getUniqueID()) {
            var = var.getReferringObjects(0).get(0);
            assertNotNull("Object "+hInstance+" not found as a referrer to "+referrers.get(0), var);
        }
    }

}
