/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.project.deps.ui.DependenciesNode;
import org.netbeans.modules.javacard.project.libraries.LibrariesManager;
import org.netbeans.modules.javacard.project.libraries.LibrariesManager.ErrFile;
import org.netbeans.modules.javacard.project.ui.JarOrDirectoryFilter;
import org.netbeans.modules.javacard.project.ui.WaitNode;
import org.netbeans.spi.actions.Single;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.NewTemplateAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;

public class JCProjectSourceNodeFactory implements NodeFactory {

    public NodeList createNodes(Project p) {
        JCProject project =
                p.getLookup().lookup(JCProject.class);
        assert project != null;
        return new JCNodeList(project);
    }

    private static class ImportantFilesNode extends AbstractNode {

        public ImportantFilesNode(JCProject project) {
            super(NodeFactorySupport.createCompositeChildren(project,
                    project.kind().importantFilesPath()),
                    Lookups.singleton(project));
            setDisplayName(NbBundle.getMessage(JCProjectSourceNodeFactory.class,
                    "LBL_IMPORTANT_FILES")); //NOI18N
            setIconBaseWithExtension(
                    "org/netbeans/modules/javacard/resources/importantfiles.png"); //NOI18N
        }
    }

    private static class JCNodeList implements NodeList<JCKey>, ChangeListener {

        private JCProject project;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        public JCNodeList(JCProject proj) {
            project = proj;
        }

        public List<JCKey> keys() {
            if (this.project.getProjectDirectory() == null || !this.project.getProjectDirectory().isValid()) {
                return Collections.<JCKey>emptyList();
            }
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);

            List<JCKey> result = new ArrayList<JCKey>(groups.length);
            for (int i = 0; i < groups.length; i++) {
                result.add(new SourceGroupKey(groups[i]));
            }
            if (!project.kind().isLibrary()) {
                result.add(new ScriptsWebPagesKey());
            }
            result.add(new ImportantFilesKey());
            result.add(new LibrariesKey());
            return result;
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public Node node(JCKey key) {
            if (key instanceof SourceGroupKey) {
                SourceGroupKey sgKey = (SourceGroupKey) key;
                try {
                    return new PackageViewFilterNode(sgKey.group, project);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (key instanceof ImportantFilesKey) {
                return new ImportantFilesNode(project);
            } else if (key instanceof LibrariesKey) {
//                return new LibrariesNode(project);
                return new DependenciesNode(project);
            } else if (key instanceof ScriptsWebPagesKey) {
                try {
                    return new ScriptsNode(project.getProjectDirectory().getFileObject(
                            project.kind().isApplet() ? "scripts" : "html"), project); //NOI18N
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            throw new AssertionError(key.getClass());
        }

        public void addNotify() {
            getSources().addChangeListener(this);
        }

        public void removeNotify() {
            getSources().removeChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            // setKeys(getKeys());
            // The caller holds ProjectManager.mutex() read lock
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    changeSupport.fireChange();
                }
            });
        }

        private Sources getSources() {
            return ProjectUtils.getSources(project);
        }
    }

    private static interface JCKey {
    }

    private static class ImportantFilesKey implements JCKey {
    }

    private static class LibrariesKey implements JCKey {
    }

    private static class ScriptsWebPagesKey implements JCKey {
    }

    private static class ScriptsNode extends FilterNode {

        ScriptsNode(FileObject fileObject, JCProject project) throws DataObjectNotFoundException {
            this(fileObject == null ? Node.EMPTY : DataObject.find(fileObject).getNodeDelegate(), project);
        }

        private ScriptsNode(Node n, JCProject project) {
            super(n, new FilterNode.Children(n), new ProxyLookup(n.getLookup(), Lookups.fixed(project)));
            disableDelegation(DELEGATE_GET_NAME);
            disableDelegation(DELEGATE_GET_SHORT_DESCRIPTION);
            disableDelegation(DELEGATE_GET_DISPLAY_NAME);
            disableDelegation(DELEGATE_GET_ACTIONS);
            disableDelegation(DELEGATE_SET_NAME);
            disableDelegation(DELEGATE_SET_SHORT_DESCRIPTION);
            disableDelegation(DELEGATE_SET_DISPLAY_NAME);
            String key = project.kind().isApplet() ? "SCRIPTS_NODE_NAME" : "WEB_PAGES_NODE_NAME"; //NOI18N
            setDisplayName(NbBundle.getMessage(ScriptsNode.class, key));
        }

