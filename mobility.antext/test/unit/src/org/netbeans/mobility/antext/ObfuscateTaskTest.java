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
 * ObfuscateTaskTest.java
 * JUnit based test
 *
 * Created on 15 November 2005, 15:08
 */
package org.netbeans.mobility.antext;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lukas
 */
public class ObfuscateTaskTest extends NbTestCase {
    
    public ObfuscateTaskTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    static Test suite() {
        TestSuite suite = new TestSuite(ObfuscateTaskTest.class);
        
        return suite;
    }
    
    /**
     * Test of execute method, of class org.netbeans.mobility.antext.ObfuscateTask.
     */
    public void testExecuteProguard()  throws IOException
    {
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();
        File jarfile   =getGoldenFile("MobileApplication.jar");
        File destfile  =new File(dir+File.separator+"MobileApplication.jar");
        String className=ObfuscateTaskTest.class.getName();
        String srcPath=Manager.normalizeFile(new File(ObfuscateTaskTest.class.getResource(className.substring(className.lastIndexOf('.')+1)+".class").getFile())).getAbsolutePath();
                
        int pos=srcPath.indexOf("mobility");
        String dd=srcPath.substring(0,pos);
        
        try
        {
            Project p=new Project();
            ObfuscateTask instance = new ObfuscateTask();
            instance.setProject(p);
            instance.setObfuscatorType("PROGUARD");
            instance.setSrcJar(jarfile);
            instance.setDestJar(destfile);
            instance.setObfuscationLevel(9);
            instance.setExclude("exclude");
            Path path=new Path(p,dd+"/mobility/proguard/lib/proguard.jar");
            Path midp=new Path(p,getGoldenFile("emptyapi.zip").getAbsolutePath()); 
            instance.setClassPath(midp);
            instance.setObfuscatorClassPath(path);
            instance.execute();

            ZipFile     zip=new ZipFile(destfile);
            ZipEntry    zipEntry=zip.getEntry("hello/Midlet.class");

            if (zipEntry==null) 
                fail("Obfuscation failed");        

            zip.close();
        }
        finally
        {
            clearWorkDir();
        }
    }
    
    public void testExecuteNone()  throws IOException
    {
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();
        File jarfile   =getGoldenFile("MobileApplication.jar");
        File destfile  =new File(dir+File.separator+"MobileApplication.jar");
        
        try
        {
            Project p=new Project();
            ObfuscateTask instance = new ObfuscateTask();
            instance.setProject(p);
            p.setProperty("libs.none.classpath",":");
            instance.setSrcJar(jarfile);
            instance.setDestJar(destfile);
            instance.execute();

            assertFile(destfile,jarfile);
        }
        finally
        {
            destfile.delete();
        }
    }
    
    }
