/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome;

import java.util.Iterator;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 * Show the welcome screen.
 * @author  Richard Gregor
 */
public class ShowWelcomeAction extends CallableSystemAction implements Presenter.Menu {

    public void performAction() {        
        WelcomeComponent topComp = null;
        Set/*<TopComponent>*/ tcs = TopComponent.getRegistry().getOpened();
        Iterator it = tcs.iterator();
        while (it.hasNext()) {
            TopComponent tc = (TopComponent)it.next();
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
    
    protected String iconResource() {
        return "org/netbeans/modules/welcome/resources/welcome.gif";  //NOI18N
    }
    
    //#46565: Do not display icon in Help menu item
    public JMenuItem getMenuPresenter() {
        JMenuItem item = new JMenuItem();
        item.setAction(this);
        item.setIcon(new ImageIcon(Utilities.loadImage("org/openide/resources/actions/empty.gif", true))); // NOI18N
        Mnemonics.setLocalizedText(item,getName());
        return item;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous(){
        return false;
    }

}
