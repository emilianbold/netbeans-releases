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

import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Alexander Simon
 */
final class ViewItemPasteType extends PasteType {

    private static final RequestProcessor RP = new RequestProcessor("ViewItemPasteType", 1); //NOI18N
    private final Folder toFolder;
    private final ViewItemNode viewItemNode;
    private final int type;
    private final MakeLogicalViewProvider provider;

    public ViewItemPasteType(Folder toFolder, ViewItemNode viewItemNode, int type, MakeLogicalViewProvider provider) {
        this.toFolder = toFolder;
        this.viewItemNode = viewItemNode;
        this.type = type;
        this.provider = provider;
    }

    private void copyItemConfigurations(ItemConfiguration[] newConfigurations, ItemConfiguration[] oldConfigurations) {
        // Only allowing copying configurations within same project
        if (newConfigurations == null || oldConfigurations == null) {
            return;
        }        
        assert newConfigurations.length == oldConfigurations.length;
        if (newConfigurations.length == 0 || oldConfigurations.length == 0) {
            return;
        }
        for (int i = 0; i < newConfigurations.length; i++) {
            newConfigurations[i].assignValues(oldConfigurations[i]);
        }
    }

    @Override
    public Transferable paste() throws IOException {
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    pasteImpl();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        return null;
    }

