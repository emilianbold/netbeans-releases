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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2009 Sun Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.php.project.ui.logicalview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.codecoverage.api.CoverageActionFactory;
import org.netbeans.modules.php.api.doc.PhpDocs;
import org.netbeans.modules.php.api.phpmodule.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.spi.actions.RunCommandAction;
import org.netbeans.modules.php.spi.doc.PhpDocProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleActionsExtender;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.LifecycleManager;
import org.openide.actions.FindAction;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author ads, Tomas Mysik
 */
public class PhpLogicalViewProvider implements LogicalViewProvider {

    private static final Logger LOGGER = Logger.getLogger(PhpLogicalViewProvider.class.getName());

    static final RequestProcessor RP = new RequestProcessor(PhpLogicalViewProvider.class);

    final PhpProject project;

    public PhpLogicalViewProvider(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        return PhpLogicalViewRootNode.createForProject(project);
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project p = root.getLookup().lookup(Project.class);
        if (p == null) {
            return null;
        }
        // Check each child node in turn.
        Node[] children = root.getChildren().getNodes(true);
        for (Node node : children) {
            if (target instanceof DataObject || target instanceof FileObject) {
                FileObject kidFO = node.getLookup().lookup(FileObject.class);
                if (kidFO == null) {
                    continue;
                }
                // Copied from org.netbeans.spi.java.project.support.ui.TreeRootNode.PathFinder.findPath:
                FileObject targetFO = null;
                if (target instanceof DataObject) {
                    targetFO = ((DataObject) target).getPrimaryFile();
                } else {
                    targetFO = (FileObject) target;
                }
                Project owner = FileOwnerQuery.getOwner(targetFO);
                if (!p.equals(owner)) {
                    return null; // Don't waste time if project does not own the fileobject
                }
                if (kidFO == targetFO) {
                    return node;
                } else if (FileUtil.isParentOf(kidFO, targetFO)) {
                    String relPath = FileUtil.getRelativePath(kidFO, targetFO);

                    // first path without extension (more common case)
                    String[] path = relPath.split("/"); // NOI18N
                    path[path.length - 1] = targetFO.getName();

                    // first try to find the file without extension (more common case)
                    Node found = findNode(node, path);
                    if (found == null) {
                        // file not found, try to search for the name with the extension
                        path[path.length - 1] = targetFO.getNameExt();
                        found = findNode(node, path);
                    }
                    if (found == null) {
                        // can happen for tests that are underneath sources directory
                        continue;
                    }
                    if (hasObject(found, target)) {
                        return found;
                    }
                    Node parent = found.getParentNode();
                    Children kids = parent.getChildren();
                    children = kids.getNodes();
                    for (Node child : children) {
                        if (hasObject(child, target)) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Node findNode(Node start, String[] path) {
        Node found = null;
        try {
            found = NodeOp.findPath(start, path);
        } catch (NodeNotFoundException ex) {
            // ignored
        }
        return found;
    }

    private boolean hasObject(Node node, Object obj) {
        if (obj == null) {
            return false;
        }
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        if (obj instanceof DataObject) {
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject == null) {
                return false;
            }
            if (dataObject.equals(obj)) {
                return true;
            }
            return hasObject(node, ((DataObject) obj).getPrimaryFile());
        } else if (obj instanceof FileObject) {
            return obj.equals(fileObject);
        }
        return false;
    }

    //~ Inner classes

    private static final class PhpLogicalViewRootNode extends AbstractNode implements ChangeListener, PropertyChangeListener {

        private static final String TOOLTIP = "<img src=\"%s\">&nbsp;%s"; // NOI18N

        private final PhpProject project;
        private final ProjectInformation projectInfo;


        private PhpLogicalViewRootNode(PhpProject project) {
            super(createChildren(project), Lookups.singleton(project));
            this.project = project;
            projectInfo = ProjectUtils.getInformation(project);
            // ui
            setIconBaseWithExtension(PhpProject.PROJECT_ICON);
            setName(ProjectUtils.getInformation(project).getDisplayName());
        }

        public static PhpLogicalViewRootNode createForProject(PhpProject project) {
            PhpLogicalViewRootNode rootNode = new PhpLogicalViewRootNode(project);
            rootNode.addListeners();
            return rootNode;
        }

        @Override
        public Image getIcon(int type) {
            return annotateImage(super.getIcon(type));
        }

        @Override
        public Image getOpenedIcon(int type) {
            return annotateImage(super.getOpenedIcon(type));
        }

        private void addListeners() {
            ProjectPropertiesSupport.addWeakProjectPropertyChangeListener(project, this);
            projectInfo.addPropertyChangeListener(WeakListeners.propertyChange(this, projectInfo));
        }

        private Image annotateImage(Image image) {
            Image badged = image;
            boolean first = true;
            for (PhpFrameworkProvider frameworkProvider : project.getFrameworks()) {
                BadgeIcon badgeIcon = frameworkProvider.getBadgeIcon();
                if (badgeIcon != null) {
                    badged = ImageUtilities.addToolTipToImage(badged, String.format(TOOLTIP, badgeIcon.getUrl(), frameworkProvider.getName()));
                    if (first) {
                        badged = ImageUtilities.mergeImages(badged, badgeIcon.getImage(), 15, 0);
                        first = false;
                    }
                } else {
                    badged = ImageUtilities.addToolTipToImage(badged, String.format(TOOLTIP, Utils.PLACEHOLDER_BADGE, frameworkProvider.getName()));
                }
            }
            return badged;
        }

        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(PhpLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName);
        }

        @Override
        public Action[] getActions(boolean context) {
            final PhpModule phpModule = project.getPhpModule();
            PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
            assert provider != null;
            List<Action> actions = new ArrayList<Action>();
            actions.add(CommonProjectActions.newFileAction());
            actions.add(null);
            actions.add(provider.getAction(ActionProvider.COMMAND_RUN));
            actions.add(provider.getAction(ActionProvider.COMMAND_DEBUG));
            actions.add(provider.getAction(ActionProvider.COMMAND_TEST));
            addDocumentationActions(actions, phpModule);
            actions.add(null);
            if (CommandUtils.getPhpUnit(project, false) != null) {
                // code coverage seems to be supported in php unit 3.3.0+
                actions.add(CoverageActionFactory.createCollectorAction(null, null));
                actions.add(null);
            }
            actions.add(CommonProjectActions.setProjectConfigurationAction());
            actions.add(null);
            actions.add(CommonProjectActions.setAsMainProjectAction());
            //actions.add(CommonProjectActions.openSubprojectsAction()); // does not make sense for php now
            actions.add(CommonProjectActions.closeProjectAction());
            actions.add(null);
            actions.add(CommonProjectActions.renameProjectAction());
            actions.add(CommonProjectActions.moveProjectAction());
            actions.add(CommonProjectActions.copyProjectAction());
            actions.add(CommonProjectActions.deleteProjectAction());
            actions.add(null);
            actions.add(SystemAction.get(FindAction.class));
            actions.add(null);

            // frameworks
            boolean hasFrameworkActions = false;
            for (PhpFrameworkProvider frameworkProvider : project.getFrameworks()) {
                PhpModuleActionsExtender actionsExtender = frameworkProvider.getActionsExtender(phpModule);
                if (actionsExtender != null) {
                    RunCommandAction runCommandAction = actionsExtender.getRunCommandAction();
                    List<? extends Action> frameworkActions = actionsExtender.getActions();
                    if (runCommandAction != null || !frameworkActions.isEmpty()) {
                        List<Action> allActions = new ArrayList<Action>(frameworkActions.size() + 2);
                        if (runCommandAction != null) {
                            allActions.add(runCommandAction);
                            if (!frameworkActions.isEmpty()) {
                                allActions.add(null);
                            }
                        }
                        allActions.addAll(frameworkActions);
                        actions.add(new FrameworkMenu(actionsExtender.getMenuName(), allActions));
                        hasFrameworkActions = true;
                    }
                }
            }
            if (hasFrameworkActions) {
                actions.add(null);
            }

            // honor 57874 contract
            actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
            actions.add(null);
            actions.add(CommonProjectActions.customizeProjectAction());
            return actions.toArray(new Action[actions.size()]);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider"); // NOI18N
        }

        private static Children createChildren(PhpProject project) {
           return NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-php-project/Nodes"); // NOI18N
        }

        /**
         * Add 'Generate documentation' menu (for 1 provider) or submenu (for more providers).
         * Do nothing if there are no providers.
         */
        private void addDocumentationActions(List<Action> actions, PhpModule phpModule) {
            List<PhpDocProvider> docProviders = PhpDocs.getDocumentations();
            if (docProviders.isEmpty()) {
                return;
            }
            List<PhpDocProvider> projectDocProviders = new ArrayList<PhpDocProvider>(docProviders.size());
            for (PhpDocProvider docProvider : docProviders) {
                if (docProvider.isInPhpModule(phpModule)) {
                    projectDocProviders.add(docProvider);
                }
            }
            if (projectDocProviders.isEmpty()) {
                return;
            }
            actions.add(null);
            if (projectDocProviders.size() == 1) {
                actions.add(new PhpDocAction(phpModule, projectDocProviders.get(0)));
            } else {
                actions.add(new DocumentationMenu(phpModule, projectDocProviders));
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    fireIconChange();
                    fireOpenedIconChange();
                    fireDisplayNameChange(null, null);
                }
            });
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    if (PhpProject.PROP_FRAMEWORKS.equals(evt.getPropertyName())) {
                        fireIconChange();
                        fireOpenedIconChange();
                    } else {
                        fireNameChange(null, null);
                        fireDisplayNameChange(null, null);
                    }
                }
            });
        }

        //~ Inner classes

        private static class FrameworkMenu extends AbstractAction implements Presenter.Popup {
            private static final long serialVersionUID = -238674120253122435L;

            private final String name;
            private final List<? extends Action> frameworkActions;

            public FrameworkMenu(String name, List<? extends Action> frameworkActions) {
                super(name, null);
                assert name != null;
                assert frameworkActions != null;

                putValue(SHORT_DESCRIPTION, name);
                this.name = name;
                this.frameworkActions = frameworkActions;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                assert false;
            }

            @Override
            public JMenuItem getPopupPresenter() {
                return new FrameworkSubMenu(name, frameworkActions);
            }
        }

        private static class FrameworkSubMenu extends BaseSubMenu {

            private static final long serialVersionUID = 9043114612433517414L;


            public FrameworkSubMenu(String name, List<? extends Action> frameworkActions) {
                super(name);
                assert name != null;
                assert frameworkActions != null;

                for (Action action : frameworkActions) {
                    if (action != null) {
                        add(toMenuItem(action));
                    } else {
                        addSeparator();
                    }
                }
            }

        }

        @NbBundle.Messages("PhpDoc.action.generate.label=Generate Documentation")
        private static class DocumentationMenu extends AbstractAction implements Presenter.Popup {

            private static final long serialVersionUID = 1587896543546879L;

            private final PhpModule phpModule;
            private final List<PhpDocProvider> docProviders;

            public DocumentationMenu(PhpModule phpModule, List<PhpDocProvider> docProviders) {
                super(Bundle.PhpDoc_action_generate_label(), null);
                assert phpModule != null;
                assert docProviders != null;

                putValue(SHORT_DESCRIPTION, Bundle.PhpDoc_action_generate_label());
                this.phpModule = phpModule;
                this.docProviders = docProviders;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                assert false;
            }

            @Override
            public JMenuItem getPopupPresenter() {
                List<PhpDocAction> docActions = new ArrayList<PhpDocAction>(docProviders.size());
                for (PhpDocProvider docProvider : docProviders) {
                    docActions.add(new PhpDocAction(docProvider.getDisplayName(), phpModule, docProvider));
                }
                return new DocumentationSubMenu(docActions);
            }
        }

        private static class DocumentationSubMenu extends BaseSubMenu {

            private static final long serialVersionUID = -6764324657641L;


            public DocumentationSubMenu(List<PhpDocAction> docActions) {
                super(Bundle.PhpDoc_action_generate_label());

                for (PhpDocAction action : docActions) {
                    add(toMenuItem(action));
                }
            }

        }

        private static final class PhpDocAction extends AbstractAction {

            private static final long serialVersionUID = 178423135454L;

            private static final RequestProcessor RP = new RequestProcessor("Generating php documentation", 2); // NOI18N

            private final PhpModule phpModule;
            private final PhpDocProvider docProvider;


            public PhpDocAction(PhpModule phpModule, PhpDocProvider docProvider) {
                this(Bundle.PhpDoc_action_generate_label(), phpModule, docProvider);
            }

            public PhpDocAction(String name, PhpModule phpModule, PhpDocProvider docProvider) {
                this.phpModule = phpModule;
                this.docProvider = docProvider;

                putValue(NAME, name);
                putValue(SHORT_DESCRIPTION, name);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        LifecycleManager.getDefault().saveAll();
                        docProvider.generateDocumentation(phpModule);
                    }
                });
            }
        }

        private abstract static class BaseSubMenu extends JMenu {

            public BaseSubMenu(String name) {
                super(name);
            }

            protected static JMenuItem toMenuItem(Action action) {
                JMenuItem item;
                if (action instanceof Presenter.Menu) {
                    item = ((Presenter.Menu) action).getMenuPresenter();
                } else {
                    item = new JMenuItem();
                    Actions.connect(item, action, false);
                }
                return item;
            }

        }

    }

    static final class CustomizeProjectAction extends AbstractAction {
        private static final long serialVersionUID = 423217315757925129L;

        private final PhpProject project;
        private final String category;

        CustomizeProjectAction(PhpProject project, String category) {
            assert project != null;
            assert category != null;

            this.project = project;
            this.category = category;

            String name = NbBundle.getMessage(PhpLogicalViewProvider.class, "LBL_Customize");
            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(category);
        }
    }

}
