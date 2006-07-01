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

package org.netbeans.progress.module;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import org.netbeans.progress.module.ui.StatusLineComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ProgressListAction extends AbstractAction implements ListDataListener, Runnable {

    /** Creates a new instance of ProcessListAction */
    public ProgressListAction() {
        this(NbBundle.getMessage(ProgressListAction.class, "CTL_ProcessListAction"));
    }
    
    public ProgressListAction(String name) {
        putValue(NAME, name);
//        putValue(MNEMONIC_KEY, new Integer((int)NbBundle.getMessage(ProgressListAction.class, "ProcessListAction.mnemonic").charAt(0)));
        Controller.getDefault().getModel().addListDataListener(this);
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
       //need to invoke later becauseotherwise the awtlistener possibly catches a mouse event
        SwingUtilities.invokeLater(this);
    }
    
    public void run() {
        ((StatusLineComponent)Controller.getDefault().getVisualComponent()).showPopup();
    }

    private void updateEnabled() {
        setEnabled(Controller.getDefault().getModel().getSize() != 0);
    }    

    public void contentsChanged(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent listDataEvent) {
        updateEnabled();
    }
    
    
}
