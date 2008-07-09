/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.openide.loaders;


import java.beans.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.*;

/** Watches over a folder and represents its
* child data objects by nodes.
*
* @author Jaroslav Tulach
*/
final class FolderChildren extends Children.Keys<FileObject>
        implements PropertyChangeListener, ChangeListener, FileChangeListener {

    /** the folder */
    private DataFolder folder;
    /** filter of objects */
    private final DataFilter filter;
    /** listener on changes in nodes */
    private PropertyChangeListener listener;
    /** file change listener */
    private FileChangeListener fcListener;    
    /** logging, if needed */
    private Logger err;
    
    /**
    * @param f folder to display content of
    * @param map map to use for holding of children
    */
    public FolderChildren (DataFolder f) {
        this (f, DataFilter.ALL);
    }

    /**
    * @param f folder to display content of
    * @param filter filter of objects
    */
    public FolderChildren(DataFolder f, DataFilter filter) {
        super(true);
        this.folder = f;
        this.filter = filter;
        this.listener = org.openide.util.WeakListeners.propertyChange(this, folder);
        this.fcListener = org.openide.filesystems.FileUtil.weakFileChangeListener(this, folder.getPrimaryFile());
        String log;
        if (f.getPrimaryFile().isRoot()) {
            log = "org.openide.loaders.FolderChildren"; // NOI18N
        } else {
            log = "org.openide.loaders.FolderChildren." + f.getPrimaryFile().getPath().replace('/','.'); // NOI18N
        }
        err = Logger.getLogger(log);
    }
    
    /** used from DataFolder */
    DataFilter getFilter () {
        return filter;
    }

    /** If the folder changed its children we change our nodes.
     */
    public void propertyChange(final PropertyChangeEvent ev) {
        if (DataFolder.PROP_CHILDREN.equals(ev.getPropertyName())) {
            err.fine("Got PROP_CHILDREN");
            refreshChildren(false);
            return;
        }
        if (DataFolder.PROP_SORT_MODE.equals(ev.getPropertyName()) ||
                DataFolder.PROP_ORDER.equals(ev.getPropertyName())) {
            err.fine("Got PROP_SORT_MODE or PROP_ORDER");
            refreshChildren(false);
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        // Filtering changed need to recompute children
        refreshChildren(true);
    }
    
    /** Deep refresh or not. */
    final void refreshChildren(boolean deep) {
        final FileObject[] arr = folder.getPrimaryFile().getChildren();
        FolderOrder order = FolderOrder.findFor(folder.getPrimaryFile());
        Arrays.sort(arr, order);

        if (deep) {
            MUTEX.postWriteRequest(new Runnable() {

                public void run() {
                    List<FileObject> emptyList = Collections.emptyList();
                    setKeys(emptyList);
                    setKeys(arr);
                }
            });
            return;
        } else {
            setKeys(arr);
        }
    }

    /** Create a node for one data object.
    * @param key DataObject
    */
    protected Node[] createNodes(FileObject fo) {
        err.fine("createNodes: " + fo);
        DataObject obj;
        try {
            obj = DataObject.find (fo);
            if (filter == null || filter.acceptDataObject (obj)) {
                return new Node[] { obj.getClonedNodeDelegate (filter) };
            } else {
                return new Node[0];
            }
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(FolderChildren.class.getName()).log(Level.FINE, null, e);
            return new Node[0];
        }
    }
  
    @Override
    public Node[] getNodes(boolean optimalResult) {
        if (optimalResult) {
            if (checkChildrenMutex()) {
                err.fine("getNodes(true)"); // NOI18N
                FolderList.find(folder.getPrimaryFile(), true).waitProcessingFinished();
                err.fine("getNodes(true): waitProcessingFinished"); // NOI18N
                //refreshChildren(false);
            } else {
                Logger.getLogger(FolderChildren.class.getName()).log(Level.WARNING, null,
                                  new java.lang.IllegalStateException("getNodes(true) called while holding the Children.MUTEX"));
            }
        }
        Node[] res = getNodes();
        return res;
    }
    
    @Override
    public Node findChild(String name) {
        if (checkChildrenMutex()) {
            getNodesCount(true);
        }
        return super.findChild(name);
    }

    @Override
    public int getNodesCount(boolean optimalResult) {
        if (optimalResult) {
            if (checkChildrenMutex()) {
                err.fine("getNodesCount(true)"); // NOI18N
                FolderList.find(folder.getPrimaryFile(), true).waitProcessingFinished();
                err.fine("getNodesCount(true): waitProcessingFinished"); // NOI18N
                //refreshChildren(false);
            } else {
                Logger.getLogger(FolderChildren.class.getName()).log(Level.WARNING, null,
                        new java.lang.IllegalStateException("getNodes(true) called while holding the Children.MUTEX"));
            }
        }
        int count = getNodesCount();
        return count;
    }

    /**
     * @return true if it is safe to wait (our thread is
     *         not in Children.MUTEX.readAccess
     */
    static boolean checkChildrenMutex() {
        return !Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess ();
    }
    
    /** Initializes the children.
    */
    @Override
    protected void addNotify () {
        err.fine("addNotify begin");
        // add as a listener for changes on nodes
        folder.addPropertyChangeListener(listener);
        folder.getPrimaryFile().addFileChangeListener(fcListener);
        // add listener to the filter
        if ( filter instanceof ChangeableDataFilter ) {
            ((ChangeableDataFilter)filter).addChangeListener( this );
        }
        // start the refresh task to compute the children
        refreshChildren(false);
        err.fine("addNotify end");
    }

    /** Deinitializes the children.
    */
    @Override
    protected void removeNotify () {
        err.fine("removeNotify begin");
        // removes the listeners
        folder.getPrimaryFile().removeFileChangeListener(fcListener);
        folder.removePropertyChangeListener(listener);
        // remove listener from filter
        if ( filter instanceof ChangeableDataFilter ) {
            ((ChangeableDataFilter)filter).removeChangeListener( this );
        }
        
        // we need to clear the children now
        List<FileObject> emptyList = Collections.emptyList();
        setKeys(emptyList);
        err.fine("removeNotify end");
    }

    /** Display name */
    @Override
    public String toString () {
        return (folder != null) ? folder.getPrimaryFile ().toString () : super.toString();
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        if (DataObject.EA_ASSIGNED_LOADER.equals(fe.getName())) {
            // make sure this event is processed by the data system
            DataObjectPool.checkAttributeChanged(fe);
            refreshKey(fe.getFile());
        }
    }

    public void fileChanged(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
         refreshChildren(false);
    }

    public void fileDeleted(FileEvent fe) {
        refreshChildren(false);
    }

    public void fileFolderCreated(FileEvent fe) {
        refreshChildren(false);
    }

    public void fileRenamed(FileRenameEvent fe) {
    }
}
