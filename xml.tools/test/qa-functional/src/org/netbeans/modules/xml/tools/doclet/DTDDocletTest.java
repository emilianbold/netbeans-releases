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
package org.netbeans.modules.xml.tools.doclet;

import junit.textui.TestRunner;
import org.netbeans.modules.xml.core.DTDDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tax.TreeDTD;
import org.netbeans.tests.xml.XTest;


/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: XMLGenerator3Test
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * DTDDocletTest checks 'Generate Documentation' action on DTD document. The action is
 * accesible from popup menu an all DTD document nodes.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test opens DTD document, generates documentation in HTML for the document and writes
 * the documentation into output.<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * DTD documentation in HTML format.<BR>
 *
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <P>
 */

public class DTDDocletTest extends XTest {
    
    /** Creates new CoreSettingsTest */
    public DTDDocletTest(String testName) {
        super(testName);
    }
    
    public void test() throws Exception {
        DTDDataObject dao = (DTDDataObject) TestUtil.THIS.findData("books.dtd");
        if (dao == null) {
            fail("\"data/books.dtd\" data object is not found!");
        }
        TreeEditorCookie cake = (TreeEditorCookie) dao.getCookie(TreeEditorCookie.class);
        TreeDTD dtd = (TreeDTD) cake.openDocumentRoot();
        DTDDoclet doclet = new DTDDoclet();
        String result = doclet.createDoclet(dtd);
        result = TestUtil.replaceString(result, "<!--", "-->", "<!-- REMOVED -->");
        ref(result);
        compareReferenceFiles();
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(DTDDocletTest.class);
    }
}
