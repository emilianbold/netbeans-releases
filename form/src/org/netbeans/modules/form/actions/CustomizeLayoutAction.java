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

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import org.netbeans.modules.form.*;

/** CustomizeLayout action - enabled on RADContainerNodes and RADLayoutNodes.
 *
 * @author   Ian Formanek
 */

public class CustomizeLayoutAction extends CookieAction {

    private static String name;

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

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(CustomizeLayoutAction.class)
                     .getString("ACT_CustomizeLayout"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizeLayoutAction.class);
    }

    /** Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        RADComponentCookie radCookie = (RADComponentCookie)
            activatedNodes[0].getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer) {
                Node layoutNode = ((RADVisualContainer)metacomp)
                                      .getLayoutNodeReference();
                if (layoutNode != null && layoutNode.hasCustomizer())
                    NodeOperation.getDefault().customize(layoutNode);
            }
        }
    }

    /*
     * In this method the enable / disable action logic can be defined.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            RADComponentCookie radCookie = (RADComponentCookie)
                activatedNodes[0].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp instanceof RADVisualContainer) {
                    Node layoutNode = ((RADVisualContainer)metacomp)
                                      .getLayoutNodeReference();
                    return layoutNode != null && layoutNode.hasCustomizer();
                }
            }
        }
        return false;
    }
}
