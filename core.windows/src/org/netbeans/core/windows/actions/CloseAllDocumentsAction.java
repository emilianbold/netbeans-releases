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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 * @author   Peter Zavadsky
 */
public class CloseAllDocumentsAction extends AbstractAction {

    public CloseAllDocumentsAction() {
        putValue(NAME, NbBundle.getMessage(CloseAllDocumentsAction.class, "CTL_CloseAllDocumentsAction"));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        Set tcs = new HashSet();
        for(Iterator it = wm.getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
                tcs.addAll(mode.getOpenedTopComponents());
            }
        }
        
        for(Iterator it = tcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            tc.close();
        }
        
        // #37290 Unmaximize editor mode if necessary.
        ModeImpl maximizedMode = wm.getMaximizedMode();
        if(maximizedMode != null && maximizedMode.getKind() == Constants.MODE_KIND_EDITOR) {
            wm.setMaximizedMode(null);
        }
    }
    
}

