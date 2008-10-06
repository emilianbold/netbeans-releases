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
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.util.RequestProcessor;

/** Watches over a folder and represents its
* child data objects by nodes.
*
* @author Jaroslav Tulach
*/
final class FolderChildrenEager extends Children.Keys<FolderChildrenPair>
implements PropertyChangeListener, ChangeListener {
    /** the folder */
    private DataFolder folder;
    /** filter of objects */
    private final DataFilter filter;
    /** listener on changes in nodes */
    private PropertyChangeListener listener;
    /** logging, if needed */
    private Logger err;
    /** true if the refrersh is done after DataFilter change */
    private boolean refresh;        
    /**  we wait for this task finished in getNodes(true) */
    private RequestProcessor.Task refreshTask;
    /** Runnable scheduled to refRP */
    private ChildrenRefreshRunnable refreshRunnable;
    
    /** Private req processor for the refresh tasks */
    private static RequestProcessor refRP = 
        new RequestProcessor("FolderChildren_Refresh"); // NOI18N
    
    /**
    * @param f folder to display content of
    * @param map map to use for holding of children
    */
    public FolderChildrenEager (DataFolder f) {
        this (f, DataFilter.ALL);
    }

    /**
    * @param f folder to display content of
    * @param filter filter of objects
    */
    public FolderChildrenEager(DataFolder f, DataFilter filter) {
        this.folder = f;
        this.filter = filter;
        this.refreshRunnable = new ChildrenRefreshRunnable();
        this.refreshTask = refRP.create(refreshRunnable);
        this.listener = org.openide.util.WeakListeners.propertyChange(this, folder);
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
    public void propertyChange (final PropertyChangeEvent ev) {
        if (DataFolder.PROP_CHILDREN.equals (ev.getPropertyName ())) {
            err.fine("Got PROP_CHILDREN");
            refreshChildren().schedule (0);
            postClearTask();
            return;
        }
        if (
            DataFolder.PROP_SORT_MODE.equals (ev.getPropertyName ()) ||
            DataFolder.PROP_ORDER.equals (ev.getPropertyName ())
        ) {
            err.fine("Got PROP_SORT_MODE or PROP_ORDER");
            refreshChildren().schedule (0);
            postClearTask();
            return;
        }
    }
    
    public void stateChanged( ChangeEvent e ) {
        // Filtering changed need to recompute children
        refresh = true;
        refreshChildren().schedule(0);
        postClearTask();
        return;
    }

    /**
     * refreshRunnable holds references to the data object
     * to prevent GC. This method post a task to the same request processor
     * (refRP) to clear this references after they are no longer needed.
     */ 
    private void postClearTask() {
        refRP.post(new Runnable() {
            public void run() {
                refreshRunnable.clear();
            }
        });
    }
    
    /** Refreshes the children.
    */
    RequestProcessor.Task refreshChildren() {
        return refreshTask;    
    }

    /** Create a node for one data object.
    * @param key DataObject
    */
    protected Node[] createNodes(FolderChildrenPair key) {
        err.fine("createNodes: " + key);
        FileObject fo = key.primaryFile;
        DataObject obj;
        try {
            obj = DataObject.find (fo);
            if (filter == null || filter.acceptDataObject (obj)) {
                return new Node[] { obj.getClonedNodeDelegate (filter) };
            } else {
                return new Node[0];
            }
        } catch (DataObjectNotFoundException e) {
            Logger.getLogger(FolderChildrenEager.class.getName()).log(Level.FINE, null, e);
            return new Node[0];
        }
    }
  
    public Node[] getNodes(boolean optimalResult) {
        Node[] res;
        Object hold;

        if (optimalResult) {
            if (checkChildrenMutex()) {
                err.fine("getNodes(true)"); // NOI18N
                FolderList.find(folder.getPrimaryFile(), true).waitProcessingFinished();
                err.fine("getNodes(true): waitProcessingFinished"); // NOI18N
                RequestProcessor.Task task = refreshChildren();
                res = getNodes();
                err.fine("getNodes(true): getNodes: " + res.length); // NOI18N
                task.schedule(0);
                task.waitFinished();
                err.fine("getNodes(true): waitFinished"); // NOI18N
            } else {
                Logger.getLogger(FolderChildrenEager.class.getName()).log(Level.WARNING, null,
                                  new java.lang.IllegalStateException("getNodes(true) called while holding the Children.MUTEX"));
            }
        }
        res = getNodes();
        err.fine("getNodes(boolean): post clear task"); // NOI18N
        postClearTask();         // we can clean the references to data objects now
                                 // they are no longer needed
        return res;
    }
    
    public Node findChild(String name) {
        if (checkChildrenMutex()) {
            getNodes(true);
        }
        return super.findChild(name);
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
    protected void addNotify () {
        err.fine("addNotify begin");
        // add as a listener for changes on nodes
        folder.addPropertyChangeListener (listener);
        // add listener to the filter
        if ( filter instanceof ChangeableDataFilter ) {
            ((ChangeableDataFilter)filter).addChangeListener( this );
        }
        // start the refresh task to compute the children
        refreshChildren().schedule(0);
        err.fine("addNotify end");
    }

    /** Deinitializes the children.
    */
    protected void removeNotify () {
        err.fine("removeNotify begin");
        // removes the listener
        folder.removePropertyChangeListener (listener);
        // remove listener from filter
        if ( filter instanceof ChangeableDataFilter ) {
            ((ChangeableDataFilter)filter).removeChangeListener( this );
        }
        
        // we need to clear the children now
        setKeys(Collections.<FolderChildrenPair>emptySet());
        err.fine("removeNotify end");
    }

    /** Display name */
    public String toString () {
        return (folder != null) ? folder.getPrimaryFile ().toString () : super.toString();
    }
    
    /**
     * Instances of this class are posted to the request processor refRP
     * (FolderChildren_refresher). We do this because we do not want
     * to call setKeys synchronously.
     */
    private final class ChildrenRefreshRunnable implements Runnable {
        /** store the referneces to the data objects to
         * prevent GC.
         */
        private DataObject[] ch;
                
        /** calls setKeys with the folder children 
         * or with empty collection if active is false
         */
        public void run() {
            // this can be run only on the refRP thread
            assert refRP.isRequestProcessorThread();

            FolderList.find(folder.getPrimaryFile(), true).waitProcessingFinished();
            
            ch = folder.getChildren();
            err.fine("Children computed");
            FolderChildrenPair[] keys = new FolderChildrenPair[ch.length];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = new FolderChildrenPair(ch[i].getPrimaryFile());
            }
            setKeys(Arrays.asList(keys));
            
            if ( refresh ) {
                refresh = false;
                for (FolderChildrenPair key : keys) {
                    refreshKey(key);
                }
            }
            
            if (!isInitialized()) {
                clear();
            }
        }
        
        /** stop holding the references to the data objects. After
         * calling this they can be GCed again.
         */
        public void clear() {
            // this can be run only on the refRP thread
            assert refRP.isRequestProcessorThread();
            err.fine("Clearing the reference to children"); // NOI18N
            ch = null;
        }
    }
}
