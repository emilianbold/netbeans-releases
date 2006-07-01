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
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;



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

