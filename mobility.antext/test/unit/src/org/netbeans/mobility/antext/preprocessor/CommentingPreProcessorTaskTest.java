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
 * CommentingPreProcessorTaskTest.java
 * JUnit based test
 *
 * Created on 24 March 2006, 16:32
 */
package org.netbeans.mobility.antext.preprocessor;

import junit.framework.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.mobility.antext.Bundle;

/**
 *
 * @author Lukas Waldmann
 */
public class CommentingPreProcessorTaskTest extends NbTestCase
{
    
    public CommentingPreProcessorTaskTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
    }

    protected void tearDown() throws Exception
    {
    }

    static Test suite()
    {
        TestSuite suite = new TestSuite(CommentingPreProcessorTaskTest.class);
        
        return suite;
    }

    public void testJustForCoverage()
    {
        new PreprocessorException("message",null);
    }
   

    /**
     * Test of execute method, of class org.netbeans.mobility.antext.preprocessor.CommentingPreProcessorTask.
     */
    public void testExecute() throws Exception
    {
        System.out.println("execute");
        
        this.clearWorkDir();
        File workDir=getWorkDir();
        File srcDir=new File(workDir,"src");
        assertTrue(srcDir.mkdir());
        File destDir=new File(workDir,"dest");
        assertTrue(destDir.mkdir());
        File errorFile=new File(srcDir,"error.java");
        File testFile=new File(srcDir,"test.java");
        
        assertTrue(testFile.createNewFile());
        assertTrue(errorFile.createNewFile());
        FileWriter writer1=new FileWriter(testFile);
        FileWriter writer2=new FileWriter(errorFile);
        writer1.write("//#ifdef Nokia \n//# test3 \n //#elifdef SE550i \n test4 \n //#endif\n");
        writer1.close();
        writer2.write("//#if Siemens");
        writer2.close();

        Project p=new Project();
        CommentingPreProcessorTask instance = new CommentingPreProcessorTask();
   
        instance.setProject(p);
        instance.setEncoding(System.getProperty("file.encoding"));
        instance.setPreprocessFor("Series40,Nokia,ScreenWidth=128,ScreenHeight=160,MMAPI");
        instance.setSrcDir(srcDir);
        instance.setDestDir(destDir);
        try
        {
            instance.execute();
        }
        catch (BuildException ex)
        {
            //We are expecting excpetion
            assertEquals(ex.getMessage(),Bundle.getMessage("MSG_PreprocessingError"));
        }
        File errorRes=new File(destDir,"error.java");
        assertFalse(errorRes.exists());
        File testRes=new File(destDir,"test.java");
        assertTrue(testRes.exists());
        FileReader reader=new FileReader(testRes);
        char cbuf[]=new char[128];
        reader.read(cbuf);
        String str=new String(cbuf);
        str=str.substring(0,str.indexOf(0));
        assertEquals(str,"//#ifdef Nokia \ntest3 \n //#elifdef SE550i \n//#  test4 \n //#endif\n");
    }
}
