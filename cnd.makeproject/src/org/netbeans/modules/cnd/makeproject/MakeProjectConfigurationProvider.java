/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
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

package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;

public class MakeProjectConfigurationProvider implements ProjectConfigurationProvider, PropertyChangeListener {
    private final Project project;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public MakeProjectConfigurationProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }

    public Collection getConfigurations() {
        return projectDescriptorProvider.getConfigurationDescriptor().getConfs().getConfsAsCollection();
    }

    public ProjectConfiguration getActiveConfiguration() {
        return projectDescriptorProvider.getConfigurationDescriptor().getConfs().getActive();
    }

    public void setActiveConfiguration(ProjectConfiguration configuration) throws IllegalArgumentException, IOException {
        if (configuration instanceof Configuration)
            projectDescriptorProvider.getConfigurationDescriptor().getConfs().setActive((Configuration)configuration);
    }

    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
        projectDescriptorProvider.getConfigurationDescriptor().getConfs().addPropertyChangeListener(this);
    }

    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
        projectDescriptorProvider.getConfigurationDescriptor().getConfs().removePropertyChangeListener(this);
    }

    public boolean hasCustomizer() {
        return false;
    }

    public void customize() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public boolean configurationsAffectAction(String command) {
        return false;
        /*
        return command.equals(ActionProvider.COMMAND_RUN) ||
        command.equals(ActionProvider.COMMAND_BUILD) ||
        command.equals(ActionProvider.COMMAND_CLEAN) ||
        command.equals(ActionProvider.COMMAND_DEBUG);
        */
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        assert pcs != null;
        
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
        /*
        if (evt.getNewValue() != evt.getOldValue()) {
            ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider) p.getLookup().lookup(ConfigurationDescriptorProvider.class );
            if (pdp != null)
                pdp.getConfigurationDescriptor().setModified();
        }
        */
    }
}
