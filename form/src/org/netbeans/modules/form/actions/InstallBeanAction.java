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

import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** This action installs new bean into the system.
 *
 * @author Petr Hamernik
 */

public class InstallBeanAction extends CallableSystemAction {

    private static String name;
    
    public InstallBeanAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(InstallBeanAction.class)
                     .getString("ACT_InstallBean"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("beans.adding"); // NOI18N
    }

    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     */
    public void performAction() {
        PaletteUtils.getPalette().showCustomizer();
    }
}
