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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.core.windows.nodes.WorkspacePoolContext;
import org.openide.modules.ManifestSection.NodeSection;

import org.netbeans.core.lookup.*;

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
        return EnvironmentNode.find (NodeSection.TYPE_ENVIRONMENT);
    }


    /** Session node */
    public Node session () {
        return EnvironmentNode.find (NodeSection.TYPE_SESSION); 
    }

    /** Control panel
    */
    public Node controlPanel () {
        return ControlPanelNode.getDefault ();
    }

    /** Project settings.
    */
    public Node project () {
        return ControlPanelNode.getProjectSettings ();
    }

    /** Node with all workspaces */
    public Node workspaces () {
        return WorkspacePoolContext.getDefault ();
    }

    /** Repository settings */
    public Node repositorySettings () {
        return FSPoolNode.getFSPoolNode ();
    }

    /** Workspace node for current project. This node can change when project changes.
    */
    public Node projectDesktop () {
        return NbProjectOperation.getProjectDesktop ();
    }

    /** Root nodes.
    */
    public Node[] roots () {
        return EnvironmentNode.find (NodeSection.TYPE_ROOTS).getChildren ().getNodes (); 
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
        return findSessionFolder ("Projects"); // NOI18N
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
    
    
    final static class Ch extends Children.Keys implements Runnable {
        /** result */
        private NbLookup.Result result;
        /** remebmber the section name */
        private String sectionName;

        /** Constructor
         * @param nodeSectionName the name of the section that should be recognized
         *   by this children
         */
        public Ch (String nodeSectionName) {
            sectionName = nodeSectionName;

            this.result = re (nodeSectionName);

            result.notify (this);
        }
        
        protected void addNotify () {
            // updates its state
            setKeys (result.allInstances ());
        }
        
        protected void removeNotify () {
            setKeys (Collections.EMPTY_SET);
        }

        /** Static method to compute a Lookup.Result from a template.
         */
        private static NbLookup.Result re (String n) {
            NbLookup.Template t = new NbLookup.Template (
                org.openide.modules.ManifestSection.NodeSection.class
            );
            return org.netbeans.core.NbTopManager.get ().getLookup ().lookup (t);
        }

        /** Method called when we are about to update the keys for 
         * this children. Hopefully never called difectly.
         */
        public final void run () {
            setKeys (result.allInstances ());
            // notify that the set of nodes has changed (probably)
            NbTopManager.get ().firePropertyChange (TopManager.PROP_PLACES, null, null);
        }

        /** Nodes for given objects.
         */
        protected Node[] createNodes (Object key) {
            NodeSection ns = (NodeSection)key;

            try {
                String type = ns.getType ();
                if (type == null || type.equals ("")) {
                    type = NodeSection.TYPE_ENVIRONMENT;
                }
                
                if (type.equalsIgnoreCase (sectionName)) {
                    return new Node[] { ns.getNode () };
                } else {
                    return new Node[0];
                }
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                return new Node[0];
            }
        }
    }
    
}
