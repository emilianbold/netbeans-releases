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

package org.netbeans.core.actions;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;

import org.netbeans.core.Splash;

/** The action that shows the AboutBox.
*
* @author Ian Formanek
*/
public class AboutAction extends CallableSystemAction {
    
    public AboutAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public void performAction () {
        Splash.showSplashDialog ();
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AboutAction.class);
    }

    public String getName() {
        return NbBundle.getMessage(AboutAction.class, "About");
    }

}
