/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.openide.filesystems.FileUtil;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.WeakListeners;

/** Watches over a folder and represents its
* child data objects by nodes.
*
* @author Jaroslav Tulach
*/
final class FolderChildren extends Children.Keys<FolderChildrenPair>
implements PropertyChangeListener, ChangeListener, FileChangeListener {
   /** Private req processor for the refresh tasks */
    private static RequestProcessor refRP = new RequestProcessor("FolderChildren_Refresh"); // NOI18N

    /** the folder */
    private FolderList folder;
    /** filter of objects */
    private final DataFilter filter;
    /** listener on changes in nodes */
    private PropertyChangeListener listener;
    /** file change listener */
    private FileChangeListener fcListener;
    /** change listener */
    private ChangeListener changeListener;
    /** logging, if needed */
    @SuppressWarnings("NonConstantLogger")
    private final Logger err;
    /** last refresh task */
    private volatile Task refTask = Task.EMPTY;

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
    @SuppressWarnings("LeakingThisInConstructor")
    public FolderChildren(DataFolder f, DataFilter filter) {
        super(true);
        String log;
        if (f.getPrimaryFile().isRoot()) {
            log = "org.openide.loaders.FolderChildren"; // NOI18N
        } else {
            log = "org.openide.loaders.FolderChildren." + f.getPrimaryFile().getPath().replace('/', '.'); // NOI18N
        }
        err = Logger.getLogger(log);
        this.folder = FolderList.find(f.getPrimaryFile(), true);
        this.filter = filter;
        this.listener = org.openide.util.WeakListeners.propertyChange(this, folder);
        this.fcListener = org.openide.filesystems.FileUtil.weakFileChangeListener(this, folder.getPrimaryFile());
    }

    /** used from DataFolder */
    DataFilter getFilter () {
        return filter;
    }

    static void waitRefresh() {
        refRP.post(Task.EMPTY, 0, Thread.MIN_PRIORITY).waitFinished();
    }

    /** If the folder changed its children we change our nodes.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent ev) {
        err.log(Level.FINE, "Got a change {0}", ev.getPropertyName());
        refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // Filtering changed need to recompute children
        refreshChildren(RefreshMode.DEEP);
    }

    private enum RefreshMode {SHALLOW, SHALLOW_IMMEDIATE, DEEP, DEEP_LATER, CLEAR}
    private void refreshChildren(RefreshMode operation) {
        class R implements Runnable {
            RefreshMode op;
            @Override
            public void run() {
                if (op == RefreshMode.DEEP) {
                    op = RefreshMode.DEEP_LATER;
                    MUTEX.postWriteRequest(this);
                    return;
                }
                err.log(Level.FINE, "refreshChildren {0}", op);

                try {
                    if (op == RefreshMode.CLEAR) {
                        setKeys(Collections.<FolderChildrenPair>emptyList());
                        return;
                    }

                    final FileObject[] arr = folder.getPrimaryFile().getChildren();
                    FolderOrder order = FolderOrder.findFor(folder.getPrimaryFile());
                    Arrays.sort(arr, order);
                    List<FolderChildrenPair> positioned = new ArrayList<FolderChildrenPair>(arr.length);
                    for (FileObject fo : FileUtil.getOrder(Arrays.asList(arr), false)) {
                        if (filter instanceof DataFilter.FileBased) {
                            DataFilter.FileBased f =(DataFilter.FileBased)filter;
                            if (!f.acceptFileObject(fo)) {
                                continue;
                            }
                        }
                        positioned.add(new FolderChildrenPair(fo));
                    }

                    if (op == RefreshMode.DEEP_LATER) {
                        setKeys(Collections.<FolderChildrenPair>emptyList());
                        setKeys(positioned);
                        return;
                    }

                    if (op == RefreshMode.SHALLOW) {
                        setKeys(positioned);
                        return;
                    }

                    throw new IllegalStateException("Unknown op: " + op); // NOI18N
                } finally {
                    err.log(Level.FINE, "refreshChildren {0}, done", op);
                }
            }
        }
        R run = new R();
        if (operation == RefreshMode.SHALLOW_IMMEDIATE) {
            refTask.waitFinished();
            run.op = RefreshMode.SHALLOW;
            run.run();
        } else {
            run.op = operation;
            refTask = refRP.post(run);
        }
    }

    /** Create a node for one data object.
    * @param key DataObject
    */
    @Override
    protected Node[] createNodes(FolderChildrenPair pair) {
        DataObject obj;
        long time = System.currentTimeMillis();
        Node ret = null;
        try {
            FileObject pf = pair.primaryFile;
            obj = DataObject.find (pf);
            if (
                obj.isValid() &&
                pf.equals(obj.getPrimaryFile()) &&
                (filter == null || filter.acceptDataObject (obj))
            ) {
                ret = obj.getClonedNodeDelegate (filter);
                if (!obj.isValid()) {
                    // #153008 - DataObject became invalid meanwhile
                    ret = null;
                }
            } 
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(FolderChildren.class.getName()).log(Level.FINE, null, e);
        } finally {
            long took = System.currentTimeMillis() - time;
            if (err.isLoggable(Level.FINE)) {
                err.log(Level.FINE, "createNodes: {0} took: {1} ms", new Object[]{pair, took});
                err.log(Level.FINE, "  returning: {0}", ret);
            }
        }
        return ret == null ? null : new Node[] { ret };
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        if (optimalResult) {
            waitOptimalResult();
        }
        return getNodes();
    }

    @Override
    public Node findChild(String name) {
        if (checkChildrenMutex()) {
            getNodesCount(true);
        }
        return super.findChild(name);
    }

    private void waitOptimalResult() {
        if (checkChildrenMutex()) {
            err.fine("waitOptimalResult"); // NOI18N
            folder.waitProcessingFinished();
            refTask.waitFinished();
            err.fine("waitOptimalResult: waitProcessingFinished"); // NOI18N
        } else {
            Logger.getLogger(FolderChildren.class.getName()).log(Level.WARNING, null,
                    new java.lang.IllegalStateException("getNodes(true) called while holding the Children.MUTEX"));
        }
    }

    @Override
    public int getNodesCount(boolean optimalResult) {
        if (optimalResult) {
            waitOptimalResult();
        }
        return getNodesCount();
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
            ChangeableDataFilter chF = (ChangeableDataFilter)filter;
            changeListener = WeakListeners.change(this, chF);
            chF.addChangeListener( changeListener );
        }
        if (Boolean.TRUE.equals(folder.getPrimaryFile().getAttribute("isRemoteAndSlow"))) { // NOI18N
            // #159628: do not block EQ loading this folder's children.
            refreshChildren(RefreshMode.SHALLOW);
        } else {
            refreshChildren(RefreshMode.SHALLOW_IMMEDIATE);
        }
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
            ((ChangeableDataFilter)filter).removeChangeListener( changeListener );
            changeListener = null;
        }

        // we need to clear the children now
        List<FolderChildrenPair> emptyList = Collections.emptyList();
        setKeys(emptyList);
        err.fine("removeNotify end");
    }

    /** Display name */
    @Override
    public String toString () {
        return (folder != null) ? folder.getPrimaryFile ().toString () : super.toString();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        if (DataObject.EA_ASSIGNED_LOADER.equals(fe.getName())) {
            // make sure this event is processed by the data system
            DataObjectPool.checkAttributeChanged(fe);
            refreshKey(new FolderChildrenPair(fe.getFile()));
            refreshChildren(RefreshMode.SHALLOW_IMMEDIATE);
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
         refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        refreshChildren(RefreshMode.SHALLOW);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        refreshChildren(RefreshMode.SHALLOW);
    }
}
