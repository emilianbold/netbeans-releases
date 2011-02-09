/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.repository.remote;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author ondra
 */
public class SetupRemoteWizard implements ChangeListener {

    private final Map<String, GitRemoteConfig> remotes;
    private final String selectedRemote;
    private PanelsIterator wizardIterator;
    private WizardDescriptor wizardDescriptor;
    private final File repository;

    public SetupRemoteWizard (File repository, Map<String, GitRemoteConfig> remotes, String selectedRemote) {
        this.repository = repository;
        this.remotes = remotes;
        this.selectedRemote = selectedRemote;
    }

    boolean show () {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);        
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(SetupRemoteWizard.class, "LBL_SetRemoteWizard.title")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        setErrorMessage(wizardIterator.selectRemoteStep.getErrorMessage());
        dialog.setVisible(true);
        dialog.toFront();
        Object value = wizardDescriptor.getValue();
        boolean finnished = value == WizardDescriptor.FINISH_OPTION;
        if (!finnished) {
            // wizard wasn't properly finnished ...
            if (value == WizardDescriptor.CLOSED_OPTION || value == WizardDescriptor.CANCEL_OPTION ) {
                // wizard was closed or canceled -> reset all steps & kill all running tasks
                wizardIterator.fetchRefsStep.cancelBackgroundTasks();
            }            
        }
        return finnished;
    }

    public GitRemoteConfig getRemote () {
        RemoteConfig remote = RemoteConfig.createUpdatableRemote(repository, wizardIterator.selectRemoteStep.getSelectedRemote());
        Panel<WizardDescriptor> currentPanel = wizardIterator.current();
        remote.setFetchUris(Arrays.asList(wizardIterator.fetchUrisStep.getURIs()));
        if (currentPanel != wizardIterator.fetchUrisStep) {
            // fetch ref specs panel accepted
            remote.setFetchRefSpecs(wizardIterator.fetchRefsStep.getRefSpecs());
        }
        return remote;
    }

    private void setErrorMessage (SelectRemoteStep.Message msg) {
        if (wizardDescriptor != null) {
            if (msg == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null); // NOI18N
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            } else {
                if (msg.isInfo()) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, msg.getMessage()); // NOI18N
                } else {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg.getMessage()); // NOI18N
                }
            }
        }
    }

    @Override
    public void stateChanged (ChangeEvent e) {
        AbstractWizardPanel step = (AbstractWizardPanel) e.getSource();
        setErrorMessage(step.getErrorMessage());
    }
    
    private class PanelsIterator extends WizardDescriptor.ArrayIterator<WizardDescriptor> {
        private SelectRemoteStep selectRemoteStep;
        private FetchUrisPanelController fetchUrisStep;
        private FetchRefsStep fetchRefsStep;

        @Override
        protected Panel<WizardDescriptor>[] initializePanels () {
            selectRemoteStep = new SelectRemoteStep(remotes, selectedRemote);
            selectRemoteStep.addChangeListener(SetupRemoteWizard.this);
            fetchUrisStep = new FetchUrisPanelController(null);
            fetchUrisStep.addChangeListener(SetupRemoteWizard.this);
            fetchRefsStep = new FetchRefsStep(FetchRefsStep.Mode.ACCEPT_EMPTY_SELECTION);
            fetchRefsStep.addChangeListener(SetupRemoteWizard.this);
            Panel[] panels = new Panel[] { selectRemoteStep, fetchUrisStep, fetchRefsStep };

            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
                }
            }
            return panels;
        }

        @Override
        public synchronized void nextPanel () {
            if (current() == selectRemoteStep) {
                fetchUrisStep.setRemote(remotes.get(selectRemoteStep.getSelectedRemote()));
            } else if (current() == fetchUrisStep) {
                String selectedUri = fetchUrisStep.getSelectedURI();
                if (selectedUri == null) {
                    selectedUri = fetchUrisStep.getURIs()[0];
                }
                fetchRefsStep.setRemote(remotes.get(selectRemoteStep.getSelectedRemote()));
                fetchRefsStep.setFetchUri(selectedUri, true);
            }
            super.nextPanel();
        }

        @Override
        public synchronized void previousPanel () {
            if (current() == fetchRefsStep) {
                fetchRefsStep.cancelBackgroundTasks();
            }
            super.previousPanel();
        }
    }
}
