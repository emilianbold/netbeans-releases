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


import org.netbeans.junit.NbTestSuite;
import gui.menu.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureJ2EEMenus  {

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
               
        suite.addTest(new J2EEProjectsViewPopupMenu("testEARProjectNodePopupMenu", "EAR Project node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testEARConfFilesNodePopupMenu", "EAR Configuration Files node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testApplicationXmlPopupMenu", "Application.xml node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testSunApplicationXmlPopupMenu", "Sun-application.xml node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testJ2eeModulesNodePopupMenu", "J2EE Modules node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testJ2eeModulesEJBNodePopupMenu", "EJB node under J2EE Modules popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testJ2eeModulesWebNodePopupMenu", "Web node under J2EE Modules popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testEJBProjectNodePopupMenu", "EJB Project node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testEJBsNodePopupMenu", "Enterprise Beans node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testEntityBeanNodePopupMenu", "Entity Bean node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testSessionBeanNodePopupMenu", "Session Bean node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testEjbJarXmlPopupMenu", "Ejb-jar.xml node popup in Projects View"));
        suite.addTest(new J2EEProjectsViewPopupMenu("testSunEjbJarXmlPopupMenu", "Sun-ejb-jar.xml node popup in Projects View"));
 
        suite.addTest(new AppServerPopupMenu("testAppServerPopupMenuRuntime", "AppServer node popup in Runtime View"));
                
        return suite;
    }
    
}
