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

package org.netbeans.modules.welcome;

import java.util.Iterator;
import java.util.Set;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * Show the welcome screen.
 * @author  Richard Gregor
 */
public class ShowWelcomeAction extends CallableSystemAction {

    public ShowWelcomeAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public void performAction() {
        WelcomeComponent topComp = null;
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        for (TopComponent tc: tcs) {
            if (tc instanceof WelcomeComponent) {                
                topComp = (WelcomeComponent) tc;               
                break;
            }
        }
        if(topComp == null){            
            topComp = WelcomeComponent.findComp();
        }
       
        topComp.open();
        topComp.requestActive();
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowWelcomeAction.class, "LBL_Action");
    }
    
    @Override protected String iconResource() {
        return "org/netbeans/modules/welcome/resources/welcome.gif";  //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override protected boolean asynchronous(){
        return false;
    }

}
