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

package org.netbeans.modules.j2ee.deployment.execution;

import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.tests.j2eeserver.devmodule.*;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author nn136682
 */
public class ServerExecutorTest extends NbTestCase {

    public ServerExecutorTest(java.lang.String testName) {
        super(testName);
    }

    private static ServerExecutorTest instance;
    private static ServerExecutorTest instance() {
        if (instance == null) 
            instance = new ServerExecutorTest("testNothing");
        return instance;
    }
    
    public void testNothing() {
    }
    
    static private DeployTarget dt;
    static private LocalFileSystem wfs;
    static FileSystem getWorkFileSystem() {
        if (wfs != null)
            return wfs;
        try {
            File workdir = instance().getWorkDir();
            wfs = new LocalFileSystem();
            wfs.setRootDirectory(workdir);
            return wfs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
        
    public static DeployTarget getDeploymentTarget(ServerString targetServer) {
        if (dt != null) {
            dt.setServer(targetServer);
            return dt;
        }
        
        try {
            FileObject testJar = FileUtil.createData(getWorkFileSystem().getRoot(),"test.jar");
            dt = new DeployTarget(new TestJ2eeModule(J2eeModule.EJB, testJar), targetServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dt;
    }
    
    public static class DeployTarget implements DeploymentTarget {
        J2eeModule j2eeMod;
        ServerString target;
        
        /** Creates a new instance of TestDeployTarget */
        public DeployTarget(J2eeModule mod, ServerString target) {
            j2eeMod = mod;
            this.target = target;
        }
        
        public boolean doFastDeploy() {
            return false;
        }
        
        public boolean dontDeploy() {
            return false;
        }
        
        public java.io.File getConfigurationFile() {
            return null;
        }
        
        public org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule getModule() {
            return j2eeMod;
        }
        
        public org.netbeans.modules.j2ee.deployment.impl.ServerString getServer() {
            return target;
        }
        public void setServer(ServerString server) { this.target = server; }
        
        public TargetModule[] getTargetModules() {
            TargetModule.List l = readTargetModule(getName());
            if (l == null)
                return new TargetModule[0];
            return  l.getTargetModules();
        }
        
        public void setTargetModules(TargetModule[] targetModules) {
            System.out.println("-------------SETTARGETMODULES: name="+getName()+","+Arrays.asList(targetModules));
                writeTargetModule(getName(),  new TargetModule.List(targetModules));
        }
        
        public void startClient() {
        }
        public void startClient(String clientURL) {
            
        }
        
        String name;
        public String getName() {
            try {
            if (name == null) {
                String serverName = this.getServer().getUrl();
                serverName = serverName.substring(serverName.lastIndexOf(':')+1);
                name = serverName + getModule().getArchive().getName();
            }
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            return name;
        }
        
        public String getDeploymentName() {
            return null;
        }

        public String getClientUrl(String partUrl) {
            return null;
        }
        
        public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter() {
            return null;
        }
        
        public org.netbeans.modules.j2ee.deployment.execution.DeploymentConfigurationProvider getDeploymentConfigurationProvider() {
            return null;
        }
        
        public org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.ConfigSupport getConfigSupport() {
            return null;
        }
        
    }
    
    public static boolean writeTargetModule(String destFile, TargetModule.List tml) {
        FileLock lock = null;
        Writer writer = null;
        try {
            if (tml == null)
                return true;
            
            FileObject fo = FileUtil.createData(getWorkFileSystem().getRoot(), destFile+".xml");
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock));
            TargetModuleConverter.create().write(writer, tml);
            return true;
            
        } catch(Exception ioe) {
            throw new RuntimeException(ioe);
        }
        finally {
            try {
            if (lock != null) lock.releaseLock();
            if (writer != null) writer.close();
            } catch (Exception e) {}
        }
    }

    public static TargetModule.List readTargetModule(String fromFile) {
        Reader reader = null;
        try {
            FileObject dir = getWorkFileSystem().getRoot();
            FileObject fo = dir.getFileObject (fromFile, "xml");
            if (fo == null) {
                System.out.println(Thread.currentThread()+ " readTargetModule: Can't get FO for "+fromFile+".xml from "+dir.getPath());
                return null;
            }
            reader = new InputStreamReader(fo.getInputStream());
            return (TargetModule.List) TargetModuleConverter.create().read(reader);
        } catch(Exception ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
            if (reader != null) reader.close();
            } catch (Exception e) {}
        }
    }
}
