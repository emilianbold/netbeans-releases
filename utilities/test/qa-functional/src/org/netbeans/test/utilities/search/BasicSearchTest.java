/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * BasicSearchTest.java
 *
 * Created on November 30, 2006, 3:31 PM
 *
 */

package org.netbeans.test.utilities.search;

import javax.swing.plaf.metal.MetalComboBoxEditor;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.utilities.operators.SearchResultsOperator;
import org.netbeans.test.utilities.testcase.Utilities;

/**
 * Some search tests
 * @author Max Sauer
 */
public class BasicSearchTest extends NbTestCase {
    /** Create test Dialog label */
    protected static final String FIND_DIALOG = Bundle.getString(
            "org.netbeans.modules.search.Bundle", "TEXT_TITLE_CUSTOMIZE");
    
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.utilities.basicsearch";
    
    /** Creates a new instance of BasicSearchTest */
    public BasicSearchTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(BasicSearchTest.class);
        return suite;
    }
    
    public void testSearchForClass() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, Utilities.SRC_PACKAGES_PATH);
        n.select();
        Utilities.takeANap(1000);
        Utilities.pushFindPopup(n);
        
        NbDialogOperator ndo = new NbDialogOperator(FIND_DIALOG);
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(ndo, 0);
        jtpo.selectPage(0);
        
        JComboBoxOperator jcbo = new JComboBoxOperator(ndo,
                new JComboBoxOperator.JComboBoxFinder());
        jcbo.enterText("class"); //enter 'class' in search comboBox and press [Enter]

        SearchResultsOperator sro = new SearchResultsOperator();
        assertTrue("Junit Output window should be visible", sro.isVisible());
        System.out.println("## Search output opened");
        Utilities.takeANap(1000);
        sro.close();
        assertFalse("Search window is visible," +
                "should be closed", sro.isShowing());
    }
    
    
}
