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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.util.NotImplementedException;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.core.windows.nodes.WorkspacePoolContext;
import org.netbeans.core.execution.ExecutionEngine;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
final class NbPlaces extends Object implements Places, Places.Nodes, Places.Folders {
    /** session settings icon base */
    private static final String SESSION_SETTINGS_ICON_BASE="/org/netbeans/core/resources/sessionSettings"; // NOI18N

    /** default */
    private static NbPlaces places;
    /** set of roots */
    private static ArrayList roots = new ArrayList ();
    /** session node */
    private static AbstractNode session;

    private static class SessionNode extends AbstractNode {
        SessionNode () {
            super (new Children.Array ());
        }
        public HelpCtx getHelpCtx () {
            return new HelpCtx (SessionNode.class);
        }
        /** serialization */
        public Node.Handle getHandle () {
            return new SessionHandle ();
        }

        static final class SessionHandle implements Node.Handle {
            public Node getNode () {
                return TopManager.getDefault ().getPlaces ().nodes (). session();
            }
        }
    }
    static {
        session = new SessionNode ();
        session.setName (NbBundle.getBundle (NbPlaces.class).getString ("CTL_Session_Settings"));
        session.setIconBase (SESSION_SETTINGS_ICON_BASE);
    }

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

    /** Adds new root node.
    */
    public static void addRoot (Node n) {
        roots.add (n);
        NbTopManager.get ().firePropertyChange (NbTopManager.PROP_PLACES, null, null);
    }

    /** Removes new root node.
    */
    public static void removeRoot (Node n) {
        if (roots.remove (n)) {
            NbTopManager.get ().firePropertyChange (NbTopManager.PROP_PLACES, null, null);
        }
    }

    /** Adds new session node.
    */
    public static void addSession (Node n) {
        session.getChildren ().add (new Node[] { n });
    }

    /** Removes new session node.
    */
    public static void removeSession (Node n) {
        session.getChildren ().remove (new Node[] { n });
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
        return EnvironmentNode.getDefault ();
    }


    /** Session node */
    public Node session () {
        return NbPlaces.session;
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
        return (Node[])roots.toArray (new Node[0]);
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
    static DataFolder findSessionFolder (String name) {
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
            throw new InternalError ("Folder not found and cannot be created: " + name); // NOI18N
        }
    }
}
