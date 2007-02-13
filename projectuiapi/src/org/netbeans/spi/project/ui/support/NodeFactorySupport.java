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

package org.netbeans.spi.project.ui.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Support class for creating Project node's children nodes from NodeFactory instances
 * in layers.
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.18
 */
public class NodeFactorySupport {
    
    private NodeFactorySupport() {
    }
    
    /**
     * Creates children list that works on top of {@link NodeFactory} instances
     * in layers.
     * @param project the project which is being displayed
     * @param folderPath the path in the System Filesystem that is used as root for subnode composition.
     *        The content of the folder is assumed to be {@link org.netbeans.spi.project.ui.support.NodeFactory} instances
     * @return a new children list
     */
    public static Children createCompositeChildren(Project project, String folderPath) {
        return new DelegateChildren(project, folderPath);
    }

    /**
     * Utility method for creating a non variable NodeList instance.
     * @param nodes a fixed set of nodes to display
     * @return a constant node list
     */
    public static NodeList fixedNodeList(Node... nodes) {
        return new FixedNodeList(nodes);
    }
    
    private static class FixedNodeList implements NodeList<Node> {
        
        private List<Node> nodes;
        
        FixedNodeList(Node... nds) {
            nodes = Arrays.asList(nds);
        }
        public List<Node> keys() {
            return nodes;
        }
        
        public void addChangeListener(ChangeListener l) { }
        
        public void removeChangeListener(ChangeListener l) { }
        
        public void addNotify() {
        }
        
        public void removeNotify() {
        }

        public Node node(Node key) {
            return key;
        }
    }
    
    static class DelegateChildren extends Children.Keys<NodeList<?>> implements LookupListener, ChangeListener {
        
        private String folderPath;
        private Project project;
        private List<NodeList<?>> nodeLists = new ArrayList<NodeList<?>>();
        private List<NodeFactory> factories = new ArrayList<NodeFactory>();
        private Lookup.Result<NodeFactory> result;
        
        public DelegateChildren(Project proj, String path) {
            folderPath = path;
            project = proj;
        }
        
        // protected for tests..
        protected Lookup createLookup() {
            FileObject root = Repository.getDefault().getDefaultFileSystem().findResource(folderPath);
            DataFolder folder = DataFolder.findFolder(root);
            return new FolderLookup(folder).getLookup();
        }
        
        protected Node[] createNodes(NodeList<?> lst) {
            return _createNodes(lst);
        }
        private static <K> Node[] _createNodes(NodeList<K> lst) {
            List<Node> toRet = new ArrayList<Node>();
            for (K elem : lst.keys()) {
                Node nd = lst.node(elem);
                if (nd != null) {
                    toRet.add(nd);
                }
            }
            return toRet.toArray(new Node[toRet.size()]);
        }
        
        protected void addNotify() {
            super.addNotify();
            result = createLookup().lookup(new Lookup.Template<NodeFactory>(NodeFactory.class));
            for (NodeFactory factory : result.allInstances()) {
                NodeList<?> lst = factory.createNodes(project);
                assert lst != null;
                nodeLists.add(lst);
                lst.addNotify();
                lst.addChangeListener(this);
                factories.add(factory);
            }
            result.addLookupListener(this);
            setKeys(nodeLists);
        }
        
        protected void removeNotify() {
            super.removeNotify();
            setKeys(Collections.<NodeList<?>>emptySet());
            for (NodeList elem : nodeLists) {
                elem.removeChangeListener(this);
                elem.removeNotify();
            }
            nodeLists.clear();
            factories.clear();
            if (result != null) {
                result.removeLookupListener(this);
                result = null;
            }
        }
        
        public void stateChanged(ChangeEvent e) {
            refreshKey((NodeList<?>) e.getSource());
        }

        public void resultChanged(LookupEvent ev) {
            int index = 0;
            for (NodeFactory factory : result.allInstances()) {
                if (!factories.contains(factory)) {
                    factories.add(index, factory);
                    NodeList<?> lst = factory.createNodes(project);
                    assert lst != null;
                    nodeLists.add(index, lst);
                    lst.addNotify();
                    lst.addChangeListener(this);
                } else {
                    while (!factory.equals(factories.get(index))) {
                        factories.remove(index);
                        NodeList<?> lst = nodeLists.remove(index);
                        lst.removeNotify();
                        lst.removeChangeListener(this);                            
                    }
                }
                index++;
            }
            setKeys(nodeLists);
        }
        
    }
    
}
