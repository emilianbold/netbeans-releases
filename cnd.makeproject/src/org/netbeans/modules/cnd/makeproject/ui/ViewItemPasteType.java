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
import org.netbeans.modules.cnd.utils.CndPathUtilities;
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
    private final Folder fromFolder;
    private final Item fromItem;
    private final int type;
    private final MakeLogicalViewProvider provider;

    ViewItemPasteType(Folder toFolder, ViewItemNode viewItemNode, int type, MakeLogicalViewProvider provider) {
        this.toFolder = toFolder;
        fromItem = viewItemNode.getItem();
        fromFolder = viewItemNode.getFolder();
        this.type = type;
        this.provider = provider;
    }
    
    ViewItemPasteType(Folder toFolder, Folder fromFolder, Item fromItem, int type, MakeLogicalViewProvider provider) {
        this.toFolder = toFolder;
        this.fromItem = fromItem;
        this.fromFolder = fromFolder;
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
            if (oldConfigurations[i] != null && newConfigurations[i] != null) {
                newConfigurations[i].assignValues(oldConfigurations[i]);
            }
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

    Transferable pasteImpl() throws IOException {
        if (!provider.gotMakeConfigurationDescriptor() || !(provider.getMakeConfigurationDescriptor().okToChange())) {
            return null;
        }
        ItemConfiguration[] oldConfigurations = fromItem.getItemConfigurations();
        if (type == DnDConstants.ACTION_MOVE) {
            // Drag&Drop, Cut&Paste
            if (toFolder.getProject() == fromFolder.getProject()) {
                // Move within same project
                if (toFolder.isDiskFolder()) {
                    FileObject itemFO = fromItem.getFileObject();
                    String toFolderPath = CndPathUtilities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDirFileObject(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                    String newName = CndPathUtilities.createUniqueFileName(toFolderFO, itemFO.getName(), itemFO.getExt());
                    FileObject movedFileFO = FileUtil.moveFile(itemFO, toFolderFO, newName);

                    String itemPath = movedFileFO.getPath();
                    itemPath = CndPathUtilities.toRelativePath(toFolder.getConfigurationDescriptor().getBaseDir(), itemPath);
                    itemPath = CndPathUtilities.normalizeSlashes(itemPath);
                    Item movedItem = toFolder.findItemByPath(itemPath);
                    if (movedItem != null) {
                        copyItemConfigurations(movedItem.getItemConfigurations(), oldConfigurations);
                    }
                } else {
                    if (fromFolder.removeItem(fromItem)) {
                        toFolder.addItem(fromItem);
                        copyItemConfigurations(fromItem.getItemConfigurations(), oldConfigurations);
                    }
                }
            } else {
                FileObject itemFO = fromItem.getFileObject();
                if (toFolder.isDiskFolder()) {
                    String toFolderPath = CndPathUtilities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDirFileObject(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                    String newName = CndPathUtilities.createUniqueFileName(toFolderFO, itemFO.getName(), itemFO.getExt());
                    FileObject movedFileFO = FileUtil.moveFile(itemFO, toFolderFO, newName);
                    if (!fromItem.getFolder().isDiskFolder()) {
                        if (fromFolder.removeItemAction(fromItem)) {
                        }
                    }
                } else {
                    if (toFolder.getConfigurationDescriptor().getBaseDirFileSystem().equals(itemFO.getFileSystem()) &&
                        (CndPathUtilities.isPathAbsolute(fromItem.getPath()) || fromItem.getPath().startsWith(".."))) { // NOI18N
                        if (CndPathUtilities.isPathAbsolute(fromItem.getPath())) {
                            if (fromFolder.removeItem(fromItem)) {
                                toFolder.addItem(fromItem);
                            }
                        } else {
                            String originalFilePath = fromFolder.getProject().getProjectDirectory().getPath();
                            String newFilePath = toFolder.getProject().getProjectDirectory().getPath();
                            String fromNewToOriginal = CndPathUtilities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                            fromNewToOriginal = CndPathUtilities.normalizeSlashes(fromNewToOriginal);
                            String newPath = fromNewToOriginal + fromItem.getPath();
                            newPath = CndPathUtilities.trimDotDot(newPath);
                            if (fromFolder.removeItemAction(fromItem)) {
                                toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilities.normalizeSlashes(newPath)));
                            }
                        }
                    } else {
                        Project toProject = toFolder.getProject();
                        FileObject fo = fromItem.getFileObject();
                        String newName = CndPathUtilities.createUniqueFileName(toProject.getProjectDirectory(), fo.getName(), fo.getExt());
                        FileObject copy = fo.copy(toProject.getProjectDirectory(), newName, fo.getExt());
                        String newPath = CndPathUtilities.toRelativePath(toProject.getProjectDirectory().getPath(), copy.getPath());
                        if (fromFolder.removeItemAction(fromItem)) {
                            fo.delete();
                            toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilities.normalizeSlashes(newPath)));
                        }
                    }
                }
            }
        } else if (type == DnDConstants.ACTION_COPY || type == DnDConstants.ACTION_NONE) {
            // Copy&Paste
            if (toFolder.getProject() == fromFolder.getProject()) {
                if ((CndPathUtilities.isPathAbsolute(fromItem.getPath()) || fromItem.getPath().startsWith("..")) && !toFolder.isDiskFolder()) { // NOI18N
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    FileObject fo = fromItem.getFileObject();
                    String ext = fo.getExt();
                    if (toFolder.isDiskFolder()) {
                        String toFolderPath = CndPathUtilities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDirFileObject(), toFolder.getRootPath());
                        FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                        String newName = CndPathUtilities.createUniqueFileName(toFolderFO, fo.getName(), ext);
                        FileObject copiedFileObject = fo.copy(toFolderFO, newName, ext);

                        String itemPath = copiedFileObject.getPath();
                        itemPath = CndPathUtilities.toRelativePath(toFolder.getConfigurationDescriptor().getBaseDir(), itemPath);
                        itemPath = CndPathUtilities.normalizeSlashes(itemPath);
                        Item copiedItemItem = toFolder.findItemByPath(itemPath);
                        if (copiedItemItem != null) {
                            copyItemConfigurations(copiedItemItem.getItemConfigurations(), oldConfigurations);
                        }
                    } else {
                        String parent = fo.getParent().getPath();
                        String newName = CndPathUtilities.createUniqueFileName(fo.getParent(), fo.getName(), ext);
                        fo.copy(fo.getParent(), newName, ext);
                        String newPath = parent + "/" + newName; // NOI18N
                        if (ext.length() > 0) {
                            newPath = newPath + "." + ext; // NOI18N
                        }
                        newPath = CndPathUtilities.toRelativePath(fromFolder.getProject().getProjectDirectory().getPath(), newPath);
                        Item newItem = Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilities.normalizeSlashes(newPath));
                        toFolder.addItemAction(newItem);
                        copyItemConfigurations(newItem.getItemConfigurations(), oldConfigurations);
                    }
                }
            } else {
                FileObject fo = fromItem.getFileObject();
                if (toFolder.isDiskFolder()) {
                    String ext = fo.getExt();
                    String toFolderPath = CndPathUtilities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDirFileObject(), toFolder.getRootPath());
                    FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(),toFolderPath);
                    String newName = CndPathUtilities.createUniqueFileName(toFolderFO, fo.getName(), ext);
                    fo.copy(toFolderFO, newName, ext);
                } else {
                    if (toFolder.getConfigurationDescriptor().getBaseDirFileSystem().equals(fo.getFileSystem()) &&
                        (CndPathUtilities.isPathAbsolute(fromItem.getPath()) || fromItem.getPath().startsWith(".."))) { // NOI18N
                        if (CndPathUtilities.isPathAbsolute(fromItem.getPath())) {
                            toFolder.addItem(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), fromItem.getPath()));
                        } else {
                            String originalFilePath = fromFolder.getProject().getProjectDirectory().getPath();
                            String newFilePath = toFolder.getProject().getProjectDirectory().getPath();
                            String fromNewToOriginal = CndPathUtilities.getRelativePath(newFilePath, originalFilePath) + "/"; // NOI18N
                            fromNewToOriginal = CndPathUtilities.normalizeSlashes(fromNewToOriginal);
                            String newPath = fromNewToOriginal + fromItem.getPath();
                            newPath = CndPathUtilities.trimDotDot(newPath);
                            toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilities.normalizeSlashes(newPath)));
                        }
                    } else {
                        Project toProject = toFolder.getProject();
                        String ext = fo.getExt();
                        String newName = CndPathUtilities.createUniqueFileName(toProject.getProjectDirectory(), fo.getName(), ext);
                        fo.copy(toProject.getProjectDirectory(), newName, ext);
                        String newPath = newName;
                        if (ext.length() > 0) {
                            newPath = newPath + "." + ext; // NOI18N
                        }
                        toFolder.addItemAction(Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), CndPathUtilities.normalizeSlashes(newPath))); // NOI18N
                    }
                }
            }
        }
        return null;
    }
}
