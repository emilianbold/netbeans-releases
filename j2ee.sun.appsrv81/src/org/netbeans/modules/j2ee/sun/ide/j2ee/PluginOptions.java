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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.sun.ide.Installer;

/**
 *
 * @author Andrei Badea
 */
public class PluginOptions extends SystemOption {
    private static final long serialVersionUID = 6035345535764474326L;
    
    public String displayName() {
        return Installer.getDeploymentFactory().getDisplayName();
    }
    
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx ("AS_RTT_Plugin"); //NOI18N
    } 
    
    public String[] getUserList() {
        return PluginProperties.getDefault().getUserList();
    }
    
    public void setUserList(String [] list) {
        PluginProperties.getDefault().setUserList(list);
    }
    
    public String[] getGroupList() {
        return PluginProperties.getDefault().getGroupList();
    }
    
    public void setGroupList(String [] list) {
        PluginProperties.getDefault().setGroupList(list);
    }
    
    public String getLogLevel() {
        return PluginProperties.getDefault().getLogLevel();
    }
    
    public void setLogLevel(String ll) {
        PluginProperties.getDefault().setLogLevel(ll);
    }
    
    public Integer getCharsetDisplayPreference() {
        return PluginProperties.getDefault().getCharsetDisplayPreference();
    }
    
    public void setCharsetDisplayPreference(Integer displayPreference) {
        PluginProperties.getDefault().setCharsetDisplayPreference(displayPreference);
    }
    
    public Boolean getIncrementalDeploy(){
        return PluginProperties.getDefault().getIncrementalDeploy();
    }
    
    public void setIncrementalDeploy(Boolean b) {
        PluginProperties.getDefault().setIncrementalDeploy(b);
    }
    public File getInstallRoot(){
        return PluginProperties.getDefault().getInstallRoot();
    }
    
    public void setInstallRoot(File f) {
        File current= getInstallRoot();
        
        if ((current==null)||(!f.getAbsolutePath().equals(current.getAbsolutePath()))){
            PluginProperties.getDefault().setInstallRoot(f);
            
        }
        
    }
    public void readExternal(java.io.ObjectInput in){
        //do nothing, we use a propertie file storage
        
    }
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        //do nothing
    }

}
