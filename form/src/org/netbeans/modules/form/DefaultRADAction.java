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

package org.netbeans.modules.form;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/** This action installs new bean into the system.
 *
 * @author Ian Formanek
 */

public class DefaultRADAction extends CookieAction {

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return "DefaultRADAction"; // NOI18N
    }

    /** Get a help context for the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DefaultRADAction.class);
    }

    /** @return the mode of action. Possible values are disjunctions of MODE_XXX
     * constants. */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    /** Creates new set of classes that are tested by the cookie.
     *
     * @return list of classes the that the cookie tests
     */
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Test for enablement based on the cookies of selected nodes.
     * Generally subclasses should not override this except for strange
     * purposes, and then only calling the super method and adding a check.
     * Just use {@link #cookieClasses} and {@link #mode} to specify
     * the enablement logic.
     * @param activatedNodes the set of activated nodes
     * @return <code>true</code> to enable
     */
//    protected boolean enable(Node[] activatedNodes) {
//        if (activatedNodes.length == 0)
//            return false;
//
//        RADComponentCookie radCookie = (RADComponentCookie)
//            activatedNodes[0].getCookie(RADComponentCookie.class);
//        if (radCookie == null)
//            return false;
//
//        RADComponent comp = radCookie.getRADComponent();
//        return comp.hasDefaultEvent();
//    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 0)
            return;

        RADComponentCookie radCookie = (RADComponentCookie)
            activatedNodes[0].getCookie(RADComponentCookie.class);
        if (radCookie == null)
            return;

//        RADComponent comp = radCookie.getRADComponent();
//        if (comp.hasDefaultEvent())
//            comp.attachDefaultEvent();
        radCookie.getRADComponent().attachDefaultEvent();
    }
}
