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

package org.netbeans.modules.j2ee.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressUI;
import org.netbeans.modules.j2ee.deployment.impl.ui.DeployProgressMonitor;
import org.netbeans.modules.j2ee.deployment.impl.projects.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.*;
import org.apache.tools.ant.Project;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;

/**
 * Ant task that starts the server if needed and deploys module to the server
 * @author Martin Grebac
 */
public class Deploy extends Task {
    
    /**
     * Holds value of property debugmode.
     */
    private boolean debugmode = false;
    
    /**
     * Holds value of property clientUrl.
     */
    private String clientUrlPart;

    public void execute() throws BuildException { 

        J2eeModuleProvider jmp = null;
        try {
            FileObject fob = FileUtil.toFileObject(getProject().getBaseDir());
            fob.refresh(); // without this the "build" directory is not found in filesystems
            jmp = (J2eeModuleProvider) FileOwnerQuery.getOwner(fob).getLookup().lookup(J2eeModuleProvider.class);
        } catch (Exception e) {
            throw new BuildException(e);
        }

        try {
            String clientUrl = Deployment.getDefault ().deploy (jmp, debugmode, null, clientUrlPart);
            getProject().setProperty("client.url", clientUrl);
        } catch (Exception ex) {
            throw new BuildException(getBundle("MSG_DeployFailed"));
        }
    }

    private String getBundle(String key) {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ant/Bundle").getString(key); // NOI18N
    }
    
    /**
     * Getter for property debugmode.
     * @return Value of property debugmode.
     */
    public boolean getDebugmode() {
        return this.debugmode;
    }
    
    /**
     * Setter for property debugmode.
     * @param debugmode New value of property debugmode.
     */
    public void setDebugmode(boolean debugmode) {
        this.debugmode = debugmode;
    }
        
    /**
     * Getter for property clientUrl.
     * @return Value of property clientUrl.
     */
    public String getClientUrlPart() {
        return this.clientUrlPart;
    }
    
    /**
     * Setter for property clientUrl.
     * @param clientUrl New value of property clientUrl.
     */
    public void setClientUrlPart(String clientUrlPart) {
        this.clientUrlPart = clientUrlPart;
    }
    
}
