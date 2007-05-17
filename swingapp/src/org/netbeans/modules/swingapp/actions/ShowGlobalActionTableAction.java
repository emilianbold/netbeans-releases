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

package org.netbeans.modules.swingapp.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.swingapp.GlobalActionTable;
import org.openide.util.NbBundle;

/**
 * A simple action which shows the global action table. It will be placed
 * in the 'Window' menu.
 * 
 * @author joshua.marinacci@sun.com
 */
public class ShowGlobalActionTableAction extends AbstractAction {
    
    /** Creates a new instance of ShowGlobalActionTableAction */
    public ShowGlobalActionTableAction() {
        //putValue(NAME, "Application Actions");
        putValue(NAME, NbBundle.getMessage(ShowGlobalActionTableAction.class, "CTL_GlobalActionTableAction"));
    }
    
    public void actionPerformed(ActionEvent e) {
        GlobalActionTable gat = GlobalActionTable.getInstance();

        
        /*Mode outputMode = WindowManager.getDefault().findMode("output");
        if(outputMode != null) {
            outputMode.dockInto(gat);
        }*/
        gat.open();
        gat.requestActive();
        
    }

}
