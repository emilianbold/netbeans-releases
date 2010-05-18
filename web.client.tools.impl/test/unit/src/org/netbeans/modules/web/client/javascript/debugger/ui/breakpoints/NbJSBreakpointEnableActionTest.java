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

import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDTestBase;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDUITestBase;
import org.openide.util.HelpCtx;

/**
 *
 * @author joelle
 */
public class NbJSBreakpointEnableActionTest extends NbJSDUITestBase {
    
    public NbJSBreakpointEnableActionTest(String testName) {
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
        tests.add(new NbJSBreakpointEnableActionTest("testIsEnabled"));
//        tests.add(new NbJSBreakpointEnableActionTest("testBreakpointLineUpdaterAttach"));
//        tests.add(new NbJSBreakpointEnableActionTest("testBreakpointLineUpdaterDetach"));

        return tests;
    }
    

    /**
     * Test of isEnabled method, of class NbJSBreakpointEnableAction.
     */
    public void testIsEnabled() {
        System.out.println("isEnabled");
        NbJSBreakpointEnableAction instance = new NbJSBreakpointEnableAction();
        boolean expResult = false;
        boolean result = instance.isEnabled();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        
    }
    

    

    /**
     * Test of getName method, of class NbJSBreakpointEnableAction.
     */
    public void testGetName() {
        System.out.println("getName");
        NbJSBreakpointEnableAction instance = new NbJSBreakpointEnableAction();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setBooleanState method, of class NbJSBreakpointEnableAction.
     */
    public void testSetBooleanState() {
        System.out.println("setBooleanState");
        boolean value = false;
        NbJSBreakpointEnableAction instance = new NbJSBreakpointEnableAction();
        instance.setBooleanState(value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHelpCtx method, of class NbJSBreakpointEnableAction.
     */
    public void testGetHelpCtx() {
        System.out.println("getHelpCtx");
        NbJSBreakpointEnableAction instance = new NbJSBreakpointEnableAction();
        HelpCtx expResult = null;
        HelpCtx result = instance.getHelpCtx();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
