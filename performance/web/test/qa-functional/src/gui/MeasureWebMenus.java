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
