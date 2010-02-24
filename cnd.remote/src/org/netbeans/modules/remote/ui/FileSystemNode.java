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

package org.netbeans.modules.remote.ui;

import java.awt.Image;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.cnd.remote.fs.RemoteFileSystemManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileSystemNode extends AbstractNode {
    private final ExecutionEnvironment env;

    public FileSystemNode(ExecutionEnvironment env) {
        super(createChildren(env), Lookups.fixed(env, getRootFileObject(env)));
        this.env = env;
    }

    private static FileObject getRootFileObject(ExecutionEnvironment env) {
        FileSystem fs = RemoteFileSystemManager.getInstance().get(env);
        FileObject fo = fs.getRoot();
        return fo;
    }

    private static Children createChildren(ExecutionEnvironment env) {
        FileObject fo = getRootFileObject(env);
        return Children.create(new FileSystemChildren(env), true);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "LBL_FileSystemRootNode");
    }

    @Override
    public Image getOpenedIcon(int type) {
       return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs_open.gif"); // NOI18N
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs.gif"); // NOI18N
    }
    
    private static class FileSystemChildren extends ChildFactory<FileObject> {

        private final ExecutionEnvironment env;
        private final FileObject rootFileObject;

        public FileSystemChildren(ExecutionEnvironment env) {
            this.env = env;
            rootFileObject = getRootFileObject(env);
        }


        @Override
        protected boolean createKeys(List<FileObject> toPopulate) {
            // TODO: add "Connect menu item and refresh"
            if (ConnectionManager.getInstance().isConnectedTo(env)) {
                FileObject fo = getRootFileObject(env);
                FileObject[] children = fo.getChildren();
                Collections.addAll(toPopulate, children);
                return true;
            } else {
                toPopulate.add(rootFileObject);
                return true;
            }
        }

        @Override
        protected Node createNodeForKey(FileObject key) {
            if (key == rootFileObject) { // == sic!
                return new NotConnectedNode(env);
            } else {
                try {
                    DataObject dao = DataObject.find(key);
                    Node node = dao.getNodeDelegate();
                    return node;
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                    return null; //TODO: error processing
                }
            }
        }
    }
}
