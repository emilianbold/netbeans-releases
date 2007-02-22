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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.customizer;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.Model;

/**
 * Represents the collection of files belonging to a common namespace.
 *
 * @author Ajit Bhate
 */
public class NamespaceChildren extends Children.Keys {
    /** Map of namespace to a list of files in that namespace. */
    private HashMap<String, List<FileObject>> nsFilesMap;
    /** Set of folders containing referencable files. */
    private FileObject[] rootFolders;
    /** Controls the appearance of child nodes. */
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of NamespaceChildren.
     *
     * @param  roots      set of root folders.
     * @param  decorator  used to decorate the nodes.
     */
    public NamespaceChildren(FileObject[] roots,
            ExternalReferenceDecorator decorator) {
        super();
        rootFolders = roots;
        this.decorator = decorator;
        nsFilesMap = new HashMap<String, List<FileObject>>();
    }

    protected Node[] createNodes(Object key) {
        if (key == WaitNode.WAIT_KEY) {
            return WaitNode.createNode();
        } else if (key instanceof String) {
            List<FileObject> fobjs = nsFilesMap.get(key);
            if (fobjs != null && !fobjs.isEmpty()) {
                Node[] filterNodes = new Node[fobjs.size()];
                int i = 0;
                for (FileObject fobj:fobjs) {
                    try {
                        Node node = DataObject.find(fobj).getNodeDelegate();
                        filterNodes[i++] = decorator.createExternalReferenceNode(node);
                    } catch (DataObjectNotFoundException donfe) {
                    }
                }
                Children.Array children = new Children.Array();
                children.add(filterNodes);
                Node node = new NamespaceNode(children, (String) key, decorator);
                return new Node[] { node };
            }
        }
        return new Node[] { };
    }

    protected void addNotify() {
        super.addNotify();
        setKeys(WaitNode.getKeys());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (FileObject root : rootFolders) {
                    java.util.Map<FileObject, String> map =
                            Utilities.getFiles2NSMappingInProj(
                            FileUtil.toFile(root), decorator.getDocumentType());
                    for (java.util.Map.Entry<FileObject, String> entry : map.entrySet()) {
                        String ns = entry.getValue();
                        List<FileObject> fobjs = nsFilesMap.get(ns);
                        if (fobjs == null) {
                            fobjs = new ArrayList<FileObject>();
                        }
                        fobjs.add(entry.getKey());
                        nsFilesMap.put(ns, fobjs);
                    }
                }
                // Set the keys on the EDT to avoid clobbering the JTree
                // and causing an AIOOBE (issue 94498).
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        setKeys(nsFilesMap.keySet());
                    }
                });
            }
        });
    }

    private static class NamespaceNode extends FolderNode
            implements ExternalReferenceNode {
        /** Controls the appearance of this node. */
        private ExternalReferenceDecorator decorator;

        NamespaceNode(Children children, String myNamespace,
                ExternalReferenceDecorator decorator) {
            super(children);
            this.decorator = decorator;
            setName(myNamespace);
            if (Utilities.NO_NAME_SPACE.equals(myNamespace)) {
                setDisplayName(NbBundle.getMessage(NamespaceNode.class,
                        "LBL_NoTargetNamespace"));
            }
        }

        public String getHtmlDisplayName() {
            String name = getDisplayName();
            if (decorator != null) {
                name = decorator.getHtmlDisplayName(name, this);
            }
            return name;
        }

        public Model getModel() {
            return null;
        }

        public String getNamespace() {
            // Our name is our namespace.
            return getName();
        }
    
        public boolean hasModel() {
            return false;
        }
    }
}
