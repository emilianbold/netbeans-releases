/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class SelectModeWizard implements WizardDescriptor.AsynchronousValidatingPanel, ChangeListener {
    
    private DiscoveryDescriptor wizardDescriptor;
    private SelectModePanel component;
    private String name;
    private boolean inited = false;

    public SelectModeWizard(){
        name = NbBundle.getMessage(SelectProviderPanel.class, "SelectModeName"); // NOI18N
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new SelectModePanel(this);
      	    component.setName(name);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(DiscoveryWizardAction.HELP_CONTEXT_SELECT_MODE);
    }
    
    @Override
    public boolean isValid() {
        boolean valid = ((SelectModePanel)getComponent()).valid(wizardDescriptor);
        if (valid) {
            wizardDescriptor.setMessage(null);
        }
        return valid;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      	fireChangeEvent();
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        if (!inited) {
            wizardDescriptor = DiscoveryWizardDescriptor.adaptee(settings);
            component.read(wizardDescriptor);
            inited = true;
        }
        ((WizardDescriptor)wizardDescriptor).putProperty("ShowAlert", Boolean.FALSE);// NOI18N
    }
    
    @Override
    public void storeSettings(Object settings) {
        component.store(DiscoveryWizardDescriptor.adaptee(settings));
        if (wizardDescriptor instanceof WizardDescriptor) {
            ((WizardDescriptor)wizardDescriptor).putProperty(WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN, Boolean.TRUE);// NOI18N
        }
    }

    @Override
    public void prepareValidation() {
        //if (wizardDescriptor.isSimpleMode()) {
            component.enableControls(false);
        //}
    }

    @Override
    public void validate() throws WizardValidationException {
        if (wizardDescriptor.isSimpleMode()) {
            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SelectProviderPanel.class, "AnalyzingProjectProgress")); // NOI18N
            handle.setInitialDelay(100);
            handle.start();
            boolean res;
            try {
                res = component.isApplicable(wizardDescriptor);
            } finally {
                handle.finish();
            }
            component.updateControls();
            if (!res) {
                fireChangeEvent();
                ((WizardDescriptor)wizardDescriptor).putProperty("ShowAlert", Boolean.TRUE);// NOI18N
            }
        } else {
            DiscoveryProvider defProvider = (DiscoveryProvider) ((WizardDescriptor)wizardDescriptor).getProperty("PreferedProvider"); // NOI18N
            if (defProvider == null) {
                ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(SelectProviderPanel.class, "AnalyzingProjectProgress")); // NOI18N
                handle.setInitialDelay(100);
                handle.start();
                try {
                    ((WizardDescriptor)wizardDescriptor).putProperty("PreferedProvider", initPreferedProvider(wizardDescriptor));// NOI18N
                } finally {
                    handle.finish();
                }
            }
        }
    }
    
    DiscoveryProvider initPreferedProvider(final DiscoveryDescriptor wizardDescriptor) {
        ProjectProxy proxy = new ProjectProxy() {
            @Override
            public boolean createSubProjects() {
                return false;
            }
            @Override
            public Project getProject() {
                return wizardDescriptor.getProject();
            }

            @Override
            public String getMakefile() {
                return null;
            }

            @Override
            public String getSourceRoot() {
                return wizardDescriptor.getRootFolder();
            }

            @Override
            public String getExecutable() {
                return wizardDescriptor.getBuildResult();
            }

            @Override
            public String getWorkingFolder() {
                return null;
            }

            @Override
            public boolean mergeProjectProperties() {
                return false;
            }
        };
        DiscoveryProvider def = null;
        int assurance = 0;
        for(DiscoveryProvider provider : DiscoveryProviderFactory.findAllProviders()){
            if (provider.isApplicable(proxy)) {
                if ("dwarf-executable".equals(provider.getID())){ // NOI18N
                    // select executable if make project has output
                    // and output has debug information.
                    provider.getProperty("executable").setValue(wizardDescriptor.getBuildResult()); // NOI18N
                } else if ("dwarf-folder".equals(provider.getID())){ // NOI18N
                    provider.getProperty("folder").setValue(wizardDescriptor.getRootFolder()); // NOI18N
                }
                int i = provider.canAnalyze(proxy).getPriority();
                if (i > assurance) {
                    def = provider;
                    assurance = i;
                }
            }
        }
        return def;
    }
}

