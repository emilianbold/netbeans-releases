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

package gui;

import gui.menu.WebProjectsViewPopupMenu;
import gui.menu.WebRuntimeViewPopupMenu;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureWebMenus extends NbTestCase {
    
    private MeasureWebMenus(String name) {
        super(name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(); 
        suite.addTest(new WebProjectsViewPopupMenu("testProjectNodePopupMenuProjects",
                "Project node popup in Projects View"));
        //suite.addTest(new WebProjectsViewPopupMenu("testProjectNodePopupMenuProjects",
        //"Project node popup in Projects View II"));
        suite.addTest(new WebProjectsViewPopupMenu("testSourcePackagesPopupMenuProjects",
                "Source Packages node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testPackagePopupMenuProjects",
                "Package node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testServletPopupMenuProjects",
                "Servlet node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testWebPagesPopupMenuProjects",
                "Web Pages node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testJspFilePopupMenuProjects",
                "JSP File node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testWebInfPopupMenuProjects",
                "WEB-INF node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testMetaInfPopupMenuProjects",
                "META-INF node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testWebXmlFilePopupMenuProjects",
                "Web.xml node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testContextXmlFilePopupMenuProjects",
                "Context.xml node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testHtmlFilePopupMenuProjects",
                "HTML node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testTagFilePopupMenuProjects",
                "Tag file node popup in Projects View"));
        suite.addTest(new WebProjectsViewPopupMenu("testTldPopupMenuProjects",
                "TLD node popup in Projects View"));
        suite.addTest(new WebRuntimeViewPopupMenu("testServerRegistryPopupMenuRuntime",
                "Servers node popup in Runtime View"));
/*        suite.addTest(new WebRuntimeViewPopupMenu("testTomcatPopupMenuRuntime",
                "Tomcat node popup in Runtime View"));
        suite.addTest(new WebRuntimeViewPopupMenu("testWebModulesPopupMenuRuntime",
                "Tomcat's Web Modules node popup in Runtime View"));
        suite.addTest(new WebRuntimeViewPopupMenu("testWebModulePopupMenuRuntime",
                "Tomcat's one Web Module node popup in Runtime View"));
*/        
        return suite;
    }
    
}
