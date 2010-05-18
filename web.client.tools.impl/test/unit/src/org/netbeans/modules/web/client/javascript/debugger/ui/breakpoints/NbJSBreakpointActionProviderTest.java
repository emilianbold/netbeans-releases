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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDUITestBase;

/**
 *
 * @author joelle
 */
public class NbJSBreakpointActionProviderTest extends NbJSDUITestBase {
    
    public NbJSBreakpointActionProviderTest(String testName) {
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
        return tests;
    }

    /**
     * Test of getActions method, of class NbJSBreakpointActionProvider.
     */
    public void testGetActions() {
        System.out.println("getActions");
        NbJSBreakpointActionProvider instance = new NbJSBreakpointActionProvider();
        Set<Object> expResult = null;
        Set<Object> result = instance.getActions();
        assertEquals( result.size(), 1);
        assertEquals( ActionsManager.ACTION_TOGGLE_BREAKPOINT, result.toArray()[0]);
    }

//    /**
//     * Test of doAction method, of class NbJSBreakpointActionProvider.
//     */
//    public void testDoAction() {
//        System.out.println("doAction");
//        Object action = null;
//        NbJSBreakpointActionProvider instance = new NbJSBreakpointActionProvider();
//        instance.doAction(action);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of propertyChange method, of class NbJSBreakpointActionProvider.
//     */
//    public void testPropertyChange() {
//        System.out.println("propertyChange");
//        PropertyChangeEvent evt = null;
//        NbJSBreakpointActionProvider instance = new NbJSBreakpointActionProvider();
//        instance.propertyChange(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
