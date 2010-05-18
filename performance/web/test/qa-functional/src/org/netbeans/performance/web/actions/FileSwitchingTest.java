/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.performance.web.actions;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.performance.web.setup.WebSetup;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * 
 */
public class FileSwitchingTest  extends PerformanceTestCase {

        EditorWindowOperator ewo = new EditorWindowOperator();
        String filenameFrom;
        String filenameTo;
    
    /** Creates a new instance of FileSwitchingTest */
    public FileSwitchingTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }
    
    /** Creates a new instance of FileSwitchingTest */
    public FileSwitchingTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(WebSetup.class)
             .addTest(FileSwitchingTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    class PhaseHandler extends Handler {

            public boolean published = false;

            public void publish(LogRecord record) {

            if (record.getMessage().equals("Open Editor, phase 1, AWT [ms]"))
               ActionTracker.getInstance().stopRecording();

            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }

        }

    PhaseHandler phaseHandler=new PhaseHandler();


    public void testSwitchJavaToJava(){
        filenameFrom = "Main.java";
        filenameTo = "Test.java";
        doMeasurement();
    }

    public void testSwitchJSPToJSP(){
        filenameFrom = "Test.jsp";
        filenameTo = "BigJsp.jsp";
        doMeasurement();
    }

    public void testSwitchJavaToJSP(){
        filenameFrom = "Test.java";
        filenameTo = "BigJsp.jsp";
        doMeasurement();
    }

    public void testSwitchJSPToXML(){
        filenameFrom = "BigJSP.jsp";
        filenameTo = "build.xml";
        doMeasurement();
    }

    public void testSwitchXMLToJava(){
        filenameFrom = "build.xml";
        filenameTo = "Main.java";
        doMeasurement();
    }

    
    @Override
    protected void initialize() {
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();

        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node prn = pto.getProjectRootNode("TestWebProject");

        new OpenAction().performAPI(new Node(new SourcePackagesNode("TestWebProject"), "test|Main.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("TestWebProject"), "test|Test.java"));
        new OpenAction().performAPI(new Node(prn, "web|Test.jsp"));
        new OpenAction().performAPI(new Node(prn, "web|BigJSP.jsp"));

        FilesTabOperator fto= FilesTabOperator.invoke();
        Node f = fto.getProjectNode("TestWebProject");

        new OpenAction().performAPI(new Node(f, "build.xml"));

    }
        
    public void prepare() {
        EditorOperator eo = ewo.selectPage(filenameFrom);
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);
    }
    
    public ComponentOperator open() {
        return new EditorOperator(filenameTo);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    protected void shutdown() {
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
        repaintManager().resetRegionFilters();
    }

}