        @Override
        public Action[] getActions(boolean context) {
            JCProject p = getLookup().lookup(JCProject.class);
            return new Action[]{new AddTemplateAction(p.kind() == ProjectKind.WEB),
                        SystemAction.get(NewTemplateAction.class)};
        }

        @Override
        public String getHtmlDisplayName() {
            if (getLookup().lookup(DataObject.class) == null) {
                //dir missing
                return "<font color=\"nb.errorForeground\">" + getDisplayName();
            }
            return null;
        }

        @Override
        public Image getIcon(int type) {
            JCProject p = getLookup().lookup(JCProject.class);
            return ImageUtilities.loadImage(p.kind().isApplet() ? "org/netbeans/modules/javacard/resources/scripts.png" : //NOI18N
                    "org/netbeans/modules/javacard/resources/webpages.png"); //NOI18N
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }

    private static final class AddTemplateAction extends Single<DataFolder> {

        private final boolean html;

        AddTemplateAction(boolean html) {
            super(DataFolder.class);
            String key = html ? "ACTION_NEW_HTML" : "ACTION_NEW_SCRIPT"; //NOI18N
            putValue(NAME, NbBundle.getMessage(AddTemplateAction.class, key));
            this.html = html;
        }

        @Override
        protected void actionPerformed(DataFolder target) {
            DataObject tpl = getTemplate();
            assert tpl != null;
            NotifyDescriptor.InputLine line = new NotifyDescriptor.InputLine(
                    NbBundle.getMessage(AddTemplateAction.class,
                    "TTL_NEW_FILE_ACTION"), //NOI18N
                    NbBundle.getMessage(AddTemplateAction.class,
                    "NEW_FILE_ACTION", tpl.getName())); //NOI18
            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(line))) {
                String filename = line.getInputText();
                try {
                    DataObject ob = tpl.createFromTemplate(target, filename);
                    EditCookie ec = ob.getLookup().lookup(EditCookie.class);
                    if (ec == null) {
                        OpenCookie oc = ob.getLookup().lookup(OpenCookie.class);
                        if (oc != null) {
                            oc.open();
                        }
                    } else {
                        ec.edit();
                    }

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                            ex.getLocalizedMessage());
                }
            }
        }

        private DataObject getTemplate() {
            String template = html ? "Templates/Other/html.html" : "Templates/javacard/APDUFileTemplate.scr";
            FileObject fo = FileUtil.getConfigFile(template);
            if (fo != null) {
                try {
                    return DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        }

        @Override
        protected boolean isEnabled(DataFolder target) {
            return getTemplate() != null && target.getPrimaryFile().canWrite();
        }
    }

    private static class SourceGroupKey implements JCKey {

        public final SourceGroup group;
        public final FileObject fileObject;

        SourceGroupKey(SourceGroup group) {
            this.group = group;
            this.fileObject = group.getRootFolder();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            String disp = this.group.getDisplayName();
            hash = 79 * hash + (fileObject != null ? fileObject.hashCode() : 0);
            hash = 79 * hash + (disp != null ? disp.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceGroupKey)) {
                return false;
            } else {
                SourceGroupKey otherKey = (SourceGroupKey) obj;

                if (fileObject != otherKey.fileObject &&
                        (fileObject == null || !fileObject.equals(otherKey.fileObject))) {
                    return false;
                }
                String thisDisplayName = this.group.getDisplayName();
                String otherDisplayName = otherKey.group.getDisplayName();
                boolean oneNull = thisDisplayName == null;
                boolean twoNull = otherDisplayName == null;
                if (oneNull != twoNull || thisDisplayName != null && !thisDisplayName.equals(otherDisplayName)) {
                    return false;
                }
                return true;
            }
        }
    }

    private static class LibrariesNode extends AbstractNode {

        LibrariesNode(JCProject project) {
            super(Children.create(new LibrariesChildFactory(project), true), Lookups.singleton(project));
            setDisplayName(NbBundle.getMessage(LibrariesNode.class, "LIBRARIES")); //NOI18N
        }

