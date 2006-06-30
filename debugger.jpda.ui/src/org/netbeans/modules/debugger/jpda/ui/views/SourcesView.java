/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            tree.setName (NbBundle.getMessage (ClassesView.class, "CTL_Sources_tooltip")); // NOI18N
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
