/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.truffle;

import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.sl.SLLanguage;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDASupport;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;

public class DebugSLTest extends NbTestCase {
    private DebuggerManager dm = DebuggerManager.getDebuggerManager();
    private final String sourceRoot = System.getProperty("test.dir.src");
    private JPDASupport support;

    public DebugSLTest(String name) {
        super(name);
    }

    public static Test suite() throws URISyntaxException {
        final File truffleAPI = new File(PolyglotEngine.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final File sl = new File(SLLanguage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final File junit = new File(TestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue("truffle-api JAR exists: " + truffleAPI, truffleAPI.exists());
        assertTrue("sl JAR exists: " + sl, sl.exists());
        assertTrue("junit JAR exists: " + junit, junit.exists());

        System.setProperty("truffle.jar", truffleAPI.getAbsolutePath());
        System.setProperty("sl.jar", sl.getAbsolutePath());
        System.setProperty("junit.jar", junit.getAbsolutePath());

        return JPDASupport.createTestSuite(DebugSLTest.class);
    }

    public void testStepIntoSL() throws Exception {
        try {
            JPDASupport.removeAllBreakpoints();
            org.netbeans.api.debugger.jpda.Utils.BreakPositions bp = org.netbeans.api.debugger.jpda.Utils.getBreakPositions(
                sourceRoot
                + "org/netbeans/modules/debugger/jpda/truffle/testapps/SLApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint(lb);
            support = JPDASupport.attach("org.netbeans.modules.debugger.jpda.truffle.testapps.SLApp",
                new String[0],
                new File[] {
                    new File(System.getProperty("truffle.jar")),
                    new File(System.getProperty("sl.jar")),
                    new File(System.getProperty("junit.jar")),
                }
            );
            support.waitState(JPDADebugger.STATE_STOPPED);
            support.stepInto();
            final JPDADebugger debugger = support.getDebugger();
            CallStackFrame frame = debugger.getCurrentCallStackFrame();
            assertNotNull(frame);
            // Check that frame is in the Truffle access method
            Field haltedClassField = TruffleAccess.class.getDeclaredField("HALTED_CLASS_NAME");
            haltedClassField.setAccessible(true);
            String haltedClass = (String) haltedClassField.get(null);
            Field haltedMethodField = TruffleAccess.class.getDeclaredField("METHOD_EXEC_HALTED");
            haltedMethodField.setAccessible(true);
            String haltedMethod = (String) haltedMethodField.get(null);
            assertEquals("Stopped in Truffle halted class", haltedClass, frame.getClassName());
            assertEquals("Stopped in Truffle halted method", haltedMethod, frame.getMethodName());
            assertEquals("Unexpected stratum", TruffleStrataProvider.TRUFFLE_STRATUM, frame.getDefaultStratum());
            
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            assertNotNull("Missing CurrentPCInfo", currentPCInfo);
            TruffleStackFrame topFrame = currentPCInfo.getTopFrame();
            assertNotNull("No top frame", topFrame);
            SourcePosition sourcePosition = topFrame.getSourcePosition();
            assertEquals("Bad source", "Meaning of world.sl", sourcePosition.getSource().getName());
            assertEquals("Bad line", 1, sourcePosition.getLine());
            assertEquals("Bad method name", "main", topFrame.getMethodName());
            
            support.doContinue();
            support.waitState(JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish();
            }
        }
    }

}
