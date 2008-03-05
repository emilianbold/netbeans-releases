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

import gui.action.ExpandNodesWebProjectsView;
import gui.action.JavaCompletionInJspEditor;
import gui.action.PageUpPageDownInJspEditor;
import gui.action.PasteInJspEditor;
import gui.action.ToggleBreakpoint;
import gui.action.TypingInJspEditor;
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
        suite.addTest(new PasteInJspEditor("index.jsp", "measureTime",
            "Paste in the JSP Editor"));
        suite.addTest(new PasteInJspEditor("BigJSP.jsp", "measureTime",
            "Paste in the JSP Editor with large file"));
        suite.addTest(new PageUpPageDownInJspEditor("Test.jsp", "measureTime",
            "Press Page Up in the JSP Editor", true));
        suite.addTest(new PageUpPageDownInJspEditor("Test.jsp", "measureTime",
            "Press Page Down in the JSP Editor", false));
        suite.addTest(new PageUpPageDownInJspEditor("BigJSP.jsp", "measureTime",
            "Press Page Up in the JSP Editor with large file", true));
        suite.addTest(new PageUpPageDownInJspEditor("BigJSP.jsp", "measureTime",
            "Press Page Down in the JSP Editor with large file", false));
        suite.addTest(new TypingInJspEditor("Test.jsp", 12, "measureTime",
            "Type a character in JSP Editor"));
        suite.addTest(new TypingInJspEditor("BigJSP.jsp", 12, "measureTime",
            "Type a character in JSP Editor with large file"));
        suite.addTest(new ToggleBreakpoint("Test.jsp", "measureTime",
            "Toggle Breakpoint"));
        suite.addTest(new JavaCompletionInJspEditor("testScriptletCC",
            "Invoke Code Completion dialog inside jsp scriptlet"));
        suite.addTest(new JavaCompletionInJspEditor("testScriptletCC",
            "Invoke Code Completion dialog inside jsp scriptlet II"));
        suite.addTest(new JavaCompletionInJspEditor("testExpressionCC",
            "Invoke Code Completion dialog inside jsp expression"));
        suite.addTest(new JavaCompletionInJspEditor("testDeclarationCC",
            "Invoke Code Completion dialog inside jsp declaration"));
        suite.addTest(new JavaCompletionInJspEditor("testAllTags",
            "Invoke Code Completion dialog after <"));
        suite.addTest(new JavaCompletionInJspEditor("testTagAttribute1",
            "Invoke Code Completion dialog after <%@page"));
        suite.addTest(new JavaCompletionInJspEditor("testTagAttribute2",
            "Invoke Code Completion dialog after <jsp:useBean"));
        suite.addTest(new JavaCompletionInJspEditor("testAttributeValue2",
            "Invoke Code Completion dialog after <%@include file="));
        suite.addTest(new JavaCompletionInJspEditor("testAttributeValue3",
            "Invoke Code Completion dialog after <jsp:useBean scope="));
        suite.addTest(new JavaCompletionInJspEditor("testAttributeValue5",
            "Invoke Code Completion dialog after <jsp:getProperty name=bean " +
            "property="));
        suite.addTest(new JavaCompletionInJspEditor("testAttributeValue6",
            "Invoke Code Completion dialog after <%@taglib tagdir="));
        suite.addTest(new JavaCompletionInJspEditor("testAttributeValue1",
            "Invoke Code Completion dialog after <%@page import="));
        suite.addTest(new JavaCompletionInJspEditor("testAttributeValue4",
            "Invoke Code Completion dialog after <jsp:useBean class="));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandProjectNode",
            "Expand Project node"));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandSourcePackagesNode",
            "Expand Source Packages node"));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandFolderWith50JspFiles",
            "Expand folder with 50 JSP files"));
        suite.addTest(new ExpandNodesWebProjectsView("testExpandFolderWith100JspFiles",
            "Expand folder with 100 JSP files"));
        
        return suite;
    }
    
}
