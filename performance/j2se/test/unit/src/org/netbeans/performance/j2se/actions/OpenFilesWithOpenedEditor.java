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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.performance.j2se.actions;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test of opening files if Editor is already opened.
 * OpenFiles is used as a base for tests of opening files
 * when editor is already opened.
 *
 * @author  mmirilovic@netbeans.org
 */
public class OpenFilesWithOpenedEditor extends OpenFiles {

    /** Name of file to pre-open */
    public static String fileName_preopen;

    /**
     * Creates a new instance of OpenFilesWithOpenedEditor
     * @param testName the name of the test
     */
    public OpenFilesWithOpenedEditor(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of OpenFilesWithOpenedEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesWithOpenedEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    @Override
    public void testOpening20kBJavaFile(){
        WAIT_AFTER_OPEN = 1500;
        setJavaEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        fileName_preopen = "Main.java";
        menuItem = OPEN;
        doMeasurement();
    }
    
    @Override
    public void testOpening20kBTxtFile(){
        WAIT_AFTER_OPEN = 1000;
        setPlainTextEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "textfile20kB.txt";
        fileName_preopen = "textfile.txt";
        menuItem = OPEN;
        doMeasurement();
    }
    
    @Override
    public void testOpening20kBXmlFile(){
        WAIT_AFTER_OPEN = 1000;
        setXMLEditorCaretFilteringOn();
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "xmlfile20kB.xml";
        fileName_preopen = "xmlfile.xml";
        menuItem = EDIT;
        doMeasurement();
    }
    
    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    @Override
    public void initialize(){
        super.initialize();
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName_preopen));
    }
    
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenFilesWithOpenedEditor("testOpening20kBTxtFile"));
    }
    
}
