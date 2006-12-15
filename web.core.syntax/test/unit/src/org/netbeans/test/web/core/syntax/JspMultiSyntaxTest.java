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

package org.netbeans.test.web.core.syntax;

import java.io.PrintStream;
import java.net.URL;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.editor.ext.java.JavaSyntax;
import org.netbeans.junit.NbTestCase;

import org.netbeans.modules.web.core.syntax.deprecated.Jsp11Syntax;
import org.openide.ErrorManager;

/** Basic jsp multisyntax parser tests.
 *
 * @author  mf100882
 */
public class JspMultiSyntaxTest extends NbTestCase {
    
    //it's static since the junit creates a new instance of this class for each test method
    private static Jsp11Syntax syntax = new Jsp11Syntax(new HTMLSyntax(), new JavaSyntax());
    
    public JspMultiSyntaxTest() {
        super("jspmultisyntaxtest");
    }
    
    public void setUp() {
        //print out a header to the ref file
        getRef().println("'token image' [offset, length]; tokenID name; tokenID id; token category name; <list of token context names>\n--------------------\n");
    }
    
    public void tearDown() {
        compareReferenceFiles();
    }
    
    //test methods -----------
    
    public void testHtml() {
        dumpTokensForContent("<html>\n<body>\n<h1>hello</h1>\n</body>\n</html>");
    }
    
    public void testJavaScripting() {
        dumpTokensForContent("<html><%! int a = 1; %>\n\n<br>\n<%=\"hello\"%>\n<br>\n<% String s = \"world\"; %>\n</html>");
    }

    public void testJspDeclaration() {
        dumpTokensForContent("<%@page contentType=\"text/html\"%>\n"+
                             "<%@page pageEncoding=\"UTF-8\"%>" + 
                             "<%@taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%>");
    }
    
    public void testExpressionLanguage() {
        dumpTokensForContent("<html>${pageContext.request.contextPath}\n<br>" + 
                             "${pageContext.request.contextPath}\n " +
                             "${header[\"host\"]} <br>\n" + 
                             "${requestScope['javax.servlet.forward.servlet_path']}\n</html>");
    }
    
    public void testBug53102() {
        dumpTokensForContent("<html>\n<head />\n<he");
    }
     
    public void testBug52942() {
        dumpTokensForContent("<a href=\"<%= 1 %>\"  >Destination</a>");
    }
    
    public void testBugWrongJsptagType() {
        dumpTokensForContent("\n<a >\n");
    }
    
    public void test50283_1() {
        dumpTokensForContent("< /jsp:element >"); //should be marked as an error
    }
     
    public void test50283_2() {
        dumpTokensForContent("</ jsp:element >"); //should be marked as an error
    }
    
    public void testJspComment() {
        dumpTokensForContent("<html><%-- text \n new line --%></html>\n");
    }
    
    public void testSimpleJspTag() {
        dumpTokensForContent("</jsp:useBean id=\"sss\">");
    }
    
    //helper methods -----------
    
    private void dumpTokensForContent(String content) {
        loadContentToSyntax(content);
        dumpTokensData(getRef()); //print output to reference stream
    }
    
    private void dumpTokensData(PrintStream out) {
        TokenID tokenID = null;
        char[] buffer = syntax.getBuffer();
        String tokenImage = null;
        TokenContextPath tcp = null;
        do {
            //acquire all token relevant data
            tokenID = syntax.nextToken();
            
            if( tokenID == null ) break;
            
            tokenImage = new String(buffer, syntax.getTokenOffset(), syntax.getTokenLength());
            tcp = syntax.getTokenContextPath();
            
            //print it
            out.print("'" + SyntaxUtils.normalize(tokenImage) + "' ["+syntax.getTokenOffset() + ", " + syntax.getTokenLength() + "]; " + tokenID.getName() + "; " + tokenID.getNumericID() + "; "+ (tokenID.getCategory() != null ? tokenID.getCategory().getName() : "-") + "; ");
            SyntaxUtils.dumpTokenContextPath(tcp, out);
            out.println();
            
        }
        while(true);
    }
    
    private void loadContentToSyntax(String content) {
        //load syntax - scan the whole buffer - the buffer is last one
        char[] buffer = content.toCharArray();
        syntax.load(null, buffer, 0, buffer.length, true);
    }
    
}
