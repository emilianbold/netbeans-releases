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

package org.openidex.search;

import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Search group which perform search on file objects. It is a
 * convenience and the default implementation of <code>SearchGroup</code>
 * abstract class.
 *
 * @author  Peter Zavadsky
 * @author  Marian Petras
 * @see org.openidex.search.SearchGroup
 */
public class FileObjectSearchGroup extends SearchGroup {

    /**
     * {@inheritDoc} If the specified search type does not support searching
     * in <code>FileObject</code>s, the group is left unmodified, too.
     *
     * @see  SearchType#getSearchTypeClasses()
     */
    @Override
    protected void add(SearchType searchType) {
        boolean ok = false;
        for (Class clazz : searchType.getSearchTypeClasses()) {
            if (clazz == FileObject.class) {
                ok = true;
                break;
            }
        }
        if (ok) {
            super.add(searchType);
        }
    }

    /**
     * Actuall search implementation. Fires PROP_FOUND notifications.
     * Implements superclass abstract method. */
    public void doSearch() {
        FileObject[] rootFolders = getFileFolders();
        
        if (rootFolders == null) {
            return;
        }
        for (FileObject rootFolder : rootFolders) {
            if (!scanFolder(rootFolder)) {
                return;
            }
        }
    }
    
    /** Gets data folder roots on which to search. */
    private FileObject[] getFileFolders() {
        Node[] nodes = normalizeNodes(searchRoots.toArray(new Node[searchRoots.size()]));

        List<FileObject> children = new ArrayList<FileObject>(nodes.length);

        for (Node node : nodes) {
            DataFolder dataFolder = node.getCookie(DataFolder.class);
            if (dataFolder != null) {
                children.add(dataFolder.getPrimaryFile());
            }
        }

        return children.toArray(new FileObject[children.size()]);
    }
    
    /** Scans data folder recursivelly. 
     * @return <code>true</code> if scanned entire folder successfully
     * or <code>false</code> if scanning was stopped. */
    private boolean scanFolder(FileObject folder) {
        for (FileObject child : folder.getChildren()) {
            // Test if the search was stopped.
            if (stopped) {
                stopped = true;
                return false;
            }
            
            if (child.isFolder()) {
                if (!scanFolder(child)) {
                    return false;
                }
            } else {
                processSearchObject(child);
            }
        }

        return true;
    }


    /** Gets node for found object. Implements superclass method.
     * @return node delegate for found data object or <code>null</code>
     * if the object is not of <code>DataObjectType</code> */
    public Node getNodeForFoundObject(final Object object) {
        if (!(object instanceof FileObject)) {
            return null;
        }
        try {
            return DataObject.find((FileObject) object).getNodeDelegate();
        } catch (DataObjectNotFoundException dnfe) {
            return new AbstractNode(Children.LEAF) {
                @Override
                public String getName() {
                    return ((FileObject) object).getName();
                }
            };
        }
    }
      
    

    /** Removes kids from node array. Helper method. */
    private static Node[] normalizeNodes(Node[] nodes) {

        List<Node> ret = new ArrayList<Node>();

        for (Node node : nodes) {
            if (!hasParent(node, nodes)) {
                ret.add(node);
            }
        }

        return ret.toArray(new Node[ret.size()]);
    }

    /** Tests if the node has parent. Helper method. */
    private static boolean hasParent(Node node, Node[] nodes) {
        for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
            for (Node n : nodes) {
                if (n.equals(parent)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
