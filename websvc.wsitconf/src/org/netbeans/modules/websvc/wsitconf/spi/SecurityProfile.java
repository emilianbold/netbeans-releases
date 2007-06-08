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

package org.netbeans.modules.websvc.wsitconf.spi;

import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 * Security Profile
 *
 * @author Martin Grebac
 */
public abstract class SecurityProfile {
    
    public static final String CFG_KEYSTORE="cfgkeystore";
    public static final String CFG_TRUSTSTORE="cfgtruststore";
    public static final String CFG_VALIDATORS="cfgvalidators";
            
    /**
     * Returns display name to be presented in UI.
     * @return 
     */
    public abstract String getDisplayName();

    /**
     * Returns a longer description of the profile to be presented in the UI.
     * @return 
     */
    public abstract String getDescription();

    /**
     * Returns id for sorting the profiles. WSIT default profiles have ids 10, 20, 30, ... to keep space for additional profiles
     * @return 
     */
    public abstract int getId();
    
    /**
     * Called when the profile is selected in the combo box.
     * @param component 
     */
    public abstract void profileSelected(WSDLComponent component);

    /**
     * Called when there's another profile selected, or security is disabled at all.
     * @param component 
     */ 
    public abstract void profileDeselected(WSDLComponent component);

    /**
     * Should return true if the profile is supported for specific component in the wsdl
     * @param p 
     * @param component 
     * @return 
     */
    public boolean isProfileSupported(Project p, WSDLComponent component, boolean sts) {
        return true;
    }

    /**
     * Should return true if the setup in the wsdl is according to developer defaults
     */
    public boolean isClientDefaultSetupUsed(WSDLComponent component, Binding serviceBinding, Project p) {
        return false;
    }

    /**
     * Should return true if the setup in the wsdl is according to developer defaults
     */
    public boolean isServiceDefaultSetupUsed(WSDLComponent component, Project p) {
        return false;
    }

    /**
     * Should return true if the setup in the wsdl is according to developer defaults
     */
    public void setServiceDefaults(WSDLComponent component, Project p) {
    }
    
    /**
     * Should return true if the setup in the wsdl is according to developer defaults
     */
    public void setClientDefaults(WSDLComponent component, WSDLComponent serviceBinding, Project project) {
    }

    /**
     * Should return true if the profile is set on component, false otherwise
     * @param component 
     * @return 
     */
    public abstract boolean isCurrentProfile(WSDLComponent component);
    
    /**
     * Should open configuration UI and block until user doesn't close it.
     * @param component 
     * @param undoManager 
     */
    public void displayConfig(WSDLComponent component, UndoManager undoManager) { }
}
