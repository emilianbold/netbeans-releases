/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */
 
/* $Id$ */

package org.netbeans.modules.form.actions;

import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.netbeans.modules.form.palette.BeanInstaller;

/** InstallToPalette action - enabled on RADContainerNodes and RADLayoutNodes.
 *
 * @author   Ian Formanek
 */
public class InstallToPaletteAction extends CookieAction {
    static final long serialVersionUID =-7793615112675198529L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -5280204757097896304L;

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
     * constants. */
    protected int mode() {
        return MODE_ALL;
    }

    /** Creates new set of classes that are tested by the cookie.
     *
     * @return list of classes the that the cookie tests
     */
    protected Class[] cookieClasses() {
        return new Class[] { InstanceCookie.class };
    }

    /** Test for enablement based on the cookies of selected nodes.
     * Generally subclasses should not override this except for strange
     * purposes, and then only calling the super method and adding a check.
     * Just use {@link #cookieClasses} and {@link #mode} to specify
     * the enablement logic.
     * @param activatedNodes the set of activated nodes
     * @return <code>true</code> to enable
     */
    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            for (int i = 0; i < activatedNodes.length; i++) {
                if (activatedNodes[i].getCookie(DataObject.class) == null) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(InstallToPaletteAction.class).getString("ACT_InstallToPalette");
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(InstallToPaletteAction.class);
    }

    /** Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource() {
        return "/org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        InstanceCookie[] cookies = new InstanceCookie[activatedNodes.length];
        for (int i = 0; i < activatedNodes.length; i++) {
            cookies[i] =(InstanceCookie)activatedNodes[i].getCookie(InstanceCookie.class);
        }
        //XXX BeanInstaller.installBeans(cookies);
        BeanInstaller.installBeans(activatedNodes);// XXX(-tdt)
    }

}
