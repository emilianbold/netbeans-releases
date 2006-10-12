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

package org.netbeans.modules.j2ee.sun.ide.j2ee;
import java.util.ResourceBundle;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;

/**
 *
 * @author Andrei Badea
 */
public class PluginOptions extends SystemOption {
    private static final long serialVersionUID = 6035345535764474326L;

    public String displayName() {
        return  ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.ide.dm.Bundle").getString("FACTORY_DISPLAYNAME");	// NOI18N);
    }

    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx ("AS_RTT_Plugin"); //NOI18N
    } 
    
    // !PW Disable visibility of these properties for now.  Edit them via the
    // security-role-mapping customizer in any project.
//    public String[] getUserList() {
//        return PluginProperties.getDefault().getUserList();
//    }
//    
//    public void setUserList(String [] list) {
//        PluginProperties.getDefault().setUserList(list);
//       firePropertyChange("displayPreference",null, list);
//    }
//    
//    public String[] getGroupList() {
//        return PluginProperties.getDefault().getGroupList();
//    }
//    
//    public void setGroupList(String [] list) {
//        PluginProperties.getDefault().setGroupList(list);
//       firePropertyChange("groupList",null, list);
//    }
    
    public String getLogLevel() {
        return PluginProperties.getDefault().getLogLevel();
    }
    
    public void setLogLevel(String ll) {
        PluginProperties.getDefault().setLogLevel(ll);
       firePropertyChange("logLevel",null, ll);
    }
    
    public Integer getCharsetDisplayPreference() {
        return PluginProperties.getDefault().getCharsetDisplayPreference();
    }
    
    public void setCharsetDisplayPreference(Integer displayPreference) {
        PluginProperties.getDefault().setCharsetDisplayPreference(displayPreference);
        firePropertyChange("charsetDisplayPreference",null, displayPreference);
    }
    
    public Boolean getIncrementalDeploy(){
        return PluginProperties.getDefault().getIncrementalDeploy();
    }
    
    public void setIncrementalDeploy(Boolean b) {
        PluginProperties.getDefault().setIncrementalDeploy(b);
        firePropertyChange("IncrementalDeploy",null, b);
    }

    public void readExternal(java.io.ObjectInput in){
        //do nothing, we use a propertie file storage
        
    }
    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        //do nothing
    }

}
