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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.design;

import java.awt.Component;
import java.awt.Image;
import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Grebac
 */
public class SecurityConfiguration implements WSConfiguration {
  
    private Service service;
    private FileObject implementationFile;
    private Project project;
    
    private ServiceModel serviceModel;
    private ServiceChangeListener scl;
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
    
    /** Creates a new instance of WSITWsConfiguration */

    public SecurityConfiguration(Service service, FileObject implementationFile) {
        this.service = service;
        this.implementationFile = implementationFile;
        this.project = FileOwnerQuery.getOwner(implementationFile);
        this.serviceModel = ServiceModel.getServiceModel(implementationFile);
    }

    public Component getComponent() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(SecurityConfiguration.class, "DesignConfigPanel.Security");
    }

    public Image getIcon() {
        return Utilities.loadImage("org/netbeans/modules/websvc/wsitconf/resources/designer-security.gif");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SecurityConfiguration.class, "DesignConfigPanel.Security");
    }
  
    public boolean isSet() {
        boolean set = false;
        Binding binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
        if (binding != null) {
            set = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        }
        if (set && (scl == null)) {
            scl = new  WsitServiceChangeListener(binding);
            serviceModel.addServiceChangeListener(scl);
        }
        return set;
    }
        
    public void set() {
        Binding binding = WSITModelSupport.getBinding(service, implementationFile, project, true, createdFiles);
        
        if (binding == null) return;
        if (!(SecurityPolicyModelHelper.isSecurityEnabled(binding))) {

            if (scl == null) {
                scl = new  WsitServiceChangeListener(binding);
            }
            serviceModel.addServiceChangeListener(scl);
            
            // default profile with the easiest setup
            ProfilesModelHelper.setSecurityProfile(binding, ComboConstants.PROF_MUTUALCERT);
            
            // enable secure conversation by default - better performance, and no need to hassle with RM set/unset
            ProfilesModelHelper.enableSecureConversation(binding, true, ComboConstants.PROF_MUTUALCERT);
            
            // setup default keystore values
            ProprietarySecurityPolicyModelHelper.setStoreLocation(binding, Util.getServerStoreLocation(project, false), false, false);
            ProprietarySecurityPolicyModelHelper.setKeyStoreAlias(binding, "xws-security-server", false); //NOI18N
            ProprietarySecurityPolicyModelHelper.setStorePassword(binding, "changeit", false, false); //NOI18N

            // setup default truststore values
            ProprietarySecurityPolicyModelHelper.setStoreLocation(binding, Util.getServerStoreLocation(project, true), false, false);
            ProprietarySecurityPolicyModelHelper.setTrustPeerAlias(binding, "xws-security-client", false); //NOI18N
            ProprietarySecurityPolicyModelHelper.setStorePassword(binding, "changeit", true, false); //NOI18N

            WSITModelSupport.save(binding);
        }
    }

    public void finalize() {
        if (scl != null) {
            serviceModel.removeServiceChangeListener(scl);
        }
    }
    
    public void unset() {
        if (scl != null) {
            serviceModel.removeServiceChangeListener(scl);
        }
        
        Binding binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
        if (binding == null) return;
        if (SecurityPolicyModelHelper.isSecurityEnabled(binding)) {
            SecurityPolicyModelHelper.disableSecurity(binding, true);
            WSITModelSupport.save(binding);
        }
    }

}

