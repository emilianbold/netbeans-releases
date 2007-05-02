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
 */

package gui;


import gui.action.ExpandNodesWebProjectsView;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureWebActions extends NbTestCase {
    
    private MeasureWebActions(String name) {
        super(name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new ExpandNodesWebProjectsView("testExpandProjectNode", "Expand Project node"));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandSourcePackagesNode", "Expand Source Packages node"));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandFolderWith50JspFiles", "Expand folder with 50 JSP files"));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandFolderWith100JspFiles", "Expand folder with 100 JSP files"));
        
        return suite;
    }
    
}
