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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 *
 * @author pzajac
 */
public class TestDepsTest extends NbTestCase {
    
    public TestDepsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TestDepsTest.class);
        return suite;
    }
    
    
    public void testDepsTest () throws Exception {
      // create test
      File projectxml = extractFile(TestDepsTest.class.getResourceAsStream("TestDepsProject.xml"),"project.xml");  
      PublicPackagesInProjectizedXMLTest.execute ("TestDeps.xml", new String[] { "-verbose", "-Dproject.file=" + projectxml, "all" });
             
      // test dist 
    }

    private File extractFile(InputStream is, String fileName) throws IOException {
        File f = new File(getWorkDir(),fileName);
        byte bytes[] = new byte[50000];
        FileOutputStream fos = new FileOutputStream(f);
        int len = is.read(bytes);
        fos.write(bytes,0,len);
        fos.close();
        return f;
    }
    
    
}
