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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.plinks;

import org.netbeans.modules.bpel.debugger.ui.execution.*;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import org.netbeans.spi.viewmodel.Models;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * {@link TopComponent} container for the BPEL Partner Links View.
 * 
 * @author Kirill Sorokin
 */
public class PLinksTopComponent extends TopComponent {
    
    public static final String VIEW_NAME = "BPELPLinksView"; // NOI18N
    
    private transient JComponent myTree;
    private transient PLinksViewListener myListener;
    
    public PLinksTopComponent() {
        setIcon(ImageUtilities.loadImage((
                PLinksNodeModel.PARTNER_LINK_ICON + ".gif"))); // NOI18N
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("debug_windows_partner_links"); // NOI18N
    }

    @Override
    protected String preferredID() {
        return VIEW_NAME;
    }

    @Override
    protected void componentShowing () {
        super.componentShowing ();
        
        if (myListener != null) {
            return;
        }
        
        if (myTree == null) {
            setLayout(new BorderLayout());
            
            myTree = Models.createView(Models.EMPTY_MODEL);
            myTree.setName(Constants.VIEW_NAME);
            
            add(myTree, BorderLayout.CENTER);
        }
        
        myListener = new PLinksViewListener(
                VIEW_NAME, myTree);
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden();

        if (myListener != null) {
            myListener.destroy();
            myListener = null;
        }
    }

    @Override
    public void requestActive() {
        if (myTree != null) {
            myTree.requestFocusInWindow();
        }
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public String getName () {
        return VIEW_NAME;
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(
            PLinksTopComponent.class, "CTL_View_Name"); // NOI18N
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(
            PLinksTopComponent.class, "CTL_View_Tooltip"); // NOI18N
    }
    
    private static final long serialVersionUID = 1L; 
}
