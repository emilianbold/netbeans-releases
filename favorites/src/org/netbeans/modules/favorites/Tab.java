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

package org.netbeans.modules.favorites;

import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeOp;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Physical tree view showing list of favorites.
 */
public class Tab extends TopComponent
implements Runnable, ExplorerManager.Provider {
    static final long serialVersionUID =-8178367548546385799L;

    /** data object which should be selected in EQ; synch array when accessing */
    private static final DataObject[] needToSelect = new DataObject[1];

    private static final Logger LOG = Logger.getLogger(Tab.class.getName());

    /* private */ static transient Tab DEFAULT; // package-private for unit tests

    /** composited view */
    transient protected TreeView view;
    /** listeners to the root context and IDE settings */
    transient private PropertyChangeListener weakRcL;
    transient private NodeListener weakNRcL;

    transient private NodeListener rcListener;
    /** validity flag */
    transient private boolean valid = true;

    private ExplorerManager manager;
    
    /** Creates */
    private Tab() {
        this.manager = new ExplorerManager();
        
        ActionMap map = this.getActionMap ();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete (manager, true)); // or false

        // following line tells the top component which lookup should be associated with it
        associateLookup (ExplorerUtils.createLookup (manager, map));
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(Tab.class);
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /** Return preferred ID */
    @Override
    protected String preferredID () {
        return "favorites"; //NOI18N
    }
    
    /** Initialize visual content of component */
    @Override
    protected void componentShowing () {
        super.componentShowing ();

        if (view == null) {
            view = initGui ();

            view.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(
                    Tab.class, "ACSN_ExplorerBeanTree"));
            view.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(
                    Tab.class, "ACSD_ExplorerBeanTree"));
        }
        
        run();
    }

    /** Initializes gui of this component. Subclasses can override
    * this method to install their own gui.
    * @return Tree view that will serve as main view for this explorer.
    */
    protected TreeView initGui () {
        TreeView view = new BeanTreeView();
        view.setRootVisible(false);
        view.setDragSource (true);
        view.setUseSubstringInQuickSearch(true);
        setLayout(new BorderLayout());
        add (view);
        return view;
    }

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
    
    /** Transfer focus to view. */
    @SuppressWarnings("deprecation") @Override
    public void requestFocus () {
        super.requestFocus();
        if (view != null) {
            view.requestFocus();
        }
    }

    /** Transfer focus to view. */
    @SuppressWarnings("deprecation") @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        if (view != null) {
            return view.requestFocusInWindow();
        } else {
            return false;
        }
    }

    /** Sets new root context to view. Name, icon, tooltip
    * of this top component will be updated properly */
    public void setRootContext (Node rc) {
        Node oldRC = getExplorerManager().getRootContext();
        // remove old listener, if possible
        if (weakRcL != null) {
            oldRC.removePropertyChangeListener(weakRcL);
        }
        if (weakNRcL != null) {
            oldRC.removeNodeListener(weakNRcL);
        }
        getExplorerManager().setRootContext(rc);
        initializeWithRootContext(rc);
    }

    public Node getRootContext () {
        return getExplorerManager().getRootContext();
    }

    /** Implementation of DeferredPerformer.DeferredCommand
    * Performs initialization of component's attributes
    * after deserialization (component's name, icon etc, 
    * according to the root context) */
    public void run() {
        if (!valid) {
            valid = true;
            validateRootContext();
        }
    }

    // Bugfix #5891 04 Sep 2001 by Jiri Rechtacek
    // the title is derived from the root context
    // it isn't changed by a selected node in the tree
    /** Called when the explored context changes.
    * Overriden - we don't want title to change in this style.
    */
    protected void updateTitle () {
        // set name by the root context
        setName(getExplorerManager ().getRootContext().getDisplayName());
    }

    private NodeListener rcListener () {
        if (rcListener == null) {
            rcListener = new RootContextListener();
        }
        return rcListener;
    }

    /** Initialize this top component properly with information
    * obtained from specified root context node */
    private void initializeWithRootContext (Node rc) {
        // update TC's attributes
        setIcon(rc.getIcon(BeanInfo.ICON_COLOR_16x16));
        setToolTipText(rc.getDisplayName());
        setName(rc.getDisplayName());
        updateTitle();
        // attach listener
        if (weakRcL == null) {
            weakRcL = WeakListeners.propertyChange(
                rcListener(), rc
            );
        }
        rc.addPropertyChangeListener(weakRcL);

        if (weakNRcL == null) {
            weakNRcL = NodeOp.weakNodeListener (
                rcListener(), rc
            );
        }
        rc.addNodeListener(weakNRcL);
    }

    // put a request for later validation
    // we must do this here, because of ExplorerManager's deserialization.
    // Root context of ExplorerManager is validated AFTER all other
    // deserialization, so we must wait for it
    protected final void scheduleValidation() {
        valid = false;
//            WindowManagerImpl.deferredPerformer().putRequest(this, null);
        SwingUtilities.invokeLater(this); // TEMP
    }

    /* Updated accessible name of the tree view */
    @Override
    public void setName(String name) {
        super.setName(name);
        if (view != null) {
            view.getAccessibleContext().setAccessibleName(name);
        }
    }

    /* Updated accessible description of the tree view */
    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        if (view != null) {
            view.getAccessibleContext().setAccessibleDescription(text);
        }
    }

    /** Multi - purpose listener, listens to: <br>
    * 1) Changes of name, icon, short description of root context.
    * 2) Changes of IDE settings, namely delete confirmation settings */
    private final class RootContextListener implements NodeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            Object source = evt.getSource();

            // root context node change
            Node n = (Node)source;
            if (Node.PROP_DISPLAY_NAME.equals(propName) ||
                    Node.PROP_NAME.equals(propName)) {
                setName(n.getDisplayName());
            } else if (Node.PROP_ICON.equals(propName)) {
                setIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            } else if (Node.PROP_SHORT_DESCRIPTION.equals(propName)) {
                setToolTipText(n.getShortDescription());
            }
        }

        public void nodeDestroyed(NodeEvent nodeEvent) {
            //Tab.this.setCloseOperation(TopComponent.CLOSE_EACH);
            Tab.this.close();
        }            

        public void childrenRemoved(NodeMemberEvent e) {}
        public void childrenReordered(NodeReorderEvent e) {}
        public void childrenAdded(NodeMemberEvent e) {}

    } // end of RootContextListener inner class

    /** Gets default instance. Don't use directly, it reserved for deserialization routines only,
     * e.g. '.settings' file in xml layer, otherwise you can get non-deserialized instance. */
    public static synchronized Tab getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Tab();
            // put a request for later validation
            // we must do this here, because of ExplorerManager's deserialization.
            // Root context of ExplorerManager is validated AFTER all other
            // deserialization, so we must wait for it
            DEFAULT.scheduleValidation();
        }

        return DEFAULT;
    }

    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized Tab findDefault() {
        if(DEFAULT == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("favorites"); // NOI18N
            if(DEFAULT == null) {
                Logger.getLogger(Tab.class.getName()).log(Level.WARNING, null,
                                  new IllegalStateException("Can not find project component for its ID. Returned " +
                                                            tc)); // NOI18N
                DEFAULT = new Tab();
                // XXX Look into getDefault method.
                DEFAULT.scheduleValidation();
            }
        }

        return DEFAULT;
    }

    /** Overriden to explicitely set persistence type of ProjectsTab
     * to PERSISTENCE_ALWAYS */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    // ---- private implementation
    
    /** Finds a node for given data object.
    */
    private static Node findClosestNode (DataObject obj, Node start, boolean useLogicalViews) {
        DataObject original = obj;
        
        Stack<DataObject> stack = new Stack<DataObject> ();
        while (obj != null) {
            stack.push(obj);
            DataObject tmp = obj.getFolder();
            if (tmp == null) {
                //Skip archive file root and do not stop at archive file root
                //ie. continue to root of filesystem.
                FileObject fo = FileUtil.getArchiveFile(obj.getPrimaryFile());
                if (fo != null) {
                    try {
                        obj = DataObject.find(fo);
                        //Remove archive root from stack
                        stack.pop();
                    } catch (DataObjectNotFoundException exc) {
                        obj = null;
                    }
                } else {
                    obj = null;
                }
            } else {
                obj = tmp;
            }
        }
        
        Node current = start;
        int topIdx = stack.size();
        int i = 0;
        while (i < topIdx) {
            Node n = findDataObject (current, stack.get (i));
            if (n != null) {
                current = n;
                topIdx = i;
                i = 0;
            } else {
                i++;
            }
        }
        if (!check(current, original) && useLogicalViews) {
            Node[] children = current.getChildren().getNodes();
            for (int j = 0; j < children.length; j++) {
                Node child = children[j];
                Node n = selectInLogicalViews(original, child);
                if (check(n, original)) {
                    current = n;
                    break;
                }
            }
        }
        return current;
    }

    private static Node selectInLogicalViews(DataObject original, Node start) {
        return start;
        /* Nothing for now.
        DataShadow shadow = (DataShadow)start.getCookie(DataShadow.class);
        if (shadow == null) {
            return findClosestNode(original, start, false);
        }
        return start;
         */
    }

    /** Selects a node for given data object.
    */
    private boolean selectNode (DataObject obj, Node root) {
        Node node = findClosestNode(obj, root, true);
        try {
            getExplorerManager ().setSelectedNodes (new Node[] { node });
        } catch (PropertyVetoException e) {
            // you are out of luck!
            throw new IllegalStateException();
        }
        return check(node, obj);
    }

    private static boolean check(Node node, DataObject obj) {
        DataObject dObj = (DataObject)node.getLookup().lookup(DataObject.class);
        if (obj == dObj) {
            return true;
        }
        if (dObj instanceof DataShadow && obj == ((DataShadow)dObj).getOriginal()) {
            return true;
        }
        return false;
    }

    /** Finds a data object among children of given node.
    * @param node the node to search in
    * @param obj the object to look for
    */
    private static Node findDataObject (Node node, DataObject obj) {
        Node[] arr = node.getChildren ().getNodes (true);
        for (int i = 0; i < arr.length; i++) {
            DataShadow ds = (DataShadow) arr[i].getCookie (DataShadow.class);
            if ((ds != null) && (obj == ds.getOriginal())) {
                return arr[i];
            } else {
                DataObject o = (DataObject) arr[i].getCookie (DataObject.class);
                if ((o != null) && (obj == o)) {
                    return arr[i];
                }
            }
        }
        return null;
    }

    /** Exchanges deserialized root context to projects root context
    * to keep the uniquennes. */
    protected void validateRootContext () {
        Node projectsRc = FavoritesNode.getNode ();
        setRootContext(projectsRc);
    }
    
    

    protected boolean containsNode(DataObject obj) {
        Node node = findClosestNode(obj, getExplorerManager ().getRootContext (), true);
        return check(node, obj);
    }

    protected void doSelectNode (DataObject obj) {
        //#142155: For some selected nodes there is no corresponding dataobject
        if (obj == null) {
            return;
        }
        Node root = getExplorerManager ().getRootContext ();
        if (selectNode (obj, root)) {
            requestActive();
            StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        } else {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(Tab.class,"MSG_NodeNotFound"));
            FileObject file = chooseFileObject(obj.getPrimaryFile());
            if (file == null) {
                return;
            }

            try {
                Node[] toShadows = new Node[] {DataObject.find(file).getNodeDelegate()};
                final DataFolder f = FavoritesNode.getFolder();
                final DataObject [] arr = f.getChildren();
                final List<DataObject> listAdd = new ArrayList<DataObject>();
                DataObject createdDO = null;

                createdDO = Actions.Add.createShadows(f, toShadows, listAdd);

                //This is done to set desired order of nodes in view
                Actions.Add.reorderAfterAddition(f, arr, listAdd);
                Actions.Add.selectAfterAddition(createdDO);
            } catch (DataObjectNotFoundException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }

    /**
     *
     * @return FileObject or null if FileChooser dialog is cancelled
     */
    private static FileObject chooseFileObject(FileObject file) {
        FileObject retVal = null;
        File chooserSelection = null;
        JFileChooser chooser = new JFileChooser ();
        chooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
        chooser.setDialogTitle(NbBundle.getBundle(Actions.class).getString ("CTL_DialogTitle"));
        chooser.setApproveButtonText(NbBundle.getBundle(Actions.class).getString ("CTL_ApproveButtonText"));
        chooser.setSelectedFile(FileUtil.toFile(file));
        int option = chooser.showOpenDialog( WindowManager.getDefault().getMainWindow() ); // Show the chooser
        if ( option == JFileChooser.APPROVE_OPTION ) {
            chooserSelection = chooser.getSelectedFile();
            File selectedFile = FileUtil.normalizeFile(chooserSelection);
            //Workaround for JDK bug #5075580 (filed also in IZ as #46882)
            if (!selectedFile.exists()) {
                if ((selectedFile.getParentFile() != null) && selectedFile.getParentFile().exists()) {
                    if (selectedFile.getName().equals(selectedFile.getParentFile().getName())) {
                        selectedFile = selectedFile.getParentFile();
                    }
                }
            }
            //#50482: Check if selected file exists eg. user can enter any file name to text box.
            if (!selectedFile.exists()) {
                String message = NbBundle.getMessage(Actions.class,"ERR_FileDoesNotExist",selectedFile.getPath());
                String title = NbBundle.getMessage(Actions.class,"ERR_FileDoesNotExistDlgTitle");
                DialogDisplayer.getDefault().notify
                (new NotifyDescriptor(message,title,NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE, new Object[] { NotifyDescriptor.CLOSED_OPTION },
                NotifyDescriptor.OK_OPTION));
            } else {
                retVal = FileUtil.toFileObject(selectedFile);
                assert retVal != null;
            }
        }
        return retVal;
    }

    /** Old (3.2) deserialization of the ProjectTab */
    public Object readResolve() throws ObjectStreamException {
        getDefault().scheduleValidation();
        return getDefault();
    }
    
} // end of Tab inner class
