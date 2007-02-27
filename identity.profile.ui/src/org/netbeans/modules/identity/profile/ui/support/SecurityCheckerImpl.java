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
package org.netbeans.modules.identity.profile.ui.support;

import org.openide.nodes.Node;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
//import org.netbeans.modules.websvc.wsitconf.spi.SecurityChecker;
import org.openide.util.NbBundle;

/**
 *
 * @author PeterLiu
 */
public class SecurityCheckerImpl { //extends SecurityChecker {
    
    private boolean isTransientStateSet = false;
    
    private boolean isSecurityEnabled = false;
    
    /** Creates a new instance of SecurityCheckerImpl */
    public SecurityCheckerImpl() {
    }
    
    
    /**
     * Returns display name to be presented in UI.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(SecurityCheckerImpl.class, "LBL_DisplayName");
    }
    
    /**
     * Should return true if technology security means represented by this checker are
     * enabled for service or client represented by passed node and jaxWsModel
     */
    public boolean isSecurityEnabled(Node node, JaxWsModel jaxWsModel) {
        if (!isTransientStateSet) {
            J2eeProjectHelper helper = new J2eeProjectHelper(node, jaxWsModel);
            
            if (helper.isAppServerSun() && helper.isSecurable()) {
                isSecurityEnabled = helper.isSecurityEnabled();
                
                //System.out.println("helper.isSecurityEnabled() = " + isSecurityEnabled);
                
                // Turn on the transient state flag so we don't need to use
                // the J2eeProjectHelper next time.
                isTransientStateSet = true;
            } else {
                isSecurityEnabled = false;
            }
        } else {
            //System.out.println("transient isSecurityEnabled = " + isSecurityEnabled);
        }
        
        return isSecurityEnabled;
    }
    
    public void setTransientState(boolean isEnabled) {
        isTransientStateSet = true;
        isSecurityEnabled = isEnabled;
    }
    
    public void clearTransientState() {
        isTransientStateSet = false;
    }
}
