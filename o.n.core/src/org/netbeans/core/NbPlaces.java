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

package org.netbeans.core;

import java.io.*;
import java.util.*;

import org.openide.*;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.util.WeakListener;

import org.netbeans.core.windows.nodes.WorkspacePoolContext;
import org.netbeans.core.modules.ManifestSection;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
final class NbPlaces extends Object implements Places, Places.Nodes, Places.Folders {
    /** default */
    private static NbPlaces places;
    
    /** No instance outside this class.
    */
    private NbPlaces() {
    }

    /** @return the default implementation of places */
    public static NbPlaces getDefault () {
        if (places == null) {
            places = new NbPlaces ();
        }
        return places;
    }

    /** Interesting places for nodes.
    * @return object that holds "node places"
    */
    public Places.Nodes nodes () {
        return this;
    }

    /** Interesting places for data objects.
    * @return interface that provides access to data objects' places
    */
    public Places.Folders folders () {
        return this;
    }

    /** Repository node.
    */
    public Node repository () {
        return DataSystem.getDataSystem ();
    }

    /** Repository node with given DataFilter. */
    public Node repository(DataFilter f) {
        return DataSystem.getDataSystem (f);
    }

    /** Get a root of packages with a given data filter.
    * @param f the requested filter
    * @return the node
    */ 
    public Node packages (DataFilter f) {
        return PackageChildren.createNode (f);
    }

    /** Node with all installed loaders.
    */
    public Node loaderPool () {
        return LoaderPoolNode.getLoaderPoolNode ();
    }

    /** Environment node. Place for all transient information about
    * the IDE.
    */
    public Node environment () {
        return EnvironmentNode.find (ManifestSection.NodeSection.TYPE_ENVIRONMENT);
    }


    /** Session node */
    public Node session () {
        return EnvironmentNode.find (ManifestSection.NodeSection.TYPE_SESSION); 
    }

    /** Control panel
    */
    public Node controlPanel () {
        return ControlPanelNode.getDefault ();
    }

    /** Workplace Node.
    */
    public Node project () {
        return org.netbeans.core.ui.WorkplaceNode.getDefault();
    }

    /** Node with all workspaces */
    public Node workspaces () {
        return WorkspacePoolContext.getDefault ();
    }

    /** Repository settings */
    public Node repositorySettings () {
        return FSPoolNode.getFSPoolNode ();
    }

    /** Active project's node, this node can change when active project changes.
    */
    public Node projectDesktop () {
        DataObject prj = org.netbeans.core.ui.WorkplaceNode.getDefault().getActiveProject ();
        if (prj == null)
            return org.netbeans.core.ui.WorkplaceNode.getDefault().cloneNode ();
        
        return prj.getNodeDelegate ().cloneNode ();
    }

    /** Root nodes.
    */
    public Node[] roots () {
        return EnvironmentNode.find (ManifestSection.NodeSection.TYPE_ROOTS).getChildren ().getNodes (); 
    }

    /** Default folder for templates.
    */
    public DataFolder templates () {
        return findSessionFolder ("Templates"); // NOI18N
    }

    /** Default folder for toolbars.
    */
    public DataFolder toolbars () {
        return findSessionFolder ("Toolbars"); // NOI18N
    }

    /** Default folder for menus.
    */
    public DataFolder menus () {
        return findSessionFolder ("Menu"); // NOI18N
    }

    /** Default folder for actions pool.
    */
    public DataFolder actions () {
        return findSessionFolder ("Actions"); // NOI18N
    }

    /** Default folder for bookmarks.
    */
    public DataFolder bookmarks () {
        return findSessionFolder ("Bookmarks"); // NOI18N
    }

    /** Default folder for projects.
    */
    public DataFolder projects () {
        return findSessionFolder (org.netbeans.core.ui.WorkplaceNode.WORKPLACE_FOLDER);
    }

    /** Startup folder.
    */
    public DataFolder startup () {
        return findSessionFolder ("Startup"); // NOI18N
    }

    /** Welcome folder.
    */
    public DataFolder welcome () {
        return findSessionFolder ("Welcome"); // NOI18N
    }

     /** Getter for project workplace. A folder that is presented to the
     * user as project desktop.
     */
     public DataFolder workplace () {
         return (DataFolder) TopManager.getDefault().getPlaces().nodes().projectDesktop().getCookie(DataFolder.class);
     }
     
