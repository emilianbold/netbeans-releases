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


package org.netbeans.modules.form;

import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/** This action installs new bean into the system.
 *
 * @author Ian Formanek
 */
public class DefaultRADAction extends CookieAction {
    static final long serialVersionUID =-1822120439841761193L;
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 7755319389083740521L;

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return FormEditor.getFormBundle().getString("MSG_DefaultRADAction");
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

    /** Test for enablement based on the cookies of selected nodes.
     * Generally subclasses should not override this except for strange
     * purposes, and then only calling the super method and adding a check.
     * Just use {@link #cookieClasses} and {@link #mode} to specify
     * the enablement logic.
     * @param activatedNodes the set of activated nodes
     * @return <code>true</code> to enable
     */
    protected boolean enable(Node[] activatedNodes) {
        RADComponent comp =((RADComponentCookie)activatedNodes[0].getCookie(RADComponentCookie.class)).getRADComponent();
        return comp.hasDefaultEvent();
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        RADComponent comp =((RADComponentCookie)activatedNodes[0].getCookie(RADComponentCookie.class)).getRADComponent();
        if (comp.hasDefaultEvent())
            comp.attachDefaultEvent();
    }

}
