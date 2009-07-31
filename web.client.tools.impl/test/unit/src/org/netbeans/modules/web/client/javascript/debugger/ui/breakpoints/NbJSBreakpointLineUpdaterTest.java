/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * @author joelle
 */
public class NbJSBreakpointLineUpdaterTest extends NbJSDTestBase {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public NbJSBreakpointLineUpdaterTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        List<NbJSDTestBase> tests = getTests();
        for (NbJSDTestBase test : tests) {
            suite.addTest(test);
        }
        return suite;
    }

    public static List<NbJSDTestBase> getTests() {
        List<NbJSDTestBase> tests = new ArrayList<NbJSDTestBase>();
        tests.add(new NbJSBreakpointLineUpdaterTest("testBreakpointConstructor"));
        tests.add(new NbJSBreakpointLineUpdaterTest("testBreakpointLineUpdaterAttach"));
        tests.add(new NbJSBreakpointLineUpdaterTest("testBreakpointLineUpdaterDetach"));

        return tests;
    }
    
    public void testBreakpointConstructor() throws IOException {
        FileObject jsFO = createJSFO();
        assertNotNull(jsFO);
        
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        assertNotNull(bp);

        NbJSBreakpointLineUpdater lineUpdater = new NbJSBreakpointLineUpdater(bp);
        try {
            NbJSBreakpointLineUpdater lineUpdater2 = new NbJSBreakpointLineUpdater(null);
        } catch ( NullPointerException npe ){
            return;
        }
        fail();
    }

    public void testBreakpointLineUpdaterAttach() throws Exception {
        FileObject jsFO = createJSFO();
        assertNotNull(jsFO);

        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        assertNotNull(bp);

        NbJSBreakpointLineUpdater lineUpdater = new NbJSBreakpointLineUpdater(bp);
//        bp.addPropertyChangeListener(new PropertyChangeListenerDummy());

        try {
            lineUpdater.attach();
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    public void testBreakpointLineUpdaterDetach() throws Exception {
        FileObject jsFO = createJSFO();
        assertNotNull(jsFO);

        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        assertNotNull(bp);

        NbJSBreakpointLineUpdater lineUpdater = new NbJSBreakpointLineUpdater(bp);
//        bp.addPropertyChangeListener(new PropertyChangeListenerDummy());

        lineUpdater.detach();
    }

//  private boolean propertyChangeListenerCalled = false;  
//  private class PropertyChangeListenerDummy implements PropertyChangeListener {
//        public void propertyChange(PropertyChangeEvent arg0) {
//            propertyChangeListenerCalled = true;
//        }
//  }
//  
    private void burnTime() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }



   public FileObject createVegJSFO() throws IOException {
        String[] vegetableContent = {
            "document.write('pea, cucumber, cauliflower, broccoli');",
        };
        File vegetableF = createJavaScript(vegetableContent, "vegetable.js");
        FileObject vegetableFO = FileUtil.toFileObject(vegetableF);
        return vegetableFO;
    }
}
