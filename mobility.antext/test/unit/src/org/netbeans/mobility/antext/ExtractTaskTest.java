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
 * ExtractTaskTest.java
 * JUnit based test
 *
 * Created on 18 October 2005, 18:45
 */
package org.netbeans.mobility.antext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
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