        @Override
        public Image getIcon(int type) {
            Icon icon = UIManager.getIcon("Tree.closedIcon"); //NOI18N
            Image img = icon == null ? super.getIcon(type) : ImageUtilities.icon2Image(icon);
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/javacard/resources/libraries-badge.png"); //NOI18N
            Image result = ImageUtilities.mergeImages(img, badge, 8, 8);
            return result;
        }

        @Override
        public Image getOpenedIcon(int type) {
            Icon icon = UIManager.getIcon("Tree.openIcon"); //NOI18N
            Image img = icon == null ? super.getOpenedIcon(type) : ImageUtilities.icon2Image(icon);
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/javacard/resources/libraries-badge.png"); //NOI18N
            Image result = ImageUtilities.mergeImages(img, badge, 8, 8);
            return result;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{new AddProjectAction(), new AddJARAction()};
        }
    }

    public static class AddJARAction extends Single<JCProject> {

        AddJARAction() {
            super(JCProject.class, NbBundle.getMessage(AddJARAction.class, "ACTION_ADD_JAR"), null);
        }

        @Override
        protected void actionPerformed(final JCProject target) {
            File[] files;
            if ((files = new FileChooserBuilder(LibrariesNode.class).setFileFilter(new JarOrDirectoryFilter()).setTitle(getValue(NAME).toString()).showMultiOpenDialog()) != null) {
                if (files.length > 0) {
                    target.getLookup().lookup(LibrariesManager.class).addToProjectClasspath(files);
                }
            }
        }
    }

    public static final class AddProjectAction extends Single<JCProject> {

        AddProjectAction() {
            super(JCProject.class, NbBundle.getMessage(AddJARAction.class, "ACTION_ADD_PROJECT"), null);
        }

