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

package org.netbeans.performance.j2se.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * Test of opening files.
 *
 * @author  mmirilovic@netbeans.org
 */
public class ShowClassMembersInNavigatorTest extends PerformanceTestCase {
    
    /** Node to be opened/edited */
    public static Node openNode, openAnotherNode ;
    /** Folder with data */
    public static String fileProject;
    /** Folder with data  */
    public static String filePackage;
    /** Name of file to open */
    public static String fileName, anotherFileName;
    /** Menu item name that opens the editor */
    public static String menuItem;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    private Logger TIMER=null;
    private boolean next=false;
    private String[] navigTime;
    NbPerformanceTest.PerformanceData d = new NbPerformanceTest.PerformanceData();

    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     */
    public ShowClassMembersInNavigatorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of OpenFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ShowClassMembersInNavigatorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(ShowClassMembersInNavigatorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    //@Override
    public void prepare() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    //@Override
    public ComponentOperator open() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return null;
    }

        class PhaseHandler extends Handler {
            
            public boolean published = false;

            public void publish(LogRecord record) {

            if (record.getMessage().startsWith("ClassMemberPanelUI refresh took:")){
                navigTime=record.getMessage().split(" ");
                next=true;
            }

            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
            
        }

    PhaseHandler phaseHandler=new PhaseHandler();

    
    public void testOpening20kBJavaFile(){

        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Main20kB.java";
        anotherFileName = "Main.java";
        menuItem = OPEN;

        TIMER=Logger.getLogger(org.netbeans.modules.java.navigation.ClassMemberPanelUI.class.getName()+".perf");
        TIMER.setLevel(Level.FINE);
        TIMER.addHandler(phaseHandler);
        openNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
        openAnotherNode=new Node(new SourcePackagesNode(fileProject), filePackage + '|' + anotherFileName);

        openNode.select(); while (!next) {};
        d.name = "Show class members in navigator";
        d.value = new Long(navigTime[3]);
        d.unit = "ms";
        d.threshold=1000;

        long[] result=new long[2];
        result[1]=d.value;
        String pass="failed";
        if (d.value<=1000) pass="passed";
        CommonUtilities.xmlTestResults(System.getProperty("nbjunit.workdir"), "UI Responsiveness J2SE Actions suite", d.name, "org.netbeans.performance.j2se.actions.ShowClassMembersInNavigatorTest" , "org.netbeans.performance.j2se.MeasureJ2SEActionsTest", d.unit, pass, 1000, result, 1);
        openAnotherNode.select();
    }

}
