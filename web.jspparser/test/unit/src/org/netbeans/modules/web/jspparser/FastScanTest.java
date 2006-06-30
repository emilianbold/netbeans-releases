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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** JUnit test suite with Jemmy support
 *
 * @author pj97932
 * @version 1.0
 */
public class FastScanTest extends NbTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FastScanTest(String testName) {
        super(testName);
    }
    
    public void testPage1() throws Exception {
        doFastScanTest("jspparser-data/wmroot", "subdir/Page1.jsp", new JspParserAPI.JspOpenInfo(false, "ISO-8859-1"));
        
    }
    
    public void testXMLFromExamples1() throws Exception {
        doFastScanTest("project3/web", "xml/xml.jsp", new JspParserAPI.JspOpenInfo(true, "UTF-8"));
    }
    
    public void testXMLFromExamples2() throws Exception {
        doFastScanTest("project3/web", "jsp2/jspx/basic.jspx", new JspParserAPI.JspOpenInfo(true, "UTF-8"));
    }
    
    public void doFastScanTest(String wmRootPath, String path, JspParserAPI.JspOpenInfo correctInfo) throws Exception {
        try{
            FileObject wmRoot = TestUtil.getFileInWorkDir(wmRootPath, this);
            StringTokenizer st = new StringTokenizer(path, "/");
            FileObject tempFile = wmRoot;
            String ss;
            while (st.hasMoreTokens()) {
                tempFile = tempFile.getFileObject(st.nextToken());
            }
            parseIt(wmRoot, tempFile, correctInfo);
        }catch(RuntimeException e){
            e.printStackTrace();
            e.printStackTrace(getRef());
            fail("Initialization of test failed! ->" + e);
        }
    }
    
    private void parseIt(FileObject root, FileObject jspFile, JspParserAPI.JspOpenInfo correctInfo) throws Exception {
        log("calling parseIt, root: " + root + "  file: " + jspFile);
        JspParserAPI api = JspParserFactory.getJspParser();
        JspParserAPI.JspOpenInfo info = api.getJspOpenInfo(jspFile, TestUtil.getWebModule(jspFile), false);
        log("file: " + jspFile + "   enc: " + info.getEncoding() + "   isXML: " + info.isXmlSyntax());
        assertEquals(correctInfo, info);
    }
    
}
