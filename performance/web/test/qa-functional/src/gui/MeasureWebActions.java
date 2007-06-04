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
        suite.addTest(new JavaCompletionInJspEditor("measureTime",
            "Invoke Code Completion dialog in JSP Editor"));
        suite.addTest(new TypingInJspEditor("Test.jsp", 12, "measureTime",
            "Type a character in JSP Editor"));
        suite.addTest(new TypingInJspEditor("BigJSP.jsp", 12, "measureTime",
            "Type a character in JSP Editor with large file"));
        suite.addTest(new TypingInJspEditor("BigJSP2.jsp", 12, "measureTime",
            "Type a character in JSP Editor at the beginning of another large file"));
        suite.addTest(new TypingInJspEditor("BigJSP2.jsp", 250, "measureTime",
            "Type a character in JSP Editor at the middle of another large file"));
        suite.addTest(new TypingInJspEditor("BigJSP2.jsp", 500, "measureTime",
            "Type a character in JSP Editor at the end of another large file"));
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
