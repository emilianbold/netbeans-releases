/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import org.netbeans.modules.web.client.javascript.debugger.models.NbJSBreakpointModel;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDUITestBase;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 *
 * @author joelle
 */
public class NbJSBreakpointModelTest extends NbJSDUITestBase {
    
    public NbJSBreakpointModelTest(String testName) {
        super(testName);
    }            

    
        /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        List<NbJSDUITestBase> tests = getTests();
        for (NbJSDUITestBase test : tests) {
            suite.addTest(test);
        }
        return suite;
    }

    public static List<NbJSDUITestBase> getTests() {
        List<NbJSDUITestBase> tests = new ArrayList<NbJSDUITestBase>();
        tests.add(new NbJSBreakpointModelTest("testGetDisplayName"));
        tests.add(new NbJSBreakpointModelTest("testGetIconBase"));
        tests.add(new NbJSBreakpointModelTest("testGetShortDescription"));
        tests.add(new NbJSBreakpointModelTest("testAddModelListener"));
        tests.add(new NbJSBreakpointModelTest("testRemoveModelListener"));
        tests.add(new NbJSBreakpointModelTest("testGetValueAt"));
        tests.add(new NbJSBreakpointModelTest("testIsReadOnly"));
        tests.add(new NbJSBreakpointModelTest("testSetValueAt"));
        return tests;
    }
    
    
    /**
     * Test of getDisplayName method, of class NbJSBreakpointModel.
     */
    public void testGetDisplayName() throws Exception {
       System.out.println("getDisplayName");
        
       FileObject jsFO = createJSFO();
       Line line = createDummyLine(jsFO, 4);
       NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
       
       NbJSBreakpointModel model = new NbJSBreakpointModel();
       String expResult = "mytestfile.js:5";
       String result = model.getDisplayName(breakpoint);
       assertEquals(expResult, result);
    }

    /**
     * Test of getIconBase method, of class NbJSBreakpointModel.
     */
    public void testGetIconBase() throws Exception {
        System.out.println("getIconBase");
        
        FileObject jsFO = createJSFO();
        Line line = createDummyLine(jsFO, 4);
        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);

        NbJSBreakpointModel model = new NbJSBreakpointModel();
        String expResult = "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
        String result = model.getIconBase(breakpoint);
        assertEquals(expResult, result);
        
        try { 
            String result2 = model.getIconBase(null);
        } catch ( NullPointerException npe){
            return;
        }
        fail("NullPointerException should have been thrown.");
    }

    /**
     * Test of getShortDescription method, of class NbJSBreakpointModel.
     */
    public void testGetShortDescription() throws Exception {
        System.out.println("getShortDescription");
        
        FileObject jsFO = createJSFO();
        Line line = createDummyLine(jsFO, 4);
        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
        
        NbJSBreakpointModel instance = new NbJSBreakpointModel();
        String expResult = "org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDUITestBase$DummyLine:4";
        String result = instance.getShortDescription(breakpoint);
        assertEquals(expResult, result);
    }

    /**
     * Test of addModelListener method, of class NbJSBreakpointModel.
     */
    public void testAddModelListener() {
        System.out.println("addModelListener");
        ModelListener l = null;
        modelChanged = false;
        NbJSBreakpointModel instance = new NbJSBreakpointModel();
        instance.addModelListener( new DummyModelListener() );
        instance.fireChanges();
        NbJSDUITestBase.burnTime(3);
        assertTrue(modelChanged);
        modelChanged = false;
        
    }
    private static boolean modelChanged = false;

    
    private class DummyModelListener implements ModelListener {
        
            public void modelChanged(ModelEvent event) {
                modelChanged = true;
            }
    }
    /**
     * Test of removeModelListener method, of class NbJSBreakpointModel.
     */
    public void testRemoveModelListener() {
        System.out.println("removeModelListener");
        
        ModelListener l = new DummyModelListener();
        modelChanged = false;
        NbJSBreakpointModel instance = new NbJSBreakpointModel();
        instance.addModelListener( l );
        NbJSDUITestBase.burnTime(3);
        instance.removeModelListener(l);
        instance.fireChanges();
        NbJSDUITestBase.burnTime(3);
        assertFalse(modelChanged);
        modelChanged = false;
    }

    /**
     * Test of getValueAt method, of class NbJSBreakpointModel.
     */
    public void testGetValueAt() throws Exception {
        System.out.println("getValueAt");
        
        FileObject jsFO = createJSFO();
        Line line = createDummyLine(jsFO, 4);
        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
        
        
        // TODO: Instead of BREAKPOINT_ENABLED_COLUMN_ID, which was removed,
        //       write test for RESOLVED_LOCATION_COLUMN_ID instead
        /* Testing Breakpoint Enabled Column ID *
        String columnID = Constants.BREAKPOINT_ENABLED_COLUMN_ID;
        NbJSBreakpointModel instance = new NbJSBreakpointModel();
        Object expResult = Boolean.valueOf(true);
        Object result = instance.getValueAt(breakpoint, columnID);
        assertEquals(expResult, result);
        
        breakpoint.disable();
        Object expResult2 = Boolean.valueOf(false);
        Object result2 = instance.getValueAt(breakpoint, columnID);
        assertEquals(expResult2, result2);
        */
    }

    /**
     * Test of isReadOnly method, of class NbJSBreakpointModel.
     */
    public void testIsReadOnly() throws Exception {
        System.out.println("isReadOnly");
        
        FileObject jsFO = createJSFO();
        Line line = createDummyLine(jsFO, 4);
        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
        
        // TODO: Instead of BREAKPOINT_ENABLED_COLUMN_ID, which was removed,
        //       write test for RESOLVED_LOCATION_COLUMN_ID instead
        /* Testing Breakpoint Enabled Column ID *
        String columnID = Constants.BREAKPOINT_ENABLED_COLUMN_ID;
        NbJSBreakpointModel instance = new NbJSBreakpointModel();
        boolean expResult = false;
        boolean result = instance.isReadOnly(breakpoint, columnID);
        assertEquals(expResult, result);
         */
    }

    /**
     * Test of setValueAt method, of class NbJSBreakpointModel.
     */
    public void testSetValueAt() throws Exception {
        System.out.println("setValueAt");
        
        
        FileObject jsFO = createJSFO();
        Line line = createDummyLine(jsFO, 4);
        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
        
        // TODO: Instead of BREAKPOINT_ENABLED_COLUMN_ID, which was removed,
        //       write test for RESOLVED_LOCATION_COLUMN_ID instead
        /* Testing Breakpoint Enabled Column ID *
        String columnID = Constants.BREAKPOINT_ENABLED_COLUMN_ID;
        NbJSBreakpointModel instance = new NbJSBreakpointModel();
        
        instance.setValueAt(breakpoint, columnID, Boolean.valueOf(false));
        
        Object expResult = Boolean.valueOf(false);
        Object result = instance.getValueAt(breakpoint, columnID);
        assertEquals(expResult, result);
        
        assertFalse(breakpoint.isEnabled());
        
        boolean hasException = false;
        try {
            instance.setValueAt(breakpoint, columnID, new Integer(5));
        } catch ( UnknownTypeException ute ){
           hasException = true;
        }
        assertTrue(hasException);
        hasException = false;
        
        String columnID2 = "someUnkownCOlumn";
        try {
            instance.setValueAt(breakpoint, columnID2, new Integer(5));
        } catch ( UnknownTypeException ute ){
           hasException = true;
        }
        assertTrue(hasException);
        */
    }
    
    
}
