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


import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;



/**
 * Action which selects next TopComponetn in container.
 *
 * @author  Peter Zavadsky
 */
public class NextTabAction extends AbstractAction {
    
    /** Creates a new instance of NextTabAction */
    public NextTabAction() {
        putValue(NAME, NbBundle.getMessage(NextTabAction.class, "CTL_NextTabAction"));
    }

    
    public void actionPerformed(ActionEvent evt) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if(tc == null) {
            return;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);

        List openedTcs = mode.getOpenedTopComponents();

        int index = openedTcs.indexOf(tc);

        if(index == -1) {
            return;
        }

        index++; // Next tab.

        if(index >= openedTcs.size()) {
            index = 0;
        }

        TopComponent select = (TopComponent)openedTcs.get(index);
        if(select == null) {
            return;
        }
        
        mode.setSelectedTopComponent(select);
    }
}

