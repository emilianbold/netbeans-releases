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

/*
 * OutputTaskTest.java
 * JUnit based test
 *
 * Created on 09 November 2005, 18:40
 */
package org.netbeans.mobility.antext;

import junit.framework.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lukas
 */
public class OutputTaskTest extends NbTestCase {
    
    public OutputTaskTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    static Test suite() {
        TestSuite suite = new TestSuite(OutputTaskTest.class);
        
        return suite;
    }

    /**
     * Test of execute method, of class org.netbeans.mobility.antext.OutputTask.
     */
    public void testExecute()  throws IOException
    {
        System.out.println("execute");
        
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();        
        File output   =File.createTempFile("output","txt",dir);
        File test1    =getGoldenFile("test1.txt");
        File test2    =getGoldenFile("test2.txt");
        String sep=System.getProperty("line.separator");
        
        Project p=new Project();
        OutputTask instance = new OutputTask();
        instance.setEncoding(System.getProperty("file.encoding"));
        instance.setProject(p);
        instance.setFile(output);
        instance.addText("test1");        
        instance.execute();
        assertFile(output,test1);
        instance.setAppend(true);
        instance.setText(sep+"test2");        
        instance.execute();
        assertFile(output,test2);
        instance.setAppend(false);
        instance.setText("test1");        
        instance.execute();
        assertFile(output,test1);
        instance.setText("test1");        
        instance.addText(sep+"test2");        
        instance.execute();
        assertFile(test2,output); 
        clearWorkDir(); 
    }
}
