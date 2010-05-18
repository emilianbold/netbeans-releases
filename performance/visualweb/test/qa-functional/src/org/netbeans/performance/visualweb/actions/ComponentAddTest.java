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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.performance.visualweb.PaletteComponentOperator;
import org.netbeans.performance.visualweb.WebFormDesignerOperator;
import org.netbeans.performance.visualweb.setup.VisualWebSetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.visualweb.VWPUtilities;

import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class ComponentAddTest extends PerformanceTestCase {

    private PaletteComponentOperator palette;
    private WebFormDesignerOperator surface;
    private String categoryName;
    private String componentName;

    /**
     * Creates a new instance of ComponentAddTest
     * 
     * @param testName 
     * 
     */
    public ComponentAddTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }     
    /**
     * Creates a new instance of ComponentAddTest
     * 
     * @param testName 
     * @param performanceDataName
     * 
     */    
    public ComponentAddTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(ComponentAddTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
   
    public void testAddTableComponent() {
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Table"; // NOI18N
        doMeasurement();
    }
    
    public void testAddButtonComponent() {
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Button"; // NOI18N
        doMeasurement();
    }
    
    public void testAddListboxComponent() {
        categoryName = "Woodstock Basic"; // NOI18N
        componentName = "Listbox"; // NOI18N
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("UltraLargeWA");
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        
        PaletteComponentOperator.invoke();
        surface = VWPUtilities.openedWebDesignerForJspFile("UltraLargeWA", "TestPage");
    }
    
    public void prepare() {
        palette = new PaletteComponentOperator();
        palette.getCategoryListOperator(categoryName).selectItem(componentName);
      }
    
    public ComponentOperator open() {
        surface.clickOnSurface(20,20);
        return null;
    }
    
    @Override
    public void close() {
    }
    
    
    @Override
    public  void shutdown() {

        try {
            new CloseViewAction().performMenu(new EditorOperator("TestPage"));
            NbDialogOperator dop = new NbDialogOperator("Question"); // NOI18N
            JButtonOperator but = new JButtonOperator(dop,"Discard"); // NOI18N
            but.push();
        } catch(Exception exc){
            // do nothing
        }
    }
}
