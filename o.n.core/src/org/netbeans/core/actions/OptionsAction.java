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

package org.netbeans.core.actions;

import java.io.ObjectStreamException;
import java.text.MessageFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreePath;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;
import org.openide.TopManager;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeTableView;
import org.openide.awt.SplittedPanel;
import org.openide.explorer.view.NodeTableModel;
import org.openide.windows.WindowManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.cookies.InstanceCookie;

import org.netbeans.core.projects.SettingChildren;
import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.NbMainExplorer;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.PersistenceManager;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.WorkspaceImpl;
import org.netbeans.core.windows.frames.WindowTypesManager;

/** Action that opens explorer view which displays global
* options of the IDE.
 *
 * @author Dafe Simonek
 */
public class OptionsAction extends CallableSystemAction {

    private static final String HELP_ID = "org.netbeans.core.actions.OptionsAction"; // NOI18N 

    /** Creates new OptionsAction. */
    public OptionsAction() {
    }

    /** Shows options panel. */
    public void performAction () {
        TopManager.getDefault().setStatusText(NbBundle.getBundle(OptionsAction.class).getString("MSG_Preparing_options"));
        OptionsPanel singleton = OptionsPanel.singleton();
        singleton.prepareNodes ();
                
        // dock Options into its mode if needed
        final Workspace w = WindowManager.getDefault().getCurrentWorkspace();
        
        ModeImpl m = (ModeImpl) w.findMode(singleton);
        boolean center = false;
        if (m == null) {
            m = (ModeImpl) w.createMode(OptionsPanel.MODE_NAME,
                                        singleton.getName(),
                                        null);
            //Center only new window
            center = true;
        }
        
        final OptionsPanel optionPanel = singleton;
        final ModeImpl mo = m;
        final boolean centerLoc = center;
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                //Center only TOP_FRAME
                if ((centerLoc) && 
                    WindowTypesManager.getDefaultFrame().equals(WindowTypesManager.TOP_FRAME)) {
                    //Initialize gui to get correct preferred size
                    ((WorkspaceImpl) w).addToShownTcs(optionPanel);
                    Rectangle r = new Rectangle();
                    Dimension d = optionPanel.getPreferredSize();
                    //Center rectangle at screen
                    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                    r.x = (size.width / 2) - (d.width / 2);
                    r.y = (size.height / 2) - (d.height / 2);
                    r.width = d.width;
                    r.height = d.height;
                    mo.setBounds(r);
                }
                mo.dockInto(optionPanel);
                mo.requestFocus();

                optionPanel.open();
                optionPanel.requestFocus();
                optionPanel.requestDefaultFocus();
                
                TopManager.getDefault ().setStatusText (""); // NOI18N
            }
        });
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "org/netbeans/core/resources/session.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (HELP_ID);
    }

    public String getName() {
        return NbBundle.getBundle(OptionsAction.class).getString("Options");
    }

    /** Options panel. Uses singleton pattern. */
    public static final class OptionsPanel extends NbMainExplorer.SettingsTab {
        /** Name of mode in which options panel is docked by default */
        public static final String MODE_NAME = "options";
        /** Singleton instance of options panel */
        private static OptionsPanel singleton;
        /** Formatted title of this view */
        private static MessageFormat formatTitle;

        /** list of String[] that should be expanded when the tree is shown */
        private Collection toExpand;
        private transient boolean expanded;
        /** root node to use */
        private transient Node rootNode;

        private OptionsPanel () {
            validateRootContext ();
            // show only name of top component is typical case
            putClientProperty(ModeImpl.NAMING_TYPE, ModeImpl.BOTH_ONLY_COMP_NAME);
            // Show without tab when alone in container cell.
            putClientProperty(WindowManagerImpl.TopComponentManager.TAB_POLICY,
                WindowManagerImpl.TopComponentManager.HIDE_WHEN_ALONE);
        }
        
        public HelpCtx getHelpCtx () {
            return ExplorerPanel.getHelpCtx (
                getExplorerManager ().getSelectedNodes (),
                new HelpCtx (HELP_ID)
            );
        }

        /** Accessor to the singleton instance */
        private static synchronized OptionsPanel singleton () {
            if (singleton == null) {
                singleton = new OptionsPanel();
            }
            return singleton;
        }

        protected TreeView initGui () {
            TTW view = new TTW ();

            SplittedPanel split = new SplittedPanel();
            PropertySheetView propertyView = new PropertySheetView();
            
            split.setSplitPosition(SplittedPanel.SECOND_PREFERRED);
            view.setPreferredSize(new Dimension(500, 450));
            split.add(view, SplittedPanel.ADD_LEFT);
            propertyView.setPreferredSize(new Dimension(300, 450));
            split.add(propertyView, SplittedPanel.ADD_RIGHT);

            setLayout (new java.awt.GridBagLayout ());

            GridBagConstraints gridBagConstraints = new GridBagConstraints ();
            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridwidth = 2;
            add (split, gridBagConstraints);

            javax.swing.JButton close = new javax.swing.JButton (NbBundle.getMessage (OptionsAction.class, "CTL_close_button"));
            close.setMnemonic(NbBundle.getMessage (OptionsAction.class, "CTL_close_button_mnemonic").charAt(0));
            close.setDefaultCapable (true);
            gridBagConstraints = new GridBagConstraints ();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets.bottom = 11;
            gridBagConstraints.insets.top = 11;
            gridBagConstraints.insets.right = 0;
            close.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    singleton ().close ();
                }
            });
            add (close, gridBagConstraints);

            javax.swing.JButton help = new javax.swing.JButton (NbBundle.getMessage (OptionsAction.class, "CTL_help_button"));
            help.setMnemonic(NbBundle.getMessage (OptionsAction.class, "CTL_help_button_mnemonic").charAt(0));
            help.setMinimumSize (close.getMinimumSize ());
            help.setMaximumSize (close.getMaximumSize ());
            help.setPreferredSize (close.getPreferredSize ());
            help.setSize (close.getSize ());
            gridBagConstraints = new GridBagConstraints ();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.weightx = 0;
            gridBagConstraints.weighty = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets.bottom = 11;
            gridBagConstraints.insets.top = 11;
            gridBagConstraints.insets.right = 11;
            gridBagConstraints.insets.left = 5;
            help.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    TopManager.getDefault ().showHelp (OptionsPanel.this.getHelpCtx ());
                }
            });
            add (help, gridBagConstraints);
            
            close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (OptionsAction.class, "ACSD_close_button"));
            help.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (OptionsAction.class, "ACSD_help_button"));
            
            return view;
        }
        
        public synchronized void prepareNodes () {
            if (toExpand != null) {
                return;
            }
            
            Node root = getExplorerManager ().getRootContext ();
            
            ArrayList arr = new ArrayList (101);
            
            expandNodes (root, 3, arr);
            
            toExpand = arr;
        }
            
        
        protected synchronized void componentShowing () {
            super.componentShowing ();
            
            if (expanded) {
                return;
            }
            
            prepareNodes ();
            
            TTW ttw = (TTW)view;
            ttw.expandTheseNodes (toExpand, getExplorerManager ().getRootContext ());
            
            expanded = true;
        }
        

        protected void validateRootContext () {
            Node n = initRC ();
            setRootContext (n);
        }
        
        /** Resolves to the singleton instance of options panel. */
        public Object readResolve ()
        throws ObjectStreamException {
            if (singleton == null) {
                singleton = this;
            }
            singleton.scheduleValidation();
            return singleton;
        }
        
        private synchronized Node initRC () {
            if (rootNode == null) {
                rootNode = new OptionsFilterNode ();
            }
            return rootNode;
        }

        /** Expands the node in explorer.
         */
        private static void expandNodes (Node n, int depth, Collection list) {
            if (depth == 0) {
                return;
            }
            
            DataObject obj = (DataObject)n.getCookie(DataObject.class);
            if (obj instanceof DataShadow) {
                obj = ((DataShadow)obj).getOriginal();
            }
            
            if (obj != null) {
                if (!obj.getPrimaryFile().getPackageName('/').startsWith ("UI/Services")) { // NOI18N
                    return;
                }

                InstanceCookie ic = (InstanceCookie)obj.getCookie (InstanceCookie.class);
                if (ic != null) {

                    if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of)ic).instanceOf (Node.class)) {
                            return;
                        }
                    } 
                }
            }
            
            // ok, expand this node
            if (!list.contains (n)) {
                list.add (n);
            }
         
            Node[] arr = n.getChildren().getNodes(true);
            for (int i = 0; i < arr.length; i++) {
                expandNodes (arr[i], depth - 1, list);
            }
        }
        
        //
        // Model to implement the special handling of SettingChildren.* properties
        //
        
        /** Model that tries to extract properties from the node.getValue 
         * instead of creating its getPropertySets.
         */
        private static class NTM extends NodeTableModel {
            public NTM () {
                super ();
            }
            
            protected Node.Property getPropertyFor(Node node, Node.Property prop) {
                Object value = node.getValue (prop.getName());
                if (value instanceof Node.Property) {
                    return (Node.Property)value;
                }
                
                return null;
            }
        }

        private static class TTW extends TreeTableView implements MouseListener, PropertyChangeListener, java.awt.event.ActionListener {
            /** Project/Session indicator property */
            private final Node.Property indicator = new SettingChildren.IndicatorProperty ();
            /** Project layer state indicator property */
            private final Node.Property project = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_PROJECT);
            /** Session layer state indicator property */
            private final Node.Property session = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_SESSION);
            /** Modules layer state indicator property */
            private final Node.Property modules = new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_MODULES);
            
            /** Active set of properties (columns) */
            private Node.Property active_set [] = null;
            PropertyChangeListener weakL = null;

            public TTW () {
                super (new NTM ());
                
                refreshColumns (true);
                addMouseListener (this);
                weakL = WeakListener.propertyChange (this, SessionManager.getDefault ());
                SessionManager.getDefault ().addPropertyChangeListener (weakL);
                
                registerKeyboardAction(
                    this,
                    javax.swing.KeyStroke.getKeyStroke('+'),
                    javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
                );

                getAccessibleContext().setAccessibleName(NbBundle.getBundle(OptionsAction.class).getString("ACSN_optionsTree"));
                getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(OptionsAction.class).getString("ACSD_optionsTree"));
            }
            public void mouseExited (MouseEvent evt) {
            }
            public void mouseReleased (MouseEvent evt) {
            }
            public void mousePressed (MouseEvent evt) {
            }
            public void mouseClicked (MouseEvent evt) {
                Component c = evt.getComponent ();
                if (c instanceof JTableHeader) {
                    JTableHeader h = (JTableHeader)c;
                    
                    // show/hide additional properties
                    if (1 == h.columnAtPoint (evt.getPoint ())) {
                        refreshColumns (true);
                    }
                }
            }
            public void mouseEntered (MouseEvent evt) {
            }
            public void propertyChange(PropertyChangeEvent evt) {
                if (SessionManager.PROP_OPEN.equals (evt.getPropertyName ())) {
                    refreshColumns (false);
                }
            }
            private void refreshColumns (boolean changeSets) {
                Node.Property new_set [] = active_set;
                int length = active_set == null ? 0 : active_set.length;

                if ((changeSets && length == 1) || (!changeSets && length > 1)) {
                    // build full_set
                    if (null != SessionManager.getDefault ().getLayer (SessionManager.LAYER_PROJECT))
                        new_set = new Node.Property [] { indicator, project, session, modules };
                    else
                        new_set = new Node.Property [] { indicator, session, modules };

                    indicator.setDisplayName (
                        NbBundle.getMessage (SettingChildren.class, "LBL_IndicatorProperty_Name_Expanded")); //NOI18N
                    indicator.setShortDescription (
                        NbBundle.getMessage (SettingChildren.class, "LBL_IndicatorProperty_Description_Expanded")); //NOI18N
                }
                else {
                    if (changeSets) {
                        new_set = new Node.Property [] { indicator };
                        indicator.setDisplayName (
                            NbBundle.getMessage (SettingChildren.class, "LBL_IndicatorProperty_Name")); //NOI18N
                        indicator.setShortDescription (
                            NbBundle.getMessage (SettingChildren.class, "LBL_IndicatorProperty_Description")); //NOI18N
                    }
                }
                
                if (active_set != new_set) {
                    // setup new columns
                    final Node.Property set [] = new_set;
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            // change columns
                            setProperties (set);

                            // set preferred colunm sizes
                            setTreePreferredWidth(set.length == 1 ? 480 : 300);
                            setTableColumnPreferredWidth (0, 20);
                            for (int i = 1; i < set.length; i++)
                                setTableColumnPreferredWidth (i, 60);
                        }
                    });

                    // remeber the last set of columns
                    active_set = new_set;
                }
            }
            
            public void actionPerformed(ActionEvent e) {
                refreshColumns(true);
            }
            
            public void expandTheseNodes (Collection paths, Node root) {
                Iterator it = paths.iterator();
                
                Node first = null;
                while (it.hasNext()) {
                    Node n = (Node)it.next();
                    if (first == null) {
                        first = n;
                    }
                    
                    this.expandNode(n);
                }

                if (first != null) {
                    collapseNode (first);
                    expandNode (first);
                }
                
                // move to top
                tree.scrollRowToVisible(0);
            }
        }
            
       
        private static class OptionsFilterNode extends FilterNode {
            public OptionsFilterNode () {
                super (
                    TopManager.getDefault().getPlaces().nodes().session(),
                    new SettingChildren (TopManager.getDefault().getPlaces().nodes().session())
                );
            }
            public HelpCtx getHelpCtx () {
                return new HelpCtx (OptionsFilterNode.class);
            }
            
            public Node.Handle getHandle () {
                return new H ();
            }
            
            private static class H implements Node.Handle {
                
                private static final long serialVersionUID = -5158460093499159177L;
                
                public Node getNode () throws java.io.IOException {
                    return new OptionsFilterNode ();
                }
            }
        }

    } // end of inner class OptionsPanel
}
