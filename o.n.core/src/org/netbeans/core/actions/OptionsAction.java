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
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallableSystemAction;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.TreeView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeTableView;
import org.openide.awt.SplittedPanel;

import org.netbeans.core.projects.SettingChildren;
import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.NbMainExplorer;

/** Action that opens explorer view which displays global
* options of the IDE.
 *
 * @author Dafe Simonek
 */
public class OptionsAction extends CallableSystemAction {

    public static final String HELP_ID = "configure"; // NOI18N 

    /** Creates new OptionsAction. */
    public OptionsAction() {
    }

    /** Shows options panel. */
    public void performAction () {
        OptionsPanel singleton = OptionsPanel.singleton();
        singleton.open();
        singleton.requestFocus();
    }

    /** URL to this action.
    * @return URL to the action icon
    */
    public String iconResource () {
        return "/org/netbeans/core/resources/session.gif"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (HELP_ID);
    }

    public String getName() {
        return NbBundle.getBundle(OptionsAction.class).getString("Options");
    }

    /** Options panel. Uses singleton pattern. */
    public static final class OptionsPanel extends NbMainExplorer.SettingsTab {

        /** Singleton instance of options panel */
        private static OptionsPanel singleton;
        /** Formatted title of this view */
        private static MessageFormat formatTitle;

        public OptionsPanel () {
            super();
            setRootContext (initRC ());
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
            TreeView view;

            if (!Boolean.getBoolean ("netbeans.options.old")) {
                view = new TTW ();
            } else {
                view = new BeanTreeView();
            }

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
            add (split, gridBagConstraints);

            javax.swing.JButton close = new javax.swing.JButton (NbBundle.getMessage (OptionsAction.class, "CTL_close_button"));
            close.setDefaultCapable (true);
            gridBagConstraints = new GridBagConstraints ();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets.bottom = 11;
            gridBagConstraints.insets.top = 11;
            gridBagConstraints.insets.right = 11;
            close.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    singleton ().close ();
                }
            });
            add (close, gridBagConstraints);

            return view;
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
            return singleton;
        }
        
        private Node initRC () {
            Node rc;
            if (!Boolean.getBoolean ("netbeans.options.old"))
                rc = new OptionsFilterNode ();
            else
                rc = TopManager.getDefault().getPlaces().nodes().session();
            
            return rc;
        }

        private static class TTW extends TreeTableView implements MouseListener, PropertyChangeListener {
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
                super ();
                refreshColumns (true);
                addMouseListener (this);
                weakL = WeakListener.propertyChange (this, SessionManager.getDefault ());
                SessionManager.getDefault ().addPropertyChangeListener (weakL);
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
                    if (0 == h.columnAtPoint (evt.getPoint ())) {
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
                }
                else {
                    if (changeSets) {
                        new_set = new Node.Property [] { indicator };
                        indicator.setDisplayName (
                            NbBundle.getMessage (SettingChildren.class, "LBL_IndicatorProperty_Name")); //NOI18N
                    }
                }
                
                if (active_set != new_set) {
                    // setup new columns
                    final Node.Property set [] = new_set;
                    javax.swing.SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            // change columns
                            setProperties (set);

                            // set preferred colunm sizes
                            setTableColumnPreferredWidth (0, 20);
                            for (int i = 1; i < set.length; i++)
                                setTableColumnPreferredWidth (i, 50);
                        }
                    });

                    // remeber the last set of columns
                    active_set = new_set;
                }
            }
        }
        
        private static class OptionsFilterNode extends FilterNode {
            public OptionsFilterNode () {
                super (
                    TopManager.getDefault().getPlaces().nodes().session(),
                    new SettingChildren (TopManager.getDefault().getPlaces().nodes().session())
                );
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

/*
* Log
*  3    Gandalf   1.2         1/12/00  Ales Novak      i18n
*  2    Gandalf   1.1         12/7/99  David Simonek   top component inner class
*       made public
*  1    Gandalf   1.0         12/3/99  David Simonek   
* $ 
*/ 
