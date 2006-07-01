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

package org.netbeans.core.windows.actions;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.DocumentsDlg;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * @author   Peter Zavadsky
 */
public class DocumentsAction extends AbstractAction implements Runnable {

    private final PropertyChangeListener propListener;
    
    public DocumentsAction() {
        putValue(Action.NAME, NbBundle.getMessage(DocumentsAction.class, "CTL_DocumentsAction"));

        propListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
                    updateState();
                }
           }
        };
        TopComponent.Registry registry = TopComponent.getRegistry();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(propListener, registry));

        // #37529 WindowsAPI to be called from AWT thread only.
        if(SwingUtilities.isEventDispatchThread()) {
            updateState();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateState();
                }
            });
        }
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }
    
    /** Display Documents dialog in AWT thread. */
    public void run () {
        DocumentsDlg.showDocumentsDialog();
    }
    
    private void updateState() {
        // PENDING get all editor modes?
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode("editor"); // NOI18N
        setEnabled(mode == null ? false : !mode.getOpenedTopComponents().isEmpty());
    }
    
}

