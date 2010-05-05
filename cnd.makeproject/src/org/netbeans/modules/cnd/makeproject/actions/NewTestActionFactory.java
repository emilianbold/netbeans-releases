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
import java.util.Collection;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
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
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Nikolay Krasilnikov (http://nnnnnk.name)
 * @author Vladimir Voskresensky
 */
public final class NewTestActionFactory {

    private NewTestActionFactory() {
    }

    public static Action[] getTestCreationActions(Project project) {
        ArrayList<Action> actions = new ArrayList<Action>();
        FileObject testFiles = FileUtil.getConfigFile("Templates/testFiles"); //NOI18N
        if (testFiles.isFolder()) {
            for (FileObject test : testFiles.getChildren()) {
                if (!"hidden".equals(test.getAttribute("templateCategory"))) { //NOI18N
                    actions.add(new NewTestAction(test, project, null, false));
                }
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    public static Action createNewTestsSubmenu() {
        return SystemAction.get(CreateTestSubmenuAction.class);
    }

    public static Action emptyTestFolderAction() {
        return SystemAction.get(NewEmptyTestAction.class);
    }
    
    public static class NewTestAction extends AbstractAction {

        private final FileObject test;
        private final Project project;
        private final Lookup context;
        private final boolean generateCode;

        public NewTestAction(FileObject test, Project project, Lookup context, boolean generateCode) {
            super.putValue(NAME, NbBundle.getMessage(CreateTestSubmenuAction.class, "NewTestNameWrapper", getName(test)));
            super.putValue(SMALL_ICON, getIcon(test));
            this.test = test;
            this.project = project;
            this.context = context;
            this.generateCode = generateCode;
        }

        public
        @Override
        void actionPerformed(ActionEvent e) {
            try {
                final TemplateWizard templateWizard = new TemplateWizard();
                Project aProject = project;
                templateWizard.putProperty("UnitTestContextLookup", context); // NOI18N
                templateWizard.putProperty("UnitTestCodeGeneration", generateCode); // NOI18N
                if (aProject == null) {
                    assert context != null;
                    Node node = context.lookup(Node.class);
                    if (node != null) {
                        FileObject fo = node.getLookup().lookup(FileObject.class);
                        if (fo != null) {
                            aProject = FileOwnerQuery.getOwner(fo);
                        }
                    }
                }
                templateWizard.putProperty("project", aProject); // NOI18N
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
        
        private String getName(FileObject test) {
            return NbBundle.getBundle((String) test.getAttribute("SystemFileSystem.localizingBundle")).getString(test.getPath());
        }

        private Icon getIcon(FileObject test) {
            URL url = (URL) test.getAttribute("SystemFileSystem.icon"); // NOI18N
            return ImageUtilities.loadImageIcon(url.getPath().substring(1), true);
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

        @Override
        public boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        private String getString(String s) {
            return NbBundle.getBundle(NewTestActionFactory.class).getString(s);
        }
    }

    private final static class CreateTestSubmenuAction extends NodeAction {

        private LazyPopupMenu popupMenu;
        private final Collection<Action> items = new ArrayList<Action>(5);

        @Override
        public JMenuItem getPopupPresenter() {
            createSubMenu();
            return popupMenu;
        }

        @Override
        public JMenuItem getMenuPresenter() {
            createSubMenu();
            return popupMenu;
        }

        private void createSubMenu() {
            if (popupMenu == null) {
                popupMenu = new LazyPopupMenu(NbBundle.getMessage(CreateTestSubmenuAction.class, "CTL_TestAction"), items);
            }
            items.clear();
            Node[] nodes = getActivatedNodes();
            if (nodes != null && nodes.length == 1) {
                FileObject fo = nodes[0].getLookup().lookup(FileObject.class);
                if (fo != null) {
                    Project project = FileOwnerQuery.getOwner(fo);
                    if (project != null) {
                        items.addAll(createActions(project));
                    }
                }
            }
            popupMenu.setEnabled(!items.isEmpty());
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length == 1) {
                NativeFileItemSet set = activatedNodes[0].getLookup().lookup(NativeFileItemSet.class);
                if (set != null && !set.isEmpty()) {
                    for (NativeFileItem nativeFileItem : set.getItems()) {
                        if (nativeFileItem instanceof Item) {
                            Item item = (Item) nativeFileItem;
                            Folder folder = item.getFolder();
                            if (folder != null && folder.isTest()) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(CreateTestSubmenuAction.class, "CTL_TestAction");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        private Collection<Action> createActions(Project project) {
            ArrayList<Action> actions = new ArrayList<Action>();
            FileObject testFiles = FileUtil.getConfigFile("Templates/testFiles"); //NOI18N
            if (testFiles.isFolder()) {
                for (FileObject test : testFiles.getChildren()) {
                    if (Boolean.TRUE.equals(test.getAttribute("templateGenerator"))) { //NOI18N
                        actions.add(new NewTestAction(test, project, org.openide.util.Utilities.actionsGlobalContext(), true));
                    }
                }
            }
            return actions;
        }
    }

    private final static class LazyPopupMenu extends JMenu {

        private final Collection<Action> items;

        public LazyPopupMenu(String name, Collection<Action> items) {
            super(name);
            assert items != null : "array must be inited";
            this.items = items;
        }

        @Override
        public synchronized JPopupMenu getPopupMenu() {
            super.removeAll();
            for (Action action : items) {
                if (action instanceof Presenter.Popup) {
                    JMenuItem item = ((Presenter.Popup) action).getPopupPresenter();
                    add(item);
                } else if (action instanceof Presenter.Menu) {
                    JMenuItem item = ((Presenter.Menu) action).getMenuPresenter();
                    add(item);
                } else {
                    add(action);
                }
            }
            return super.getPopupMenu();
        }
    }
}
