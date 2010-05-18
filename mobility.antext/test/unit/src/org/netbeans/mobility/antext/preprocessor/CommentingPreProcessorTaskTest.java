/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
