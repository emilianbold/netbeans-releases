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
import java.awt.BorderLayout;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
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

        /** Accessor to the singleron instance */
        static OptionsPanel singleton () {
            if (singleton == null) {
                singleton = new OptionsPanel();
            }
            return singleton;
        }

        protected TreeView initGui () {
            TreeView view;

            if (!Boolean.getBoolean ("netbeans.options.old")) {
                view = new TreeTableView();
                ((TreeTableView)view).setProperties ( new Node.Property [] { 
                    new SettingChildren.IndicatorProperty () ,
                    new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_PROJECT),
                    new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_SESSION),
                    new SettingChildren.FileStateProperty (SettingChildren.PROP_LAYER_MODULES)
                });
            } else {
                view = new BeanTreeView();
            }

            SplittedPanel split = new SplittedPanel();
            PropertySheetView propertyView = new PropertySheetView();
            split.add(view, SplittedPanel.ADD_LEFT);
            split.add(propertyView, SplittedPanel.ADD_RIGHT);
            // add to the panel
            setLayout(new BorderLayout());
            add(split, BorderLayout.CENTER);
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
