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

/* $Id$ */

package org.netbeans.modules.form.actions;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.modules.form.palette.BeanInstaller;

/** This action installs new bean into the system.
 *
 * @author Petr Hamernik
 */
public class InstallBeanAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 7755319389083740521L;

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getBundle(InstallBeanAction.class).getString("ACT_InstallBean");
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(InstallBeanAction.class);
    }

    /** Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource() {
        return "/org/netbeans/modules/form/resources/installBean.gif"; // NOI18N
    }

    /** This method is called by one of the "invokers" as a result of
     * some user's action that should lead to actual "performing" of the action.
     */
    public void performAction() {
        BeanInstaller.installBean();
    }

}
