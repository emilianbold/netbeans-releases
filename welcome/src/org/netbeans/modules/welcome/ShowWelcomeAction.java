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

package org.netbeans.modules.welcome;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.awt.HtmlBrowser;
import org.openide.windows.*;

import java.awt.*;
import java.net.*;
import javax.swing.*;
import java.util.*;

/**
 * Action that can always be invoked and work procedurally.
 *
 * @author  Richard Gregor
 */
public class ShowWelcomeAction extends CallableSystemAction {
    private JPanel content;
    public String WELCOME_MODE_NAME="welcome";
    
    public void performAction() {        
        Workspace ws = WindowManager.getDefault().getCurrentWorkspace();               
        Mode mode = ws.findMode("editor");
        if(mode == null)
            mode = ws.createMode(WELCOME_MODE_NAME, NbBundle.getMessage(ShowWelcomeAction.class, "LBL_WelcomeMode"), null);
        WelcomeComponent topComp = null;
        TopComponent[] tc = mode.getTopComponents();
        for(int i=0; i < tc.length ; i++){
            if(tc[i] instanceof WelcomeComponent){                
                topComp = (WelcomeComponent)tc[i];               
                break;
            }
        }
        if(topComp == null){            
            topComp = WelcomeComponent.findComp();
            mode.dockInto(topComp);
        }
       
        topComp.open();
        topComp.requestActive();
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowWelcomeAction.class, "LBL_Action");
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/welcome/resources/welcome.gif";
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx (MyAction.class);
    }
    
    protected boolean asynchronous(){
        return false;
    }

    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize () {
     * super.initialize ();
     * putProperty (Action.SHORT_DESCRIPTION, NbBundle.getMessage (MyAction.class, "HINT_Action"));
     * }
     */
    
}