        @Override
        protected void actionPerformed(JCProject target) {
            JFileChooser chooser = ProjectChooser.projectChooser();
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(WindowManager.getDefault().getMainWindow())) {
                File[] files = chooser.getSelectedFiles();
                target.getLookup().lookup(LibrariesManager.class).addProjectsToClasspath(files);
            }
        }
    }

    private static class LibrariesChildFactory extends ChildFactory.Detachable<File> implements ChangeListener {

        private JCProject project;

        LibrariesChildFactory(JCProject project) {
            this.project = project;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            SubprojectProvider prov = project.getLookup().lookup(SubprojectProvider.class);
            prov.addChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            SubprojectProvider prov = project.getLookup().lookup(SubprojectProvider.class);
            prov.removeChangeListener(this);
            super.removeNotify();
        }

        @Override
        protected boolean createKeys(List<File> l) {
            l.addAll(project.getLookup().lookup(LibrariesManager.class).getSubprojectArtifacts(true, true));
            return true;
        }

        @Override
        protected Node createNodeForKey(File key) {
            return key instanceof ErrFile ? new ErrNode(((ErrFile) key).val, project)
                    : new LibNode(key, project);
        }

        @Override
        protected Node createWaitNode() {
            return new WaitNode();
        }

        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }
    }

    private static final class ErrNode extends AbstractNode {

        ErrNode(String uri, JCProject project) {
            super(Children.LEAF, Lookups.singleton(project));
            setDisplayName(uri);
            setName(uri);
            setIconBaseWithExtension("org/netbeans/modules/javacard/resources/libraries.gif"); //NOI18N
        }

        @Override
        public String getHtmlDisplayName() {
            String unk = NbBundle.getMessage(ErrNode.class, "BAD_LIB_REF"); //NOI18N
            return unk + "<font color='!nb.errorForeground'>" + getDisplayName(); //NOI18N
        }

        @Override
        public Action[] getActions(boolean ignored) {
            return new Action[]{new RemoveClasspathEntryAction(this, new File(getName()))};
        }

        @Override
        public Image getIcon(int type) {
            Image result = super.getIcon(type);
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/javacard/resources/brokenProjectBadge.png"); //NOI18N
            return ImageUtilities.mergeImages(result, badge, 8, 8);
        }
    }

    private static final class OpenProjectAction extends Single<File> {

        OpenProjectAction() {
            super(File.class, NbBundle.getMessage(OpenProjectAction.class,
                    "ACTION_OPEN_PROJECT"), null); //NOI18N
        }

        @Override
        protected void actionPerformed(File target) {
            while (target != null && !target.exists()) {
                target = target.getParentFile();
            }
            Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(FileUtil.normalizeFile(target)));
            if (p != null) {
                OpenProjects.getDefault().open(new Project[]{p}, false);
            }
        }

        @Override
        protected boolean isEnabled(File target) {
            while (target != null && !target.exists()) {
                target = target.getParentFile();
            }
            if (target == null) {
                return false;
            }
            Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(FileUtil.normalizeFile(target)));
            if (p == null) {
                return false;
            }
            return !OpenProjects.getDefault().isProjectOpen(p);
        }
    }

    private static final class RemoveClasspathEntryAction extends Single<JCProject> {

        private final File file;
        private Node n;

        RemoveClasspathEntryAction(Node n, File file) {
            super(JCProject.class, NbBundle.getMessage(RemoveClasspathEntryAction.class, "ACTION_REMOVE"), null); //NOI18N
            this.file = file;
            this.n = n;
        }

        @Override
        protected void actionPerformed(JCProject target) {
            String toRemove = file instanceof ErrFile ? ((ErrFile) file).val : file.getAbsolutePath();
            target.getLookup().lookup(LibrariesManager.class).removeFileFromClasspath(toRemove);
            try {
                if (n instanceof ErrNode) {
                    ((ErrNode) n).destroy();
                } else if (n instanceof LibNode) {
                    ((LibNode) n).destroy();
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    private static final class LibNode extends AbstractNode implements FileChangeListener {

        LibNode(File file, JCProject project) {
            super(Children.LEAF, Lookups.fixed(file, project));
            setIconBaseWithExtension("org/netbeans/modules/javacard/resources/libraries.gif"); //NOI18N
            setDisplayName(file.getAbsolutePath());
            setName(file.getAbsolutePath());
            FileUtil.addFileChangeListener(this, file);
        }

        @Override
        public Image getIcon(int type) {
            //Try to get the owning project's icon
            File f = getLookup().lookup(File.class);
            while (!f.exists() && f.getParentFile() != null) {
                f = f.getParentFile();
            }
            if (f != null) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                if (fo != null) {
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p != null) {
                        ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
                        if (info != null) {
                            Icon icon = info.getIcon();
                            if (icon != null) {
                                return ImageUtilities.icon2Image(icon);
                            }
                        }
                    }
                }
            }
            return super.getIcon(type);
        }

        @Override
        public Action[] getActions(boolean ignored) {
            return new Action[]{new OpenProjectAction(), null,
                        new RemoveClasspathEntryAction(this, getLookup().lookup(File.class))};
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getHtmlDisplayName() {
            File f = getLookup().lookup(File.class);
            if (f != null && !f.exists()) {
                return "<font color='!nb.errorForeground'>" + getDisplayName(); //NOI18N
                }
            return null;
        }

        public void fileFolderCreated(FileEvent fe) {
            fireDisplayNameChange(null, getDisplayName());
            fireIconChange();
        }

        public void fileDataCreated(FileEvent fe) {
            fireDisplayNameChange(null, getDisplayName());
            fireIconChange();
        }

        public void fileChanged(FileEvent fe) {
            //do nothing
            }

        public void fileDeleted(FileEvent fe) {
            fireDisplayNameChange(null, getDisplayName());
            fireIconChange();
        }

        public void fileRenamed(FileRenameEvent fe) {
            //do nothing
            }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            //do nothing
            }
    }

    /** Yet another cool filter node just to add properties action
     */
    private static class PackageViewFilterNode extends FilterNode {

        Action[] actions;

        public PackageViewFilterNode(SourceGroup sourceGroup, Project project) throws DataObjectNotFoundException {
            super(sourceGroup instanceof ScriptsSourceGroup ? DataObject.find(sourceGroup.getRootFolder()).getNodeDelegate() : PackageView.createPackageView(sourceGroup));
            if (sourceGroup instanceof ScriptsSourceGroup) {
                super.disableDelegation(DELEGATE_SET_DISPLAY_NAME |
                        DELEGATE_GET_DISPLAY_NAME);
                setDisplayName(sourceGroup.getDisplayName());
            }
        }

        @Override
        public Action[] getActions(boolean context) {
            if (!context) {
                if (actions == null) {
                    Action superActions[] = super.getActions(context);
                    actions = new Action[superActions.length + 1];
                    System.arraycopy(superActions, 0,
                            actions, 0, superActions.length);
                    actions[superActions.length] = null;
                }
                return actions;
            } else {
                return super.getActions(context);
            }
        }
    }
}
