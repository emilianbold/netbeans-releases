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

package org.netbeans.modules.java.platform.ui;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;



public class PlatformsCustomizerAction extends CallableSystemAction {

    public PlatformsCustomizerAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void performAction() {
        org.netbeans.api.java.platform.PlatformsCustomizer.showCustomizer(null);
    }

    public String getName() {
        return NbBundle.getMessage(PlatformsCustomizerAction.class,"CTL_PlatformManager");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous () {
        return false;
    }
}
