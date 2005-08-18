/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.views;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.netbeans.spi.viewmodel.Models;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class SourcesView extends TopComponent {
// ====
public class SourcesView extends TopComponent implements org.openide.util.HelpCtx.Provider {
// </RAVE>    
    
    private transient JComponent tree;
    private transient ViewModelListener viewModelListener;
    
    
    public SourcesView () {
        setIcon (Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/root.gif")); // NOI18N
    }

    protected String preferredID() {
        return this.getClass().getName();
    }

    protected void componentShowing () {
        super.componentShowing ();
        if (viewModelListener != null)
            return;
        if (tree == null) {
            setLayout (new BorderLayout ());
            tree = Models.createView (Models.EMPTY_MODEL);
            tree.setName ("SourcesView");
            add (tree, "Center");  //NOI18N
        }
        if (viewModelListener != null)
            throw new InternalError ();
        viewModelListener = new ViewModelListener (
            "SourcesView",
            tree
        );
    }
    
    protected void componentHidden () {
        super.componentHidden ();
        viewModelListener.destroy ();
        viewModelListener = null;
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerSourcesNode"); // NOI18N
    }
    // </RAVE>
    
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (tree == null) return false;
        return tree.requestFocusInWindow ();
    }
    
    public String getName () {
        return NbBundle.getMessage (SourcesView.class, "CTL_Sourcess_view");
    }
    
    public String getToolTipText () {
        return NbBundle.getMessage (SourcesView.class, "CTL_Sources_tooltip");// NOI18N
    }
}
