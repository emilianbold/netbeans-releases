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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;


/**
 * @author   Peter Zavadsky
 */
public class SwitchToRecentDocumentAction extends AbstractAction
implements PropertyChangeListener {

    public SwitchToRecentDocumentAction() {
        putValue(Action.NAME, NbBundle.getMessage(SwitchToRecentDocumentAction.class, "CTL_SwitchToRecentDocumentAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent[] tcs = wm.getRecentViewList();
        
        if(tcs.length == 0) {
            return;
        }

        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = (TopComponent)tcs[i];
            
            ModeImpl mode = (ModeImpl)wm.findMode(tc);
            if(mode == null) {
                continue;
            }
            
            if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
                // #37030 Unmaximize other mode if needed.
                if(mode != wm.getMaximizedMode()) {
                    wm.setMaximizedMode(null);
                }
                tc.requestActive();
                break;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        for(Iterator it = WindowManagerImpl.getInstance().getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(mode.getKind() == Constants.MODE_KIND_EDITOR
            && !mode.getOpenedTopComponents().isEmpty()) {
                setEnabled(true);
                return;
            }
        }
        setEnabled(false);
    }
}

