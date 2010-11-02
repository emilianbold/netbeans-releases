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

package org.netbeans.modules.remote.impl;

import org.netbeans.modules.remote.util.ExecSupport;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.WeakHashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.filesystems.AbstractFileSystem;

/**
 *
 * @author ak119685
 */
public class RemoteFileSystem extends AbstractFileSystem {

    private final ExecutionEnvironment env;
    private final WeakHashMap<String, FileNode> nodesCache = new WeakHashMap<String, FileNode>();
    private final NativeProcessBuilder npb;
    private volatile String root;
    private static final String[] NO_CHILDREN = new String[0];

    public RemoteFileSystem(final ExecutionEnvironment env) {
        this.env = env;
        info = new FSInfo();
        list = new FSList();
        attr = new FSAttr();
        root = "/"; // NOI18N

        npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("/usr/bin/ls"); // NOI18N
        npb.getEnvironment().put("LC_TIME", "C"); // NOI18N
    }

    @Override
    public String getDisplayName() {
        return env.getDisplayName();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    private FileNode getFileNode(String name, boolean ensureChildren) {
        FileNode node = nodesCache.get(name);

//        System.out.println("GET NODE FOR " + name);

        try {
            if (node == null) {
                String parent = getParent(name);
                updateNodesChildren(parent);
                node = nodesCache.get(name);
            }

            // TODO: hack!
            if (ensureChildren && (node == null || node.children == null || node.children.length == 0)) {
                updateNodesChildren(name);
            }

            node = nodesCache.get(name);
        } catch (IOException ex) {
//            System.out.println("INTERRUPTED! " + name + " " + (node==null ? "NULL" : node.name));
            return null;
        }

        return node;
    }

    private FileNode createFileNode(String name) throws IOException {
        String[] children = NO_CHILDREN;

//        System.out.println("createFileNode(" + name + ")");


        npb.setArguments("-laL", root + name); // NOI18N
        FileNode thisNode = nodesCache.get(name);

        try {
            ExecSupport.Status status = ExecSupport.call(npb);

            if (status.isOK()) {
                int idx = 0;
                java.util.List<String> output = status.output;
                children = new String[output.size()];

                for (String line : output) {
                    try {
                        FileNode childNode = new FileNode(line);
                        if (children.length == 1) {
                            thisNode = childNode;
                            thisNode.children = new String[0];

                        } else {
                            if (".".equals(childNode.name)) { // NOI18N
                                thisNode = new FileNode(name, childNode.attrs, childNode.size);
//                                System.out.println("PUT NODE FOR " + name);
                                nodesCache.put(name, thisNode);
                                continue;
                            }

                            if ("..".equals(childNode.name)) { // NOI18N
                                continue;
                            }

                            String cname = childNode.name;
                            if ("".equals(name) || name.endsWith("/")) { // NOI18N
                                cname = name + cname;
                            } else {
                                cname = name + "/" + cname; // NOI18N
                            }

                            nodesCache.put(cname, childNode);
//                            System.out.println("PUT NODE FOR " + cname);

                            children[idx++] = childNode.name;
                        }
                    } catch (IllegalArgumentException ex) {
                    }
                }
            }

            if (thisNode != null) {
                thisNode.children = children;
            }

        } catch (Throwable th) {
            throw new IOException(th.getMessage());
        }

        return thisNode;
    }

    @SuppressWarnings("deprecation") // need to set it for compat
    private void _setSystemName(String s) throws PropertyVetoException {
        setSystemName(s);
    }

    /** Set the root directory of the filesystem.
     * @param r file to set root to
     * @exception PropertyVetoException if the value if vetoed by someone else (usually
     *    by the {@link org.openide.filesystems.Repository Repository})
     * @exception IOException if the root does not exists or some other error occured
     */
    public synchronized void setRootDirectory(String dir) throws PropertyVetoException, IOException {
        String oldDisplayName = getDisplayName();
        _setSystemName(dir);
        firePropertyChange(PROP_ROOT, null, refreshRoot());
        firePropertyChange(PROP_DISPLAY_NAME, oldDisplayName, getDisplayName());
        root = dir;
    }

    private FileNode updateNodesChildren(String name) throws IOException {
        FileNode thisNode = createFileNode(name);
        nodesCache.put(name, thisNode);
//        System.out.println("PUT NODE FOR " + name);
        return thisNode;
    }

    private String getParent(String name) {
        int idx = name.lastIndexOf('/');
        return idx < 0 ? "" : name.substring(0, idx); // NOI18N
    }

    private static class FSAttr implements AbstractFileSystem.Attr {

        public Object readAttribute(String name, String attrName) {
            if (attrName.equals("isRemoteAndSlow")) { // NOI18N
                return true; // #159628
            }
//
//            if ("java.io.File".equals(attrName)) {
//                return name.length() == 0 ? new RemoteFile("/") : new RemoteFile(name);
//            }
//
//            System.out.println("readAttribute(" + name + ", " + attrName + ")");
            return null;
        }

        public void writeAttribute(String name, String attrName, Object value) throws IOException {
//            System.out.println("writeAttribute(" + name + ", " + attrName + ", " + value + ")");
        }

        public Enumeration<String> attributes(String name) {
            return Collections.enumeration(Collections.<String>emptyList());
        }

        public void renameAttributes(String oldName, String newName) {
//            System.out.println("renameAttributes(" + oldName + ", " + newName + ")");
        }

        public void deleteAttributes(String name) {
//            System.out.println("deleteAttributes(" + name + ")");
        }
    }

    private class FSInfo implements AbstractFileSystem.Info {

        public Date lastModified(String name) {
            return new Date();
        }

        public boolean folder(String name) {
            FileNode node = getFileNode(name, false);
            return node == null ? true : node.isFolder();
        }

        public boolean readOnly(String name) {
            return true;
        }

        public String mimeType(String name) {
            return null;
        }

        public long size(String name) {
            FileNode fileNode = getFileNode(name, false);
            return fileNode == null ? 0 : fileNode.size;
        }

        public InputStream inputStream(String name) throws FileNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");// NOI18N
        }

        public OutputStream outputStream(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");// NOI18N
        }

        public void lock(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");// NOI18N
        }

        public void unlock(String name) {
            throw new UnsupportedOperationException("Not supported yet.");// NOI18N
        }

        public void markUnimportant(String name) {
            throw new UnsupportedOperationException("Not supported yet.");// NOI18N
        }
    }

    private class FSList implements AbstractFileSystem.List {

        private FSList() {
        }

        public String[] children(String name) {
            FileNode node = getFileNode(name, true);
            return node == null ? null : node.children;
        }
    }

    private static final class FileNode {

        final String attrs;
        final int size;
        final String name;
        private String[] children;

        public FileNode(String name, String attrs, int size) {
            this.attrs = attrs;
            this.size = size;
            this.name = name;
        }

        FileNode(String lsOutput) throws IllegalArgumentException {
            String[] chunks = lsOutput.split(" +", 9); // NOI18N
            if (chunks.length != 9) {
                throw new IllegalArgumentException();
            }

            try {
                attrs = chunks[0];
                size = Integer.parseInt(chunks[4]);
                name = chunks[8];
            } catch (Throwable th) {
                throw new IllegalArgumentException(th);
            }
        }

        private boolean isFolder() {
            return attrs.startsWith("d"); // NOI18N
        }
    }
}