     /**
     * Returns a DataFolder subfolder of the session folder.  In the DataFolder
     * folders go first (sorted by name) followed by the rest of objects sorted
     * by name.
     */
     static synchronized DataFolder findSessionFolder (String name) {
        try {
            FileSystem fs = NbTopManager.get ().getRepository().getDefaultFileSystem ();
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                // resource not found, try to create new folder
                fo = fs.getRoot ().createFolder (name);
            }
            DataFolder df = DataFolder.findFolder(fo);
            return df;
        } catch (IOException ex) {
            Error e = new InternalError ("Folder not found and cannot be created: " + name); // NOI18N
            TopManager.getDefault ().getErrorManager ().annotate (e, ex);
            throw e;
        }
    }
    
    
    final static class Ch extends Children.Keys 
    implements LookupListener, NodeListener {
        /** result */
        private Lookup.Result result;
        /** remebmber the section name */
        private String sectionName;
        /** default section node */
        private Node defaultNode;

        /** Constructor
         * @param nodeSectionName the name of the section that should be recognized
         *   by this children
         */
        public Ch (String nodeSectionName) {
            sectionName = nodeSectionName;
        }
        
        protected void addNotify () {
            this.result = re (sectionName);
            result.addLookupListener (
                (LookupListener)WeakListener.create (LookupListener.class, this, result)
            );
            
            // updates its state
            updateKeys ();
        }
        
        protected void removeNotify () {
            setKeys (Collections.EMPTY_SET);
            
            result = null;
        }

        /** Static method to compute a Lookup.Result from a template.
         */
        private static Lookup.Result re (String n) {
            Lookup.Template t = new Lookup.Template (ManifestSection.NodeSection.class);
            return Lookup.getDefault ().lookup (t);
        }

        /** Method called when we are about to update the keys for 
         * this children. Hopefully never called difectly.
         */
        public final void resultChanged (LookupEvent ev) {
            if (isInitialized ()) {
                updateKeys ();
            }
            
            if (ManifestSection.NodeSection.TYPE_ROOTS.equals (sectionName)) {
                // notify that the set of nodes has changed (probably)
                NbTopManager.get ().firePropertyChange (TopManager.PROP_PLACES, null, null);
            }
        }

        /** Update keys takes the all results, plus name of the section.
        */
        private void updateKeys () {
            ArrayList list = new ArrayList (result.allInstances ());
            list.add (sectionName);
            setKeys (list);
        }

        /** Nodes for given objects.
         */
        protected Node[] createNodes (Object key) {
            if (key instanceof String) {
                return defaultNodesForType ((String)key);
            }

            ManifestSection.NodeSection ns = (ManifestSection.NodeSection)key;

            try {
                String type = ns.getType ();
                
                if (type.equals (sectionName)) {
                    return new Node[] { (Node)ns.getInstance () };
                } else {
                    return new Node[0];
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return new Node[0];
            }
        }

        /** Default node for a type.
         */
        private Node[] defaultNodesForType (String section) {
            if (defaultNode == null) {
                if (ManifestSection.NodeSection.TYPE_SESSION.equals (section)) {
                    defaultNode = new org.netbeans.core.ui.LookupNode ();
                } else {
                    defaultNode = Node.EMPTY;
                }

                defaultNode.addNodeListener (this);
            }

            // the arr must never be garbage collected and that is why...
            Node[] arr = defaultNode.getChildren ().getNodes ();
            for (int i = 0; i < arr.length; i++) {
                // the arr[i] creates filter node and does not use 
                // arr[i].cloneNode (), because we need to prevent
                // garbage colleciton
                arr[i] = new FilterNode (arr[i]);
            }

            return arr;
        }
    
        /** Fired when a set of new children is added.
        * @param ev event describing the action
        */
        public void childrenAdded (NodeMemberEvent ev) {
            refreshKey (sectionName);
        }
    
        /** Fired when a set of children is removed.
        * @param ev event describing the action
        */
        public void childrenRemoved (NodeMemberEvent ev) {
            refreshKey (sectionName);
        }
 
        /** Fired when the order of children is changed.
        * @param ev event describing the change
        */
        public void childrenReordered(NodeReorderEvent ev) {
            refreshKey (sectionName);
        }
 
        /** Fired when the node is deleted.
        * @param ev event describing the node
        */
        public void nodeDestroyed (NodeEvent ev) {}

        public void propertyChange (java.beans.PropertyChangeEvent ev) {
        }
    }
    
}
