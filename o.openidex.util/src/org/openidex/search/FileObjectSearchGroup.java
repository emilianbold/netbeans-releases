/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    protected void add(SearchType searchType) {
        boolean ok = false;
        Class[] classes = searchType.getSearchTypeClasses();
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] == FileObject.class) {
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
        for(int i = 0; i < rootFolders.length; i++) {
            if (!scanFolder(rootFolders[i])) {
                return;
            }
        }
    }
    
    /** Gets data folder roots on which to search. */
    private FileObject[] getFileFolders() {
        Node[] nodes = normalizeNodes((Node[])searchRoots.toArray(new Node[searchRoots.size()]));

        List children = new ArrayList(nodes.length);

        for (int i = 0; i < nodes.length; i++) {
            DataFolder dataFolder = (DataFolder) nodes[i].getCookie(DataFolder.class);
            if (dataFolder != null) {
                children.add(dataFolder.getPrimaryFile());
            }
        }

        return (FileObject[])children.toArray(new FileObject[children.size()]);
    }
    
    /** Scans data folder recursivelly. 
     * @return <code>true</code> if scanned entire folder successfully
     * or <code>false</code> if scanning was stopped. */
    private boolean scanFolder(FileObject folder) {
        FileObject[] children = folder.getChildren();

        for (int i = 0; i < children.length; i++) {
            // Test if the search was stopped.
            if (stopped) {
                stopped = true;
                return false;
            }
            
            if (children[i].isFolder()) {
                if (!scanFolder(children[i])) {
                    return false;
                }
            } else {
                processSearchObject(children[i]);
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
                public String getName() {
                    return ((FileObject) object).getName();
                }
            };
        }
    }
      
    

    /** Removes kids from node array. Helper method. */
    private static Node[] normalizeNodes(Node[] nodes) {

        List ret = new ArrayList();

        for (int i = 0; i<nodes.length; i++) {
            if (!hasParent(nodes[i], nodes)) {
                ret.add(nodes[i]);
            }
        }

        return (Node[]) ret.toArray(new Node[ret.size()]);
    }

    /** Tests if the node has parent. Helper method. */
    private static boolean hasParent(Node node, Node[] nodes) {
        for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
            for (int i = 0; i<nodes.length; i++) {
                if (nodes[i].equals(parent)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
