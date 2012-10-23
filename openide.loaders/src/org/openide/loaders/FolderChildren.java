/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


import java.awt.EventQueue;
import java.beans.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.*;
import org.netbeans.modules.openide.loaders.DataNodeUtils;
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
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Watches over a folder and represents its
* child data objects by nodes.
*
* @author Jaroslav Tulach
*/
final class FolderChildren extends Children.Keys<FolderChildrenPair>
implements PropertyChangeListener, ChangeListener, FileChangeListener {
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
    private static final boolean DELAYED_CREATION_ENABLED;
    static {
        DELAYED_CREATION_ENABLED = !"false".equals( // NOI18N
            System.getProperty("org.openide.loaders.FolderChildren.delayedCreation") // NOI18N
        );
    }

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
        DataNodeUtils.reqProcessor().post(Task.EMPTY, 0, Thread.MIN_PRIORITY).waitFinished();
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
            refTask = DataNodeUtils.reqProcessor().post(run);
        }
    }

    /** Create a node for one data object.
    * @param key DataObject
    */
    @Override
    protected Node[] createNodes(FolderChildrenPair pair) {
        boolean delayCreation = 
            DELAYED_CREATION_ENABLED && 
            EventQueue.isDispatchThread() && 
            !pair.primaryFile.isFolder();
        Node ret;
        if (delayCreation) {
            ret = new DelayedNode(pair);
        } else {
            ret = createNode(pair);
        }
        return ret == null ? null : new Node[] { ret };
    }
    
    final Node createNode(FolderChildrenPair pair) {
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
        return ret;
    }

    @Override
    public Node[] getNodes(boolean optimalResult) {
        Node[] arr;
        for (;;) {
            if (optimalResult) {
                waitOptimalResult();
            }
            arr = getNodes();
            boolean stop = true;
            for (Node n : arr) {
                if (n instanceof DelayedNode) {
                    DelayedNode dn = (DelayedNode)n;
                    if (checkChildrenMutex() && dn.waitFinished()) {
                        stop = false;
                    }
                }
            }
            if (stop) {
                break;
            }
        }
        return arr;
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
            if (!isInitialized()) {
                refreshChildren(RefreshMode.SHALLOW);
            }
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
        // #159628, #189979: do not block EQ loading this folder's children.
        refreshChildren(RefreshMode.SHALLOW);
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
    
    private final class DelayedNode extends FilterNode implements Runnable {
        final FolderChildrenPair pair;
        private volatile RequestProcessor.Task task;

        public DelayedNode(FolderChildrenPair pair) {
            this(pair, new DelayedLkp(new InstanceContent()));
        }
        
        private DelayedNode(FolderChildrenPair pair, DelayedLkp lkp) {
            this(pair, new AbstractNode(Children.LEAF, lkp));
            lkp.ic.add(pair.primaryFile);
            lkp.node = this;
        }
        
        private DelayedNode(FolderChildrenPair pair, AbstractNode an) {
            super(an);
            this.pair = pair;
            an.setName(pair.primaryFile.getNameExt());
            an.setIconBaseWithExtension("org/openide/loaders/unknown.gif"); // NOI18N
            
            task = DataNodeUtils.reqProcessor().post(this);
        }
        
        @Override
        public void run() {
            Node n = createNode(pair);
            if (n != null) {
                changeOriginal(n, !n.isLeaf());
            } else {
                refreshKey(pair);
            }
            task = null;
        }
        
        /* @return true if there was some change in the node while waiting */
        public final boolean waitFinished() {
            RequestProcessor.Task t = task;
            if (t == null) {
                return false;
            }
            t.waitFinished();
            return true;
        }
    }
    
    private final class DelayedLkp extends AbstractLookup {
        DelayedNode node;
        final InstanceContent ic;
        
        public DelayedLkp(InstanceContent content) {
            super(content);
            ic = content;
        }
        
        @Override
        protected void beforeLookup(Template<?> template) {
            Class<?> type = template.getType();
            if (DataObject.class.isAssignableFrom(type)) {
                ic.add(convert(node));
            }
        }
        
        public DataObject convert(DelayedNode obj) {
            final FolderChildrenPair pair = obj.pair;
            if (EventQueue.isDispatchThread()) {
                err.log(Level.WARNING, "Attempt to obtain DataObject for {0} from EDT", pair.primaryFile);
                boolean assertsOn = false;
                assert assertsOn = true;
                if (assertsOn) {
                    err.log(Level.INFO, "Ineffective since #199391 was implemented", new Exception("Find for " + pair.primaryFile));
                }
            }
            try {
                return DataObject.find(pair.primaryFile);
            } catch (DataObjectNotFoundException ex) {
                err.log(Level.INFO, "Cannot convert " + pair.primaryFile, ex);
                return null;
            }
        }
    }
}
