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
package org.netbeans.performance.j2se.actions;

import java.io.IOException;

import org.netbeans.jellytools.Bundle;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.SearchResultsOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of Find Usages
 *
 * @author  mmirilovic@netbeans.org
 */
public class SearchTest extends PerformanceTestCase {

    //private Node testNode;
    private NbDialogOperator dlg_Find;
    private TopComponentOperator srchResult = null;
 

    /** Creates a new instance of RefactorFindUsagesDialog */
    public SearchTest(String testName) {
        super(testName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }

    /** Creates a new instance of RefactorFindUsagesDialog */
    public SearchTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 120000; // the action has progress indication and it is expected it will last
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class).addTest(SearchTest.class).enableModules(".*").clusters(".*")));
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
//      testNode = new Node(new SourcePackagesNode("src_app"), new SourcePackagesNode("src_app").getChildren()[0]);
        new Node(new SourcePackagesNode("jEdit"), new SourcePackagesNode("jEdit").getChildren()[0]).select();
        MainWindowOperator.getDefault().menuBar().pushMenu(Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit") + "|" + Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "LBL_Action_FindInProjects"), "|");
    }

    public void prepare() {

//      testNode.select();
        
        if (srchResult != null) {
            new JButtonOperator(srchResult, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_CUSTOMIZE")).push();
        }

        dlg_Find = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "LBL_FindInProjects"));
        JComboBoxOperator searchPattern = new JComboBoxOperator(dlg_Find, 0);
        //searchPattern.typeText("100%NoSuchTextExists");
        searchPattern.typeText("static");
        new JButtonOperator(dlg_Find, Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_BUTTON_SEARCH")).push();
    }

    public ComponentOperator open() {
        SearchResultsOperator srchOp = new SearchResultsOperator();
        srchOp.waitEndOfSearch();
        /*
        final TopComponentOperator srch_wnd = new TopComponentOperator(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TITLE_SEARCH_RESULTS"));
        srch_wnd.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 5 * 60 * 1000);
        srch_wnd.getTimeouts().setTimeout("JTreeOperator.WaitNodeVisibleTimeout", 5 * 60 * 1000);

//      Dumper.dumpComponent(srch_wnd.getSource(), getLog("dump.xml"));

        srch_wnd.waitState(new ComponentChooser() {

            JTreeOperator tree = new JTreeOperator(srch_wnd);

            public boolean checkComponent(Component comp) {
                return new Node(tree, tree.getPathForRow(0)).getText().contains(Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "TEXT_MSG_NO_NODE_FOUND"));
            }

            public String getDescription() {
                return "Search window contains any result";
            }
        });
*/
        return srchOp; //srch_wnd;
    }

    @Override
    public void close() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            srchResult = (TopComponentOperator) testedComponentOperator;
//            srchResult.close();
/*
            if (flag) {
                JTabbedPaneOperator tpo = new JTabbedPaneOperator(srchResult);
                for (int t = tpo.getTabCount(); t>=0; t--) tpo.removeTabAt(t);
            }
            flag = true;
            srchResult.close();
 **/
        }
    }

    public void shutdown() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            srchResult = (TopComponentOperator) testedComponentOperator;
            srchResult.close();
        }
    }

}


