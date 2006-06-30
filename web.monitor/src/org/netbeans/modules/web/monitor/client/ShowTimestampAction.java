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

/**
 * ShowTimestampAction.java
 *
 * Created on June 23, 2004, 10:35 AM
 *
 * @author  Stepan Herold
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;


public class ShowTimestampAction extends BooleanStateAction {

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void initialize() {
        super.initialize();
        TransactionView transView =  TransactionView.getInstance();
        setBooleanState(transView.isTimestampButtonSelected());
        // listen to changes made by toolbar button
        transView.addTimestampButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setBooleanState(!getBooleanState());
            }
        });
        setIcon(null);
    }    
    
    public String getName() {
        return NbBundle.getBundle(ReloadAction.class).getString("MON_Show_timestamp");
    }
        
    public void actionPerformed(ActionEvent ev) {
        super.actionPerformed(ev);
        TransactionNode.toggleTimeStamp();
        TransactionView.getInstance().setTimestampButtonSelected(getBooleanState());
        MonitorAction.getController().updateNodeNames();
    }    
}
