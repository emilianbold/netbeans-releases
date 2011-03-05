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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.SelectHostWizardProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.actions.ShadowProjectSynchronizer;
import org.netbeans.modules.cnd.makeproject.ui.wizards.RemoteProjectImportWizard.ImportedProject;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

public final class RemoteProjectImportWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    // To invoke this wizard, copy-paste and run the following code, e.g. from
    // SomeAction.performAction():
    /*
    WizardDescriptor.Iterator iterator = new RemoteProjectImportWizardIterator();
    WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
    // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
    // {1} will be replaced by WizardDescriptor.Iterator.name()
    wizardDescriptor.setTitleFormat(new MessageFormat("{0} ({1})"));
    wizardDescriptor.setTitle("Your wizard dialog title here");
    Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
    dialog.setVisible(true);
    dialog.toFront();
    boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
    if (!cancelled) {
    // do something
    }
     */
    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private final List<ImportedProject> projects = new ArrayList<ImportedProject>();
    private WizardDescriptor wizard;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            SelectHostWizardProvider hostPanelProvider = SelectHostWizardProvider.createInstance(false, true, new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                      fireChangeEvent();
                    }
                });
            
            @SuppressWarnings("unchecked")
            WizardDescriptor.Panel<WizardDescriptor>[] wizardPanels = new WizardDescriptor.Panel[] {
                hostPanelProvider.getSelectHostPanel(),
                new RemoteProjectImportWizardPanel(),
            };
            panels = wizardPanels;
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(RemoteProjectImportWizardIterator.class, "RemoteProjectImportWizardIterator.indexOf", index + 1, getPanels().length);
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

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

    Object getErrorMessage() {
        return null;
    }

    @Override
    public Set<ImportedProject> instantiate() throws IOException {
        return instantiate(null);
    }
    
    @Override
    public Set<ImportedProject> instantiate(ProgressHandle handle) throws IOException {
        try {
            if (handle != null) {
                handle.start();
            }
            try {
                this.projects.clear();
                @SuppressWarnings("unchecked")
                Collection<RemoteProjectImportWizard.ImportedProject> remoteProjects = (Collection<RemoteProjectImportWizard.ImportedProject>) wizard.getProperty(RemoteProjectImportWizard.PROPERTY_REMOTE_PROJECTS);
                this.projects.addAll(remoteProjects);
                for (ImportedProject importedProject : remoteProjects) {
                    ExecutionEnvironment remoteEnvironment = importedProject.getRemoteEnvironment();
                    if (handle != null) {
                        handle.progress(NbBundle.getMessage(RemoteProjectImportWizardIterator.class, "RemoteProjectImportWizardIterator.setupRecord", remoteEnvironment.getDisplayName()));
                    }
                    boolean newHost = true;
                    for (ExecutionEnvironment Env : ServerList.getEnvironments()) {
                        if (remoteEnvironment.equals(Env)) {
                            newHost = false;
                            break;
                        }
                    }
                    ServerRecord record;
                    if (newHost) {
                        record = ServerList.addServer(remoteEnvironment, remoteEnvironment.getDisplayName(), RemoteSyncFactory.getDefault(), false, true);
                    } else {
                        record = ServerList.get(remoteEnvironment);
                    }
                    if (record.isSetUp() || record.setUp()) {
                        String remoteProjectFolder = importedProject.getRemoteProjectFolder();
                        if (handle != null) {
                            handle.progress(NbBundle.getMessage(RemoteProjectImportWizardIterator.class, "RemoteProjectImportWizardIterator.import", CndPathUtilitities.getDirName(remoteProjectFolder)));
                        }
                        ShadowProjectSynchronizer synchronizer = new ShadowProjectSynchronizer(remoteProjectFolder, importedProject.getLocalProjectDestinationFolder(), remoteEnvironment);
                        FileObject localProject = synchronizer.createShadowProject();
                        assert localProject != null;
                        Project findProject = ProjectManager.getDefault().findProject(localProject);
                        if (findProject != null) {
                            OpenProjects.getDefault().open(new Project[]{findProject}, false);
                        } else {
                            throw new IOException(NbBundle.getMessage(RemoteProjectImportWizardIterator.class, "RemoteProjectImportWizardIterator.canNotOpenProject"));
                        }                   
                    }
                }
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
            return Collections.emptySet();
        } finally {
            if (handle != null) {
                handle.finish();
            }
        }
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        getPanels();
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        
    }

    List<ImportedProject> getProjects() {
        return this.projects;
    }
}
