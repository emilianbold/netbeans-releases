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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Properties;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 *
 * @author pzajac
 */
public class ShorterPathsTest extends NbTestCase {
    
    public ShorterPathsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public void testShorterPaths () throws Exception {
      // create test
      File wd = getWorkDir(); 
      File modules = new File(wd,"modules");
      modules.mkdirs();
      File module = new File(modules,"module.jar");
      module.createNewFile();
      File extlib = new File(wd,"extlib.jar");
      File extraLibsDir = new File (wd,"extralibs");
      File testProperties = new File (wd,"outtest.properties");
      extraLibsDir.mkdirs();
      
      PrintStream ps = new PrintStream(extlib); 
      ps.println("content");
      ps.close();
      
      
      PublicPackagesInProjectizedXMLTest.execute ("ShorterPathsTest.xml", new String[] {"-verbose",  
                                                                                        "-Dtest.ext.lib=" + extlib.getPath(),
                                                                                        "-Dtest.modules.dir=" + modules.getPath(),
                                                                                        "-Dextra.test.libs.dir=" + extraLibsDir.getPath(),
                                                                                        "-Dtest.properties=" + testProperties.getPath(),
                                                                                        "all"});
      File extralibCopy = new File(extraLibsDir,"extlib.jar");
      
      assertTrue("No extra library has been copied",extralibCopy.exists());
      BufferedReader reader = new BufferedReader(new FileReader(extralibCopy));
      assertEquals("Different content in copy of extra library:","content",reader.readLine());

      Properties props = new Properties();
      FileInputStream propsIs = new FileInputStream(testProperties);
      props.load(propsIs);
      propsIs.close();
      assertEquals("extra.test.libs.dir","${extra.test.libs}/extlib.jar",props.getProperty("extra.test.libs.dir"));
      assertEquals("test.unit.run.cp","${nb.root.test.dir}/module.jar",props.getProperty("test.unit.run.cp"));
      assertEquals("test-unit-sys-prop.prop1","value1",props.getProperty("test-unit-sys-prop.prop1"));
      assertEquals("test-unit-sys-prop.prop2","${nb.root.test.dir}/module.jar",props.getProperty("test-unit-sys-prop.prop2"));
      assertNull(props.getProperty("test-unit-sys-prop.xtest.data"));       
      assertEquals("props.size()",4,props.size());
      
      
      // test dist 
    }    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ShorterPathsTest.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}

    
    
    
}
