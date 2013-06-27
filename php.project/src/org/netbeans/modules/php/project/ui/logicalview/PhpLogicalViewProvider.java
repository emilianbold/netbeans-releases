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
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
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
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.api.framework.BadgeIcon;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.documentation.PhpDocumentationProvider;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;

/**
 * @author ads, Tomas Mysik
 */
@ActionReferences({
    @ActionReference(
        id=@ActionID(id="org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory", category="Project"),
        position=1950,
        path="Projects/org-netbeans-modules-php-phpproject/Actions")
})
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
            super(createChildren(project), createLookup(project));
            this.project = project;
            projectInfo = ProjectUtils.getInformation(project);
            // ui
            setIconBaseWithExtension(PhpProject.PROJECT_ICON);
            setName(projectInfo.getDisplayName());
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

        private static Lookup createLookup(PhpProject project) {
            final InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(project);
            instanceContent.add(project, new InstanceContent.Convertor<PhpProject, FileObject>() {
                @Override
                public FileObject convert(PhpProject obj) {
                    return obj.getProjectDirectory();
                }

                @Override
                public Class<? extends FileObject> type(PhpProject obj) {
                    return FileObject.class;
                }

                @Override
                public String id(PhpProject obj) {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? "" : fo.getPath();  // NOI18N
                }

                @Override
                public String displayName(PhpProject obj) {
                    return obj.toString();
                }

            });
            instanceContent.add(project, new InstanceContent.Convertor<PhpProject, DataObject>() {
                @Override
                public DataObject convert(PhpProject obj) {
                    try {
                        final FileObject fo = obj.getProjectDirectory();
                        return fo != null && fo.isValid() ? DataObject.find(fo) : null;
                    } catch (DataObjectNotFoundException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                        return null;
                    }
                }

                @Override
                public Class<? extends DataObject> type(PhpProject obj) {
                    return DataObject.class;
                }

                @Override
                public String id(PhpProject obj) {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? "" : fo.getPath();  // NOI18N
                }

                @Override
                public String displayName(PhpProject obj) {
                    return obj.toString();
                }

            });
            return new AbstractLookup(instanceContent);
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
                    badged = ImageUtilities.addToolTipToImage(badged, String.format(TOOLTIP, Utils.PLACEHOLDER_BADGE_URL, frameworkProvider.getName()));
                }
            }
            return badged;
        }

        @Override
        public String getName() {
            // i would expect getName() here but see #222588
            return projectInfo.getDisplayName();
        }

        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return NbBundle.getMessage(PhpLogicalViewProvider.class, "HINT_project_root_node", prjDirDispName);
        }

        @Override
        public String getHtmlDisplayName() {
            String dispName = super.getDisplayName();
            try {
                dispName = XMLUtil.toElementContent(dispName);
            } catch (CharConversionException ex) {
                return dispName;
            }
            return PhpProjectValidator.isBroken(project)
                    ? "<font color=\"#" + Integer.toHexString(Utils.getErrorForeground().getRGB() & 0xffffff) + "\">" + dispName + "</font>" // NOI18N
                    : null;
        }

        @Override
        public Action[] getActions(boolean context) {
            List<Action> actions = new LinkedList<>(Arrays.asList(CommonProjectActions.forType("org-netbeans-modules-php-phpproject"))); // NOI18N
            // XXX code coverage cannot be added since it already is ContextAwareAction (but the Factory needs to be ContextAwareAction as well)
            addCodeCoverageAction(actions);
            // XXX similarly for frameworks - they are directly in the context menu, not in any submenu
            addFrameworks(actions);
            return actions.toArray(new Action[actions.size()]);
        }

        private void addCodeCoverageAction(List<Action> actions) {
            boolean coverageSupported = false;
            PhpModule phpModule = project.getPhpModule();
            for (PhpTestingProvider testingProvider : project.getTestingProviders()) {
                if (testingProvider.isCoverageSupported(phpModule)) {
                    coverageSupported = true;
                    break;
                }
            }
            if (coverageSupported) {
                int activeConfigActionIndex = actions.size();
                for (int i = 0; i < actions.size(); i++) {
                    Action action = actions.get(i);
                    if (action != null
                            && action.getClass().getName().equals("org.netbeans.modules.project.ui.actions.ActiveConfigAction")) { // NOI18N
                        activeConfigActionIndex = i;
                        break;
                    }
                }
                actions.add(++activeConfigActionIndex, CoverageActionFactory.createCollectorAction(null, null));
            }
        }

        private void addFrameworks(List<Action> actions) {
            // find index
            int documentationIndex = actions.size();
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                if (action instanceof DocumentationActionFactory) {
                    documentationIndex = i;
                    break;
                }
            }
            // insert actions
            boolean separatorAdded = false;
            PhpModule phpModule = project.getPhpModule();
            for (PhpFrameworkProvider frameworkProvider : project.getFrameworks()) {
                PhpModuleActionsExtender actionsExtender = frameworkProvider.getActionsExtender(phpModule);
                if (actionsExtender != null) {
                    RunCommandAction runCommandAction = actionsExtender.getRunCommandAction();
                    List<? extends Action> frameworkActions = actionsExtender.getActions();
                    if (runCommandAction != null || !frameworkActions.isEmpty()) {
                        List<Action> allActions = new ArrayList<>(frameworkActions.size() + 2);
                        if (runCommandAction != null) {
                            allActions.add(runCommandAction);
                            if (!frameworkActions.isEmpty()) {
                                allActions.add(null);
                            }
                        }
                        allActions.addAll(frameworkActions);
                        if (!separatorAdded) {
                            separatorAdded = true;
                            actions.add(++documentationIndex, null);
                        }
                        actions.add(++documentationIndex, new FrameworkMenu(actionsExtender.getMenuName(), allActions));
                    }
                }
            }
            if (separatorAdded) {
                actions.add(++documentationIndex, null);
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider"); // NOI18N
        }

        private static Children createChildren(PhpProject project) {
           return NodeFactorySupport.createCompositeChildren(project, "Projects/org-netbeans-modules-php-project/Nodes"); // NOI18N
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

    }

    //~ Inner classes

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
            PhpProjectUtils.openCustomizer(project, category);
        }
    }

    /**
     * Add 'Generate documentation' menu (for 1 provider) or submenu (for more providers).
     * Do nothing if there are no providers.
     */
    @ActionID(id="org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider$DocumentationActionFactory", category="Project")
    @ActionRegistration(displayName="#PhpDoc.action.generate.label", lazy=false)
    @ActionReference(position=800, path="Projects/org-netbeans-modules-php-phpproject/Actions")
    public static final class DocumentationActionFactory extends AbstractAction implements ContextAwareAction {

        private static final long serialVersionUID = 5687856454545L;


        public DocumentationActionFactory() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
            if (projects.size() != 1) {
                return this;
            }
            PhpProject phpProject = projects.iterator().next().getLookup().lookup(PhpProject.class);
            if (phpProject == null) {
                return this;
            }
            List<PhpDocumentationProvider> docProviders = PhpDocumentations.getDocumentations();
            if (docProviders.isEmpty()) {
                return this;
            }

            PhpModule phpModule = phpProject.getPhpModule();
            List<PhpDocumentationProvider> projectDocProviders = new ArrayList<>(docProviders.size());
            for (PhpDocumentationProvider docProvider : docProviders) {
                if (docProvider.isInPhpModule(phpModule)) {
                    projectDocProviders.add(docProvider);
                }
            }
            if (projectDocProviders.isEmpty()) {
                return this;
            }
            if (projectDocProviders.size() == 1) {
                return new PhpDocAction(phpProject, projectDocProviders.get(0));
            }
            return new DocumentationMenu(phpProject, projectDocProviders);
        }

    }

    private static class DocumentationMenu extends AbstractAction implements Presenter.Popup {

        private static final long serialVersionUID = 1587896543546879L;

        private final PhpProject phpProject;
        private final List<PhpDocumentationProvider> docProviders;

        public DocumentationMenu(PhpProject phpProject, List<PhpDocumentationProvider> docProviders) {
            super(NbBundle.getMessage(DocumentationMenu.class, "PhpDoc.action.generate.label"), null);
            assert phpProject != null;
            assert docProviders != null;

            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DocumentationMenu.class, "PhpDoc.action.generate.label"));
            this.phpProject = phpProject;
            this.docProviders = docProviders;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public JMenuItem getPopupPresenter() {
            List<PhpDocAction> docActions = new ArrayList<>(docProviders.size());
            for (PhpDocumentationProvider docProvider : docProviders) {
                docActions.add(new PhpDocAction(docProvider.getDisplayName(), phpProject, docProvider));
            }
            return new DocumentationSubMenu(docActions);
        }
    }

    private static class DocumentationSubMenu extends BaseSubMenu {

        private static final long serialVersionUID = -6764324657641L;


        public DocumentationSubMenu(List<PhpDocAction> docActions) {
            super(NbBundle.getMessage(DocumentationSubMenu.class, "PhpDoc.action.generate.label"));

            for (PhpDocAction action : docActions) {
                add(toMenuItem(action));
            }
        }

    }

    private static final class PhpDocAction extends AbstractAction {

        private static final long serialVersionUID = 178423135454L;

        private static final RequestProcessor RP = new RequestProcessor("Generating php documentation", 2); // NOI18N

        private final PhpProject phpProject;
        private final PhpDocumentationProvider docProvider;


        public PhpDocAction(PhpProject phpProject, PhpDocumentationProvider docProvider) {
            this(NbBundle.getMessage(PhpDocAction.class, "PhpDoc.action.generate.label"), phpProject, docProvider);
        }

        public PhpDocAction(String name, PhpProject phpProject, PhpDocumentationProvider docProvider) {
            this.phpProject = phpProject;
            this.docProvider = docProvider;

            putValue(NAME, name);
            putValue(SHORT_DESCRIPTION, name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (PhpProjectValidator.isFatallyBroken(phpProject)) {
                // broken project
                Utils.warnInvalidSourcesDirectory(phpProject);
                return;
            }
            RP.post(new Runnable() {
                @Override
                public void run() {
                    LifecycleManager.getDefault().saveAll();
                    docProvider.generateDocumentation(phpProject.getPhpModule());
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
