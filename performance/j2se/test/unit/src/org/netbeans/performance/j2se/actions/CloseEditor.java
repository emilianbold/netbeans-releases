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
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of Closing Editor tabs.
 *
 * @author  mmirilovic@netbeans.org
 */
public class CloseEditor extends PerformanceTestCase {
    
    /** Folder with data */
    public static String fileProject;
    
    /** Folder with data "gui/data" */
    public static String filePackage;
    
    /** Name of file to open */
    public static String fileName;
    
    
    /**
     * Creates a new instance of CloseEditor
     * @param testName the name of the test
     */
    public CloseEditor(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE=1000;
    }
    
    /**
     * Creates a new instance of CloseEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseEditor(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE=1000;
    }
    
    
    public void testClosing20kBJavaFile(){
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        doMeasurement();
    }
    
    public void testClosing20kBFormFile(){
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        doMeasurement();
    }
    
    public void initialize(){
        EditorOperator.closeDiscardAll();
    }

    public void shutdown(){
        EditorOperator.closeDiscardAll();
    }
    
    public void prepare(){
        new OpenAction().performAPI(new Node(new SourcePackagesNode(fileProject),filePackage + '|' + fileName));
        
        new EventTool().waitNoEvent(5000);
    }
    
    public ComponentOperator open(){
        if(fileName.equalsIgnoreCase("JFrame20kB.java")){
            new CloseViewAction().performMenu(new FormDesignerOperator(fileName)); 
        }else{
            new CloseViewAction().performMenu(new EditorOperator(fileName)); 
        }
        return null;
    }
    
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        //junit.textui.TestRunner.run(new CloseEditor("testClosing20kBJavaFile"));
        junit.textui.TestRunner.run(new CloseEditor("testClosing20kBFormFile"));
    }
    
}
