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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.*;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

import org.netbeans.core.startup.ManifestSection;

/** Important places in the system.
*
* @author Jaroslav Tulach
*/
public final class NbPlaces extends Object {
    private final List listeners = new ArrayList(); // List<ChangeListener>
    
    /** No instance outside this class.
    */
    private NbPlaces() {
    }
    
    private static NbPlaces DEFAULT;
    
    /** Getter for default instance.
     */
    public static synchronized NbPlaces getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new NbPlaces();
        }
        return DEFAULT;
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    void fireChange() {
        ChangeListener[] l;
        synchronized (listeners) {
            l = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < l.length; i++) {
            l[i].stateChanged(ev);
        }
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

    /** Node with all workspaces */
    public Node workspaces () {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem ();
        // Not sure whether this is the right place for this node in layer.
        FileObject fo = fs.findResource("UI/Services/IDEConfiguration/LookAndFeel/Workspace.instance"); // NOI18N
        if(fo != null) {
            try {
                DataObject dob = DataObject.find(fo);
                InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
                if(ic != null && Node.class.isAssignableFrom(ic.instanceClass())) {
                    Node node = (Node)ic.instanceCreate();
                    if(node != null) {
                        return node;
                    }
                }
            } catch(IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            } catch(ClassNotFoundException cnfe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, cnfe);
            }
        }
        
        Node n = new AbstractNode(Children.LEAF);
        n.setName(NbBundle.getMessage(NbPlaces.class, "CTL_NoWorkspaces"));
        return n;
    }

    /** Repository settings */
    public Node repositorySettings () {
        return new AbstractNode(Children.LEAF);
    }

    /** Workspace node for current project. This node can change when project changes.
    */
    public Node projectDesktop () {
        return workplace().getNodeDelegate();
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
    public DataFolder workplace() {
         return findSessionFolder("Workplace"); // NOI18N
     }
     
     /**
     * Returns a DataFolder subfolder of the session folder.  In the DataFolder
     * folders go first (sorted by name) followed by the rest of objects sorted
     * by name.
     */
     public static DataFolder findSessionFolder (String name) {
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem ();
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                // resource not found, try to create new folder
                fo = fs.getRoot();
                StringTokenizer st = new StringTokenizer(name, "/"); // NOI18N
                while(st.hasMoreTokens()) {
                    String subFolderName = st.nextToken();
                    FileObject ff = fo.getFileObject(subFolderName);
                    if(ff != null && ff.isFolder()) {
                        fo = ff;
                        continue;
                    }
                    fo = fo.createFolder(subFolderName);
                }
            }
            DataFolder df = DataFolder.findFolder(fo);
            return df;
        } catch (IOException ex) {
            IllegalStateException e = new IllegalStateException(
                "Folder not found and cannot be created: " + name); // NOI18N
            ErrorManager.getDefault ().annotate (e, ex);
            throw e;
        }
    }

}
