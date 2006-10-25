/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.jarloader;

import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;


/**
 * A node to represent a JAR file.
 * @author Jesse Glick
 */
final class JarDataNode extends DataNode {

    public JarDataNode(JarDataObject obj) {
        this(obj, new DummyChildren());
    }
    
    private JarDataNode(JarDataObject obj, DummyChildren c) {
        super(obj, c);
        c.attachJarNode(this);
        setIconBaseWithExtension("org/netbeans/modules/java/jarloader/jar.gif"); // NOI18N
    }
    
    public Action getPreferredAction() {
        return null;
    }
    
    private static Children childrenFor(FileObject jar) {
        if (!FileUtil.isArchiveFile(jar)) {
            // Maybe corrupt, etc.
            return Children.LEAF;
        }
        FileObject root = FileUtil.getArchiveRoot(jar);
        if (root != null) {
            return DataFolder.findFolder(root).createNodeChildren(DataFilter.ALL);
        } else {
            return Children.LEAF;
        }
    }
    
    /**
     * There is no nice way to lazy create delegating node's children.
     * So, in order to fix #83595, here is a little hack that schedules
     * replacement of this dummy children on addNotify call.
     */
    final static class DummyChildren extends Children implements Runnable {

        private JarDataNode node;

        protected void addNotify() {
            super.addNotify();
            assert node != null;
            RequestProcessor.getDefault().post(this);
        }

        private void attachJarNode(JarDataNode jarDataNode) {
            this.node = jarDataNode;
        }

        public void run() {
            node.setChildren(childrenFor(node.getDataObject().getPrimaryFile()));
        }
        
        public boolean add(final Node[] nodes) {
            // no-op
            return false;
        }

        public boolean remove(final Node[] nodes) {
            // no-op
            return false;
        }
        
    }
    
}
