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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.openide.WizardDescriptor;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryExtension implements IteratorExtension {
    
    /** Creates a new instance of DiscoveryExtension */
    public DiscoveryExtension() {
    }
    
    public WizardDescriptor.Panel[] getPanels() {
        return new WizardDescriptor.Panel[] {
            new SelectProviderWizard(),
            new SelectObjectFilesWizard(),
            new ConsolidationStrategyWizard(),
            new SelectConfigurationWizard(),
            new RemoveUnusedWizard()
        };
    }
    
    public void apply(WizardDescriptor wizard, Project project) throws IOException {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.setProject(project);
        DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
        generator.makeProject();
    }

    public void uninitialize(WizardDescriptor wizard) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        descriptor.clean();
    }

    public boolean canApply(WizardDescriptor wizard, Project project) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(wizard);
        return !descriptor.isInvokeProvider() &&
               descriptor.getConfigurations() != null &&
               descriptor.getIncludedFiles() != null &&
               descriptor.getIncludedFiles() != null;
    }
}
