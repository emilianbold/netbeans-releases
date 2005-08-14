/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4;


import java.io.FileInputStream;
import java.util.jar.JarFile;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.modules.j2ee.jboss4.ide.JBDeploymentStatus;
import org.netbeans.modules.j2ee.jboss4.ide.JBJ2eePlatformFactory;
import org.netbeans.modules.j2ee.jboss4.ide.JBLogWriter;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBInstantiatingIterator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import org.w3c.dom.*;

/**
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentManager implements DeploymentManager {
    
    private DeploymentManager dm;
    private String uri;
    private String realUri;
    private String domain="";
    
    private String host;
    private int port;
    private int debuggingPort = 8787;
    
    // ide specific data
    private JBLogWriter logWriter;
    
    /** Creates a new instance of JBDeploymentManager */
    public JBDeploymentManager(DeploymentManager dm, String uri, String username, String password) {
        realUri = uri;
        this.dm = dm;
        if (uri.indexOf("#")!=-1){//NOI18N
            this.uri = uri.substring(0, uri.indexOf("#") );//NOI18N
            domain = uri.substring(uri.indexOf("#") +1);//NOI18N
        } else{
            this.uri = uri;
        }
        
        this.host = this.uri.substring(this.uri.indexOf(':') + 1, this.uri.lastIndexOf(':'));
        this.port = new Integer(this.uri.substring(this.uri.lastIndexOf(':')+1)).intValue();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Connection data methods
    ////////////////////////////////////////////////////////////////////////////
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getDebuggingPort() {
        return debuggingPort;
    }
    
    public String getUrl() {
        if (domain.equals(""))
            return this.uri;
        else
            return this.uri+"#"+this.domain;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // IDE data methods
    ////////////////////////////////////////////////////////////////////////////
    public JBLogWriter getLogWriter() {
        return this.logWriter;
    }
    
    public void setLogWriter(JBLogWriter logWriter) {
        this.logWriter = logWriter;
    }

    ////////////////////////////////////////////////////////////////////////////
    // DeploymentManager Implementation
    ////////////////////////////////////////////////////////////////////////////
    public ProgressObject distribute(Target[] target, File file, File file2) throws IllegalStateException {
        return new JBDeployer(realUri).deploy(target, file, file2, getHost(), getPort());
    }
    
    public DeploymentConfiguration createConfiguration(DeployableObject deployableObject) throws InvalidModuleException {
        return new JBDeploymentConfiguration(deployableObject);
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) throws UnsupportedOperationException, IllegalStateException {
        return dm.redeploy(targetModuleID, inputStream, inputStream2);
    }
    
    public ProgressObject distribute(Target[] target, InputStream inputStream, InputStream inputStream2) throws IllegalStateException {
        return dm.distribute(target, inputStream, inputStream2);
    }
    
    public ProgressObject undeploy(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return dm.undeploy(targetModuleID);
    }
    
    public ProgressObject stop(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return dm.stop(targetModuleID);
    }
    
    public ProgressObject start(TargetModuleID[] targetModuleID) throws IllegalStateException {
        return dm.start(targetModuleID);
    }
    
    public void setLocale(Locale locale) throws UnsupportedOperationException {
        dm.setLocale(locale);
    }
    
    public boolean isLocaleSupported(Locale locale) {
        return dm.isLocaleSupported(locale);
    }
    
    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        //return dm.getAvailableModules(moduleType, target);
        return new TargetModuleID[]{};
    }
    
    public TargetModuleID[] getNonRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        //return dm.getNonRunningModules(moduleType, target);
        return new TargetModuleID[]{};
    }
    
    public TargetModuleID[] getRunningModules(ModuleType moduleType, Target[] target) throws TargetException, IllegalStateException {
        return dm.getRunningModules(moduleType, target);
    }
    
    public ProgressObject redeploy(TargetModuleID[] targetModuleID, File file, File file2) throws UnsupportedOperationException, IllegalStateException {
        return new JBDeployer(realUri).redeploy(targetModuleID, file, file2);
    }
    
    public void setDConfigBeanVersion(DConfigBeanVersionType dConfigBeanVersionType) throws DConfigBeanVersionUnsupportedException {
        dm.setDConfigBeanVersion(dConfigBeanVersionType);
    }
    
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dConfigBeanVersionType) {
        return dm.isDConfigBeanVersionSupported(dConfigBeanVersionType);
    }
    
    public void release() {
        dm.release();
    }
    
    public boolean isRedeploySupported() {
        return dm.isRedeploySupported();
    }
    
    public Locale getCurrentLocale() {
        return dm.getCurrentLocale();
    }
    
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return dm.getDConfigBeanVersion();
    }
    
    public Locale getDefaultLocale() {
        return dm.getDefaultLocale();
    }
    
    public Locale[] getSupportedLocales() {
        return dm.getSupportedLocales();
    }
    
    public Target[] getTargets() throws IllegalStateException {
        return dm.getTargets();
    }
 
    private JBJ2eePlatformFactory.J2eePlatformImplImpl jbPlatform;
    
    public JBJ2eePlatformFactory.J2eePlatformImplImpl getJBPlatform () {
        if (jbPlatform == null) {
            jbPlatform = new JBJ2eePlatformFactory.J2eePlatformImplImpl(this);
        }
        return jbPlatform;
    }
}
