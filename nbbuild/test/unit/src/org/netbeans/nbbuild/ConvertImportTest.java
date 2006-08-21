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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 *
 * @author pzajac
 */
public class ConvertImportTest extends NbTestCase {
    private File testFile;
    public ConvertImportTest(java.lang.String testName) {
        super(testName);
    }

    
    public void testConvertImport() throws IOException {
       String xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       String xmlOut =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../templates/xtest-unit.xml\"/>\n" +
           "</project>";

       String xmlOutPrefix =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"${test.dist.dir}/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       
       createFile(xml);
       
       ConvertImport convert = new ConvertImport();
       convert.setFile(testFile); 
       convert.setOldName("templates/xtest-unit.xml");
       convert.setNewPath("../templates/xtest-unit.xml");
       convert.execute();
       assertNewXml(xmlOut);
       
       createFile(xml);
       convert.setPropertyPrefixName("test.dist.dir");
       convert.setNewPath("templates/xtest-unit.xml");
       convert.execute();
       assertNewXml(xmlOutPrefix); 
 
        xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<!-- <import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n-->" +
           "</project>";
       createFile(xml);
       convert.execute();
       assertNewXml(xml);
 
    }

    private File createFile(String xml) throws IOException {
       testFile = new File(getWorkDir(),"testFile.xml");
       PrintStream ps = new PrintStream(testFile);
       ps.print(xml);
       ps.close();
       return testFile;
    }

    private void assertNewXml(String xmlOut) throws IOException {
        File file = new File(getWorkDir(),"ref.xml");
        PrintStream ps = new PrintStream(file);
        ps.print(xmlOut);
        assertFile(testFile,file);
    }
}
