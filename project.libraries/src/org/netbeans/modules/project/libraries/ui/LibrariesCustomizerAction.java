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
package org.netbeans.modules.project.libraries.ui;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;




public class LibrariesCustomizerAction extends CallableSystemAction {

    public LibrariesCustomizerAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public void performAction() {
        org.netbeans.api.project.libraries.LibrariesCustomizer.showCustomizer(null);
    }

    public String getName() {
        return NbBundle.getMessage(LibrariesCustomizerAction.class,"CTL_LibrariesManager");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous () {
        return false;
    }
    
}
