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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of popup menu on nodes in Projects View.
 * @author  mmirilovic@netbeans.org
 */
public class VWProjectsViewPopupMenu extends ProjectsViewPopupMenu {

    /** Creates a new instance of ProjectsViewPopupMenu */
    public VWProjectsViewPopupMenu(String testName) {
        super(testName);
    }

    /** Creates a new instance of ProjectsViewPopupMenu */
    public VWProjectsViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new VWProjectsViewPopupMenu("testVWProjectNodePopupMenuProjects", "VW Project node popup in Projects View"));
        suite.addTest(new VWProjectsViewPopupMenu("testVWApplicationBeanPopupMenuProjects", "Application Bean node popup in Projects View"));
        return suite;
    }
    
    public void testVWProjectNodePopupMenuProjects() {
        testNode(getProjectNode("HugeApp"));
    }
    
    public void testVWApplicationBeanPopupMenuProjects(){
        testNode(new Node(getProjectNode("HugeApp"), "Application Bean")); // NOI18N
    }
    

}
