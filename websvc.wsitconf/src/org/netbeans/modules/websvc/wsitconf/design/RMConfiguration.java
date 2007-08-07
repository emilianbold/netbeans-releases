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

package org.netbeans.modules.websvc.wsitconf.design;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Grebac
 */
public class RMConfiguration implements WSConfiguration {
  
    private Service service;
    private DataObject implementationFile;
    private Project project;
    
    private ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
    private Binding binding;
    
    private ComponentListener cl;
    
    /** Creates a new instance of WSITWsConfiguration */

    public RMConfiguration(Service service, FileObject implementationFile) {
        try {
            this.service = service;
            this.implementationFile = DataObject.find(implementationFile);
            this.project = FileOwnerQuery.getOwner(implementationFile);
            this.binding = WSITModelSupport.getBinding(service, implementationFile, project, false, createdFiles);
            this.cl = new ComponentListener() {

                private void update() {
                    boolean enabled = RMModelHelper.isRMEnabled(binding);
                    for (PropertyChangeListener pcl : listeners) {
                        PropertyChangeEvent pce = new PropertyChangeEvent(RMConfiguration.this, WSConfiguration.PROPERTY, null, enabled);
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
        return NbBundle.getMessage(RMConfiguration.class, "DesignConfigPanel.rmCB.text");
    }
    
    public Image getIcon() {
        return Utilities.loadImage("org/netbeans/modules/websvc/wsitconf/resources/designer-rm.gif");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(RMConfiguration.class, "DesignConfigPanel.rmCB.text");
    }
  
    public boolean isSet() {
        if (binding != null) {
            return RMModelHelper.isRMEnabled(binding);
        }
        return false;
    }
        
    public void set() {
        binding = WSITModelSupport.getBinding(service, implementationFile.getPrimaryFile(), project, true, createdFiles);
        if (binding == null) return;
        binding.getModel().addComponentListener(cl);
        if (!(RMModelHelper.isRMEnabled(binding))) {
            RMModelHelper.enableRM(binding);
            WSITModelSupport.save(binding);            
        }
    }

    public void unset() {
        if (binding == null) return;
        if (RMModelHelper.isRMEnabled(binding)) {
            RMModelHelper.disableRM(binding);
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
        if (binding != null) {
            binding.getModel().removeComponentListener(cl);
        }
    }
    
    public boolean isEnabled() {
        return true;
    }
}
