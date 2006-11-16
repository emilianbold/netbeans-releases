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
                if(mode != wm.getCurrentMaximizedMode()) {
                    wm.switchMaximizedMode(null);
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

