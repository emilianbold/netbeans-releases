/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.ErrorManager;

/** DeploymentManager that can deploy to 
 * Tomcat 5 using manager application.
 *
 * @author  Radim Kubacki
 */
public class TomcatManager implements DeploymentManager {

    /** Enum value for get*Modules methods. */
    private static final int ENUM_AVAILABLE = 0;
    
    /** Enum value for get*Modules methods. */
    private static final int ENUM_RUNNING = 1;
    
    /** Enum value for get*Modules methods. */
    private static final int ENUM_NONRUNNING = 2;
    
    
    /** Manager state. */
    private boolean connected;
    
    /** uri of this DeploymentManager. */
    private String uri;
    
    /** Username used for connecting. */
    private String username;
    /** Password used for connecting. */
    private String password;
    
    /** Creates a new instance of TomcatManager */
    public TomcatManager (boolean connected, String uri, String uname, String passwd) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("Creating TomcatManager uri="+uri+", uname="+uname+(connected?" (connected)":" (disconnected)"));
        }
        this.connected = connected;
        this.uri = uri;
        username = uname;
        password = passwd;
    }
    
    public DeploymentConfiguration createConfiguration (DeployableObject deplObj) 
    throws InvalidModuleException {
        if (!ModuleType.WAR.equals (deplObj.getType ())) {
            throw new InvalidModuleException ("Only WAR modules are supported for TomcatManager"); // NOI18N
        }
        
        return new WebappConfiguration (deplObj);
    }
    
    public Locale getCurrentLocale () {
        return Locale.getDefault ();
    }
    
    public Locale getDefaultLocale () {
        return Locale.getDefault ();
    }
    
    public Locale[] getSupportedLocales () {
        return Locale.getAvailableLocales ();
    }
    
    public boolean isLocaleSupported (Locale locale) {
        if (locale == null) {
            return false;
        }
        
        Locale [] supLocales = getSupportedLocales ();
        for (int i =0; i<supLocales.length; i++) {
            if (locale.equals (supLocales[i])) {
                return true;
            }
        }
        return false;
    }
    
    public TargetModuleID[] getAvailableModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_AVAILABLE, moduleType, targetList);
    }
    
    public TargetModuleID[] getNonRunningModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_RUNNING, moduleType, targetList);
    }
    
    public TargetModuleID[] getRunningModules (ModuleType moduleType, Target[] targetList) 
    throws TargetException, IllegalStateException {
        return modules (ENUM_NONRUNNING, moduleType, targetList);
    }
    
    /** Utility method that retrieve the list of J2EE application modules 
     * distributed to the identified targets.
     * @param state     One of available, running, non-running constants.
     * @param moduleType    Predefined designator for a J2EE module type.
     * @param targetList    A list of deployment Target designators.
     */
    private TargetModuleID[] modules (int state, ModuleType moduleType, Target[] targetList)
    throws TargetException, IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.modules called on disconnected instance");   // NOI18N
        }
        if (targetList.length != 1) {
            throw new TargetException ("TomcatManager.modules supports only one target");   // NOI18N
        }
        
        if (!ModuleType.WAR.equals (moduleType)) {
            return new TargetModuleID[0];
        }
        
        // PENDING 
        return null;
    }
    
    
    public Target[] getTargets () throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.getTargets called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return new TomcatTarget [] { 
            new TomcatTarget (uri, "Tomcat at "+uri)
        };
    }
    
    public DConfigBeanVersionType getDConfigBeanVersion () {
        // PENDING 
        return null;
    }
    
    public void setDConfigBeanVersion (DConfigBeanVersionType version) 
    throws DConfigBeanVersionUnsupportedException {
        if (!DConfigBeanVersionType.V1_3_1.equals (version)) {
            throw new DConfigBeanVersionUnsupportedException ("unsupported version");
        }
    }
    
    public boolean isDConfigBeanVersionSupported (DConfigBeanVersionType version) {
        return DConfigBeanVersionType.V1_3_1.equals (version);
    }
    
    public boolean isRedeploySupported () {
        // PENDING
        return false;
    }
    
    public ProgressObject redeploy (TargetModuleID[] targetModuleID, InputStream inputStream, InputStream inputStream2) 
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }
    
    public ProgressObject redeploy (TargetModuleID[] targetModuleID, File file, File file2) 
    throws UnsupportedOperationException, IllegalStateException {
        // PENDING
        throw new UnsupportedOperationException ("TomcatManager.redeploy not supported yet.");
    }
    
    public void release () {
    }
    
    public void setLocale (Locale locale) throws UnsupportedOperationException {
    }
    
    public ProgressObject start (TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.start called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return null;
    }
    
    public ProgressObject stop (TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.stop called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return null;
    }
    
    public ProgressObject undeploy (TargetModuleID[] targetModuleID) throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.undeploy called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return null;
    }
    
    public ProgressObject distribute (Target[] targets, InputStream moduleArchive, InputStream deplPlan) 
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return null;
    }
    
    public ProgressObject distribute (Target[] targets, File moduleArchive, File deplPlan) 
    throws IllegalStateException {
        if (!isConnected ()) {
            throw new IllegalStateException ("TomcatManager.distribute called on disconnected instance");   // NOI18N
        }
        
        // PENDING 
        return null;
    }
    
    private boolean isConnected () {
        return connected;
    }
}
