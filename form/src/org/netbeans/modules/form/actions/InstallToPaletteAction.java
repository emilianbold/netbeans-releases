/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import org.openide.cookies.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.form.palette.BeanInstaller;

/**
 * InstallToPalette action - installs selected classes as beans to palette.
 *
 * @author   Ian Formanek
 */

public class InstallToPaletteAction extends CookieAction {

    private static String name;

    public InstallToPaletteAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected int mode() {
        return MODE_ALL;
    }

    protected Class[] cookieClasses() {
        return new Class[] { SourceCookie.class };
    }

    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(InstallToPaletteAction.class)
                     .getString("ACT_InstallToPalette"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("beans.adding"); // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    protected void performAction(Node[] activatedNodes) {
        BeanInstaller.installBeans(activatedNodes);
    }

}
