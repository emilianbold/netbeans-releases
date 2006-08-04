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
 * RunTaskTest.java
 * JUnit based test
 *
 * Created on 10 November 2005, 14:39
 */
package org.netbeans.mobility.antext;

import java.io.FileOutputStream;
import java.io.PrintStream;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lukas
 */
public class RunTaskTest extends NbTestCase {
    
    final String DEFAULT_PLATFORM_TYPE = "UEI-1.0"; 
        
    
    public RunTaskTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    static Test suite() {
        TestSuite suite = new TestSuite(RunTaskTest.class);
        
        return suite;
    }

    
   
    /**
     * Test of execute method, of class org.netbeans.mobility.antext.RunTask.
     */
    public void testExecute() throws IOException
    {
        System.out.println("execute");
        
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();                
        String path    =getGoldenFile("bin").getParent()+File.separator;
        File srcDir    =new File(path);
        File jadfile   =new File(path+"MobileApplication1.jad");
        File jarfile   =new File(path+"MobileApplication1.jad");
        File f=File.createTempFile("cmdline","",dir);
        File log=null;
        PrintStream out=new PrintStream(new FileOutputStream(f));
        File em=null;
        File exe=null;
        try
        {
            Project p=new Project();
            RunTask instance = new RunTask();
            instance.setProject(p);
            instance.setClassPath(new Path(p,srcDir.getAbsolutePath()));
            instance.setPlatformHome(srcDir);
            instance.setJadFile(jadfile);
            instance.setJarFile(jarfile);
            instance.setJadUrl(jadfile.toURL().toString());
            instance.setDevice("MyDevice");
            instance.setDebug(true);
            instance.setDebugAddress("MyAddress");
            instance.setDebugServer(true);
            instance.setDebugSuspend(false);

            String name=System.getProperty("os.name");
            StringBuffer commandLine = new StringBuffer(Bundle.getMessage("CMD_Run_" + DEFAULT_PLATFORM_TYPE));

            /* Prepare right executable */
            int index=name.indexOf("Windows");            
            em=index!=-1?getGoldenFile("bin/emulator.bin"):getGoldenFile("bin/emulator.sh");
            exe=new File(em.getParent()+"/emulator");
            exe.delete();                
            em.renameTo(exe);                

            HashMap args = new HashMap ();
            args.put ("platformhome", srcDir); // No I18N
            args.put ("classpath", srcDir.getAbsolutePath()); // No I18N
            args.put ("jadfile",jadfile);
            args.put ("jarfile",jarfile);
            args.put ("jadurl",jadfile.toURL().toString());
            args.put ("device","MyDevice");
            args.put ("debug", ""); // NO I18N
            args.put ("debugaddress","MyAddress");
            args.put ("debugserver", "y"); // NO I18N
            args.put ("debugsuspend", "n"); // NO I18N
            args.put ("debugtransport", "dt_socket"); // NO I18N
            args.put ("/", File.separator); // NO I18N
            String line = EMapFormat.format(commandLine.toString(), args);

            String[] commands = Commandline.translateCommandline (line);
            if (name.indexOf("Windows")==-1)
            {
                Process pr=java.lang.Runtime.getRuntime().exec("chmod +x "+commands[0]);
                try {
                    assertTrue(pr.waitFor()==0);
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            for (int i=0;i<commands.length;i++)
            {
                out.println(commands[i]);
            }           
            instance.execute();
            String wdir=System.getProperty("user.dir");
            log=new File(wdir+File.separator+"cmdLine.log");
            this.assertFile(log,f);
        }
        finally
        {
            out.close();
            clearWorkDir();
            if (f!=null) f.delete();
            if (log!=null) log.delete();
            if (exe!=null) exe.renameTo(em);
        }
    }
    
    /**
     * Test of execute method, of class org.netbeans.mobility.antext.RunTask.
     */
    public void testExecuteList() throws IOException
    {
        System.out.println("executeList");
        
        //Prepare test
        File dir=getWorkDir();
        clearWorkDir();                
        String path    =getGoldenFile("bin").getParent()+File.separator;
        File srcDir    =new File(path);
        File jadfile   =new File(path+"MobileApplication.jad");
        File jarfile   =new File(path+"MobileApplication.jar");
        File f1=File.createTempFile("cmdline1","",dir);
        File f2=File.createTempFile("cmdline2","",dir);
        File f3=File.createTempFile("cmdline3","",dir);
        File log=null;
        PrintStream out1=new PrintStream(new FileOutputStream(f1));
        PrintStream out2=new PrintStream(new FileOutputStream(f2));
        PrintStream out3=new PrintStream(new FileOutputStream(f3));
        File em=null;
        File exe=null;
        try
        {
            Project p=new Project();
            RunTask instance = new RunTask();
            instance.setProject(p);
            instance.setClassPath(new Path(p,srcDir.getAbsolutePath()));
            instance.setPlatformHome(srcDir);
            instance.setJadFile(jadfile);
            instance.setJarFile(jarfile);
            instance.setJadUrl(jadfile.toURL().toString());
            instance.setDevice("MyDevice");
            instance.setDebug(true);
            instance.setDebugAddress("MyAddress");
            instance.setDebugServer(true);
            instance.setDebugSuspend(false);
            instance.setExecMethod("OTA");
            instance.setCmdOptions("option");

            /* Prepare right executable */
            String name=System.getProperty("os.name");
            int index=name.indexOf("Windows");            
            em=index!=-1?getGoldenFile("bin/emulator.bin"):getGoldenFile("bin/emulator.sh");
            exe=new File(em.getParent()+"/emulator");
            exe.delete();                
            em.renameTo(exe);                
            
            HashMap args = new HashMap ();
            args.put ("platformhome", srcDir); // No I18N
            args.put ("classpath", srcDir.getAbsolutePath()); // No I18N
            args.put ("jadfile",jadfile);
            args.put ("jarfile",jarfile);
            args.put ("jadurl",jadfile.toURL().toString());
            args.put ("device","MyDevice");
            args.put ("debug", ""); // NO I18N
            args.put ("debugaddress","MyAddress");
            args.put ("debugserver", "y"); // NO I18N
            args.put ("debugsuspend", "n"); // NO I18N
            args.put ("debugtransport", "dt_socket"); // NO I18N
            args.put ("cmdoptions", "option"); // NO I18N
            args.put ("/", File.separator); // NO I18N
            
            /* Main emulator */
            StringBuffer commandLine = new StringBuffer(Bundle.getMessage("CMD_Run_" + DEFAULT_PLATFORM_TYPE + "_" + "OTA"));            
            String line = EMapFormat.format(commandLine.toString(), args);
            String[] commands = Commandline.translateCommandline (line);
            if (name.indexOf("Windows")==-1)
            {
                Process pr=java.lang.Runtime.getRuntime().exec("chmod +x "+commands[0]);
                try {
                    assertTrue(pr.waitFor()==0);
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            for (int i=0;i<commands.length;i++)
            {
                out1.println(commands[i]);
            }

            /* OTA List */
            commandLine = new StringBuffer(Bundle.getMessage("CMD_Run_" + DEFAULT_PLATFORM_TYPE + "_" + "OTA" + "_List"));
            line = EMapFormat.format(commandLine.toString(), args);
            commands = Commandline.translateCommandline (line);
            if (name.indexOf("Windows")==-1)
            {
                Process pr=java.lang.Runtime.getRuntime().exec("chmod +x "+commands[0]);
                try {
                    assertTrue(pr.waitFor()==0);
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            for (int i=0;i<commands.length;i++)
            {
                out2.println(commands[i]);
            }
            
            /* OTA Run */
            commandLine = new StringBuffer(Bundle.getMessage("CMD_Run_" + DEFAULT_PLATFORM_TYPE + "_" + "OTA" + "_Run"));
            line = EMapFormat.format(commandLine.toString(), args);
            commands = Commandline.translateCommandline (line);
            if (name.indexOf("Windows")==-1)
            {
                Process pr=java.lang.Runtime.getRuntime().exec("chmod +x "+commands[0]);
                try {
                    assertTrue(pr.waitFor()==0);
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            for (int i=0;i<commands.length;i++)
            {
                out3.println(commands[i]);
            }
            
            instance.execute();
            String wdir=System.getProperty("user.dir");
            log=new File(wdir+File.separator+"cmdLine.log");
            this.assertFile(log,f1);
            log.delete();
            log=new File(wdir+File.separator+"cmdLine.loglis");           
            this.assertFile(log,f2);
            log.delete();
            log=new File(wdir+File.separator+"cmdLine.logrun");
            this.assertFile(log,f3);
            log.delete();
        }
        finally
        {
            out1.close();
            out2.close();
            out3.close();
            clearWorkDir();
            if (f1!=null) f1.delete();
            if (f2!=null) f1.delete();
            if (f3!=null) f1.delete();
            if (log!=null) log.delete();
            if (exe!=null) exe.renameTo(em);
        }
    }

}
