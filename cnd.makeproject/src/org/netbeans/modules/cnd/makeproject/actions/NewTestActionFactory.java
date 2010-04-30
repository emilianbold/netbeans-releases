/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Nikolay Krasilnikov (http://nnnnnk.name)
 */
public class NewTestActionFactory {

    private NewTestActionFactory() {
    }

    public static Action[] getTestCreationActions(Project project) {
        ArrayList<Action> actions = new ArrayList<Action>();
        FileObject testFiles = FileUtil.getConfigFile("Templates/testFiles"); //NOI18N
        if (testFiles.isFolder()) {
            for (FileObject test : testFiles.getChildren()) {
                if (!"hidden".equals(test.getAttribute("templateCategory"))) { //NOI18N
                    actions.add(new NewTestAction(project, test));
                }
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    public static Action emptyTestFolderAction() {
        return SystemAction.get(NewEmptyTestAction.class);
    }
    
    public static class NewTestAction extends AbstractAction {

        private final FileObject test;
        private final Project project;

        public NewTestAction(Project project, FileObject test) {
            super(getString("NewTestPrefix") + // NOI18N
                    NbBundle.getBundle((String) test.getAttribute("SystemFileSystem.localizingBundle")).getString(test.getPath()) + //NOI18N
                    getString("NewTestPostfix"), // NOI18N
                    getIcon(test));
            this.test = test;
            this.project = project;
        }

        public
        @Override
        void actionPerformed(ActionEvent e) {
            try {
                final TemplateWizard templateWizard = new TemplateWizard();
                templateWizard.putProperty("project", project); // NOI18N
                Set<DataObject> files = templateWizard.instantiate(DataObject.find(FileUtil.getConfigFile(test.getPath())));
                if (files != null && !files.isEmpty()) {
                    MakeLogicalViewProvider.setVisible(project,
                        getMakeConfigurationDescriptor(project).findProjectItemByPath(
                        files.iterator().next().getPrimaryFile().getPath()).getFolder());

                    for (DataObject file : files) {
                        Openable open = file.getLookup().lookup(Openable.class);
                        if (open != null) {
                            open.open();
                            // org.netbeans.modules.project.ui.actions.NewFile would also select new file in Projects
                        }
                    }
                } // else wizard was canceled
            } catch (IOException x) {
                // log somehow
            }
        }

        private MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
            ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp == null) {
                return null;
            }
            return pdp.getConfigurationDescriptor();
        }
        
        private static Icon getIcon(FileObject test) {
            URL url = (URL) test.getAttribute("SystemFileSystem.icon"); // NOI18N
            ImageIcon imageIcon = new ImageIcon(ImageUtilities.loadImage(url.getPath().substring(1), true));
            return imageIcon;
        }

        private static String getString(String s) {
            return NbBundle.getBundle(NewTestActionFactory.class).getString(s);
        }
    }

    public static class NewEmptyTestAction extends NodeAction {

        @Override
        public String getName() {
            return getString("NewEmptyTestActionName"); // NOI18N
        }

        @Override
        public void performAction(Node[] activatedNodes) {
            Node n = activatedNodes[0];
            Folder folder = (Folder) n.getValue("Folder"); // NOI18N
            assert folder != null;
            Node thisNode = (Node) n.getValue("This"); // NOI18N
            assert thisNode != null;
            Project project = (Project) n.getValue("Project"); // NOI18N
            assert project != null;

            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            if (!makeConfigurationDescriptor.okToChange()) {
                return;
            }

            NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(getString("TestName"), getString("NewTest"));
            dlg.setInputText(folder.suggestedNewTestFolderName());
            String newname = null;

            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
                newname = dlg.getInputText();
            } else {
                return;
            }

            Folder newFolder = folder.addNewFolder(true, Folder.Kind.TEST);
            newFolder.setDisplayName(newname);
            MakeLogicalViewProvider.setVisible(project, newFolder);
        }

        public boolean enable(Node[] activatedNodes) {
            return true;
        }

        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        private String getString(String s) {
            return NbBundle.getBundle(NewTestActionFactory.class).getString(s);
        }
    }
}
