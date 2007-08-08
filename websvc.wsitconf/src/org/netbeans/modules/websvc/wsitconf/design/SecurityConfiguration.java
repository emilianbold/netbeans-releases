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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Grebac
 */
public class SecurityConfiguration implements WSConfiguration {
  
    private Service service;
    private DataObject implementationFile;
    private Project project;
    
    private ServiceModel serviceModel;
    private ServiceChangeListener scl;

    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private Binding binding;
    
    private ComponentListener cl;
    
    /** Creates a new instance of WSITWsConfiguration */

    public SecurityConfiguration(Service service, FileObject implementationFile) {
        try {
            this.service = service;
            this.implementationFile = DataObject.find(implementationFile);
            this.project = FileOwnerQuery.getOwner(implementationFile);
            this.serviceModel = ServiceModel.getServiceModel(implementationFile);
            setListener();
            this.cl = new ComponentListener() {

                private void update() {
                    boolean enabled = SecurityPolicyModelHelper.isSecurityEnabled(binding);
                    for (PropertyChangeListener pcl : listeners) {
                        PropertyChangeEvent pce = new PropertyChangeEvent(SecurityConfiguration.this, WSConfiguration.PROPERTY, null, enabled);
                        pcl.propertyChange(pce);
                    }
                }

                public void valueChanged(ComponentEvent evt) {
                    update();
                }

                public void childrenAdded(ComponentEvent evt) {
                    update();
                }

                public void childrenDeleted(ComponentEvent evt) {
                    update();
                }
            };
            if (binding != null) {
                binding.getModel().addComponentListener(cl);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
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
  
    private void setListener() {
        if (scl == null) {
            scl = new  WsitServiceChangeListener(service, implementationFile.getPrimaryFile(), project);
        }
        if ((scl != null) && (serviceModel != null)) {
            serviceModel.addServiceChangeListener(scl);
        }
    }
    
    public boolean isSet() {
        boolean set = false;
        this.binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, false, null);
        if (binding != null) {
            set = SecurityPolicyModelHelper.isSecurityEnabled(binding);
        }
        return set;
    }
        
    public void set() {
        binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, true, null);
        if (binding == null) return;

        if (!(SecurityPolicyModelHelper.isSecurityEnabled(binding))) {
            
            // default profile with the easiest setup
            ProfilesModelHelper.setSecurityProfile(binding, ComboConstants.PROF_MUTUALCERT);
            
            // enable secure conversation by default - better performance, and no need to hassle with RM set/unset
            ProfilesModelHelper.enableSecureConversation(binding, true, ComboConstants.PROF_MUTUALCERT);
            
            Util.fillDefaults(project, false);
            ProfilesModelHelper.setServiceDefaults(ComboConstants.PROF_MUTUALCERT, binding, project);

            WSITModelSupport.save(binding);
        }
    }

    public void unset() {
        if (binding == null) return;
        if (SecurityPolicyModelHelper.isSecurityEnabled(binding)) {
            SecurityPolicyModelHelper.disableSecurity(binding, true);
            WSITModelSupport.save(binding);
        }
    }

    public void registerListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void unregisterListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    protected void finalize() {
        if ((scl != null) && (serviceModel != null)) {
            serviceModel.removeServiceChangeListener(scl);
        }
        if (binding != null) {
            binding.getModel().removeComponentListener(cl);
        }
    }

    public boolean isEnabled() {
        boolean enabled =  false;
        if ((implementationFile == null) || (!implementationFile.isValid())) {
            return false;
        }
        Node n = implementationFile.getNodeDelegate();
        JaxWsModel model = project.getLookup().lookup(JaxWsModel.class);
        enabled = !SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(n, model);
        return enabled;
    }
}
