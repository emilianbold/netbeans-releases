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

import java.io.IOException;

import org.netbeans.jellytools.Bundle;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.guitracker.ActionTracker;

/**
 * Test of Find Usages
 *
 * @author  mmirilovic@netbeans.org
 */
public class SearchTest extends PerformanceTestCase {
    
    private Node testNode;
    private NbDialogOperator dlgSearch;
    JButtonOperator btn_Find;
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public SearchTest(String testName) {
        super(testName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }
    
    /** Creates a new instance of RefactorFindUsagesDialog */
    public SearchTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(SearchTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testSetupProject() {

       CommonUtilities.jEditProjectOpen();
        try {
            this.openDataProjects("jEdit41");
        } catch (IOException ex) {
        }
    }

    public void testFindInProjects_Text() {
        doMeasurement();
    }    
    
    @Override
    public void initialize() {
        testNode = new Node(new SourcePackagesNode("jEdit"), new SourcePackagesNode("jEdit").getChildren()[0]);
    }
    
    
    public void prepare() {
        testNode.select();
        MainWindowOperator.getDefault().menuBar().pushMenu(Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle","Menu/Edit") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "LBL_Action_FindInProjects"),"|");

        dlgSearch = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "LBL_FindInProjects"));
        dlgSearch.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60*1000);

        JComboBoxOperator  searchPattern = new JComboBoxOperator(dlgSearch,  0);
        searchPattern.typeText("100%NoSuchTextExists");

        btn_Find = new JButtonOperator(dlgSearch, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle","TEXT_BUTTON_SEARCH"));
        
    }
    
    public ComponentOperator open() {
        btn_Find.push();
        JComponentOperator srch_wnd = new JComponentOperator(MainWindowOperator.getDefault(), new NameComponentChooser(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle","TITLE_SEARCH_RESULTS")));

        JTreeOperator tree = new JTreeOperator(srch_wnd);
        tree.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 5*60*1000);

        Node results = new Node(tree, tree.getPathForRow(0));

        while (!results.getText().contains(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_MSG_NO_NODE_FOUND"))) 
            results = new Node(tree, tree.getPathForRow(0));

        return null;
    }

    @Override
    public void close() {
    }

}
