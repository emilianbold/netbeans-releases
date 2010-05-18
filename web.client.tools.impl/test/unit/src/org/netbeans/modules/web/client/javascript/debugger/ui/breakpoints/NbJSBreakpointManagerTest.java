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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDTestBase;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;

/**
 *
 * @author joelle
 */
public class NbJSBreakpointManagerTest extends NbJSDTestBase {

    public NbJSBreakpointManagerTest(String testName) {
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
        tests.add(new NbJSBreakpointManagerTest("testCreateBreakpoint"));
        tests.add(new NbJSBreakpointManagerTest("testAddBreakpoint"));
        tests.add(new NbJSBreakpointManagerTest("testRemoveBreakpoint"));
        tests.add(new NbJSBreakpointManagerTest("testGetBreakpoints"));
        tests.add(new NbJSBreakpointManagerTest("testIsBreakpointOnLine"));
        return tests;
    }

    /**
     * Test of createBreakpoint method, of class NbJSBreakpointManager.
     */
    public void testCreateBreakpoint() throws IOException {
        System.out.println("createBreakpoint");
        FileObject fo = createJSFO();
        int lineNum = 3;
        Line line = createDummyLine(fo, lineNum);
        NbJSBreakpoint result = NbJSBreakpointManager.createBreakpoint(line);
        assertNotNull(result);
        assertEquals("The line number should be the same after the breakpoint is created.", lineNum, line.getLineNumber());

    }

    /**
     * Test of addBreakpoint method, of class NbJSBreakpointManager.
     */
    public void testAddBreakpoint() throws IOException {
        System.out.println("addBreakpoint");

        FileObject fo = createJSFO();
        int lineNum = 3;
        Line line = createDummyLine(fo, lineNum);

        NbJSBreakpoint result = NbJSBreakpointManager.addBreakpoint(line);
        assertNotNull(result);
        assertEquals(line, result.getLine());
        Breakpoint[] bps = DebuggerManager.getDebuggerManager().getBreakpoints();
        assertEquals("When a breakpoing is added it should also be added to the debugger manager.", result, bps[0]);

    }

    /**
     * Test of removeBreakpoint method, of class NbJSBreakpointManager.
     */
    public void testRemoveBreakpoint() throws IOException {
        System.out.println("removeBreakpoint");


        FileObject fo = createJSFO();
        int lineNum = 3;
        Line line = createDummyLine(fo, lineNum);

        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);

        Breakpoint[] bps = DebuggerManager.getDebuggerManager().getBreakpoints();
        assertEquals("When a breakpoing is added it should also be added to the debugger manager.", breakpoint, bps[0]);

        NbJSBreakpointManager.removeBreakpoint(breakpoint);
        Breakpoint[] bps2 = DebuggerManager.getDebuggerManager().getBreakpoints();
        assertEquals("There should be no more breakpoints left", bps2.length, 0);
    }

    /**
     * Test of getBreakpoints method, of class NbJSBreakpointManager.
     */
    public void testGetBreakpoints() throws IOException {
        System.out.println("getBreakpoints");


        FileObject fo = createJSFO();
        int maxLineNum = 4;
        List<Breakpoint> bpList = new ArrayList<Breakpoint>();

        for (int i = 0; i < maxLineNum; i++) {
            Line line = createDummyLine(fo, i);
            NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
            bpList.add(breakpoint);
        }

        Breakpoint[] bps = NbJSBreakpointManager.getBreakpoints();
        for (Breakpoint bp : bps) {
            assertTrue(bpList.contains(bp));
        }

        assertEquals(DebuggerManager.getDebuggerManager().getBreakpoints().length, bps.length);

    }

    /**
     * Test of isBreakpointOnLine method, of class NbJSBreakpointManager.
     */
    public void testIsBreakpointOnLine() throws IOException {
        System.out.println("isBreakpointOnLine");
        FileObject jsFO = createJSFO();
        int lineNum = 3;
        boolean expResult1 = false;
        boolean result1 = NbJSBreakpointManager.isBreakpointOnLine(jsFO, 4);
        assertEquals(expResult1, result1);
        
        boolean expResult2 = true;
        Line line = createDummyLine(jsFO, lineNum);
        NbJSBreakpoint breakpoint = NbJSBreakpointManager.addBreakpoint(line);
        boolean result2 = NbJSBreakpointManager.isBreakpointOnLine(jsFO, 4);
        assertEquals(expResult2, result2);
    }

    /**
     * NOT REALLY TESTABLE WITHOUT OPENING A TOPCOMPONENT.
     * Test of getCurrentLineBreakpoint method, of class NbJSBreakpointManager.
     */
    public void testGetCurrentLineBreakpoint() throws IOException{
        System.out.println("getCurrentLineBreakpoint");
        
        FileObject jsFO = createJSFO();
        Line line = NbJSEditorUtil.getCurrentLine();
        assertNotNull(line);
        
        NbJSFileObjectBreakpoint expResult = null;
        NbJSBreakpoint result = NbJSBreakpointManager.getCurrentLineBreakpoint();
        assertEquals(expResult, result);
        
        NbJSFileObjectBreakpoint expResult2 = (NbJSFileObjectBreakpoint) NbJSBreakpointManager.addBreakpoint(line);
        NbJSBreakpoint result2 = NbJSBreakpointManager.getCurrentLineBreakpoint();
        assertEquals(expResult2, result2);
        
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
