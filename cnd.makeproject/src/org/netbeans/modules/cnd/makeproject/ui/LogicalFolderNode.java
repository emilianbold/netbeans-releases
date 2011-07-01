/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.actions.AddExistingFolderItemsAction;
import org.netbeans.modules.cnd.makeproject.api.actions.AddExistingItemAction;
import org.netbeans.modules.cnd.makeproject.actions.DebugTestAction;
import org.netbeans.modules.cnd.makeproject.api.actions.NewFolderAction;
import org.netbeans.modules.cnd.makeproject.actions.NewTestActionFactory;
import org.netbeans.modules.cnd.makeproject.actions.RunTestAction;
import org.netbeans.modules.cnd.makeproject.actions.StepIntoTestAction;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.NodeActionFactory.RenameNodeAction;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander Simon
 */
final class LogicalFolderNode extends AnnotatedNode implements ChangeListener {

    private final Folder folder;
    private final MakeLogicalViewProvider provider;
    private final String pathPostfix;

    public LogicalFolderNode(Node folderNode, Folder folder, MakeLogicalViewProvider provider) {
        super(new LogicalViewChildren(folder, provider), createLFNLookup(folderNode, folder, provider), MakeLogicalViewProvider.ANNOTATION_RP);
        this.folder = folder;
        this.provider = provider;
        String postfix = "";
        if (folder != null && folder.getRoot() != null) {
            String absPath = folder.getAbsolutePath();
//            String AbsRootPath = CndPathUtilitities.toAbsolutePath(provider.getMakeConfigurationDescriptor().getBaseDir(), folder.getRoot());
//            AbsRootPath = RemoteFileUtil.normalizeAbsolutePath(AbsRootPath, provider.getProject());
//            FileObject folderFile = RemoteFileUtil.getFileObject(AbsRootPath, provider.getProject());
            if (absPath != null) {
                postfix = " - " + absPath; // NOI18N
            }
        }
        pathPostfix = postfix;
        setForceAnnotation(true);
        updateAnnotationFiles();
    }

    private static Lookup createLFNLookup(Node folderNode, Folder folder, MakeLogicalViewProvider provider) {
        List<Object> elems = new ArrayList<Object>(3);
        elems.add(folder);
        elems.add(provider.getProject());
        elems.add(new FolderSearchInfo(folder));
        if (folder.isDiskFolder()) {
            MakeConfigurationDescriptor conf = folder.getConfigurationDescriptor();
            if (conf != null) {
                String rootPath = folder.getRootPath();
                FileObject fo = RemoteFileUtil.getFileObject(conf.getBaseDirFileObject(), rootPath);
                if (fo != null /*paranoia*/ && fo.isValid() && fo.isFolder()) {
                    DataFolder dataFolder = DataFolder.findFolder(fo);
                    if (dataFolder != null) {
                        elems.add(dataFolder);
                    }
                }
            }
        }
        return Lookups.fixed(elems.toArray());
    }

    private void updateAnnotationFiles() {
        MakeLogicalViewProvider.ANNOTATION_RP.post(new FileAnnotationUpdater(this));
    }

    private final class FileAnnotationUpdater implements Runnable {

        private LogicalFolderNode logicalFolderNode;

        FileAnnotationUpdater(LogicalFolderNode logicalFolderNode) {
            this.logicalFolderNode = logicalFolderNode;
        }

        @Override
        public void run() {
            setFiles(new HashSet<FileObject>() /*Collections.EMPTY_SET*/ /*folder.getAllItemsAsFileObjectSet(true)*/); // See IZ 100394 for details
            List<Folder> allFolders = new ArrayList<Folder>();
            allFolders.add(folder);
            allFolders.addAll(folder.getAllFolders(true));
            Iterator<Folder> iter = allFolders.iterator();
            while (iter.hasNext()) {
                iter.next().addChangeListener(logicalFolderNode);
            }
        }
    }

