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

import gui.action.OpenServletFile;
import gui.action.OpenServletFileWithOpenedEditor;
import gui.action.OpenWebFiles;
import gui.action.OpenWebFilesWithOpenedEditor;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureWebDialogs extends NbTestCase {
    
    private MeasureWebDialogs(String name) {
        super(name);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new OpenWebFiles("testOpeningTagFile", "Open Tag file"));
        suite.addTest(new OpenServletFile("testOpeningJavaFile", "Open Java file"));
        suite.addTest(new OpenServletFile("testOpeningServletFile", "Open Servlet file"));
        //suite.addTest(new OpenServletFile("testOpeningServletFile", "Open Servlet file II"));
        suite.addTest(new OpenWebFiles("testOpeningWebXmlFile", "Open web.xml file"));
        suite.addTest(new OpenWebFiles("testOpeningContextXmlFile", "Open context.xml file"));
        //suite.addTest(new OpenWebFiles("testOpeningBigJSPFile", "Open Big JSP file"));
        suite.addTest(new OpenWebFiles("testOpeningHTMLFile", "Open HTML file"));
        suite.addTest(new OpenWebFiles("testOpeningTldFile", "Open TLD file"));

        suite.addTest(new OpenServletFileWithOpenedEditor("testOpeningJavaFile", "Open Java file if Editor opened"));
        suite.addTest(new OpenServletFileWithOpenedEditor("testOpeningServletFile", "Open Servlet file if Editor opened"));
        suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningWebXmlFile", "Open web.xml file if Editor opened"));
        suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningContextXmlFile", "Open context.xml file if Editor opened"));
        suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningJSPFile", "Open JSP file if Editor opened"));
        //suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningBigJSPFile", "Open Big JSP file if Editor opened"));
        suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningHTMLFile", "Open HTML file if Editor opened"));
        suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningTagFile", "Open Tag file if Editor opened"));
        suite.addTest(new OpenWebFilesWithOpenedEditor("testOpeningTldFile", "Open TLD file if Editor opened"));
        suite.addTest(new OpenWebFiles("testOpeningJSPFile", "Open JSP file"));        
        return suite;
    }
    
}
