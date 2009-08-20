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

package org.netbeans.modules.project.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.groups.Group;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** TopComponment for viewing open projects. 
 * <P>
 * PENEDING : Fix persistence when new Winsys allows 
 *
 * @author Petr Hrebejk
 */
public class ProjectTab extends TopComponent 
                        implements ExplorerManager.Provider, PropertyChangeListener {
                
    public static final String ID_LOGICAL = "projectTabLogical_tc"; // NOI18N                            
    public static final String ID_PHYSICAL = "projectTab_tc"; // NOI18N                        
    
    private static final Image ICON_LOGICAL = ImageUtilities.loadImage( "org/netbeans/modules/project/ui/resources/projectTab.png" );
    private static final Image ICON_PHYSICAL = ImageUtilities.loadImage( "org/netbeans/modules/project/ui/resources/filesTab.png" );
    
    private static Map<String, ProjectTab> tabs = new HashMap<String, ProjectTab>();                            
                            
    private transient final ExplorerManager manager;
    private transient Node rootNode;
    
    private String id;
    private transient final ProjectTreeView btv;

    private final JLabel noProjectsLabel = new JLabel(NbBundle.getMessage(ProjectTab.class, "NO_PROJECT_OPEN"));

    private boolean synchronizeViews = false;

    private FileObject objectToSelect;

    private Task selectionTask;

    private static final int NODE_SELECTION_DELAY = 200;

    public ProjectTab( String id ) {
        this();
        this.id = id;
        initValues();
    }
    
    public ProjectTab() {
        
        // See #36315        
        manager = new ExplorerManager();
        
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        
        initComponents();

        btv = new ProjectTreeView();    // Add the BeanTreeView
        
        btv.setDragSource (true);
        btv.setUseSubstringInQuickSearch(true);
        btv.setRootVisible(false);
        
        add( btv, BorderLayout.CENTER ); 

        OpenProjects.getDefault().addPropertyChangeListener(this);

        noProjectsLabel.addMouseListener(new LabelPopupDisplayer(noProjectsLabel));
        noProjectsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noProjectsLabel.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("window"); // NOI18N
        noProjectsLabel.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        noProjectsLabel.setOpaque(true);

        associateLookup( ExplorerUtils.createLookup(manager, map) );

        selectionTask = createSelectionTask();

        Preferences nbPrefs = NbPreferences.forModule(SyncEditorWithViewsAction.class);
        synchronizeViews = nbPrefs.getBoolean(SyncEditorWithViewsAction.SYNC_ENABLED_PROP_NAME, false);
        nbPrefs.addPreferenceChangeListener(new NbPrefsListener());
        
    }

    /**
     * Update display to reflect {@link org.netbeans.modules.project.ui.groups.Group#getActiveGroup}.
     * @param group current group, or null
     */
    public void setGroup(Group g) {
        if (id.equals(ID_LOGICAL)) {
            if (g != null) {
                setName(NbBundle.getMessage(ProjectTab.class, "LBL_projectTabLogical_tc_with_group", g.getName()));
            } else {
                setName(NbBundle.getMessage(ProjectTab.class, "LBL_projectTabLogical_tc"));
            }
        } else {
            setName(NbBundle.getMessage(ProjectTab.class, "LBL_projectTab_tc"));
        }
        // Seems to be useless: setToolTipText(getName());
    }

    private void initValues() {
        setGroup(Group.getActiveGroup());
        
        if (id.equals(ID_LOGICAL)) {
            setIcon( ICON_LOGICAL ); 
        }
        else {
            setIcon( ICON_PHYSICAL );
        }
            
        if ( rootNode == null ) {
            // Create the node which lists open projects      
            rootNode = new ProjectsRootNode(id.equals(ID_LOGICAL) ? ProjectsRootNode.LOGICAL_VIEW : ProjectsRootNode.PHYSICAL_VIEW);
        }
        manager.setRootContext( rootNode );
    }
            
    /** Explorer manager implementation 
     */
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /* Singleton accessor. As ProjectTab is persistent singleton this
     * accessor makes sure that ProjectTab is deserialized by window system.
     * Uses known unique TopComponent ID TC_ID = "projectTab_tc" to get ProjectTab instance
     * from window system. "projectTab_tc" is name of settings file defined in module layer.
     * For example ProjectTabAction uses this method to create instance if necessary.
     */
    public static synchronized ProjectTab findDefault( String tcID ) {

        ProjectTab tab = tabs.get(tcID);
        
        if ( tab == null ) {
            //If settings file is correctly defined call of WindowManager.findTopComponent() will
            //call TestComponent00.getDefault() and it will set static field component.
            
            TopComponent tc = WindowManager.getDefault().findTopComponent( tcID ); 
            if (tc != null) {
                if (!(tc instanceof ProjectTab)) {
                    //This should not happen. Possible only if some other module
                    //defines different settings file with the same name but different class.
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + ProjectTab.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    tab = ProjectTab.getDefault( tcID );
                }
                else {
                    tab = (ProjectTab)tc;
                }
            } 
            else {
                //This should not happen when settings file is correctly defined in module layer.
                //TestComponent00 cannot be deserialized
                //Fallback to accessor reserved for window system.
                tab = ProjectTab.getDefault( tcID );
            }
        }
        return tab;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * ProjectTab instance from settings file when method is given. Use <code>findDefault</code>
     * to get correctly deserialized instance of ProjectTab */
    public static synchronized ProjectTab getDefault( String tcID ) {
        
        ProjectTab tab = tabs.get(tcID);
        
        if ( tab == null ) {
            tab = new ProjectTab( tcID );            
            tabs.put( tcID, tab );
        }
        
        return tab;        
    }
    
    public static TopComponent getLogical() {
        return getDefault( ID_LOGICAL );
    }
    
    public static TopComponent getPhysical() {
        return getDefault( ID_PHYSICAL );
    }
    
    @Override
    protected String preferredID () {
        return id;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return ExplorerUtils.getHelpCtx( 
            manager.getSelectedNodes(),
            ID_LOGICAL.equals( id ) ? new HelpCtx( "ProjectTab_Projects" ) : new HelpCtx( "ProjectTab_Files" ) );
    }

     
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    // APPEARANCE
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
        
    @SuppressWarnings("deprecation") 
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return btv.requestFocusInWindow();
    }

    //#41258: In the SDI, requestFocus is called rather than requestFocusInWindow:
    @SuppressWarnings("deprecation") 
    @Override
    public void requestFocus() {
        super.requestFocus();
        btv.requestFocus();
    }
    
    // PERSISTENCE
    
    private static final long serialVersionUID = 9374872358L;
    
    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal( out );
        
        out.writeObject( id );
        out.writeObject( rootNode.getHandle() );                
        out.writeObject( btv.getExpandedPaths() );
        out.writeObject( getSelectedPaths() );
    }

    @SuppressWarnings("unchecked") 
    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {        
        super.readExternal( in );
        id = (String)in.readObject();
        rootNode = ((Node.Handle)in.readObject()).getNode();
	List<String[]> exPaths = (List<String[]>)in.readObject();
        List<String[]> selPaths = null;
        try {
            selPaths = (List<String[]>)in.readObject();
        }
        catch ( java.io.OptionalDataException e ) {
            // Sel paths missing
        }
        initValues();
// fix for #55701 (Expanding of previously expanded folder in explorer slows down startup)
// the expansion scales very bad now and can prolong startup up to several minutes
// disabling the expansion of nodes after start altogether
// (thus getting back to how it worked in NB 4.0 FCS, but letting the user turn it back on)
        if (System.getProperty ("netbeans.keep.expansion") != null)
        {
            btv.expandNodes( exPaths );
            selectPaths( selPaths );
        }

    }
    
    // MANAGING ACTIONS
    
    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    
    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    // SEARCHING NODES

    private static final Lookup context = Utilities.actionsGlobalContext();

    private static final Lookup.Result<FileObject> foSelection = context.lookup(new Lookup.Template<FileObject>(FileObject.class));

    private static final Lookup.Result<DataObject> doSelection = context.lookup(new Lookup.Template<DataObject>(DataObject.class));

    private final LookupListener baseListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            if (TopComponent.getRegistry().getActivated() == ProjectTab.this) {
                // Do not want to go into a loop.
                return;
            }
            if (synchronizeViews) {
                Collection<? extends FileObject> fos = foSelection.allInstances();
                if (fos.size() == 1) {
                    selectNodeAsyncNoSelect(fos.iterator().next());
                } else {
                    Collection<? extends DataObject> dos = doSelection.allInstances();
                    if (dos.size() == 1) {
                        selectNodeAsyncNoSelect((dos.iterator().next()).getPrimaryFile());
                    }
                }
            }
        }
    };

    private final LookupListener weakListener = WeakListeners.create(LookupListener.class, baseListener, null);

    private void startListening() {
        foSelection.addLookupListener(weakListener);
        doSelection.addLookupListener(weakListener);
        baseListener.resultChanged(null);
    }

    private void stopListening() {
        foSelection.removeLookupListener(weakListener);
        doSelection.removeLookupListener(weakListener);
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        startListening();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        stopListening();
    }

    // Called from the SelectNodeAction
    
    private final RequestProcessor RP = new RequestProcessor();
    
    public void selectNodeAsync(FileObject object) {
        setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) );
        open();
        requestActive();
        selectNodeAsyncNoSelect(object);
    }

    private Task createSelectionTask() {
        Task task = RP.create(new Runnable() {
            public void run() {
                if (objectToSelect == null) {
                    return;
                }
                ProjectsRootNode root = (ProjectsRootNode) manager.getRootContext();
                 Node tempNode = root.findNode(objectToSelect);
                 if (tempNode == null) {
                     Project project = FileOwnerQuery.getOwner(objectToSelect);
                     Project found = null;
                     for (;;) {
                         if (project != null) {
                             for (Project p : OpenProjectList.getDefault().getOpenProjects()) {
                                 if (p.getProjectDirectory().equals(project.getProjectDirectory())) {
                                     found = p;
                                     break;
                                 }
                             }
                         }
                         if (found instanceof LazyProject) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                         } else {
                             tempNode = root.findNode(objectToSelect);
                             break;
                         }
                     }
                     if (project != null && found == null) {
                         String message = NbBundle.getMessage(ProjectTab.class, "MSG_openProject_confirm", //NOI18N
                                 ProjectUtils.getInformation(project).getDisplayName());
                         String title = NbBundle.getMessage(ProjectTab.class, "MSG_openProject_confirm_title");//NOI18N
                         NotifyDescriptor.Confirmation confirm =
                                 new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.OK_CANCEL_OPTION);
                         DialogDisplayer.getDefault().notify(confirm);
                         if (confirm.getValue() == NotifyDescriptor.OK_OPTION) {
                             if (!OpenProjectList.getDefault().isOpen(project)) {
                                 OpenProjects.getDefault().open(new Project[] { project }, false);
                             }
                             tempNode = root.findNode(objectToSelect);
                         }
                     }
                 }
                 final Node selectedNode = tempNode;
                // Back to AWT             // Back to AWT
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        if ( selectedNode != null ) {
                            try {
                                manager.setSelectedNodes( new Node[] { selectedNode } );
                                btv.scrollToNode(selectedNode);
                                StatusDisplayer.getDefault().setStatusText( "" ); // NOI18N
                            }
                            catch ( PropertyVetoException e ) {
                                // Bad day node found but can't be selected
                            }
                        }
                        else {
                            StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage( ProjectTab.class,
                                                     ID_LOGICAL.equals( id ) ? "MSG_NodeNotFound_ProjectsTab" : "MSG_NodeNotFound_FilesTab" ) ); // NOI18N
                        }
                        setCursor( null );
                    }
                } );
            }
        });
        return task;
    }

    private void selectNodeAsyncNoSelect(final FileObject object) {
        objectToSelect = object;
        selectionTask.schedule(NODE_SELECTION_DELAY);
    }
    
    public boolean selectNode(FileObject object) {
        // System.out.println("Selecting node " + id + " : " + object + " -AWT- " + SwingUtilities.isEventDispatchThread() );
        
        ProjectsRootNode root = (ProjectsRootNode)manager.getRootContext();
        Node selectedNode = root.findNode( object );
        if ( selectedNode != null ) {
            try {                
                manager.setSelectedNodes( new Node[] { selectedNode } );                
                btv.scrollToNode(selectedNode);
                return true;
            }
            catch ( PropertyVetoException e ) {
                // Bad day node found but can't be selected
                return false;
            }
        }
        
        return false;
    }
    
    public void expandNode( Node node ) {
        btv.expandNode( node );
    }
    
    private List<String[]> getSelectedPaths() {
        Node selectedNodes[] = manager.getSelectedNodes();
        List<String[]> result = new ArrayList<String[]>();
        Node rootNode = manager.getRootContext();
                
        for( int i = 0; i < selectedNodes.length; i++ ) {
            String[] path = NodeOp.createPath( selectedNodes[i], rootNode );
            if ( path != null ) {
                result.add( path );
            }
        }
        
        return result;
    }
    
    
    private void selectPaths( List<String[]> paths ) {
        
        if ( paths == null ) {
            return;
        }
        
        List<Node> selectedNodes = new ArrayList<Node>();
        
        Node rootNode = manager.getRootContext();
        
        for( Iterator<String[]> it = paths.iterator(); it.hasNext(); ) {
            String[] sp = it.next();
            try {
                Node n = NodeOp.findPath( rootNode, sp );
                if ( n != null ) {
                    selectedNodes.add( n );
                }
            }
            catch( NodeNotFoundException e ) {
                // Node wont be added                
            }
        }
        
        if ( !selectedNodes.isEmpty() ) {
            Node nodes[] = new Node[ selectedNodes.size() ];
            selectedNodes.toArray( nodes );
            try { 
                manager.setSelectedNodes( nodes );
            }
            catch( PropertyVetoException e ) {
                // Bad day no selection change
            }
        }
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            if (OpenProjects.getDefault().getOpenProjects().length > 0) {
                restoreTreeView();
            } else {
                showNoProjectsLabel();
            }
        }
    }

    private void showNoProjectsLabel() {
        if (noProjectsLabel.isShowing()) {
            return;
        }
        remove(btv);
        add(noProjectsLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void restoreTreeView() {
        if (btv.isShowing()) {
            return;
        }
        remove(noProjectsLabel);
        add(btv, BorderLayout.CENTER );
        revalidate();
        repaint();
    }

    // Private innerclasses ----------------------------------------------------
    
    /** Extending bean treeview. To be able to persist the selected paths
     */
    private class ProjectTreeView extends BeanTreeView {
        public void scrollToNode(final Node n) {
            // has to be delayed to be sure that events for Visualizers
            // were processed and TreeNodes are already in hierarchy
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    TreeNode tn = Visualizer.findVisualizer(n);
                    if (tn == null) {
                        return;
                    }
                    TreeModel model = tree.getModel();
                    if (!(model instanceof DefaultTreeModel)) {
                        return;
                    }
                    TreePath path = new TreePath(((DefaultTreeModel) model).getPathToRoot(tn));
                    Rectangle r = tree.getPathBounds(path);
                    if (r != null) {
                        tree.scrollRectToVisible(r);
                    }
                }
            });
    }
                        
        public List<String[]> getExpandedPaths() { 

            List<String[]> result = new ArrayList<String[]>();
            
            TreeNode rtn = Visualizer.findVisualizer( rootNode );
            TreePath tp = new TreePath( rtn ); // Get the root
            
            for( Enumeration exPaths = tree.getExpandedDescendants( tp ); exPaths != null && exPaths.hasMoreElements(); ) {
                TreePath ep = (TreePath)exPaths.nextElement();
                Node en = Visualizer.findNode( ep.getLastPathComponent() );                
                String[] path = NodeOp.createPath( en, rootNode );
                
                // System.out.print("EXP "); ProjectTab.print( path );
                
                result.add( path );
            }
            
            return result;
            
        }
        
        /** Expands all the paths, when exists
         */
        public void expandNodes( List exPaths ) {
            
            for( Iterator it = exPaths.iterator(); it.hasNext(); ) {
                String[] sp = (String[])it.next();
                TreePath tp = stringPath2TreePath( sp );
                
                if ( tp != null ) {                
                    showPath( tp );
                }
            }
        }
        
                
        
        /** Converts path of strings to TreePath if exists null otherwise
         */
        private TreePath stringPath2TreePath( String[] sp ) {

            try {
                Node n = NodeOp.findPath( rootNode, sp ); 
                
                // Create the tree path
                TreeNode tns[] = new TreeNode[ sp.length + 1 ];
                
                for ( int i = sp.length; i >= 0; i--) {
                    if ( n == null ) { // Fix for 54832 it seems that sometimes                         
                        return null;   // we get unparented node
                    }
                    tns[i] = Visualizer.findVisualizer( n );
                    n = n.getParentNode();                    
                }                
                return new TreePath( tns );
            }
            catch ( NodeNotFoundException e ) {
                return null;
            }
        }
        
    }
    

    // showing popup on right click in projects tab when label <No Project Open> is shown
    private class LabelPopupDisplayer extends MouseAdapter {

        private Component component;

        public LabelPopupDisplayer(Component comp) {
            component = comp;
        }

        private void showPopup(int x, int y) {
            Action actions[] = rootNode.getActions(false);
            JPopupMenu popup = Utilities.actionsToPopup(actions, component);
            popup.show(component, x, y);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger() && id.equals(ID_LOGICAL)) {
                showPopup(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && id.equals(ID_LOGICAL)) {
                showPopup(e.getX(), e.getY());
            }
        }

    }

    private class NbPrefsListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (SyncEditorWithViewsAction.SYNC_ENABLED_PROP_NAME.equals(evt.getKey())) {
                synchronizeViews = Boolean.parseBoolean(evt.getNewValue());
            }
        }

    }

}
