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
 * ExtractTaskTest.java
 * JUnit based test
 *
 * Created on 18 October 2005, 18:45
 */
package org.netbeans.modules.j2me.common.ant;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import junit.framework.*;
import org.netbeans.junit.NbTestCase
        
;/**
 *
 * @author Lukas Waldmann
 */public class ExtractTaskTest extends NbTestCase {
    
    public ExtractTaskTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    static Test suite() {
        TestSuite suite = new TestSuite(ExtractTaskTest.class);
        
        return suite;
    }

    /**
     * Test of execute method without any parameter set, of class org.netbeans.mobility.antext.ExtractTask.
     */
    public void testExecuteNoParams() {
        System.out.println("execute - no parameters set");
        
        ExtractTask instance = new ExtractTask();
        
        try
        {
            instance.execute();
        }
        catch (BuildException ex)
        {
           return; 
        }
        
        fail("Execute run without correct parameters");
    }

    /**
     * Test of setClassPath method, of class org.netbeans.mobility.antext.ExtractTask.
     */
    public void testSetClassPath() {
        System.out.println("setClassPath");
        
        ExtractTask instance = new ExtractTask();
        Path classPath = new Path(instance.getProject(),"/tmp");
                
        instance.setClassPath(classPath);
        
        try
        {
            instance.execute();
        }
        catch (BuildException ex)
        {
            if (ex.getMessage().indexOf("classPath") != -1)
                fail("Classpath was not set");
        }
        
        
    }
    

    /**
     * Test of setDir method, of class org.netbeans.mobility.antext.ExtractTask.
     */
    public void testSetDir() throws IOException
    {
        System.out.println("setDir");
        
        File dir = getWorkDir();
        Project p=new Project();
        ExtractTask instance = new ExtractTask();
        
        Path classPath = new Path(instance.getProject(),dir.getAbsolutePath());                
        instance.setClassPath(classPath);        
        instance.setDir(dir);
        instance.setProject(p);
        try
        {
            instance.execute();
        }
        catch (BuildException ex)
        {
            if (ex.getMessage().indexOf("\"dir\"") != -1)
                fail("Directory was not set");
        }
    }
    
    /**
     * Test of setDir method with incorrect directory, of class org.netbeans.mobility.antext.ExtractTask.
     */
    public void testSetNotExistingDir() {
        System.out.println("setNotExisitingDir");
        
        File dir = new File("FGHFGHFGH.FGHF^%&%^&JTGHJ");
        ExtractTask instance = new ExtractTask();
        
        Path classPath = new Path(instance.getProject(),"/tmp");                
        instance.setClassPath(classPath);        
        instance.setDir(dir);
        
        try
        {
            instance.execute();
        }
        catch (BuildException ex)
        {
            if (ex.getMessage().indexOf("Invalid target") == -1)
                fail("Check for not existing directory failed");
        }
    }
    
    private void  rmdir(File dir)
    {
        if (dir.isDirectory())
        {
            File list[]=dir.listFiles();
            for (int i=0;i<list.length;i++)
            {
                if (list[i].isDirectory()) rmdir(list[i]);
                                    else   list[i].delete();
            }
            dir.delete();
        }
    }

    private AbstractCollection filelist(File dir, AbstractCollection flist,int prefix)
    {
        if (dir.isDirectory())
        {
            File list[]=dir.listFiles();
            for (int i=0;i<list.length;i++)
            {
                if (list[i].isDirectory()) filelist(list[i],flist,prefix);
                flist.add(list[i].getPath().substring(prefix).replace('/','\\'));
            }
        }
        return flist;
    }
    
    
    private boolean compareFile(AbstractCollection list,File input)
    {
        try
        {
            BufferedReader reader=new BufferedReader(new FileReader(input));
            while (reader.ready())
            {
                String line=reader.readLine();
                if (!list.remove(line)) 
                    return false;
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return list.size()==0;
    }
    
    
    /**
     * Test of extraction, of class org.netbeans.mobility.antext.ExtractTask.
     */
    public void testExtract() throws IOException
    {
        System.out.println("setExtract");
        
        //Prepare test
        File f=null;
        File dir=getWorkDir();
        clearWorkDir();        
        File jar    =getGoldenFile("test.jar");
        File zip    =getGoldenFile("test.zip");
        File comp   =getGoldenFile("contents.txt");
        File dirlist=getGoldenFile("dirlist.txt");

        //Here we go
        try
        {     
            
            Project p=new Project();
            ExtractTask instance = new ExtractTask();

            instance.setProject(p);
            
            //Jar and zip test
            Path classPath = new Path(p,jar.getAbsolutePath());               
            instance.setClassPath(classPath);
            classPath = new Path(p,zip.getAbsolutePath());
            instance.setClassPath(classPath);        
            instance.setDir(dir);

            instance.execute();           
            AbstractCollection list=filelist(dir,new ArrayList(),dir.getPath().length());
            assertTrue(compareFile(list,comp));
            clearWorkDir();
            
            //Directory test
            instance = new ExtractTask();
            instance.setProject(p);
            instance.setDir(dir);
            classPath = new Path(p,zip.getParent());
            instance.setClassPath(classPath);
            
            instance.execute();           
            list=filelist(dir,new ArrayList(),dir.getPath().length());
            assertTrue(compareFile(list,dirlist));    
            clearWorkDir();
        }
        finally
        {
            clearWorkDir();
            rmdir(dir);
            if (f!=null) f.delete();
        }
    }
    
}
