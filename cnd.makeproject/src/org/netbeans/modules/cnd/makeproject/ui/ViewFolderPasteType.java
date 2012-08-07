/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.makeproject.api.configurations.*;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Alexander Simon
 */
final class ViewFolderPasteType  extends PasteType {

    private static final RequestProcessor RP = new RequestProcessor("ViewFolderPasteType", 1); //NOI18N
    private final Folder toFolder;
    private final LogicalFolderNode viewFolderNode;
    private final int type;
    private final MakeLogicalViewProvider provider;

    public ViewFolderPasteType(Folder toFolder, LogicalFolderNode viewFolderNode, int type, MakeLogicalViewProvider provider) {
        this.toFolder = toFolder;
        this.viewFolderNode = viewFolderNode;
        this.type = type;
        this.provider = provider;
    }

    private void copyItemConfigurations(ItemConfiguration[] newConfigurations, ItemConfiguration[] oldConfigurations) {
        // Only allowing copying configurations within same project
        if (newConfigurations == null || oldConfigurations == null) {
            return;
        }
        assert newConfigurations.length == oldConfigurations.length;
        for (int i = 0; i < newConfigurations.length; i++) {
            newConfigurations[i].assignValues(oldConfigurations[i]);
        }
    }
    
    private FileObject getFolderFileObject(Folder folder) {
        String rootPath = folder.getRootPath();
        return RemoteFileUtil.getFileObject(folder.getConfigurationDescriptor().getBaseDirFileObject(), rootPath);
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
        Folder folder = viewFolderNode.getFolder();
        //ItemConfiguration[] oldConfigurations = folder.getItemConfigurations();
//            if (oldConfigurations.length == 0) {
//                // Item may have been removed or renamed inbetween copy and paste
//                return null;
//            }
        if (type == DnDConstants.ACTION_MOVE) {
            // Drag&Drop, Cut&Paste
                // Move within same project
            if (toFolder.isDiskFolder()) {
                FileObject itemFO = getFolderFileObject(folder);

                String toFolderPath = CndPathUtilitities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileObject().getFileSystem(), toFolderPath); // should it be normalized?
                String newName = CndPathUtilitities.createUniqueFileName(toFolderFO, itemFO.getNameExt(), ""); // NOI18N
                final FileLock lock = itemFO.lock();
                try {
                    FileObject movedFileFO = itemFO.move(lock, toFolderFO, newName, ""); // NOI18N
                    Folder movedFolder = toFolder.findFolderByAbsolutePath(movedFileFO.getPath());
                    if (toFolder.getProject() == viewFolderNode.getFolder().getProject()) {
                        if (movedFolder != null) {
                            //copyItemConfigurations(movedItem.getItemConfigurations(), oldConfigurations);
                        }
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } else if (type == DnDConstants.ACTION_COPY || type == DnDConstants.ACTION_NONE) {
            // Copy&Paste
            if (toFolder.isDiskFolder()) {
                FileObject fo = getFolderFileObject(folder);
                String toFolderPath = CndPathUtilitities.toAbsolutePath(toFolder.getConfigurationDescriptor().getBaseDir(), toFolder.getRootPath());
                FileObject toFolderFO = CndFileUtils.toFileObject(toFolder.getConfigurationDescriptor().getBaseDirFileSystem(), toFolderPath); // should it be normalized?
                String newName = CndPathUtilitities.createUniqueFileName(toFolderFO, fo.getNameExt(), "");
                FileObject copiedFileObject = fo.copy(toFolderFO, newName, "");

                Folder copiedFolder = toFolder.findFolderByAbsolutePath(copiedFileObject.getPath());
                if (toFolder.getProject() == viewFolderNode.getFolder().getProject()) {
                    if (copiedFolder != null) {
                        //copyItemConfigurations(copiedItemItem.getItemConfigurations(), oldConfigurations);
                    }
                }
            }
        }
        return null;
    }
}
