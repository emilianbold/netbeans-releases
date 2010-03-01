/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.class)
public class BuildActionsProviderImpl extends BuildActionsProvider {

    @Override
    public List<BuildAction> getActions(String ioTabName, ProjectActionEvent[] events) {
        //return Collections.<Action>emptyList();
        List<BuildAction> res = new ArrayList<BuildAction>();
        if (events != null && events.length == 2) {
            if (events[0].getType() == ProjectActionEvent.PredefinedType.CLEAN &&
                events[1].getType() == ProjectActionEvent.PredefinedType.BUILD &&
                (events[1].getConfiguration() instanceof MakeConfiguration)&&
                 events[1].getConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
                res.add(new ConfigureAction(ioTabName, events));
            }
        }
        return res;
    }

    private static final class ConfigureAction extends AbstractAction implements BuildAction {
        private String ioTabName;
        private ProjectActionEvent[] events;
        private int step = -1;

        public ConfigureAction(String ioTabName, ProjectActionEvent[] events) {
            this.ioTabName = ioTabName;
            this.events = events;
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(BuildActionsProviderImpl.class.getResource("/org/netbeans/modules/cnd/discovery/wizard/resources/configure.png")); // NOI18N
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return NbBundle.getBundle(BuildActionsProviderImpl.class).getString("OUTPUT_LOG_ACTION_TEXT"); // NOI18N
            } else {
                return super.getValue(key);
            }
        }

        @Override
        public void executionStarted(int pid) {
            setEnabled(false);
        }

        @Override
        public void executionFinished(int rc) {
            if (step == 1 && rc == 0) {
                setEnabled(true);
            }
        }

        @Override
        public void setStep(int step) {
            this.step = step;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            JEditorPane pane = findPane();
            if (pane != null && step >= 0 && step < events.length) {
                Project project = events[step].getProject();
                String fileName = saveLog(pane);
                if (fileName != null) {
                    invokeWizard(project, fileName);
                }
            }
        }

        private String saveLog(JEditorPane pane){
            // TODO: this method does not work for sun studio compilers. Action should listen output writer.
            // Provide parameter outputListener for DefaultProjectActionHandler.ProcessChangeListener
            BufferedWriter bw = null;
            String name = null;
            try {
                File file = File.createTempFile("tmplog", ".log"); // NOI18N
                if (file.exists()){
                    file.delete();
                }
                file.deleteOnExit();
                name = file.getAbsolutePath();
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                Document doc = pane.getDocument();
                int nleft = doc.getLength();
                Segment text = new Segment();
                int offs = 0;
                text.setPartialReturn(true);   
                while (nleft > 0) {
                    try {
                        doc.getText(offs, nleft, text);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    for(char c: text.array) {
                        if (c != 0) {
                            bw.append(c);
                        }
                    }
                    nleft -= text.count;
                    offs += text.count;
                }
                bw.flush();
                bw.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException ex1) {
                        Exceptions.printStackTrace(ex1);
                    }
                }
                name = null;
            }
            return name;
        }
        
        private void invokeWizard(Project project, String fileName) {
            DiscoveryProvider provider = DiscoveryExtension.findProvider("make-log"); // NOI18N
            if (provider == null) {
                return;
            }
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                getString("OUTPUT_LOG_DILOG_COMTENT_TEXT"), // NOI18N
                getString("OUTPUT_LOG_DILOG_TITLE_TEXT"), // NOI18N
                NotifyDescriptor.YES_NO_OPTION)) != NotifyDescriptor.YES_OPTION){
                return;
            }
            //provider.getProperty("make-log-file").setValue(fileName);
            DiscoveryWizardDescriptor wizardDescriptor = new DiscoveryWizardDescriptor(getPanels());
            wizardDescriptor.setSimpleMode(true);
            wizardDescriptor.setProvider(provider);
            wizardDescriptor.setProject(project);
            wizardDescriptor.setRootFolder(DiscoveryWizardAction.findSourceRoot(project));
            //wizardDescriptor.setBuildResult(DiscoveryWizardAction.findBuildResult(project));
            wizardDescriptor.setBuildLog(fileName);
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
            wizardDescriptor.setTitle(getString("WIZARD_TITLE_TXT")); // NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
            dialog.setVisible(true);
            dialog.toFront();
            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                // do something
            }
            dialog.dispose();
        }
        
        private String getString(String key) {
            return NbBundle.getBundle(BuildActionsProviderImpl.class).getString(key);
        }

        private InstantiatingIterator getPanels() {
            WizardDescriptor.Panel[] simple = new WizardDescriptor.Panel[] {
                new SimpleConfigurationWizard()
            };
            String[] steps = new String[simple.length];
            for (int i = 0; i < simple.length; i++) {
                Component c = simple[i].getComponent();
                steps[i] = c.getName();
                DiscoveryWizardAction.setupComponent(steps, null, i, c);
            }
            return new DiscoveryWizardIterator(simple, simple);
        }
        
        private JEditorPane findPane(){
            TopComponent component = TopComponent.getRegistry().getActivated();
            return findPane(component);
        }
        
        private JEditorPane findPane(Container container){ 
            for(Component component : container.getComponents()){
                if (component instanceof JEditorPane) {
                    return (JEditorPane) component;
                } else if (component instanceof JTabbedPane){
                    JTabbedPane jt = (JTabbedPane) component;
                    if (jt.getComponentCount() > 0) {
                        Component t = jt.getSelectedComponent();
                        if (t instanceof JEditorPane) {
                            return (JEditorPane) t;
                        } else if (t instanceof Container){
                            JEditorPane res = findPane((Container)t);
                            if (res != null) {
                                return res;
                            }
                        }
                    }
                } else if (component instanceof Container) {
                    JEditorPane res = findPane((Container)component);
                    if (res != null) {
                        return res;
                    }
                }
            }
            return null;
        }
    }
}