    private final class VisualUpdater implements Runnable {

        @Override
        public void run() {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    /*
     * Something in the folder has changed
     **/

    @Override
    public void stateChanged(ChangeEvent e) {
        updateAnnotationFiles();
        EventQueue.invokeLater(new VisualUpdater()); // IZ 151257
//            fireIconChange(); // LogicalFolderNode
//            fireOpenedIconChange();
    }

    public Folder getFolder() {
        return folder;
    }

    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return folder;
        } else if (valstring.equals("Project")) // NOI18N
        {
            return provider.getProject();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        }
        return super.getValue(valstring);
    }

    @Override
    public Image getIcon(int type) {
        if (folder.isTest()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testContainer.gif"), type); // NOI18N
        } else if (folder.isTestRootFolder()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolder.gif"), type); // NOI18N
        } else if (folder.isDiskFolder() && folder.isTestLogicalFolder()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolder.gif"), type); // NOI18N
        } else if (folder.isDiskFolder()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/tree_folder.gif"), type); // NOI18N
        } else {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolder.gif"), type); // NOI18N
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        if (folder.isTest()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testContainer.gif"), type); // NOI18N
        } else if (folder.isTestRootFolder()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolderOpened.gif"), type); // NOI18N
        } else if (folder.isDiskFolder() && folder.isTestLogicalFolder()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/testFolder.gif"), type); // NOI18N
        } else if (folder.isDiskFolder()) {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/tree_folder.gif"), type); // NOI18N
        } else {
            return annotateIcon(ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/logicalFilesFolderOpened.gif"), type); // NOI18N
        }
    }

    @Override
    public String getName() {
        return folder.getDisplayName();
    }

    @Override
    public String getDisplayName() {
        return annotateName(folder.getDisplayName() + pathPostfix);
    }

    @Override
    public void setName(String newName) {
        String oldName = folder.getDisplayName();
        if (folder.isDiskFolder()) {
            String rootPath = folder.getRootPath();
            FileObject fo;
//            if (CndFileUtils.isLocalFileSystem(folder.getConfigurationDescriptor().getBaseDirFileSystem())) {
//                String AbsRootPath = CndPathUtilitities.toAbsolutePath(folder.getConfigurationDescriptor().getBaseDir(), rootPath);
//                fo = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(AbsRootPath));
//            } else {
                // looks like line below is OK for all cases
                fo = RemoteFileUtil.getFileObject(folder.getConfigurationDescriptor().getBaseDirFileObject(), rootPath);
//            }
            if (fo == null /*paranoia*/ || !fo.isValid() || !fo.isFolder()) {
                return;
            }
            try {
                fo.rename(fo.lock(), newName, null);
            } catch (IOException ioe) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ioe.getMessage()));
            }
            return;
        }
        if (folder.getParent() != null && folder.getParent().findFolderByDisplayName(newName) != null) {
            String msg = NbBundle.getMessage(MakeLogicalViewProvider.class, "CANNOT_RENAME", oldName, newName); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return;
        }
        folder.setDisplayName(newName);
        fireDisplayNameChange(oldName, newName);
    }

