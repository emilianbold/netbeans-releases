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

package gui.action;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.EditorOperator;

/**
 *
 * 
 */
public class SwitchToFile  extends org.netbeans.performance.test.utilities.PerformanceTestCase {

        EditorWindowOperator ewo = new EditorWindowOperator();
        String filenameFrom;
        String filenameTo;
    
    /** Creates a new instance of SwitchToFile */
    public SwitchToFile(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node prn = pto.getProjectRootNode("PerformanceTestWebApplication");

        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|StatusBar.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|CompleteWord.java"));
        new OpenAction().performAPI(new Node(prn, "web|Test.jsp"));
        new OpenAction().performAPI(new Node(prn, "web|BigJSP.jsp"));

        FilesTabOperator fto= FilesTabOperator.invoke();
        Node f = fto.getProjectNode("PerformanceTestWebApplication");

        new OpenAction().performAPI(new Node(f, "build.xml"));
    }
    
    /** Creates a new instance of SwitchToFile */
    public SwitchToFile(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node prn = pto.getProjectRootNode("PerformanceTestWebApplication");

        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|StatusBar.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|CompleteWord.java"));
        new OpenAction().performAPI(new Node(prn, "web|Test.jsp"));
        new OpenAction().performAPI(new Node(prn, "web|BigJSP.jsp"));

        FilesTabOperator fto= FilesTabOperator.invoke();
        Node f = fto.getProjectNode("PerformanceTestWebApplication");

        new OpenAction().performAPI(new Node(f, "build.xml"));
    }

    public void testSwitchJavaToJava(){
        filenameFrom = "CompleteWord.java";
        filenameTo = "StatusBar.java";
        doMeasurement();
    }

    public void testSwitchJSPToJSP(){
        filenameFrom = "Test.jsp";
        filenameTo = "BigJsp.jsp";
        doMeasurement();
    }

    public void testSwitchJavaToJSP(){
        filenameFrom = "StatusBar.java";
        filenameTo = "BigJsp.jsp";
        doMeasurement();
    }

    public void testSwitchJSPToXML(){
        filenameFrom = "BigJSP.jsp";
        filenameTo = "build.xml";
        doMeasurement();
    }

    public void testSwitchXMLToJSP(){
        filenameFrom = "build.xml";
        filenameTo = "BigJSP.jsp";
        doMeasurement();
    }

    
    protected void initialize() {
        log(":: initialize");

        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        setJavaEditorCaretFilteringOn();
    }
        
    public void prepare() {
        log(":: prepare");
        EditorOperator eo = ewo.selectPage(filenameFrom);
    }
    
    public ComponentOperator open() {
        log(":: open");
        return new EditorOperator(filenameTo);
    }
    
    public void close() {
        log("::close");
    }
    
    @Override
    protected void shutdown() {
        repaintManager().resetRegionFilters();
    }

    
    
    public static void main(String[] args) {
        repeat = 3;
        junit.textui.TestRunner.run(new SwitchToFile("measureTime"));
    }
}