    private Transferable pasteImpl() throws IOException {
        if (!provider.gotMakeConfigurationDescriptor() || !(provider.getMakeConfigurationDescriptor().okToChange())) {
            return null;
        }
        Item item = viewItemNode.getItem();
        ItemConfiguration[] oldConfigurations = item.getItemConfigurations();
//            if (oldConfigurations.length == 0) {
//                // Item may have been removed or renamed inbetween copy and paste
//                return null;
//            }
        if (type == DnDConstants.ACTION_MOVE) {
            // Drag&Drop, Cut&Paste
            if (toFolder.getProject() == viewItemNode.getFolder().getProject()) {
                // Move within same project
                if (toFolder.isDiskFolder()) {
                    FileObject itemFO = item.getFileObject();
                    String toFolderPath = CndPathUtilitities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                    String newName = CndPathUtilitities.createUniqueFileName(toFolderFO, itemFO.getName(), itemFO.getExt());
                    FileObject movedFileFO = FileUtil.moveFile(itemFO, toFolderFO, newName);

                    String itemPath = movedFileFO.getPath();
                    itemPath = CndPathUtilitities.toRelativePath(toFolder.getConfigurationDescriptor().getBaseDir(), itemPath);
                    itemPath = CndPathUtilitities.normalizeSlashes(itemPath);
                    Item movedItem = toFolder.findItemByPath(itemPath);
                    if (movedItem != null) {
                        copyItemConfigurations(movedItem.getItemConfigurations(), oldConfigurations);
                    }
                } else {
                    if (viewItemNode.getFolder().removeItem(item)) {
                        toFolder.addItem(item);
                        copyItemConfigurations(item.getItemConfigurations(), oldConfigurations);
                    }
                }
            } else {
                FileObject itemFO = item.getFileObject();
                if (toFolder.isDiskFolder()) {
                    String toFolderPath = CndPathUtilitities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                    String newName = CndPathUtilitities.createUniqueFileName(toFolderFO, itemFO.getName(), itemFO.getExt());
                    FileObject movedFileFO = FileUtil.moveFile(itemFO, toFolderFO, newName);
                    if (!item.getFolder().isDiskFolder()) {
                        if (viewItemNode.getFolder().removeItemAction(item)) {
                        }
                    }
                } else {
                    if (toFolder.getConfigurationDescriptor().getBaseDirFileSystem().equals(itemFO.getFileSystem()) &&
                        (CndPathUtilitities.isPathAbsolute(item.getPath()) || item.getPath().startsWith(".."))) { // NOI18N
                        if (CndPathUtilitities.isPathAbsolute(item.getPath())) {
                            if (viewItemNode.getFolder().removeItem(item)) {
                                toFolder.addItem(item);
                            }
                        } else {
                            String originalFilePath = viewItemNode.getFolder().getProject().getProjectDirectory().getPath();
                            String newFilePath = toFolder.getProject().getProjectDirectory().getPath();
                            String fromNewToOriginal = CndPathUtilitities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                            fromNewToOriginal = CndPathUtilitities.normalizeSlashes(fromNewToOriginal);
                            String newPath = fromNewToOriginal + item.getPath();
                            newPath = CndPathUtilitities.trimDotDot(newPath);
                            if (viewItemNode.getFolder().removeItemAction(item)) {
                                toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilitities.normalizeSlashes(newPath)));
                            }
                        }
                    } else {
                        Project toProject = toFolder.getProject();
                        FileObject fo = item.getFileObject();
                        String newName = CndPathUtilitities.createUniqueFileName(toProject.getProjectDirectory(), fo.getName(), fo.getExt());
                        FileObject copy = fo.copy(toProject.getProjectDirectory(), newName, fo.getExt());
                        String newPath = CndPathUtilitities.toRelativePath(toProject.getProjectDirectory().getPath(), copy.getPath());
                        if (viewItemNode.getFolder().removeItemAction(item)) {
                            fo.delete();
                            toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilitities.normalizeSlashes(newPath)));
                        }
                    }
                }
            }
        } else if (type == DnDConstants.ACTION_COPY || type == DnDConstants.ACTION_NONE) {
            // Copy&Paste
            if (toFolder.getProject() == viewItemNode.getFolder().getProject()) {
                if ((CndPathUtilitities.isPathAbsolute(item.getPath()) || item.getPath().startsWith("..")) && !toFolder.isDiskFolder()) { // NOI18N
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    FileObject fo = item.getFileObject();
                    String ext = fo.getExt();
                    if (toFolder.isDiskFolder()) {
                        String toFolderPath = CndPathUtilitities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                        FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                        String newName = CndPathUtilitities.createUniqueFileName(toFolderFO, fo.getName(), ext);
                        FileObject copiedFileObject = fo.copy(toFolderFO, newName, ext);

                        String itemPath = copiedFileObject.getPath();
                        itemPath = CndPathUtilitities.toRelativePath(toFolder.getConfigurationDescriptor().getBaseDir(), itemPath);
                        itemPath = CndPathUtilitities.normalizeSlashes(itemPath);
                        Item copiedItemItem = toFolder.findItemByPath(itemPath);
                        if (copiedItemItem != null) {
                            copyItemConfigurations(copiedItemItem.getItemConfigurations(), oldConfigurations);
                        }
                    } else {
                        String parent = fo.getParent().getPath();
                        String newName = CndPathUtilitities.createUniqueFileName(fo.getParent(), fo.getName(), ext);
                        fo.copy(fo.getParent(), newName, ext);
                        String newPath = parent + "/" + newName; // NOI18N
                        if (ext.length() > 0) {
                            newPath = newPath + "." + ext; // NOI18N
                        }
                        newPath = CndPathUtilitities.toRelativePath(viewItemNode.getFolder().getProject().getProjectDirectory().getPath(), newPath);
                        Item newItem = Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilitities.normalizeSlashes(newPath));
                        toFolder.addItemAction(newItem);
                        copyItemConfigurations(newItem.getItemConfigurations(), oldConfigurations);
                    }
                }
            } else {
                FileObject fo = item.getFileObject();
                if (toFolder.isDiskFolder()) {
                    String ext = fo.getExt();
                    String toFolderPath = CndPathUtilitities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(),toFolderPath);
                    String newName = CndPathUtilitities.createUniqueFileName(toFolderFO, fo.getName(), ext);
                    fo.copy(toFolderFO, newName, ext);
                } else {
                    if (toFolder.getConfigurationDescriptor().getBaseDirFileSystem().equals(fo.getFileSystem()) &&
                        (CndPathUtilitities.isPathAbsolute(item.getPath()) || item.getPath().startsWith(".."))) { // NOI18N
                        if (CndPathUtilitities.isPathAbsolute(item.getPath())) {
                            toFolder.addItem(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), item.getPath()));
                        } else {
                            String originalFilePath = viewItemNode.getFolder().getProject().getProjectDirectory().getPath();
                            String newFilePath = toFolder.getProject().getProjectDirectory().getPath();
                            String fromNewToOriginal = CndPathUtilitities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                            fromNewToOriginal = CndPathUtilitities.normalizeSlashes(fromNewToOriginal);
                            String newPath = fromNewToOriginal + item.getPath();
                            newPath = CndPathUtilitities.trimDotDot(newPath);
                            toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilitities.normalizeSlashes(newPath)));
                        }
                    } else {
                        Project toProject = toFolder.getProject();
                        String ext = fo.getExt();
                        String newName = CndPathUtilitities.createUniqueFileName(toProject.getProjectDirectory(), fo.getName(), ext);
                        fo.copy(toProject.getProjectDirectory(), newName, ext);
                        String newPath = newName;
                        if (ext.length() > 0) {
                            newPath = newPath + "." + ext; // NOI18N
                        }
                        toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilitities.normalizeSlashes(newPath))); // NOI18N
                    }
                }
            }
        }
        return null;
    }
}
