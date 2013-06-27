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
package org.netbeans.modules.cnd.makeproject.api;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.MakeSharabilityQuery;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.ProjectItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Customization of Make project shows dialog
 */
public class MakeCustomizerProvider implements CustomizerProvider {

    private final Project project;
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = 1;
    private static final int OPTION_APPLY = 2;
    // Option command names
    public static final String COMMAND_OK = "OK";          // NOI18N
    public static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    public static final String COMMAND_APPLY = "APPLY";  // NOI18N
    private DialogDescriptor dialogDescriptor;
    private Map<Project, Dialog> customizerPerProject = new WeakHashMap<Project, Dialog>(); // Is is weak needed here?
    private final ConfigurationDescriptorProvider projectDescriptorProvider;
    private String currentCommand;
    private final Map<MakeContext.Kind, String> lastCurrentNodeName = new EnumMap<MakeContext.Kind, String>(MakeContext.Kind.class);
    private final Set<ActionListener> actionListenerList = new HashSet<ActionListener>();
    private static final RequestProcessor RP = new RequestProcessor("MakeCustomizerProvider", 1); //NOI18N
    private static final RequestProcessor RP_SAVE = new RequestProcessor("MakeCustomizerProviderSave", 1); //NOI18N

    public MakeCustomizerProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }

    @Override
    public void showCustomizer() {
        showCustomizer(lastCurrentNodeName.get(MakeContext.Kind.Project), null, null);
    }

    public void showCustomizer(Item item) {
        showCustomizer(lastCurrentNodeName.get(MakeContext.Kind.Item), item, null);
    }

    public void showCustomizer(Folder folder) {
        showCustomizer(lastCurrentNodeName.get(MakeContext.Kind.Folder), null, folder);
    }

    public void showCustomizer(String preselectedNodeName) {
        showCustomizer(preselectedNodeName, null, null);
    }

    public void showCustomizer(final String preselectedNodeName, final Item item, final Folder folder) {
        if (!projectDescriptorProvider.gotDescriptor() || projectDescriptorProvider.getConfigurationDescriptor().getConfs().size() == 0) {
            //TODO: show warning dialog
            return;
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                showCustomizerWorker(preselectedNodeName, item, folder);
            }
        });
    }

    private void showCustomizerWorker(String preselectedNodeName, Item item, Folder folder) {

        if (customizerPerProject.containsKey(project)) {
            Dialog dlg = customizerPerProject.get(project);

            // check if the project is being customized
            if (dlg.isShowing()) {
                // make it showed
                dlg.setVisible(true);
                return;
            }
        }

        if (folder != null) {
            // Make sure all FolderConfigurations are created (they are lazyly created)
            Configuration[] configurations = projectDescriptorProvider.getConfigurationDescriptor().getConfs().toArray();
            for (int i = 0; i < configurations.length; i++) {
                folder.getFolderConfiguration(configurations[i]);
            }
        }

        // Make sure all languages are update
        projectDescriptorProvider.getConfigurationDescriptor().refreshRequiredLanguages();

        // Create options
        JButton options[] = new JButton[]{
            new JButton(NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Customizer_Ok_Option")), // NOI18N
            new JButton(NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Customizer_Cancel_Option")), // NOI18N
            new JButton(NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Customizer_Apply_Option")), // NOI18N
        };

        // Set commands
        options[OPTION_OK].setActionCommand(COMMAND_OK);
        options[OPTION_OK].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class, "ACSD_Customizer_Ok_Option")); // NOI18N
        options[OPTION_CANCEL].setActionCommand(COMMAND_CANCEL);
        options[OPTION_CANCEL].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class, "ACSD_Customizer_Cancel_Option")); // NOI18N
        options[OPTION_APPLY].setActionCommand(COMMAND_APPLY);
        options[OPTION_APPLY].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class, "ACSD_Customizer_Apply_Option")); // NOI18N

        //A11Y
        options[OPTION_OK].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class, "AD_MakeCustomizerProviderOk")); // NOI18N
        options[OPTION_CANCEL].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class, "AD_MakeCustomizerProviderCancel")); // NOI18N
        options[OPTION_APPLY].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProvider.class, "AD_MakeCustomizerProviderApply")); // NOI18N

        // Mnemonics
        options[OPTION_APPLY].setMnemonic(NbBundle.getMessage(MakeCustomizerProvider.class, "MNE_Customizer_Apply_Option").charAt(0)); // NOI18N

        // RegisterListener
        ConfigurationDescriptor clonedProjectdescriptor = projectDescriptorProvider.getConfigurationDescriptor().cloneProjectDescriptor();
        ArrayList<JComponent> controls = new ArrayList<JComponent>();
        controls.add(options[OPTION_OK]);
        MakeCustomizer innerPane = new MakeCustomizer(project, preselectedNodeName, clonedProjectdescriptor, item, folder, Collections.unmodifiableCollection(controls));
        ActionListener optionsListener = new OptionListener(project, projectDescriptorProvider.getConfigurationDescriptor(), clonedProjectdescriptor, innerPane, folder, item);
        options[OPTION_OK].addActionListener(optionsListener);
        options[OPTION_CANCEL].addActionListener(optionsListener);
        options[OPTION_APPLY].addActionListener(optionsListener);

        String dialogTitle;
        if (item != null) {
            dialogTitle = NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_File_Customizer_Title", item.getName()); // NOI18N 
        } else if (folder != null) {
            dialogTitle = NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Folder_Customizer_Title", folder.getName()); // NOI18N 
        } else {
            dialogTitle = NbBundle.getMessage(MakeCustomizerProvider.class, "LBL_Project_Customizer_Title", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N 
        }

        dialogDescriptor = new DialogDescriptor(
                innerPane, // innerPane
                dialogTitle,
                true, // modal
                options, // options
                options[OPTION_OK], // initial value
                DialogDescriptor.BOTTOM_ALIGN, // options align
                null, // helpCtx
                null);                                 // listener

        dialogDescriptor.setClosingOptions(new Object[]{options[OPTION_OK], options[OPTION_CANCEL]});
        innerPane.setDialogDescriptor(dialogDescriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);

        customizerPerProject.put(project, dialog);
        currentCommand = COMMAND_CANCEL;

        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }

        MakeContext lastContext = innerPane.getLastContext();
        String nodeName = innerPane.getCurrentNodeName();
        if (lastContext != null) {
            lastCurrentNodeName.put(lastContext.getKind(), nodeName);
        }
        if (currentCommand.equals(COMMAND_CANCEL)) {
            fireActionEvent(new ActionEvent(project, 0, currentCommand));
        }
    }

    /**
     * Listens to the actions on the Customizer's option buttons
     */
    private final class OptionListener implements ActionListener {

        private Project project;
        private ConfigurationDescriptor projectDescriptor;
        private ConfigurationDescriptor clonedProjectdescriptor;
        private MakeCustomizer makeCustomizer;
        private Folder folder;
        private Item item;

        OptionListener(Project project, ConfigurationDescriptor projectDescriptor, ConfigurationDescriptor clonedProjectdescriptor, MakeCustomizer makeCustomizer, Folder folder, Item item) {
            this.project = project;
            this.projectDescriptor = projectDescriptor;
            this.clonedProjectdescriptor = clonedProjectdescriptor;
            this.makeCustomizer = makeCustomizer;
            this.folder = folder;
            this.item = item;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            currentCommand = e.getActionCommand();

            if (currentCommand.equals(COMMAND_OK) || currentCommand.equals(COMMAND_APPLY)) {
                makeCustomizer.save();
                //non UI actions such as as update of MakeConfiguration accessing filesystem should be invoked from non EDT
                RP_SAVE.post(new Runnable() {
                    @Override
                    public void run() {
                        int previousVersion = projectDescriptor.getVersion();
                        int currentVersion = CommonConfigurationXMLCodec.CURRENT_VERSION;
                        if (previousVersion < currentVersion) {
                            // Check
                            boolean issueRequiredProjectBuildWarning = false;
                            if (previousVersion < 76) {
                                for (Configuration configuration : projectDescriptor.getConfs().getConfigurations()) {
                                    MakeConfiguration makeConfiguration = (MakeConfiguration) configuration;
                                    if (!makeConfiguration.isMakefileConfiguration()) {
                                        continue;
                                    }
                                    List<ProjectItem> projectLinkItems = makeConfiguration.getRequiredProjectsConfiguration().getValue();
                                    for (ProjectItem projectItem : projectLinkItems) {
                                        if (projectItem.getMakeArtifact().getBuild()) {
                                            issueRequiredProjectBuildWarning = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            String txt;

                            if (issueRequiredProjectBuildWarning) {
                                txt = getString("UPGRADE_RQ_TXT");
                            } else {
                                txt = getString("UPGRADE_TXT");
                            }
                            NotifyDescriptor d = new NotifyDescriptor.Confirmation(txt, getString("UPGRADE_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION); // NOI18N
                            if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                                return;
                            }
                            projectDescriptor.setVersion(currentVersion);
                        }

                        List<String> oldSourceRoots = ((MakeConfigurationDescriptor) projectDescriptor).getSourceRoots();
                        List<String> newSourceRoots = ((MakeConfigurationDescriptor) clonedProjectdescriptor).getSourceRoots();
                        List<String> oldTestRoots = ((MakeConfigurationDescriptor) projectDescriptor).getTestRoots();
                        List<String> newTestRoots = ((MakeConfigurationDescriptor) clonedProjectdescriptor).getTestRoots();
                        Configuration oldActive = projectDescriptor.getConfs().getActive();
                        if (oldActive != null) {
                            oldActive = oldActive.cloneConf();
                        }
                        Configuration[] oldConf = projectDescriptor.getConfs().toArray();
                        Configuration newActive = clonedProjectdescriptor.getConfs().getActive();
                        Configuration[] newConf = clonedProjectdescriptor.getConfs().toArray();

                        projectDescriptor.assign(clonedProjectdescriptor);
                        projectDescriptor.getConfs().fireChangedConfigurations(oldConf, newConf);
                        projectDescriptor.setModified();
                        projectDescriptor.save(); // IZ 133606

                        // IZ#179995
                        MakeSharabilityQuery query = project.getLookup().lookup(MakeSharabilityQuery.class);
                        if (query != null) {
                            query.update();
                        }
                        ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedItems(project, folder, item);
                        ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedSourceRoots(oldSourceRoots, newSourceRoots);
                        ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedTestRoots(oldTestRoots, newTestRoots);
                        ((MakeConfigurationDescriptor) projectDescriptor).checkConfigurations(oldActive, newActive);
                    }
                });

            }
            if (!currentCommand.equals(COMMAND_CANCEL)) {
                fireActionEvent(new ActionEvent(project, 0, currentCommand));
            }
            if (currentCommand.equals(COMMAND_APPLY)) {
                
                makeCustomizer.refresh();
            }

        }
    }

    public void addActionListener(ActionListener cl) {
        synchronized (actionListenerList) {
            actionListenerList.add(cl);
        }
    }

    public void removeActionListener(ActionListener cl) {
        synchronized (actionListenerList) {
            actionListenerList.remove(cl);
        }
    }

    private void fireActionEvent(ActionEvent e) {
        Iterator<ActionListener> it;

        synchronized (actionListenerList) {
            it = new HashSet<ActionListener>(actionListenerList).iterator();
        }
        while (it.hasNext()) {
            it.next().actionPerformed(e);
        }
    }

    /**
     * Look up i18n strings here
     */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeCustomizerProvider.class, s);
    }
}
