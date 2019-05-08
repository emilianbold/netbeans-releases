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

package org.netbeans.modules.remote.ui;

import java.awt.Image;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class FileSystemRootNode extends AbstractNode {

    private final ExecutionEnvironment env;
    private static enum Kind {
        HOME,
        MIRROR,
        ROOT,
        DISCONNECTED
    }

    public FileSystemRootNode(ExecutionEnvironment env) {
        super(createChildren(env), Lookups.fixed(env));
        this.env = env;
    }

    @Override
    public Image getOpenedIcon(int type) {
       return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs_open.gif"); // NOI18N
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/fs.gif"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "LBL_FileSystemRootNode");
    }
    
    private static Children createChildren(ExecutionEnvironment env) {
        return Children.create(new FileSystemRootChildren(env), true);
    }

    private static FileObject getRootFileObject(ExecutionEnvironment env) {
        FileSystem fs = FileSystemProvider.getFileSystem(env);
        FileObject fo = fs.getRoot();
        return fo;
    }

    /*package*/ void refresh() {
        setChildren(createChildren(env));
    }

    private static class FileSystemRootChildren extends ChildFactory<Kind> {

        private final ExecutionEnvironment env;
        private final FileObject rootFileObject;

        public FileSystemRootChildren(ExecutionEnvironment env) {
            this.env = env;
            rootFileObject = getRootFileObject(env);
        }

        @Override
        protected boolean createKeys(List<Kind> toPopulate) {
            if (ConnectionManager.getInstance().isConnectedTo(env)) {
                toPopulate.add(Kind.ROOT);
                toPopulate.add(Kind.HOME);
                toPopulate.add(Kind.MIRROR);
            } else {
                toPopulate.add(Kind.DISCONNECTED);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Kind key) {
            FileObject fo = null;
            switch (key) {
                case DISCONNECTED:
                    return new NotConnectedNode(env);
                case HOME:
                    try {
                        String homeDir = HostInfoUtils.getHostInfo(env).getUserDir();
                        fo = rootFileObject.getFileObject(homeDir);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (CancellationException ex) {
                        // don't report CancellationException
                    }
                    break;
                case MIRROR:
                    String mirror = RemotePathMap.getRemoteSyncRoot(env);
                    if (mirror!= null) {
                        fo = rootFileObject.getFileObject(mirror);
                    }
                    break;
                case ROOT:
                    fo = rootFileObject;
                    break;
                default:
                    fo = rootFileObject;
                    break;
            }
            if (fo != null) {
                return new FileSystemNode(env, fo);
            }
        return null; // TODO: error processing
        }
    }
}