//        @Override
//        public void setDisplayName(String newName) {
//            setDisplayName(newName);
//        }
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return getFolder().isDiskFolder();
    }

    @Override
    public boolean canCut() {
        return false; // FIXUP
    }

    @Override
    public boolean canCopy() {
        return false; // FIXUP
    }

    @Override
    public void destroy() throws IOException {
        if (!getFolder().isDiskFolder()) {
            return;
        }
        String absPath = CndPathUtilitities.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDir(), getFolder().getRootPath());
        FileObject folderFileObject = CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(absPath));
        if (folderFileObject == null /*paranoia*/ || !folderFileObject.isValid() || !folderFileObject.isFolder()) {
            return;
        }
        folderFileObject.delete();
        super.destroy();
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                return super.getDropType(transferable, action, index);
            }
        }
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable transferable, List<PasteType> list) {
        if (folder.isTestLogicalFolder()) {
            // Don't drop items into a regular test folder (IZ 185173)
            return;
        }
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            if (flavors[i].getSubType().equals(MakeLogicalViewProvider.SUBTYPE)) {
                try {
                    ViewItemNode viewItemNode = (ViewItemNode) transferable.getTransferData(flavors[i]);
                    int type = new Integer(flavors[i].getParameter(MakeLogicalViewProvider.MASK)).intValue();
                    list.add(new ViewItemPasteType(this.getFolder(), viewItemNode, type, provider));
                } catch (Exception e) {
                }
            }
        }
        super.createPasteTypes(transferable, list);
    }

    public void newLogicalFolder() {
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] result;
        ResourceBundle bundle = NbBundle.getBundle(MakeLogicalViewProvider.class);
        if (folder.isTestRootFolder()) {
            result = new Action[]{ //
                        null,
                        ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null),
                        null,
                        NewTestActionFactory.emptyTestFolderAction(),
                        SystemAction.get(NewFolderAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
            result = NodeActionFactory.insertAfter(NewTestActionFactory.getTestCreationActions(folder.getProject()), result);
        } else if (folder.isTestLogicalFolder() && !folder.isDiskFolder()) {
            result = new Action[]{ //
                        null,
                        NewTestActionFactory.emptyTestFolderAction(),
                        SystemAction.get(NewFolderAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        SystemAction.get(RemoveFolderAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
            result = NodeActionFactory.insertAfter(NewTestActionFactory.getTestCreationActions(folder.getProject()), result);
        } else if (folder.isTest()) {
            result = new Action[]{ //
                        CommonProjectActions.newFileAction(), //
                        SystemAction.get(AddExistingItemAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class), //
                        null,
                        SystemAction.get(RunTestAction.class),
                        SystemAction.get(DebugTestAction.class),
                        SystemAction.get(StepIntoTestAction.class),
                        null,
                        SystemAction.get(RemoveFolderAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
        } else if (folder.isDiskFolder()) {
            result = new Action[]{
                        CommonProjectActions.newFileAction(),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        SystemAction.get(CutAction.class),
                        SystemAction.get(CopyAction.class),
                        SystemAction.get(PasteAction.class),
                        null,
                        //                        new RefreshItemAction((LogicalViewChildren) getChildren(), folder, null),
                        //                        null,
                        SystemAction.get(DeleteAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
        } else {
            result = new Action[]{
                        CommonProjectActions.newFileAction(),
                        SystemAction.get(NewFolderAction.class),
                        SystemAction.get(AddExistingItemAction.class),
                        SystemAction.get(AddExistingFolderItemsAction.class),
                        SystemAction.get(org.openide.actions.FindAction.class),
                        null,
                        //                        new RefreshItemAction((LogicalViewChildren) getChildren(), folder, null),
                        //                        null,
                        SystemAction.get(CutAction.class),
                        SystemAction.get(CopyAction.class),
                        SystemAction.get(PasteAction.class),
                        null,
                        SystemAction.get(RemoveFolderAction.class),
                        //                SystemAction.get(RenameAction.class),
                        NodeActionFactory.createRenameAction(),
                        null,
                        SystemAction.get(PropertiesFolderAction.class),};
        }
        // makeproject sensitive actions
        final MakeProjectTypeImpl projectKind = provider.getProject().getLookup().lookup(MakeProjectTypeImpl.class);
        final List<? extends Action> actionsForMakeProject = Utilities.actionsForPath(projectKind.folderActionsPath());
        result = NodeActionFactory.insertAfter(result, actionsForMakeProject.toArray(new Action[actionsForMakeProject.size()]), RenameNodeAction.class);
        result = NodeActionFactory.insertSyncActions(result, RenameNodeAction.class);
        return result;
    }
}
