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

package org.netbeans.core.windows.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.DocumentsDlg;

import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;


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
        
        updateState();
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

