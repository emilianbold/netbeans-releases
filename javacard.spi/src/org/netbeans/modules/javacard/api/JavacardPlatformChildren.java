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
package org.netbeans.modules.javacard.api;

import java.awt.EventQueue;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.netbeans.modules.javacard.common.Utils;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 * Node Children factory which shows only registered JavacardPlatforms (the folder
 * they are registered in contains other kinds of files).
 * @author Tim Boudreau
 */
public class JavacardPlatformChildren extends ChildFactory.Detachable<FileObject> implements FileChangeListener, Runnable {
    private final FileObject folder;
    protected List<Node> nodes = new ArrayList<Node>();
    public static Children createChildren() {
        return Children.create(new JavacardPlatformChildren(), false);
    }

    private FileObject getFolder() {
        return folder;
    }

    private JavacardPlatformChildren(FileObject folder) {
        this.folder = folder;
    }
    
    public JavacardPlatformChildren() {
        this ((String) null);
    }

    public JavacardPlatformChildren(String expectedName) {
        this (Utils.sfsFolderForRegisteredJavaPlatforms(expectedName));
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        FileObject fld = getFolder();
        if (fld != null) {
            fld.addFileChangeListener(this);
        }
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        keysCount = 0;
        createNodeCount = 0;
        FileObject fld = getFolder();
        if (fld != null) {
            fld.removeFileChangeListener(this);
        }
        nodes.clear();
    }
    private int createNodeCount;

    protected Node findNodeNamed (String name) {
        for (Node n : nodes) {
            String nm = n.getLookup().lookup(DataObject.class).getName();
            if (name.equals(nm)){
                return n;
            }
        }
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    @Override
    protected Node createNodeForKey(FileObject key) {
        try {
            Node result = new FilterNode(DataObject.find(key).getNodeDelegate(), createChildren(key));
            createNodeCount++;
            if (createNodeCount == keysCount) {
                EventQueue.invokeLater(this);
            }
            nodes.add(result);
            return result;
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return Node.EMPTY;
        }
    }
    private volatile int keysCount;

    @Override
    protected boolean createKeys(List<FileObject> files) {
        keysCount = 0;
        FileObject fld = getFolder();
        if (fld != null) {
            for (FileObject f : fld.getChildren()) {
                try {
                    if (JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(f.getExt()) || 
                        DataObject.find(f).getNodeDelegate().getLookup().lookup(JavacardPlatform.class) != null) {
                        keysCount++;
                        files.add(f);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return true;
    }

    protected Children createChildren(FileObject key) {
        return Children.LEAF;
    }

    public void fileFolderCreated(FileEvent arg0) {
        queueRefresh();
    }

    public void fileDataCreated(FileEvent arg0) {
        queueRefresh();
    }

    public void fileChanged(FileEvent arg0) {
        queueRefresh();
    }

    public void fileDeleted(FileEvent arg0) {
        queueRefresh();
    }

    public void fileRenamed(FileRenameEvent arg0) {
        queueRefresh();
    }

    private void queueRefresh () {
        //Keep ProjectManager.mutex() out of the way of Children.MUTEX
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                refresh(false);
            }
        });
    }

    public void fileAttributeChanged(FileAttributeEvent arg0) {
        //do nothing
    }

    public void run() {
        onAllNodesCreated();
    }

    void onAllNodesCreated() {
    }
}