/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openidex.search;


import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.TopManager;


/**
 * Search group which perform search on data objects. It is a
 * convenience and the default implementation of <code>SearchGroup</code>
 * abstract class.
 *
 * @author  Peter Zavadsky
 * @see org.openidex.search.SearchGroup
 */
public class DataObjectSearchGroup extends SearchGroup {

    
    /** Adds item to this search group. Implements superclass abstract method.
     * @param item <code>SearchType</code> type to be added. */
    protected void add(SearchType item) {
        if(!Arrays.asList(item.getSearchTypeClasses()).contains(DataObject.class))
            return;
     
        List list = new ArrayList(Arrays.asList(searchTypes));
        
        if(!list.contains(item))
            list.add(item);
        
        searchTypes = (SearchType[])list.toArray(new SearchType[list.size()]);
    }

    /**
     * Actuall search implementation. Fires PROP_FOUND notifications.
     * Implements superclass abstract method. */
    public void doSearch() {
        DataFolder[] rootFolders = getDataFolders();
        
        if(rootFolders == null)
            return;
        
        for(int i = 0; i < rootFolders.length; i++) {
            if(!scanFolder(rootFolders[i])) {
                return;
            }
        }
    }
    
    /** Gets data folder roots on which to search. */
    private DataFolder[] getDataFolders() {
        Node[] nodes = normalizeNodes((Node[])searchRoots.toArray(new Node[searchRoots.size()]));

        List children = new ArrayList(nodes.length);

        // test whether scan whole repository
        if(nodes.length == 1) {
            InstanceCookie ic = (InstanceCookie)nodes[0].getCookie(InstanceCookie.class);

            try {
                if(ic != null && Repository.class.isAssignableFrom(ic.instanceClass())) {
                    Repository rep = TopManager.getDefault().getRepository();
                    Enumeration fss = rep.getFileSystems();

                    while (fss.hasMoreElements()) {
                        FileSystem fs = (FileSystem)fss.nextElement();
                        if (fs.isValid() && !fs.isHidden())
                            children.add(DataObject.find(fs.getRoot()));
                    }

                    return (DataFolder[])children.toArray(new DataFolder[children.size()]);
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();                    
            } catch(ClassNotFoundException cne) {
                cne.printStackTrace();
            }
        }


        for(int i = 0; i<nodes.length; i++) {
            DataFolder dataFolder = (DataFolder)nodes[i].getCookie(DataFolder.class);
            if(dataFolder != null) {
                children.add(dataFolder);
            }
        }

        return (DataFolder[])children.toArray(new DataFolder[children.size()]);
    }
    
    /** Scans data folder recursivelly. 
     * @return <code>true</code> if scanned entire folder successfully
     * or <code>false</code> if scanning was stopped. */
    private boolean scanFolder(DataFolder folder) {
        DataObject[] children = folder.getChildren();

        for(int i = 0; i < children.length; i++) {
            // Test if the search was stopped.
            if(stopped) {
                stopped = true;
                return false;
            }
            
            if(children[i] instanceof DataFolder) {
                if(!scanFolder((DataFolder)children[i])) {
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
    public Node getNodeForFoundObject(Object object) {
        if(!(object instanceof DataObject))
            return null;
        
        return ((DataObject)object).getNodeDelegate();
    }
      
    

    /** Removes kids from node array. Helper method. */
    private static Node[] normalizeNodes(Node[] nodes) {

        List ret = new ArrayList();

        for(int i = 0; i<nodes.length; i++) {
            if(!hasParent(nodes[i],nodes))
                ret.add(nodes[i]);
        }

        return (Node[])ret.toArray(new Node[ret.size()]);
    }

    /** Tests if the node has parent. Helper method. */
    private static boolean hasParent(Node node, Node[] nodes) {
        for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
            for (int i = 0; i<nodes.length; i++) {
                if (nodes[i].equals(parent)) return true;
            }
        }
        return false;
    }
    
}
