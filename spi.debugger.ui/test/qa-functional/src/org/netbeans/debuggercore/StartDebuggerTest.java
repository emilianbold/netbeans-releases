/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
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
 * Contributor(s): Sun Microsystems, Inc.
 */

package org.netbeans.debuggercore;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;

public class StartDebuggerTest extends JellyTestCase {
    
    public StartDebuggerTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(StartDebuggerTest.class).addTest(
                "testDebugMainProject",
                "testDebugProject",
                "testDebugFile",
                "testRunDebuggerStepInto",
                "testRunDebuggerRunToCursor"
                ).enableModules(".*").clusters(".*"));
    }
    
    public void setUp() throws IOException {
        openDataProjects(Utilities.testProjectName);
        System.out.println("########  " + getName() + "  #######");
    }
    
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        Utilities.endAllSessions();
    }
    
    public void testDebugMainProject() throws Throwable {
        try {
            new Action(null, Utilities.setMainProjectAction).perform(new ProjectsTabOperator().getProjectRootNode(Utilities.testProjectName));
            new Action(Utilities.runMenu+"|"+Utilities.debugMainProjectItem, null).perform();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    public void testDebugProject() throws Throwable {
        try {
            Node projectNode = ProjectsTabOperator.invoke().getProjectRootNode(Utilities.testProjectName);
            new DebugProjectAction().perform(projectNode);
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    public void testDebugFile() throws Throwable {
        try {
            //open source
            Node beanNode = new Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            EditorOperator eo = new EditorOperator("MemoryView.java");
            new Action(null, null, Utilities.debugFileShortcut).performShortcut();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole(Utilities.runningStatusBarText, 0);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    public void testRunDebuggerStepInto() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            new Action(null, null, Utilities.stepIntoShortcut).performShortcut();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:", 0);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
    
    public void testRunDebuggerRunToCursor() throws Throwable {
        try {
            EditorOperator eo = new EditorOperator("MemoryView.java");
            Utilities.setCaret(eo, 75);
            new Action(null, null, Utilities.runToCursorShortcut).performShortcut();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:75", 0);
        } catch (Throwable th) {
            Utilities.captureScreen(this);
            throw th;
        }
    }
}
