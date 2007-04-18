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

package org.netbeans.modules.bpel.debugger.ui.execution;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import org.netbeans.spi.viewmodel.Models;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessExecutionTopComponent extends TopComponent {
    
    public static final String NAME = "ProcessExecutionView"; // NOI18N
    private static final long serialVersionUID = 1L; 

    private transient JComponent myTree;
    private transient ProcessExecutionViewListener myProcessExecutionViewListener;
    
    public ProcessExecutionTopComponent() {
        setIcon(Utilities.loadImage(
          "org/netbeans/modules/bpel/debugger/ui/" + // NOI18N
          "resources/image/process_execution.png")); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ProcessExecutionTopComponent.class);
    }

    protected String preferredID() {
        return NAME;
    }

    protected void componentShowing () {
        super.componentShowing ();

        if (myProcessExecutionViewListener != null) {
            return;
        }
        if (myTree == null) {
            setLayout(new BorderLayout());
            myTree = Models.createView(Models.EMPTY_MODEL);
            myTree.setName(NAME);
            add(myTree, BorderLayout.CENTER);
        }
        myProcessExecutionViewListener = new ProcessExecutionViewListener(NAME, myTree);
    }
    
    protected void componentHidden() {
        super.componentHidden();

        if (myProcessExecutionViewListener != null) {
            myProcessExecutionViewListener.destroy();
            myProcessExecutionViewListener = null;
        }
    }

    public void requestActive() {
        if (myTree != null) {
            myTree.requestFocusInWindow();
        }
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
        
    public String getName () {
        return NAME;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage (
            ProcessExecutionTopComponent.class, "CTL_Process_Execution_View"); // NOI18N
    }

    public String getToolTipText() {
        return NbBundle.getMessage (
            ProcessExecutionTopComponent.class, "CTL_Process_Execution_View_Tooltip"); // NOI18N
    }
}
