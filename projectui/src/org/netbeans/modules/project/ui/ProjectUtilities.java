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

package org.netbeans.modules.project.ui;

import java.util.*;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** The util methods for projectui module.
 *
 * @author  Jiri Rechtacek
 */
public class ProjectUtilities {
    
    /** Creates a new instance of CloseAllProjectDocuments */
    private ProjectUtilities () {
    }
    
    /** Closes all documents in editor area which are owned by one of given projects.
     * If some documents are modified then an user is notified by Save/Discard/Cancel dialog.
     * Dialog is showed only once for all project's documents together.
     *
     * @param p project to close
     * @return false if an user cancel the Save/Discard/Cancel dialog, true otherwise
     */    
    final public static boolean closeAllDocuments (Project[] projects) {
        if (projects == null) {
            throw new IllegalArgumentException ("No proects are specified."); // NOI18N
        }
        
        if (projects.length == 0) {
            // no projects to close, no documents will be closed
            return true;
        }
        List listOfProjects = Arrays.asList (projects);
        Set modifiedFiles = new HashSet ();
        Set tc2close = new HashSet ();
        Mode editorMode = WindowManager.getDefault ().findMode (CloneableEditorSupport.EDITOR_MODE);
        TopComponent[] openTCs = editorMode.getTopComponents ();
        for (int i = 0; i < openTCs.length; i++) {
            Node[] nodes = openTCs[i].getActivatedNodes ();
            if (nodes != null) {
                for (int j = 0; j < nodes.length; j++) {
                  Node n = nodes[j];
                  DataObject dobj = (DataObject)n.getCookie (DataObject.class);
                  if (dobj != null) {
                      FileObject fobj = dobj.getPrimaryFile ();
                      Project owner = FileOwnerQuery.getOwner (fobj);
                      if (listOfProjects.contains (owner)) {
                          modifiedFiles.add (n);
                          tc2close.add (openTCs[i]);
                      }
                  }
                }
            }
        }
        
        if (modifiedFiles.isEmpty ()) {
            return true;
        }
        
        Node[] modifiedNodes = new Node[modifiedFiles.size ()];
        Iterator it = modifiedFiles.iterator ();
        int i = 0;
        while (it.hasNext ()) {
            modifiedNodes[i++] = (Node)it.next ();
        }
        boolean result = ExitDialog.showDialog (modifiedNodes);
        
        if (result) {
            // close documents
            it = tc2close.iterator ();
            while (it.hasNext ()) {
                ((TopComponent)it.next ()).close ();
            }
        }
        
        return result;
    }
    
    /** Closes all documents of the given project in editor area. If some documents
     * are modified then an user is notified by Save/Discard/Cancel dialog.
     *
     * @param p project to close
     * @return false if an user cancel the Save/Discard/Cancel dialog, true otherwise
     */    
    final public static boolean closeAllDocuments (Project p) {
        if (p == null) {
            throw new IllegalArgumentException ("No specified project."); // NOI18N
        }
        return closeAllDocuments (new Project[] { p });
    }
    
}
