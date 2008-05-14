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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.menu.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureJ2EEMenusTest  {

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

// workaround. JAX-RPC support is a separate plugin, this test just closes warning dialog
//        suite.addTest(new gui.setup.EJBSetupTest("testJAXRPC"));
               
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
