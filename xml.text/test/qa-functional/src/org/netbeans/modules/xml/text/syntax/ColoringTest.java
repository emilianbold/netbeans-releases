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
package org.netbeans.modules.xml.text.syntax;

import org.openide.execution.NbfsURLConnection;
import org.openide.loaders.DataFolder;
import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.DTDDataObject;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;

import org.netbeans.modules.xml.core.XMLDataObject;
import org.openide.nodes.CookieSet;
import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.tests.xml.XTest;
import org.openide.loaders.DataObject;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: CreateSimpleXML
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test creates simple XML document with DTD and writes it into output.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create empty XML document from template<BR>
 * 2) create new Document Type and add it into document<BR>
 * 3) append XML elements<BR>
 * 4) write the document into output<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * XML document with DTD.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on December 20, 2000, 12:33 PM
 * <P>
 */
public class ColoringTest extends XTest {
    private static String XML_TEMPLATE = "XML/XMLwithDTD.xml";
    private static String DOCUMENT_NAME = "Books";
    private static String DTD_SYS_ID = "simple.dtd";
    private static String INTERNAL_DTD = "internalDTD.dtd";
    private static int TREE_LEVELS = 3;
    
    /** Creates new CoreSettingsTest */
    public ColoringTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    public void testXMLColoring() throws Exception {
        dumpTokens("XMLColoring", "xml");
    }
    
    public void testDTDColoring() throws Exception {
        dumpTokens("DTDColoring", "dtd");
    }
    
    public void testCSSColoring() throws Exception {
        dumpTokens("CSSColoring", "css");
    }
    
    // LIBS ////////////////////////////////////////////////////////////////////
    
    public void dumpTokens(String fileName, String ext) throws Exception {
        String pkgName = getClass().getPackage().getName();
        DataObject obj = TestUtil.THIS.findDataObject(pkgName + ".data", fileName, ext);
        EditorCookie ed = (EditorCookie) obj.getCookie(EditorCookie.class);
        BaseDocument doc = (BaseDocument) ed.openDocument();
        ExtSyntaxSupport ess = (ExtSyntaxSupport) doc.getSyntaxSupport();
        TokenItem token = ess.getTokenChain(0, doc.getLength());
        
        while (token != null) {
            TokenID tokenID = token.getTokenID();
            ref(tokenID.getName()+ ": " + token.getImage());
            token = token.getNext();
        }
        compareReferenceFiles();
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(ColoringTest.class);
    }
}
