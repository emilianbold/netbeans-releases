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

package org.netbeans.modules.bpel.debugger.ui.process;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import org.netbeans.spi.viewmodel.Models;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.11.29
 */
public class ProcessesTopComponent extends TopComponent {
    
    private transient JComponent myTree;
    private transient ProcessesViewListener myBpelProcessViewListener;

    /**{@inheritDoc}*/
    public ProcessesTopComponent () {
        setIcon (Utilities.loadImage (
          "org/netbeans/modules/bpel/debugger/ui/" + // NOI18N
          "resources/image/process.gif")); // NOI18N
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
    }

    /**{@inheritDoc}*/
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.bpel.debugger." + // NOI18N
                "ui.process.BpelProcessTopComponent"); // NOI18N
    }

    @Override
    protected String preferredID() {
        return VIEW_NAME;
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();

        if (myBpelProcessViewListener != null) {
            return;
        }
        
        if (myTree == null) {
            setLayout(new BorderLayout());
            myTree = Models.createView(Models.EMPTY_MODEL);
            myTree.setName(VIEW_NAME);
            add(myTree, BorderLayout.CENTER);
        }
        
        myBpelProcessViewListener = 
                new ProcessesViewListener(VIEW_NAME, myTree);
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden();
        
        if (myBpelProcessViewListener != null) {
            myBpelProcessViewListener.destroy();
            myBpelProcessViewListener = null;
        }
    }
    
    /**{@inheritDoc}*/
    @Override
    public void requestActive() {
        if (myTree != null) {
            myTree.requestFocusInWindow();
        }
    }
    
    /**{@inheritDoc}*/
    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    /**{@inheritDoc}*/
    @Override
    public String getName () {
        return VIEW_NAME;
    }
    
    /**{@inheritDoc}*/
    @Override
    public String getDisplayName () {
        return NbBundle.getMessage (
            ProcessesTopComponent.class, "CTL_Process_View"); // NOI18N
    }

    /**{@inheritDoc}*/
    @Override
    public String getToolTipText () {
        return NbBundle.getMessage (
            ProcessesTopComponent.class, "CTL_Process_View_Tooltip"); // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String VIEW_NAME = "ProcessView"; // NOI18N
    private static final long serialVersionUID = 1L; 
}
