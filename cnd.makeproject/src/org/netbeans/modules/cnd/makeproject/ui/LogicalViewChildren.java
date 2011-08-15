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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.ui.LogicalViewNodeProviders;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
class LogicalViewChildren extends BaseMakeViewChildren implements PropertyChangeListener {

    public LogicalViewChildren(Folder folder, MakeLogicalViewProvider provider) {
        super(folder, provider);
        if (folder != null && folder.isDiskFolder()) {
            MakeOptions.getInstance().addPropertyChangeListener(LogicalViewChildren.this);
        }
    }

    @Override
    protected void onFolderChange(Folder folder) {
        if (folder != null && folder.isDiskFolder()) {
            MakeOptions.getInstance().addPropertyChangeListener(LogicalViewChildren.this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (property.equals(MakeOptions.VIEW_BINARY_FILES)) {
            stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof LoadingNode) {
            //System.err.println("LogicalViewChildren: return wait node");
            node = (Node) key;
        } else if (key instanceof Node) {
            node = (Node) key;
        } else if (key instanceof Folder) {
            Folder folder = (Folder) key;
            if (folder.isProjectFiles() || folder.isTestLogicalFolder() || folder.isTest()) {
                //FileObject srcFileObject = project.getProjectDirectory().getFileObject("src");
                FileObject srcFileObject = getProject().getProjectDirectory();
                DataObject srcDataObject = null;
                try {
                    if (srcFileObject.isValid()) {
                        srcDataObject = DataObject.find(srcFileObject);
                    }
                } catch (DataObjectNotFoundException e) {
                    // Do not throw Exception.
                    // It is normal use case when folder can be deleted at build time.
                    //throw new AssertionError(e);
                }
                if (srcDataObject != null) {
                    node = new LogicalFolderNode(((DataFolder) srcDataObject).getNodeDelegate(), folder, provider);
                } else {
                    // Fix me. Create Broken Folder
                    //node = new BrokenViewFolderNode(this, getFolder(), folder);
                }
            } else {
                node = new ExternalFilesNode(folder, provider);
            }
        } else if (key instanceof Item) {
            Item item = (Item) key;
            DataObject fileDO = item.getDataObject();
            if (fileDO != null) {
                node = new ViewItemNode(this, getFolder(), item, fileDO, provider.getProject());
            } else {
                node = new BrokenViewItemNode(this, getFolder(), item, provider.getProject());
            }
        } else if (key instanceof AbstractNode) {
            node = (AbstractNode) key;
        }
        if (node == null) {
            return new Node[]{};
        }
        return new Node[]{node};
    }

    @Override
    protected Collection<Object> getKeys() {
        Collection<Object> collection;
        if (getFolder().isDiskFolder()) {
            // Search disk folder for C/C++ files and add them to the view (not the project!).
            ArrayList<Object> collection2 = new ArrayList<Object>(getFolder().getElements());
            FileObject fileObject = RemoteFileUtil.getFileObject(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), getFolder().getRootPath());
            if (fileObject != null && fileObject.isValid() && fileObject.isFolder()) {
                FileObject[] children = fileObject.getChildren();
                if (children != null) {
                    for (FileObject child : children) {
                        if (child == null || !child.isValid() || child.isFolder()) {
                            // it's a folder
                            continue;
                        }
                        if (getFolder().findItemByName(child.getNameExt()) != null) {
                            // Already there
                            continue;
                        }
                        if (!VisibilityQuery.getDefault().isVisible(child)) {
                            // not visible
                            continue;
                        }

                        if (!getFolder().isTestLogicalFolder()) {
                            if (!MakeOptions.getInstance().getViewBinaryFiles() && CndFileVisibilityQuery.getDefault().isIgnored(child.getNameExt())) {
                                continue;
                            }
                        }

                        // Add file to the view
                        Item item = Item.createInFileSystem(provider.getMakeConfigurationDescriptor().getBaseDirFileSystem(), child.getPath());
                        Folder.insertItemElementInList(collection2, item);
                    }
                }
            }
            collection = collection2;
        } else {
            collection = getFolder().getElements();
        }

        switch (getFolder().getConfigurationDescriptor().getState()) {
            case READING:
                if (collection.isEmpty()) {
                    collection = Collections.singletonList((Object) new LoadingNode());
                }
                break;
            case BROKEN:
            // TODO show broken node
            }
        if ("root".equals(getFolder().getName())) { // NOI18N
            LogicalViewNodeProvider[] providers = LogicalViewNodeProviders.getInstance().getProvidersAsArray();
            if (providers.length > 0) {
                for (int i = 0; i < providers.length; i++) {
                    AbstractNode node = providers[i].getLogicalViewNode(provider.getProject());
                    if (node != null) {
                        collection.add(node);
                    }
                }
            }
        }

        return collection;
    }
}
