/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.actions;

import java.io.ObjectStreamException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.*;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.NodeTableModel;
import org.openide.windows.WindowManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.windows.Workspace;
import org.openide.cookies.InstanceCookie;

import org.netbeans.core.projects.SettingChildren;
import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.NbMainExplorer;
import org.netbeans.core.NbPlaces;
import org.openide.windows.Mode;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;

/** Action that opens explorer view which displays global
* options of the IDE.
 *
 * @author Dafe Simonek
 */
public class OptionsAction extends CallableSystemAction {

    private static final String HELP_ID = "org.netbeans.core.actions.OptionsAction"; // NOI18N 

    public void performAction () {
        final OptionsPanel singleton = OptionsPanel.singleton();
        singleton.prepareNodes();
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                
        // dock Options into its mode if needed
        final Workspace w = WindowManager.getDefault().getCurrentWorkspace();
        
        Mode m = w.findMode(singleton);
        boolean center = false;
        if (m == null) {
            m = w.createMode(OptionsPanel.MODE_NAME, singleton.getName(), null);
            //Center only new window
            center = true;
        }
        
        final OptionsPanel optionPanel = singleton;
        final Mode mo = m;
        final boolean centerLoc = center;
                //Center only TOP_FRAME
                if (centerLoc ) {
                    //Bugfix #33888: Initialize GUI of optionPanel here to get correct
                    //preferred size of Options window.
                    optionPanel.componentShowing();
                    mo.setBounds(Utilities.findCenterBounds(optionPanel.getPreferredSize()));
                }
                mo.dockInto(optionPanel);

                optionPanel.open();
                optionPanel.requestFocus();
                optionPanel.requestDefaultFocus();
            }
        }); // EQ.iL
    }
    
    protected boolean asynchronous() {
        return false;
    }

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
        
        private static String TEMPLATES_DISPLAY_NAME = NbBundle.getBundle (org.netbeans.core.NbTopManager.class).getString("CTL_Templates_name"); // NOI18N
        
        /** list of String[] that should be expanded when the tree is shown */
        private Collection toExpand;
        private transient boolean expanded;
        /** root node to use */
        private transient Node rootNode;

        private OptionsPanel () {
            validateRootContext ();
            // show only name of top component is typical case
            putClientProperty("NamingType", "BothOnlyCompName"); // NOI18N
            // Show without tab when alone in container cell.
            putClientProperty("TabPolicy", "HideWhenAlone"); // NOI18N
        }
        
        public HelpCtx getHelpCtx () {
            HelpCtx defaultHelp = new HelpCtx (HELP_ID);
            HelpCtx help = ExplorerPanel.getHelpCtx (
                getExplorerManager ().getSelectedNodes (),
                defaultHelp
            );
            // bugfix #23551, add help id to subnodes of Templates category
            // this check prevents mixed help ids on more selected nodes
            if (!defaultHelp.equals (help)) {
                // try if selected node isn't template
                Node n = getExplorerManager ().getSelectedNodes ()[0];
                Node parent = n.getParentNode ();
                if (parent != null && TEMPLATES_DISPLAY_NAME.equals (parent.getDisplayName ())) {
                    // it's template, return specific help id
                    DataObject dataObj = (DataObject)n.getCookie (DataObject.class);
                    if (dataObj != null) {
                        Object o = dataObj.getPrimaryFile ().getAttribute ("helpID"); // NOI18N
                        if (o != null) {
                            return new HelpCtx (o.toString ());
                        }
                    }
                    return new HelpCtx ("org.netbeans.core.actions.OptionsAction$TemplatesSubnode"); // NOI18N
                }
            }
            return help;
        }

        /** Accessor to the singleton instance */
        static OptionsPanel singleton () {
            if (singleton == null) {
                singleton = new OptionsPanel();
            }
            return singleton;
        }
        
        public void reshape (int x, int y, int w, int h) {
            super.reshape (x,y,w,h);
            //issue 34104, bad sizing/split location for Chinese locales that require
            //a larger default font size
            split.setDividerLocation ((w / 3) + (w/7));
        }

        private transient JSplitPane split=null;
        protected TreeView initGui () {
            TTW view = new TTW ();
            
            
            split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
            PropertySheetView propertyView = new PropertySheetView();
            
            split.setLeftComponent(view);
            split.setRightComponent(propertyView);

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
                    org.netbeans.core.NbTopManager.get().showHelp (
                            OptionsPanel.this.getHelpCtx ());
                }
            });
            add (help, gridBagConstraints);
            
            close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (OptionsAction.class, "ACSD_close_button"));
            help.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (OptionsAction.class, "ACSD_help_button"));
            
            return view;
        }

        /** Overridden to provide a larger preferred size if the default font
         *  is larger, for locales that require this.   */
        public Dimension getPreferredSize() {
            //issue 34104, bad sizing/split location for Chinese locales that require
            //a larger default font size
            Dimension result = super.getPreferredSize();
            int fontsize = 
                javax.swing.UIManager.getFont ("Tree.font").getSize(); //NOI18N
            if (fontsize > 11) {
                int factor = fontsize - 11;
                result.height += 15 * factor;
                result.width += 50 * factor;
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                if (result.height > screen.height) {
                    result.height = screen.height -30;
                }
                if (result.width > screen.width) {
                    result.width = screen.width -30;
                }
            } else {
                result.width += 20;
                result.height +=20;
            }
            return result;
        }
        
        /**
         * Begins expanding nodes. Does not finish synch, but has posted
         * all events to EQ that it needs before finishing.
         */
        public void prepareNodes() {
            /*
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(OptionsAction.class, "MSG_Preparing_options"));
             */
            
            if (toExpand != null) {
                return;
            }
            ArrayList arr = new ArrayList (101);
            toExpand = arr;
            
            Node root = getExplorerManager ().getRootContext ();
            expandNodes(root, 3, arr);
            
            /*
            invokeLaterLowPriority(new Runnable() {
                public void run() {
                    StatusDisplayer.getDefault().setStatusText(""); // NOI18N
                }
            });
             */
        }
            
        
        protected void componentShowing () {
            super.componentShowing ();
            
            if (expanded) {
                return;
            }
            
            // bugfix #19939, get selected node for set back after expanding view
            final Node[] selectedNodes = getExplorerManager ().getSelectedNodes ();
            prepareNodes();
            invokeLaterLowPriority(new Runnable() {
                public void run() {
            
            TTW ttw = (TTW)view;
            ttw.expandTheseNodes (toExpand, getExplorerManager ().getRootContext ());
            
            try {
                getExplorerManager ().setSelectedNodes (selectedNodes);
            } catch (java.beans.PropertyVetoException pve) {
                // notify non-success during set selected nodes back
                ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, pve);
            }
            
            expanded = true;
                }
            }); // EQ.iL
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
            // set deserialized root node
            rootNode = getRootContext ();
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
        private static void expandNodes (Node n, final int depth, final Collection list) {
            assert EventQueue.isDispatchThread();
            if (depth == 0) {
                return;
            }
            
            DataObject obj = (DataObject)n.getCookie(DataObject.class);
            if (obj instanceof DataShadow) {
                obj = ((DataShadow)obj).getOriginal();
            }
            
            if (obj != null) {
                if (!obj.getPrimaryFile().getPath().startsWith ("UI/Services")) { // NOI18N
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
                final Node p = arr[i];
                invokeLaterLowPriority(new Runnable() {
                    public void run() {
                        expandNodes(p, depth - 1, list);
                    }
                });
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
                weakL = org.openide.util.WeakListeners.propertyChange (this, SessionManager.getDefault ());
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
                    NbPlaces.getDefault().session(),
                    new SettingChildren (NbPlaces.getDefault().session())
                );
            }
            public HelpCtx getHelpCtx () {
                return new HelpCtx (OptionsFilterNode.class);
            }
            
            public Node.Handle getHandle () {
                return new H ();
            }
            
            private static class H implements Node.Handle {
                H() {}
                
                private static final long serialVersionUID = -5158460093499159177L;
                
                public Node getNode () throws java.io.IOException {
                    return new OptionsFilterNode ();
                }
            }
        }
        
    } // end of inner class OptionsPanel
    
    /**
     * Similar to {@link EventQueue#invokeLater} but posts the event at the same
     * priority as paint requests, to avoid bad visual artifacts.
     * XXX later Mutex.EVENT etc. should do this automatically, in which case replace...
     */
    private static void invokeLaterLowPriority(Runnable r) {
        Toolkit t = Toolkit.getDefaultToolkit();
        EventQueue q = t.getSystemEventQueue();
        q.postEvent(new PaintPriorityEvent(t, r));
    }
    
    static final class PaintPriorityEvent extends InvocationEvent {
        public PaintPriorityEvent(Toolkit t, Runnable runnable) {
            super(t, PaintEvent.PAINT, runnable, null, false);
        }
    }
    
}
